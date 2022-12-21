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

import java.util.List;

import javax.persistence.Query;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.OpenHelperCallbacksImpl;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.pp.PeopleAndPets;
import au.com.cybersearch2.pp.jpa.QueryForAllGenerator;

/**
 * PetsOpenHelperCallbacks
 * @author Andrew Bowley
 * 24 Nov 2014
 */
public class PetsOpenHelperCallbacks extends OpenHelperCallbacksImpl 
{

	public PetsOpenHelperCallbacks() 
	{
		super(PeopleAndPets.PETS_PU);
		
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
				PetDataV2 simple1 = new PetDataV2("Misha", QuoteSource.getQuote());
                entityManager.persist(simple1);
                PetDataV2 simple2 = new PetDataV2("Buffy", QuoteSource.getQuote());
                entityManager.persist(simple2);
                //logMessage(puName, "Created 2 new PetData entries: " + millis);
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
        QueryForAllGenerator allSimpleDataObjects = 
                new QueryForAllGenerator(persistenceAdmin);
        persistenceAdmin.addNamedQuery(PetDataV2.class, PeopleAndPets.ALL_PET_DATA, allSimpleDataObjects);
    	doWork(connectionSource, getUpgradeTask1());
    }
    
    
	@SuppressWarnings("unchecked")
	protected PersistenceTask getUpgradeTask1()
	{
		return new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		    	// Query for all of the data objects in the database
		        Query query = entityManager.createNamedQuery(PeopleAndPets.ALL_PET_DATA);
		        List<PetDataV2> list = (List<PetDataV2>) query.getResultList();
		
				// If we already have items in the database
				for (PetDataV2 simple : list) 
				{
					simple.setQuote(QuoteSource.getQuote());
					entityManager.merge(simple);
				}
		    }
		};
	}

}
