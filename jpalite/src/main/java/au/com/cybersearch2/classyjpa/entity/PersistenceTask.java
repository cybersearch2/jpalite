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

/**
 * PersistenceTask performs Java PersistenceUnitAdmin operations in a container
 * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork
 * @author Andrew Bowley
 * 20 Nov 2014
 */
public interface PersistenceTask 
{
    /**
     * Perform a persistence operation. Execute on a background thread if multiple connections on a DataSource are allowed.
     * @param entityManager Entity manager provided to perform persistence operation(s). Do not call close().
     */
	void doTask(EntityManagerLite entityManager);
}
