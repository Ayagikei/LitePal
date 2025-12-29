# LitePal 通过原生 SQL 创建复合索引（Composite Index）方案

## 背景与现状

LitePal 目前只提供了“单列索引”的建模能力：通过字段注解 `@Column(index = true)` 生成 `CREATE INDEX ... ON table(column)`。
对“复合索引（多列 index）/ 复合唯一索引（multi-column unique index）”没有直接的声明方式。

因此，如果你希望为某些查询模式（例如 `WHERE a = ? AND b = ?`、`ORDER BY a, b`）创建复合索引，最小改动方案是：**使用原生 SQL 在数据库打开时创建索引**。

## 方案概述

- 推荐使用 `LitePal.registerIndexListener(...)` 在数据库 **create/upgrade** 后执行索引创建 SQL（避免每次 open 都执行）。
- 也可以使用 `LitePal.registerDatabaseListener(...)` 在 `onOpen(db)` 回调中执行索引创建 SQL（最通用，且能覆盖极端场景）。
- 索引 SQL 使用 `IF NOT EXISTS`，确保重复执行是幂等的。
- 由于 LitePal 升级过程中可能会“重建表”，重建会导致索引丢失，因此建议在 **每次升级后补建**；如需更强兜底，可在 `onOpen(db)` 再补建一次。

## 为什么要放在 onOpen(db)（可选）

- `DatabaseListener.onOpen(db)` 会在数据库打开后回调，表已经存在（新建/升级完成后也会进入 open）。
- `onCreate()` / `onUpgrade(oldVersion, newVersion)` 在当前实现中没有提供 `SQLiteDatabase` 参数，不适合直接执行 `execSQL`。

## 实现步骤

### 1）注册 IndexListener（推荐）

Kotlin 示例：

```kotlin
LitePal.registerIndexListener(object : IndexListener {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_demo_a_b ON demo(a, b)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_demo_a_b ON demo(a, b)")
    }
})
```

### 2）注册 DatabaseListener（可选兜底）

Kotlin 示例：

```kotlin
LitePal.registerDatabaseListener(object : DatabaseListener {
    override fun onConfigure(db: SQLiteDatabase) = Unit

    override fun onOpen(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_demo_a_b ON demo(a, b)"
        )

        // 如需复合唯一索引：
        // db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS uk_demo_a_b ON demo(a, b)")
    }

    override fun onCreate() = Unit

    override fun onUpgrade(oldVersion: Int, newVersion: Int) = Unit
})
```

### 3）确认表名/列名与 LitePal 实际一致

LitePal 的建表 SQL 会按 `litepal.xml` 中的 `cases` 配置对标识符做大小写转换，且关键字冲突列可能会被追加后缀。
因此建议在上线前通过 `PRAGMA table_info(<table>)`、`PRAGMA index_list(<table>)` 校验最终名称。

### 4）幂等与命名建议

- 推荐使用 `IF NOT EXISTS`，避免重复创建异常。
- 索引名建议保持稳定（例如 `idx_<table>_<col1>_<col2>` / `uk_<table>_<col1>_<col2>`），便于排查与迁移。

## 升级/重建的影响与注意事项

### 1）LitePal 升级不会“每次都重建所有表”

但当发生删列/改类型/约束变化、或新增 not null/unique 等场景时，LitePal 可能会对**相关表**进行重建（rename temp + create + migrate + drop temp），从而导致索引丢失。
因此“在 onOpen 补建索引”是必要的。

### 2）复合索引可能触发额外的表重建（尤其是复合 UNIQUE）

（未修复版本）LitePal 在读取现有索引时只会拿到 `PRAGMA index_info` 的第一列信息，因此复合索引在它的视角里可能被当成“首列索引/首列 unique”，从而触发“约束不一致”的判定并导致表重建。

（本仓库已做小改动规避）通过在 `DBUtility.findIndexedColumns` 中**忽略多列索引**（只把单列索引用作 schema diff 的依据），可以避免复合索引/复合 UNIQUE 因误判而触发表重建；但当表因为其他原因被重建时，复合索引仍会丢失，所以仍建议在 `onOpen(db)` 补建。

规避建议（按风险从低到高，适用于未修复版本）：

- **普通复合索引（非 unique）**：可考虑把复合索引的第一列同时标注 `@Column(index = true)`，减少被误判为“约束漂移”的概率；复合索引本身仍用 SQL 补建。
- **复合唯一索引（unique）**：更难与 LitePal 的字段级 `unique` 对齐，可能在升级时被反复判定为不一致；如必须使用，建议评估升级频率与重建成本，或考虑改造/迁移到可显式管理 migration 的方案（如 Room）。

## 验证与排查

- 查看表索引：
  - `PRAGMA index_list(demo)`
- 查看索引包含的列：
  - `PRAGMA index_info(idx_demo_a_b)`

如果发现索引在升级后消失：
- 确认 `onOpen(db)` 是否每次都执行；
- 确认索引 SQL 的表名/列名与最终 schema 一致；
- 检查是否存在“升级触发表重建”的场景，导致索引被 drop 后需要补建。
