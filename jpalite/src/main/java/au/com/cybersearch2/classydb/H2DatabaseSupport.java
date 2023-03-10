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

import org.h2.jdbcx.JdbcDataSource;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.H2DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.logger.Logger;

import au.com.cybersearch2.classylog.LogManager;

/**
 * H2DatabaseSupport
 * 
 * @author Andrew Bowley 16 May 2015
 */
public class H2DatabaseSupport extends DatabaseSupportBase {
	private static Logger logger = LogManager.getLogger(H2DatabaseSupport.class);
	/** H2 memory path */
	private static final String IN_MEMORY_PATH = "jdbc:h2:mem:";

	private File databaseDirectory;

	/**
	 * Construct an H2DatabaseSupport object
	 * 
	 * @param connectionType ConnectionType - memory, file or pooled
	 */
	public H2DatabaseSupport(ConnectionType connectionType) {
		super(new H2DatabaseType(), connectionType, logger);
	}

	/**
	 * Construct an H2DatabaseSupport object
	 * 
	 * @param databaseDirectory Database location. ConnectionType is automatically
	 *                          "file"
	 */
	public H2DatabaseSupport(File databaseDirectory) {
		super(new H2DatabaseType(), databaseDirectory != null ? ConnectionType.file : ConnectionType.memory, logger);
		this.databaseDirectory = databaseDirectory;
	}

	@Override
	protected File getDatabaseLocation() {
		return databaseDirectory == null ? new File(DEFAULT_FILE_LOCATION) : databaseDirectory;
	}

	@Override
	protected String getVersionUpdateStatement(String infoTableName, int version) {
		return "UPDATE `" + infoTableName + "` set `version` = " + version;
	}

	@Override
	protected String getVersionCreateStatement(String infoTableName) {
		return "CREATE TABLE `" + infoTableName + "` (`version` INTEGER )";
	}

	@Override
	protected String getVersionInsertStatement(String infoTableName, int version) {
		return "INSERT INTO `" + infoTableName + "` (`version`) values (" + version + ")";
	}

	@Override
	protected ConnectionSource getConnectionSourceForType(String databaseName, Properties properties)
			throws SQLException {
		String fileLocation = getDatabaseLocation().getAbsolutePath();
		switch (connectionType) {
		case file: {
			String url = "jdbc:h2:" + fileLocation + "/" + databaseName;
			return getDataSourceConnectionSource(url, properties);
		}
		case pooled: // TODO - Add H2 Connection Pool
			return getPooledConnectionSource(databaseName, fileLocation, properties);
		case memory:
		default: {
			String url = IN_MEMORY_PATH + databaseName;
			return getDataSourceConnectionSource(url, properties);
		}
		}
	}

	private DataSourceConnectionSource getDataSourceConnectionSource(String url, Properties properties)
			throws SQLException {
		JdbcDataSource jdbcDataSource = new JdbcDataSource();
		String finalUrl = appendProperties(url, properties, jdbcDataSource);
		jdbcDataSource.setURL(finalUrl);
		return new DataSourceConnectionSource(jdbcDataSource, finalUrl);
	}

	private JdbcPooledConnectionSource getPooledConnectionSource(String databaseName, String fileLocation,
			Properties properties) throws SQLException {
		String url = appendProperties("jdbc:h2:" + fileLocation + "/" + databaseName, properties);
		return new JdbcPooledConnectionSource(url);
	}

}
