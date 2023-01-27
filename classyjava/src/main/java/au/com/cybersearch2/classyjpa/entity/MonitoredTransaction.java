package au.com.cybersearch2.classyjpa.entity;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.PreCommit;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classyjpa.transaction.TransactionStateFactory;

public class MonitoredTransaction extends EntityTransactionImpl {

    private static class OnTransactionPreCommitCallback implements TransactionCallable
    {
    	private final OrmEntityMonitor entityMonitor;
   
    	public OnTransactionPreCommitCallback(OrmEntityMonitor entityMonitor) {
    		this.entityMonitor = entityMonitor;
    	}
    	
        @Override
        public Boolean call(DatabaseConnection databaseConnection) throws Exception 
        {
        	entityMonitor.updateAllManagedObjects();
            return true; // Commit 
        }        

    }

	private final OrmEntityMonitor entityMonitor;
	private final ConnectionSource connectionSource;

	/**
	 * Construct a ClassyEntityTransaction instance
	 * 
	 * @param transactionStateFactory Creates TransactionState instances
	 * @param entityMonitor           Delegate management of entity objects
	 */
	public MonitoredTransaction(TransactionStateFactory tranStateFactory, OrmEntityMonitor entityMonitor) {
		super(tranStateFactory, new OnTransactionPreCommitCallback(entityMonitor));
		this.entityMonitor = entityMonitor;
		connectionSource = tranStateFactory.getConnectionSource();
	}

	public OrmEntityMonitor getEntityMonitor() {
		return entityMonitor;
	}
	
	public ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	protected PreCommit getPreCommit() {
		return super.getPreCommit();
	}
}
