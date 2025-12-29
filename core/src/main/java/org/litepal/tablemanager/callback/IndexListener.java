/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litepal.tablemanager.callback;

import android.database.sqlite.SQLiteDatabase;

/**
 * Callback for creating indexes (especially composite indexes) after LitePal creates or upgrades
 * database schema.
 */
public interface IndexListener {

    /**
     * Called after LitePal creates all tables.
     */
    void onCreate(SQLiteDatabase db);

    /**
     * Called after LitePal upgrades schema and before the new version is marked as applied.
     */
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
