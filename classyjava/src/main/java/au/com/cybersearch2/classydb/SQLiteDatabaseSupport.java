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

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SQLiteDatabaseSupport
 * SQLite implementation for direct database access to allow native operations to be performed
 * @author Andrew Bowley
 * 16/06/2014
 */
public class SQLiteDatabaseSupport extends DatabaseSupportBase
{
	/** Log name */
    private static final String TAG = "SQLiteDatabaseSupport";
    static Log log = JavaLogger.getLogger(TAG);
    /** SQLite memory path */
    private static final String IN_MEMORY_PATH = "jdbc:sqlite::memory:";
    
    private File databaseDirectory;
 
    /**
     * Construct a SQLiteDatabaseSupport object
     * @param connectionType ConnectionType - memory, file or pooled
     */
    public SQLiteDatabaseSupport(ConnectionType connectionType)
    {
    	super(new SqliteDatabaseType(), connectionType, log, TAG);
    }

    public SQLiteDatabaseSupport(File databaseDirectory) {
    	super(new SqliteDatabaseType(), ConnectionType.file, log, TAG);
    	this.databaseDirectory = databaseDirectory;
    }

    protected File getDatabaseLocation()
    {
    	return databaseDirectory == null ? new File(DEFAULT_FILE_LOCATION) : databaseDirectory;
    }
    
	@Override
	protected String getVersionUpdateStatement(String infoTableName, int version) 
	{
		return "UPDATE `" + infoTableName + "` set `version` = " + version;
	}

	@Override
	protected String getVersionCreateStatement(String infoTableName) 
	{
		return "CREATE TABLE `" + infoTableName + "` (`version` INTEGER )";
	}

	@Override
	protected String getVersionInsertStatement(String infoTableName, int version) 
	{
		return "INSERT INTO `" + infoTableName + "` (`version`) values (" + version  + ")";
	}

	@Override
	protected ConnectionSource getConnectionSourceForType(String databaseName, Properties properties) throws SQLException
    {
		String fileLocation = getDatabaseLocation().getAbsolutePath();
        switch(connectionType)
	        {
	        case file:
	            return new JdbcConnectionSource("jdbc:sqlite:" + fileLocation  + "/" + databaseName);
	        case pooled:
	            return new JdbcPooledConnectionSource("jdbc:sqlite:" + fileLocation  + "/" + databaseName); 
	        case memory: 
	        default:
	            return new JdbcConnectionSource(IN_MEMORY_PATH /*+ databaseName*/);
	        }
    }
}
