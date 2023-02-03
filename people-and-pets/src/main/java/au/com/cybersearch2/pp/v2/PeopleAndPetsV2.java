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

import au.com.cybersearch2.classyjpa.QueryForAllGenerator;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.container.PersistenceUnit;
import au.com.cybersearch2.pp.PeopleAndPets;

public class PeopleAndPetsV2 extends PeopleAndPets {

 	@Override
    protected int getJpaVersion() {
		return 2;
	}

 	@Override
    protected boolean initializeDatabase() throws InterruptedException
    {
        // Get Interface for JPA Support, required to create named queries
    	PersistenceUnit petsUnit = jpaContainer.getUnit(PETS_PU);
        PersistenceAdmin persistenceAdmin1 = petsUnit.getPersistenceAdmin();
        // Create named queries to find all objects of an entity class.
        // Note QueryForAllGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator<PetDataV2> allSimpleDataObjects = 
                new QueryForAllGenerator<>(PetDataV2.class, persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(PetDataV2.class, ALL_PET_DATA, allSimpleDataObjects);
    	PersistenceUnit peopleUnit = jpaContainer.getUnit(PEOPLE_PU);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = peopleUnit.getPersistenceAdmin();
        QueryForAllGenerator<PersonDataV2> allComplexDataObjects = 
                new QueryForAllGenerator<>(PersonDataV2.class, persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(PersonDataV2.class, ALL_PERSON_DATA, allComplexDataObjects);
        return true;
    }
}
