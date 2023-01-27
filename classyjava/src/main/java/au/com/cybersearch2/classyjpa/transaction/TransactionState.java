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
package au.com.cybersearch2.classyjpa.transaction;

import java.sql.SQLException;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classylog.LogManager;

/**
 * Manages transaction connection for commit and rollback
 * 
 * @author Andrew Bowley 09/05/2014
 */
public class TransactionState {
	private static Logger logger = LogManager.getLogger(TransactionState.class);

	/** Database connection with special connection, save point and auto commit features */
	private final TransactionConnection transConnection;
	/** Unique transaction identity */
	private final int transactionId;

	/**
	 * Construct a TransactionState instance
	 * 
	 * @param trandConnection Database connection with special connection, save point and auto commit features
	 * @param transactionId Unique transaction identity
	 * @throws SQLException if ConnectionSource error occurs
	 */
	public TransactionState(TransactionConnection transConnection, int transactionId) {
		this.transConnection = transConnection;
		this.transactionId = transactionId;
	}

	public DatabaseConnection getDatabaseConnection() {
		return transConnection.getDatabaseConnection();
	}

	public boolean isActive() {
		return transConnection.isActive();
	}
	
	/**
	 * Commit
	 * 
	 * @throws SQLException if database error occurs
	 */
	public void doCommit() throws SQLException {
		try {
			// Perform check for release state.
			if (!transConnection.canCommit()) {
				if (logger.isLevelEnabled(Level.WARNING)
						&& transConnection.excludeAutoCommit())
					logger.warn("doCommit() called while connection in invalid state");
				return;
			}
			// System.out.println("Transaction " + transactionId + " about to commit");
			transConnection.commit();
			if (logger.isLevelEnabled(Level.DEBUG))
				logger.debug(String.format("Committed transaction id %d", transactionId));
		} catch (SQLException e) {
			if (transConnection.getHasSavePoint()) {
				try {
					transConnection.rollback();
				} catch (SQLException e2) {
					logger.error("After commit exception, rolling back to save-point also threw exception", e2);
					// we continue to throw the commit exception
				} finally {
					if (logger.isLevelEnabled(Level.DEBUG))
						logger.debug(String.format("Rolled back transaction id %d - \"%s\"", transactionId, e.getMessage()));
				}
			}
			throw e;
		} finally {
			transConnection.release();
		}
	}

	/**
	 * Rollback
	 * 
	 * @throws SQLException if database error occurs
	 */
	public void doRollback() throws SQLException {
		try {
			// Perform check for release state.
			if (!transConnection.canCommit()) {
				if (logger.isLevelEnabled(Level.WARNING))
					logger.warn("doRollback() called while connection in invalid state");
				return;
			}
			transConnection.rollback();
			if (logger.isLevelEnabled(Level.DEBUG))
				logger.debug(String.format("Rolled back transaction id %d", transactionId));
		} finally {
			transConnection.release();
		}
	}

	protected int getTransactionId() {
		return transactionId;
	}

}
