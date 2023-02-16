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
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.PersistenceException;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Creates TransactionState instances, generating a unique transaction id for each one
 */
public class TransactionStateFactory {

	private static final String SQL_ERROR = "SQL error while creating transaction %d connection";

	/** Each transaction is uniquely identified using this generator */
	// Counter values provide unique savepoint identifiers
	// Note SQLite does not support nested save points
	protected static AtomicInteger savePointCounter = new AtomicInteger();

	/** Open connection source */
	private final ConnectionSource connectionSource;
	/** Current transaction identifier. Used only to generate unique savepoint name */
	private int transactionId;
	/** Transaction connect - recycled, if possible */
	private TransactionConnection transConnection;

	/**
	 * Construct TransactionStateFactory object
	 * @param connectionSource Open connection source
	 * @throws PersistenceException if error occurs while creating the database connection
	 */
	public TransactionStateFactory(ConnectionSource connectionSource) {
		this.connectionSource = connectionSource;
		transactionId = savePointCounter.incrementAndGet();
		// Create connection before the first transaction begins for early detection of trouble
		createConnection();
	}
	
	/**
	 * Returns a new TransactionState instance
	 * @return TransactionState object
	 */
	public TransactionState transactionStateInstance() {
		return createTransactionState();
	}
	
	public ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	protected int getTransactionId() {
		return transactionId;
	}

	private TransactionState createTransactionState() {
		if (transConnection.isActive()) 
			// This is not expected, but a new transaction is allowed while the previous one
			// is rolled back 
			createConnection();
		else
			// Recycle the transaction connection
			try {
			    transConnection.activate(transactionId);
			} catch (SQLException e) {
				throw new PersistenceException(String.format(SQL_ERROR, transactionId), e);
			}
		TransactionState transState = new TransactionState(transConnection, transactionId);
		// Increment transaction id to next value
		transactionId = savePointCounter.incrementAndGet();
		return transState;
	}

	private void createConnection() {
		try {
			transConnection = new TransactionConnection(connectionSource, transactionId);
		} catch (SQLException e) {
			throw new PersistenceException(String.format(SQL_ERROR, transactionId), e);
		}
	}
}
