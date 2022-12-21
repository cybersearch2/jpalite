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
package au.com.cybersearch2.classyjpa.entity;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext.EntityManagerProvider;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.ProgressListener;
import au.com.cybersearch2.classytask.WorkUnit;
import au.com.cybersearch2.classytask.SyncTaskRunner;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.TaskMessenger;
import au.com.cybersearch2.classytask.TaskRunner;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * Creates execution context for a persistence work unit. There are 3 constructors
 * each providing a distinct variation on how the execution is performed.
 */
public class PersistenceWorkModule
{

    private final String puName;

    private final boolean async;

    private final PersistenceWork persistenceWork;

    private final TaskMessenger<Void,Boolean> taskMessenger;

    private final TaskExecutor taskExecutor;

    private ConnectionSource connectionSource;

    private boolean isUserTransactions;

    /**
     * Construct PersistenceWorkModule object for asynchronous execution.
     * The work unit executes on one thread while a separate monitor thread 
     * waits for a result message to be posted upon the work unit termination.
     * @param puName Persistence unit name
     * @param persistenceWork Persistence uork unit
     * @param taskMessenger Communications between execution thread and monitor thread
     * @param taskExecutor Execution service with thread poo
     */
    public PersistenceWorkModule(String puName, 
    		                      PersistenceWork persistenceWork, 
    		                      TaskMessenger<Void,Boolean> taskMessenger, 
    		                      TaskExecutor taskExecutor)
    {
        this.puName = puName;
        this.persistenceWork = persistenceWork;
        this.taskMessenger = taskMessenger;
        this.taskExecutor = taskExecutor;
        async = taskExecutor != null;
    }

    /**
     * Construct PersistenceWorkModule object for synchronous execution
     * in which work unit computation and following post execution are
     * both performed on the caller's thread.
     * @param puName Persistence unit name
     * @param persistenceWork Persistence uork unit
     */
    public PersistenceWorkModule(String puName, PersistenceWork persistenceWork)
	{
    	this(puName, persistenceWork, null, null);
	}

    /**
     * Construct PersistenceWorkModule object for synchronous execution
     * in which the given database connection source is applied. This is
     * used to support operations relating to opening a database.
     * @param puName Persistence unit name
     * @param connectionSource Database connection source
     * @param persistenceWork Persistence uork unit
     */
    public PersistenceWorkModule(String puName, 
    		ConnectionSource connectionSource, 
            PersistenceWork persistenceWork)
	{
    	this(puName, persistenceWork);
    	this.connectionSource = connectionSource;
	}
    
    /**
     * Set user transaction mode. The transaction is accessed by calling EntityManager getTransaction() method.
     * @param isUserTransactions boolean
     */
    public void setUserTransactions(boolean isUserTransactions)
    {
        this.isUserTransactions = isUserTransactions;
    }

    public PersistenceWork getPersistenceWork()
    {
        return persistenceWork;
    }

    /**
     * Execute persistence work unit
     * @param persistenceContext Persistence context
     * @return TaskStatus object
     */
    public TaskStatus doTask(PersistenceContext persistenceContext) {
    	return doTask(persistenceContext.getPersistenceAdmin(puName));
    }

    /**
     * Execute persistence work unit
     * @param persistenceAdmin Administration of selected Persistence Unit
     * @return TaskStatus object
     */
    public TaskStatus doTask(PersistenceAdmin persistenceAdmin)
    {
		PersistenceContainer container = new PersistenceContainer(persistenceAdmin, async);
		container.setUserTransactionMode(isUserTransactions);
        JavaPersistenceContext jpaContext = 
        		connectionSource == null ? 
        			container.getPersistenceTask(persistenceWork) :
        			container.getPersistenceTask(persistenceWork, new EntityManagerProvider(){

        		        @Override
        		        public EntityManagerLite entityManagerInstance()
        		        {
        		            return persistenceAdmin.createEntityManager(connectionSource);
        		        }
        		    });
        WorkUnit<Void> backgroundTask = new WorkUnit<Void>() {

			@Override
			public boolean doTask(ProgressListener<Void> backgroundListener) {
				return jpaContext.doTask();
			}}; 
        TaskBase<Void>  taskBase;
        if (!async) {
        	taskBase = new TaskBase<Void>(jpaContext) {
	        		
	        		
				@Override
				protected void postResult(Boolean result) throws InterruptedException {
					finish(result);
					backgroundTask.setWorkStatus(Boolean.TRUE.equals(result) ? WorkStatus.FINISHED : WorkStatus.FAILED);
				}
	
				@Override
				protected void publishProgress(Void progress) throws InterruptedException {
				}
	        };
        } else {
	        	taskBase = new TaskBase<Void>(jpaContext) {
	
	
				@Override
			    public void finish(Boolean result) {
					super.finish(result);
					backgroundTask.setWorkStatus(Boolean.TRUE.equals(result) ? WorkStatus.FINISHED : WorkStatus.FAILED);
				}

				@Override
				protected void postResult(Boolean result) throws InterruptedException {
					taskMessenger.sendResult(this, result);
				}
	
				@Override
				protected void publishProgress(Void progress) throws InterruptedException {
					taskMessenger.sendProgress(this, null);
				}
	        };
        }
	    if (async) {
	        TaskRunner<Void> taskRunner = new TaskRunner<Void>(taskExecutor);
            taskRunner.execute(backgroundTask, 
            		           taskBase.getTaskListener(),	
            		           taskBase.getBackgroundListener());
	    }
	    else {
	    	SyncTaskRunner syncTaskRunner = new SyncTaskRunner();;
	    	try {
	    		syncTaskRunner.execute(backgroundTask, 
     		           taskBase.getTaskListener(),	
     		           taskBase.getBackgroundListener());
	    	} catch (Throwable e) {
	    		throw new RuntimeException(e);
	    	}
	    }
	    return backgroundTask;
    }
    
}
