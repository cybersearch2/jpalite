/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.classyjpa.transaction;

import java.sql.SQLException;
import java.sql.Savepoint;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classylog.LogManager;

/**
 * Database connection with special connection, save point and auto commit features
 */
public class TransactionConnection {

	protected static final String SAVE_POINT_PREFIX = "ORMLITE";
	
	private static Logger logger = LogManager.getLogger(TransactionConnection.class);

	/** Open connection source */
	private final ConnectionSource connectionSource;
	private boolean savedSpecialConnection;
	private boolean isNestedSavePointsSupported;
	private DatabaseConnection connection;
	private Boolean hasSavePoint;
	private Boolean autoCommitAtStart;
	private Savepoint savePoint;
	private String savePointName;

	/**
	 * Construct TransactionConnection object
	 * @param connectionSource Open connection source
	 * @param transactionId Transaction id used to create unique savepoint name
	 * @throws SQLException if error occurs during savepoint or autocommit operation
	 */
	public TransactionConnection(ConnectionSource connectionSource, int transactionId) throws SQLException {
		this.connectionSource = connectionSource;
		activate(transactionId);
	}

	/**
	 * Returns flag set true if autocommit needs to be turned off
	 * @return boolean
	 */
	public boolean excludeAutoCommit() {
		return savedSpecialConnection || isNestedSavePointsSupported;
	}

	/**
	 * Returns read/write connection provided by the connection source
	 * @return DatabaseConnection object
	 */
	public DatabaseConnection getDatabaseConnection() {
		return connection;
	}

	public void commit() throws SQLException {
		connection.commit(savePoint);
	}
	
	public void rollback() throws SQLException {
		connection.rollback(savePoint);
	}
	
	/**
	 * Reset everything to initial state
	 */
	public void release() {
		if (connection != null) {
			// try to restore if we are in auto-commit mode
			if ((autoCommitAtStart != null) && autoCommitAtStart)
				restoreAutoCommit();
			if (savedSpecialConnection)
			    clearSpecialConnection();
			savePoint = null;
			hasSavePoint = null;
			connection = null;
		}
	}

	/**
	 * Returns flag set if database connection is open and transaction-capable
	 * @return boolean
	 */
	public boolean isActive() {
		return (connection != null) && 
				(!excludeAutoCommit() ||
			        (savePoint != null && hasSavePoint != null));
	}

	/**
	 * Returns flag set true if commit can proceed
	 * @return boolean
	 * @throws SQLException if error occurs checking autocommit status
	 */
	public boolean canCommit() throws SQLException {
		return isValid() && !connection.isAutoCommit();
	}

	/**
	 * Open the database connection and prepare it to perform transactions
	 * @param transactionId Transaction id used to create unique savepoint name
	 * @throws SQLException if error occurs during savepoint or autocommit operation
	 */
	public void activate(int transactionId) throws SQLException {
		if (connectionSource == null)
			throw new IllegalStateException("Connection source is null");
		try {
			connection = connectionSource.getReadWriteConnection("");
			savedSpecialConnection = connectionSource.saveSpecialConnection(connection);
			isNestedSavePointsSupported = connectionSource.getDatabaseType().isNestedSavePointsSupported();
			if (excludeAutoCommit()) {
				if (connection.isAutoCommitSupported())
					ensureAutoCommitOff();
				setSavePoint(transactionId);
			}
		} finally {
			if ((excludeAutoCommit() && 
			        (savePoint == null || hasSavePoint == null)))
				try {
					release();
				} catch (Throwable t) {
					logger.error("Error while creating a transaction connection", t);
				}
		}
	}

	protected Boolean getHasSavePoint() {
		return hasSavePoint;
	}

	protected String getSavePointName() {
		return savePointName;
	}

	/**
	 * Check for release state
	 * 
	 * @return boolean true for is valid
	 */
	private boolean isValid() {
		return (connection != null) && (hasSavePoint != null) && (autoCommitAtStart != null);
	}

	/**
	 * Turn off auto commit if currently on
	 * 
	 * @throws SQLException if database error occurs
	 */
	private void ensureAutoCommitOff() throws SQLException {
			autoCommitAtStart = Boolean.valueOf(connection.isAutoCommit());
			if (autoCommitAtStart) {
				// Disable auto-commit mode if supported and enabled at start
				connection.setAutoCommit(false);
				if (logger.isLevelEnabled(Level.DEBUG))
					logger.debug("Had to set auto-commit to false");
			}
	}

	/**
	 * Store save point
	 * 
	 * @throws SQLException if database error occurs
	 */
	private void setSavePoint(int transactionId) throws SQLException {
		savePointName = SAVE_POINT_PREFIX + transactionId;
		savePoint = connection.setSavePoint(savePointName);
		if (logger.isLevelEnabled(Level.DEBUG))
			logger.debug("Started savePoint transaction " + savePointName);
		hasSavePoint = Boolean.TRUE;
	}

	/**
	 * Restore auto commit if required
	 */
	private void restoreAutoCommit() {
			try {
				connection.setAutoCommit(true);
				if (logger.isLevelEnabled(Level.DEBUG))
					logger.debug("restored auto-commit to true");
			} catch (SQLException e) {
				if (logger.isLevelEnabled(Level.WARNING))
					logger.warn("setAutoCommit() failed");
			} finally {
				autoCommitAtStart = null;
			}
	}

	/**
	 * Clear arrangement to use a single connection for the transaction
	 */
	private void clearSpecialConnection() {
		connectionSource.clearSpecialConnection(connection);
		try {
			connectionSource.releaseConnection(connection);
		} catch (SQLException e) {
			if (logger.isLevelEnabled(Level.WARNING))
				logger.warn(String.format("releaseConnection() failed - \"%s\"", e.getMessage()));
		}
	}

}
