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

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;

/**
 * TestDatabaseAdminImpl
 * @author Andrew Bowley
 * 01/08/2014
 */
public class TestDatabaseAdminImpl extends DatabaseAdminImpl
{
	TransactionCallable processFilesCallable;
    
    /**
     * @param puName
     * @param persistenceAdmin
     */
    public TestDatabaseAdminImpl(String puName,
            PersistenceAdmin persistenceAdmin,
            ResourceEnvironment resourceEnvironment)
    {
        super(persistenceAdmin, resourceEnvironment, null);

    }

    @Override
    protected void executeTask(ConnectionSource connectionSource, TransactionCallable processFilesCallable)
    {
        this.processFilesCallable = processFilesCallable;
    }
}
