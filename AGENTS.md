# IMPORTANT

Always respond in Chinese.

## Build / Publish（本地）

本仓库使用 Gradle Wrapper + JDK 11 构建。为保证可复现（含 SDK/Gradle cache），建议按如下方式运行：

```bash
HOME="$PWD/.home" \
ANDROID_SDK_HOME="$PWD/.android-home" \
ANDROID_USER_HOME="$PWD/.android-home" \
JAVA_HOME="$PWD/.tooling/jdk-11.0.29+7/Contents/Home" \
GRADLE_USER_HOME="$PWD/.gradle" \
./gradlew :core:assembleRelease --no-daemon
```

发布到本地 Maven 仓库（路径见 `gradle.properties` 的 `SNAPSHOT_REPOSITORY_URL` / `RELEASE_REPOSITORY_URL`）：

```bash
HOME="$PWD/.home" \
ANDROID_SDK_HOME="$PWD/.android-home" \
ANDROID_USER_HOME="$PWD/.android-home" \
JAVA_HOME="$PWD/.tooling/jdk-11.0.29+7/Contents/Home" \
GRADLE_USER_HOME="$PWD/.gradle" \
./gradlew :core:uploadArchives --no-daemon
```

默认本地仓库目录：`/Users/kei/workspace/project/mvn-repo`（包含 `snapshots/` 与 `release/`）。

