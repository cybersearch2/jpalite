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

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * TestPersistenceEnvironment
 * @author Andrew Bowley
 * 15/06/2014
 */
public class TestPersistenceEnvironment 
{
    private static final String TAG = "TestPersistenceEnvironment";
    protected static ConnectionType connectionType = ConnectionType.memory;
    
    static Log log = JavaLogger.getLogger(TAG);

    protected DatabaseSupport testDatabaseSupport;
    protected PersistenceFactory persistenceFactory;
    protected ResourceEnvironment resourceEnvironment;
     
    public TestPersistenceEnvironment()
    {
        testDatabaseSupport = new SQLiteDatabaseSupport(connectionType);
        resourceEnvironment = new JavaTestResourceEnvironment();
        persistenceFactory = new PersistenceFactory(testDatabaseSupport, resourceEnvironment);
    }

    public void close()
    {
        testDatabaseSupport.close();
    }
    
    public static void setConnectionType(ConnectionType value)
    {
        connectionType = value;
    }

    public PersistenceFactory getPersistenceFactory() 
    {
        return persistenceFactory;
    }

    public DatabaseSupport getDatabaseSupport() 
    {
        return testDatabaseSupport;
    }
    
}
