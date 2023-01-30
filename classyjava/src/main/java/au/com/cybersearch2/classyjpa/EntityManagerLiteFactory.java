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
package au.com.cybersearch2.classyjpa;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Interface used to interact with the entity manager factory for the persistence unit.
 * Only PersistenceUnitAdmin API 1.0 methods supported
 */
public interface EntityManagerLiteFactory {

	/**
	 * Close the factory, releasing any resources that it holds. After a factory instance has been closed, all methods invoked on it will
	 * throw the IllegalStateException, except for isOpen, which will return false. Once an EntityManagerFactory has been closed, all its
	 * entity managers are considered to be in the closed state.
	 * 
	 * @throws IllegalStateException
	 *             if the entity manager factory has been closed
	 */
	void close();

	/**
	 * Create a new application-managed EntityManager. This method returns a new EntityManager instance each time it is invoked. The isOpen
	 * method will return true on the returned instance.
	 * 
	 * @return entity manager instance
	 * @throws IllegalStateException
	 *             if the entity manager factory has been closed
	 */
	EntityManagerLite createEntityManager();

	/**
	 * Create a EntityManager bound to an existing connectionSource. Use only for
	 * special case of database creation or update.
	 * 
	 * @param connectionSource The existing ConnectionSource object
	 * @return Entity manager instance
	 * @throws IllegalStateException
	 *             if the entity manager factory has been closed
	 */
	EntityManagerLite createEntityManager(ConnectionSource connectionSource);

	/**
	 * Indicates whether the factory is open. Returns true until the factory has been closed.
	 * 
	 * @return boolean indicating whether the factory is open
	 */
	boolean isOpen();

}
