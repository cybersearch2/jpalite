/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.classydb;

import com.j256.ormlite.support.ConnectionSource;

/**
 * DatabaseAdmin
 * @author Andrew Bowley
 * 05/07/2014
 */
public interface DatabaseAdmin
{

    public final static String SCHEMA_FILENAME = "schema-filename";

    public final static String DROP_SCHEMA_FILENAME = "drop-schema-filename";

    public final static String DATA_FILENAME = "data-filename";

    public final static String UPGRADE_FILENAME_FORMAT = "upgrade-filename-format";

    public final static String DATABASE_VERSION = "database-version";

    public final static String DATABASE_NAME = "database-name";

    /**
     * To support android.database.sqlite.SQLiteOpenHelper
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param connectionSource Open Connection Source
     */
    void onCreate(ConnectionSource connectionSource);
    
    /**
     * See android.database.sqlite.SQLiteOpenHelper
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param connectionSource Open Connection Source
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    void onUpgrade(ConnectionSource connectionSource, int oldVersion, int newVersion);

    OpenHelperCallbacks getCustomOpenHelperCallbacks();
}
