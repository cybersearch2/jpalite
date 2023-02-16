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
 * PersistenceWork
 * Interface for execution of work in a PersistenceUnitAdmin context. 
 * @see PersistenceContainer
 * @author Andrew Bowley
 * 28/06/2014
 */
public interface PersistenceWork extends PersistenceTask
{
    /**
     * Runs on separate thread after successful completion of {@link PersistenceTask#doTask(EntityManagerLite entityManager)}.
     * @param success True if PersistenceWork completed successfully, otherwise false
     */
    void onPostExecute(boolean success);

    /**
     * Handle rollback caused by exception while executing {@link PersistenceTask#doTask(EntityManagerLite entityManager)}
     * @param rollbackException Throwable exception which caused rollback
     */
    void onRollback(Throwable rollbackException);
}
