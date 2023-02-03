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

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.container.JpaProcess;
import au.com.cybersearch2.container.JpaSetting;
import au.com.cybersearch2.container.PersistenceUnit;

/**
 * Implementation of onCreate() and onUpdate() OpenHelper abstract methods.
 * This is a persistence container in which tasks can be performed using a supplied
 * EntityManager object by calling doWork(). Unlike other PersistenceContainer 
 * implementations, the execution takes place in the current thread using supplied
 * ConnectionSource to support ORMLite's special connection implementation. Any
 * RuntimeException will be forwarded to the caller.
 * 
 * @author Andrew Bowley
 * 24/06/2014
 */
public class OpenHelperImpl implements OpenHelper
{
	protected PersistenceUnit unit;
    
    /**
     * Create ClassyOpenHelperCallbacks object
     * @param puName PersistenceUnitAdmin Unit name
     */
    public OpenHelperImpl(String puName)
    {
    }

	@Override
	public void setPersistenceUnit(PersistenceUnit unit) {
        this.unit = unit;
    }

    /**
     * What to do when your database needs to be created. Usually this entails creating the tables and loading any
     * initial data.
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    @Override
    public void onCreate(ConnectionSource connectionSource) 
    {
        validate(unit);
        unit.getDatabaseAdmin().onCreate(connectionSource);
    }

	/**
     * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
     * Maybe adding or deleting database columns, etc..
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    @Override
    public void onUpgrade(
            ConnectionSource connectionSource, 
            int oldVersion,
            int newVersion) 
    {
        validate(unit);
        unit.getDatabaseAdmin().onUpgrade(connectionSource, oldVersion, newVersion);
    }

    /**
     * Execute persistence work in same thread as caller
     * @param connectionSource Open ConnectionSource object
     * @param persistenceTask Object specifying unit of work
     * @return Executable object to track task status
     */
    protected WorkStatus doWork(final ConnectionSource connectionSource, final PersistenceTask persistenceTask)
    {
        validate(unit);
        // PersistenceUnitAdmin work required for JavaPersistenceContext, but only doTask() is relevant
        // as work is performed on caller's thread
        PersistenceWork persistenceWork = new PersistenceWork(){

            @Override
            public void doTask(EntityManagerLite entityManager)
            {
                persistenceTask.doTask(entityManager);
            }

            @Override
            public void onPostExecute(boolean success)
            {
            	// TODO onPostExecute
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
            	// TODO onRollback
            }
        };
		boolean isUserTransactions = 
				unit.getPersistenceAdmin().hasSetting(JpaSetting.user_transactions);
		JpaProcess jpaProcess = 
			new JpaProcess(unit, persistenceWork, true);
		if (isUserTransactions)
			jpaProcess.setUserTransactions(true);
		return jpaProcess.waitFor().exitValue();
    }

    private void validate(PersistenceUnit unit2) {
		if (unit == null)
			throw new IllegalStateException("Persistence unit not set");
		
	}

}
