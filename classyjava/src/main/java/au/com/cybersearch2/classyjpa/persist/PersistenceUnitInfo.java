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
package au.com.cybersearch2.classyjpa.persist;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import au.com.cybersearch2.container.JpaSetting;
import au.com.cybersearch2.container.SettingsMap;

/**
 * PersistenceUnitInfo
 * @see javax.persistence.spi.PersistenceUnitInfo
 * @author Andrew Bowley
 * 13/06/2014
 */
public class PersistenceUnitInfo
{
    public static final String PERSISTENCE_CONFIG_FILENAME = "persistence.xml";
    public static final String PU_NAME_PROPERTY = "persistence-unit-name";
    public static final String CUSTOM_OHC_PROPERTY = "open-helper-class";
    
    private final String persistenceUnitName;
    private final Set<String> managedClassNames;
   
    private String persistenceProviderClassName = "";
    private Properties properties;
    private SettingsMap settingsMap;
 
    /**
     * Construct PersistenceUnitInfo object
     * @param persistenceUnitName Name
     */
    public PersistenceUnitInfo(String persistenceUnitName)
    {
        this.persistenceUnitName = persistenceUnitName;
        managedClassNames = new HashSet<>();
        properties = new Properties();
        settingsMap = new SettingsMap();
    }

    public void addClassName(String className) {
    	managedClassNames.add(className);
    }
    
    public Set<String> getManagedClassNames() 
    {
        return Collections.unmodifiableSet(managedClassNames);
    }

    protected void setPersistenceProviderClassName(String persistenceProviderClassName) {
		this.persistenceProviderClassName = persistenceProviderClassName;
	}

	public String getPersistenceProviderClassName() 
    {
        return persistenceProviderClassName;
    }

    public String getPersistenceUnitName() 
    {
        return persistenceUnitName;
    }

    public Properties getProperties() 
    {
    	Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

	public void setProperty(String key, String attribute) {
		properties.setProperty(key, attribute);
	}

	public void put(JpaSetting key, String value) {
		settingsMap.put(key, value);
	}

	public SettingsMap getSettingsMap() {
		return settingsMap;
	}
}
