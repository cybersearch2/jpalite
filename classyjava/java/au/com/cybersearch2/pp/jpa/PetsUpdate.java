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
package au.com.cybersearch2.pp.jpa;

import java.util.List;

import javax.persistence.TypedQuery;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.pp.PeopleAndPets;
import au.com.cybersearch2.pp.PeopleAndPetsMain;
import au.com.cybersearch2.pp.api.ObjectsStore;
import au.com.cybersearch2.pp.api.Pet;

/**
 * PetsUpdate
 * @author Andrew Bowley
 */
public class PetsUpdate implements PersistenceTask
{
	public static String NAMES[] = 
	{
		"Cuddles",
		"Axel",
		"Rex"
	};
	protected final ObjectsStore objectsStore;
    protected final String context;
    protected final Transcript transcript;
     
    /**
     * Create PetsUpdate object
     * @param objectsStore Object store
     * @param context Context
     */
    public PetsUpdate(ObjectsStore objectsStore, String context)
    {
    	this.objectsStore = objectsStore;
    	this.context = context;
    	transcript = new Transcript(); 
    }

    public String getMessage()
    {
    	StringBuilder sb = new StringBuilder();
    	transcript.getContents().forEach(text -> sb.append(text + '\n'));
    	return sb.toString();
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doTask(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
		// Query for all of the data objects in the database
    	/* Comments starting with "///" are alternate EntityManagerDelegate approach, 
    	 * but you will need to handle lots of places where checked SQLException is thrown. */
    	/// EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
    	/// PersistenceDao<PetData, Integer> petDao = (PersistenceDao<PetData, Integer>) delegate.getDaoForClass(PetData.class);
		/// List<PetData> list = petDao.queryForAll();
        TypedQuery<Pet> query = entityManager.createNamedQuery(PeopleAndPets.ALL_PET_DATA, Pet.class);
        List<Pet> list = query.getResultList();

		transcript.add("Got " + list.size() + " Pet entries in " + context);
		transcript.add("------------------------------------------");

		// If we already have items in the database
		int count = 0;
		for (Pet pet : list) 
		{
			transcript.add("[" + count + "] = " + pet.toString());
			++count;
		}
		transcript.add("------------------------------------------");
		for (Pet pet : list) 
		{
			/// petDao.delete(pet);
			// Note objects returned from queries are not managed, so need to call merge() on them
	        entityManager.merge(pet);
	        entityManager.remove(pet);
			transcript.add("Deleted Pet id " + pet.getId());
			PeopleAndPetsMain.logInfo(PeopleAndPets.PETS_PU, "Deleting Pet(" + pet.getId() + ")");
		}
		for (int i = 0; i < NAMES.length; i++) 
		{
			// Create a new pet object
			Pet pet = objectsStore.petInstance(NAMES[i]);
			// store it in the database
			/// petDao.create(pet);
			entityManager.persist(pet);
			PeopleAndPetsMain.logInfo(PeopleAndPets.PETS_PU, "Created Pet(" + pet.getId() + ")");
			// output it
			transcript.add("------------------------------------------");
			transcript.add("Created PetData #" + (i + 1) + ":");
			transcript.add(pet.toString());
		}
    }
}
