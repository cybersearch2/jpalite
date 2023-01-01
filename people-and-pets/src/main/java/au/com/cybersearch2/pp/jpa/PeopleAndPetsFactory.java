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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;

public class PeopleAndPetsFactory {

	private final PeopleAndPetsModule module;
    private PersistenceContext persistenceContext;
    private DatabaseSupport databaseSupport;
    
    public PeopleAndPetsFactory(int version) {
    	module = 
    		new PeopleAndPetsModule(
                    new ResourceEnvironment() {

           			 @Override
           			 public InputStream openResource(String resourceName) throws IOException {
         				String path = "v" + version + "/" + resourceName;
        				return PeopleAndPetsModule.class.getClassLoader().getResourceAsStream(path);
           			 }
           			
           			 @Override
           			 public Locale getLocale() {
           				 // Developed in Australia
           				 return new Locale("en", "AU");
               		 }
                   }); 
        databaseSupport = module.getDatabaseSupport();
    }
    
    public PersistenceContext getPersistenceContext() {
    	if (persistenceContext == null)
            persistenceContext = module.providePersistenceContext();
        return persistenceContext;
    }
    
	public DatabaseSupport getDatabaseSupport() {
		return databaseSupport;
	}
}
