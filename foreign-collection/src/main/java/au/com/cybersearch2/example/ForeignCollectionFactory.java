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
package au.com.cybersearch2.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyjpa.entity.EntityClassLoader;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.TaskMessenger;
import au.com.cybersearch2.classytask.TaskStatus;

/**
 * ForeignCollectionFactory
 * @author Andrew Bowley
 * 8 Jan 2016
 */
public class ForeignCollectionFactory
{
    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
    }

    private final TaskExecutor taskExecutor;
    private final TaskMessenger<Void,Boolean> taskMessenger;

    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    
    public ForeignCollectionFactory(TaskExecutor taskExecutor, TaskMessenger<Void,Boolean> taskMessenger)
    {
    	this.taskExecutor = taskExecutor;
    	this.taskMessenger = taskMessenger;
        component = new ApplicationComponent() {

        	ForeignCollectionModule module = new ForeignCollectionModule(
                new ResourceEnvironment() {

    			@Override
    			public InputStream openResource(String resourceName) throws IOException {
    		        File resourceFile = new File("src/main/resources", resourceName);
    		        if (!resourceFile.exists())
    		            throw new FileNotFoundException(resourceName);
    		        InputStream instream = new FileInputStream(resourceFile);
    		        return instream;
    			}
    			
    			@Override
    			public Locale getLocale() {
    				return new Locale("en", "AU");
    			}

    			@Override
    			public File getDatabaseDirectory() {
    				return null;
    			}

    			@Override
    			public EntityClassLoader getEntityClassLoader(String puName) {
    				return null;
    			}}); 
       	
			@Override
			public PersistenceContext persistenceContext() {
				return module.providePersistenceContext();
			}

        };
    }
    
    public PersistenceContext getPersistenceContext()
    {
        return component.persistenceContext();
    }
    
    public TaskStatus doTask(PersistenceWork persistenceWork)
    {
        persistenceWorkModule = new PersistenceWorkModule(ForeignCollectionMain.ACCOUNTS_PU, persistenceWork, taskMessenger, taskExecutor);
		return persistenceWorkModule.doTask(component.persistenceContext());
    }
}
