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
package au.com.cybersearch2.pp.v2;

import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.QueryForAllGenerator;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.pp.PeopleAndPets;
import au.com.cybersearch2.pp.jpa.PeopleAndPetsModule;

public class PeopleAndPetsV2 extends PeopleAndPets {

	public PeopleAndPetsV2(TaskExecutor taskExecutor) {
		super(taskExecutor);
	}

	static public final int JPA_VERSION = 2;

	@Override
    protected PersistenceContext initializeApplication()
    {
        // Set up dependency injection, which creates an ObjectGraph from a PeopleAndPetsModule configuration object
        component = new ApplicationComponent(new PeopleAndPetsModule(JPA_VERSION) {
  
        	@Override
            public  DatabaseSupport provideDatabaseSupport()
            {
                DatabaseSupport databaseSupport = super.provideDatabaseSupport();
                databaseSupport.registerOpenHelperCallbacks(new PeopleOpenHelperCallbacks());
                databaseSupport.registerOpenHelperCallbacks(new PetsOpenHelperCallbacks());
                return databaseSupport;    
            }

        });
        return component.persistenceContext();
    }
    
	@Override
    protected void initializeDatabase() throws InterruptedException
    {
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistenceContext.getPersistenceAdmin(PETS_PU);
        // Create named queries to find all objects of an entity class.
        // Note QueryForAllGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator<PetDataV2> allSimpleDataObjects = 
                new QueryForAllGenerator<>(PetDataV2.class, persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(PetDataV2.class, ALL_PET_DATA, allSimpleDataObjects);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = persistenceContext.getPersistenceAdmin(PEOPLE_PU);
        QueryForAllGenerator<PersonDataV2> allComplexDataObjects = 
                new QueryForAllGenerator<>(PersonDataV2.class, persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(PersonDataV2.class, ALL_PERSON_DATA, allComplexDataObjects);
    }
}
