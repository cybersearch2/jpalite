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

import javax.persistence.TypedQuery;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.OpenHelperCallbacksImpl;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.QueryForAllGenerator;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.pp.PeopleAndPets;

/**
 * PeopleOpenHelperCallbacks
 * @author Andrew Bowley
 * 24 Nov 2014
 */
public class PeopleOpenHelperCallbacks extends OpenHelperCallbacksImpl 
{

	public PeopleOpenHelperCallbacks() 
	{
		super(PeopleAndPets.PEOPLE_PU);
	}

    /**
     * What to do when your database needs to be created. Usually this entails creating the tables and loading any
     * initial data.
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    @Override
    public void onCreate(ConnectionSource connectionSource) 
    {
        super.onCreate(connectionSource);
    	doWork(connectionSource, getPopulateTask1());
    }

	protected PersistenceTask getPopulateTask1()
	{
		return new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				// create some entries in the onCreate
				PersonDataV2 complex1 = new PersonDataV2("Sarah", QuoteSource.getQuote());
				entityManager.persist(complex1);
				PersonDataV2 complex2 = new PersonDataV2("Karen", QuoteSource.getQuote());
				entityManager.persist(complex2);
				//logMessage(puName, "Created 2 new PersonData entries: " + millis);
			}};
	}

    /**
     * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
     * Maybe adding or deleting database columns, etc..
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    @Override
    public void onUpgrade(
            ConnectionSource connectionSource, int oldVersion, int newVersion) 
    {
    	super.onUpgrade(connectionSource, oldVersion, newVersion);
        QueryForAllGenerator<PersonDataV2> allComplexDataObjects = 
                new QueryForAllGenerator<>(PersonDataV2.class, persistenceAdmin);
        persistenceAdmin.addNamedQuery(PersonDataV2.class, PeopleAndPets.ALL_PERSON_DATA, allComplexDataObjects);
    	doWork(connectionSource, getUpgradeTask1());
    }
    
    
	protected PersistenceTask getUpgradeTask1()
	{
		return new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		    	// Query for all of the data objects in the database
		        TypedQuery<PersonDataV2> query = 
		        	entityManager.createNamedQuery(PeopleAndPets.ALL_PERSON_DATA, PersonDataV2.class);
				// If we already have items in the database
				for (PersonDataV2 complex : query.getResultList()) 
				{
					complex.setQuote(QuoteSource.getQuote());
					entityManager.merge(complex);
				}
		    }
		};
	}

}
