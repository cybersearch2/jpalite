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
import au.com.cybersearch2.pp.api.Person;

/**
 * PeopleUpdate
 * @author Andrew Bowley
 */
public class PeopleUpdate implements PersistenceTask
{
	public static String NAMES[] = 
	{
		"Barack",
		"Abraham",
		"Bill"
	};
	
	protected final ObjectsStore objectsStore;
    protected final String context;
    protected final Transcript transcript;
    
    /**
     * Create PeopleUpdate object
     * @param objectsStore Object store
     * @param context Context
     */
    public PeopleUpdate(ObjectsStore objectsStore, String context)
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
    	/// PersistenceDao<Person, Integer> personDao = (PersistenceDao<Person, Integer>) delegate.getDaoForClass(Person.class);
		/// List<Person> list = personDao.queryForAll();
        TypedQuery<Person> query = entityManager.createNamedQuery(PeopleAndPets.ALL_PERSON_DATA, Person.class);
        List<Person> list = query.getResultList();

		transcript.add("Got " + list.size() + " Person entries in " + context);
		transcript.add("------------------------------------------");

		// If we already have items in the database
		int count = 0;
		for (Person person : list) 
		{
			transcript.add("[" + count + "] = " + person);
			++count;
		}
		transcript.add("------------------------------------------");
		for (Person person : list) 
		{
			/// personDao.delete(person);
			// Note objects returned from queries are not managed, so need to call merge() on them
	        entityManager.merge(person);
	        entityManager.remove(person);
			transcript.add("Deleted Person id " + person.getId());
			PeopleAndPetsMain.logInfo(PeopleAndPets.PEOPLE_PU, "Deleting Person(" + person.getId() + ")");
		}
		for (int i = 0; i < NAMES.length; i++) 
		{
			// Create a new person object
			Person person = objectsStore.personInstance(NAMES[i]);
			// store it in the database
			/// personDao.create(person);
			entityManager.persist(person);
			PeopleAndPetsMain.logInfo(PeopleAndPets.PEOPLE_PU, "Created Person(" + person.getId() + ")");
			// output it
			transcript.add("------------------------------------------");
			transcript.add("Created Person #" + (i + 1) + ":");
			transcript.add(person.toString());
		}
    }
}
