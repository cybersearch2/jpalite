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

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext.EntityManagerProvider;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;

/**
 * PersistenceContainer Provides task-scoped persistence context with automatic
 * rollback if a fatal exception occurs. The unit of work is passed as a
 * PersistenceWork object which handles one of 3 outcomes: success, failure and
 * rollback. Failure is intended for when pre-conditions are not satisfied. If
 * failure occurs after changes have be made, then rollback should be invoked.
 * 
 * @author Andrew Bowley 27/06/2014
 */
public class PersistenceContainer {

	/** JPA EntityManager "lite" factory */
	private final EntityManagerLiteFactory entityManagerFactory;
	/**
	 * Flag to indicate user transaction. If false, then only transaction method
	 * supported is setRollbackOnly()
	 */
	private volatile boolean isUserTransactionMode;

	/**
	 * Create PersistenceContainer object
	 * 
	 * @param persistenceAdmin Persistence Admin
	 * @param async            Flag set if executes asynchronously
	 */
	public PersistenceContainer(PersistenceAdmin persistenceAdmin, boolean async) {
		entityManagerFactory = persistenceAdmin.getEntityManagerFactory();
	}

	/**
	 * Set user transaction mode. The transaction is accessed by calling
	 * EntityManager getTransaction() method.
	 * 
	 * @param value boolean
	 */
	public void setUserTransactionMode(boolean value) {
		isUserTransactionMode = value;
	}

	/**
	 * Returns object which creates a persistence context and executes a task in
	 * that context
	 * 
	 * @param persistenceWork Persistence work
	 * @return JavaPersistenceContext object
	 */
	public JavaPersistenceContext getPersistenceTask(PersistenceWork persistenceWork) {
		return getPersistenceTask(persistenceWork, new EntityManagerProvider() {

			@Override
			public EntityManagerLite entityManagerInstance() {
				return entityManagerFactory.createEntityManager();
			}
		});
	}

	/**
	 * Returns object which creates a persistence context and executes a task in
	 * that context
	 * 
	 * @param persistenceWork       Persistence work
	 * @param entityManagerProvider Custom entity manager provider
	 * @return JavaPersistenceContext object
	 */
	public JavaPersistenceContext getPersistenceTask(PersistenceWork persistenceWork,
			EntityManagerProvider entityManagerProvider) {
		JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, entityManagerProvider);
		jpaContext.getTransactionInfo().setUserTransaction(isUserTransactionMode);
		return jpaContext;
	}
}
