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
package au.com.cybersearch2.classyjpa.persist;

import java.sql.SQLException;

import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyfy.data.alfresco.RecordFolder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * AndroidEnv
 * @author Andrew Bowley
 * 20/06/2014
 */
public class TestPersistenceFactory
{
    ConnectionSource connectionSource;
    PersistenceContext persistenceContext;
    
    public TestPersistenceFactory(PersistenceContext persistenceContext)
    {
        this.persistenceContext = persistenceContext;
        PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(TestClassyApplication.PU_NAME);
        EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
        persistenceAdmin.addNamedQuery(RecordCategory.class, TestClassyApplication.CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
        persistenceAdmin.addNamedQuery(RecordFolder.class, TestClassyApplication.FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
    }
    
    public ConnectionSource setUpDatabase() throws SQLException 
    {
        PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(TestClassyApplication.PU_NAME);
        return persistenceAdmin.getConnectionSource();
    }

    public void onShutdown()
    {
    	persistenceContext.getPersistenceAdmin(TestClassyApplication.PU_NAME).close();
    }
    
    public PersistenceContext getPersistenceEnvironment()
    {
        return persistenceContext;
    }
}
