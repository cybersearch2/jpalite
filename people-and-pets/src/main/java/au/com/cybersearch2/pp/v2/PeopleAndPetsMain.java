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

import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Person;
import au.com.cybersearch2.pp.api.Pet;

/**
 * This application supports two databases and so has two persistence units in 
 * the persistence.xml configuration file. Each persistence unit is referenced in the code by name.
 * To support version upgrades, this application also includes a database version stored in a 
 * "User_Info" table on each database - see test java au.com.cybersearch2.pp.v2.PeopleAndPetsMain. 
 */
public class PeopleAndPetsMain extends au.com.cybersearch2.pp.PeopleAndPetsMain {

    private static final ObjectsStore objectsStoreV2;

    static {
    	objectsStoreV2= new ObjectsStore() {

    		@Override
    		public Person personInstance(String name) {
    			return new PersonDataV2(name, QuoteSource.getQuote());
    		}

    		@Override
    		public Pet petInstance(String name) {
    			return new PetDataV2(name, QuoteSource.getQuote());
    		}};
    }

    /**
     * Test 2 Databases accessed by application
     * @param args Not used
     */
	public static void main(String[] args)
	{
     	int returnCode = 1;
     	try {
            PeopleAndPetsV2 peopleAndPetsV2 = new PeopleAndPetsV2();
            if (peopleAndPetsV2.setUp())
            	returnCode = peopleAndPetsV2.performTasks(objectsStoreV2);
     	} catch (Throwable t) {
     		t.printStackTrace();
     	} finally {
     		System.exit(returnCode);
     	}
	}
	


}
