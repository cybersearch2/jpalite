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

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.TaskMessenger;
import au.com.cybersearch2.classytask.TaskStatus;

/**
 * H2ManyToManyFactory
 * @author Andrew Bowley
 * 8 Jan 2016
 */
public class H2ManyToManyFactory
{
    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
    }

    private final TaskExecutor taskExecutor;
    private final TaskMessenger<Void,Boolean> taskMessenger;

    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    
    public H2ManyToManyFactory(TaskExecutor taskExecutor, TaskMessenger<Void,Boolean> taskMessenger)
    {
    	this.taskExecutor = taskExecutor;
    	this.taskMessenger = taskMessenger;
        component = new ApplicationComponent() {

        	H2ManyToManyModule module = new H2ManyToManyModule(new JavaTestResourceEnvironment("src/main/resources"));
        	
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
        persistenceWorkModule = new PersistenceWorkModule("manytomany", persistenceWork, taskMessenger, taskExecutor);
		return persistenceWorkModule.doTask(component.persistenceContext());
    }

}
