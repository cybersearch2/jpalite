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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import au.com.cybersearch2.classydb.DatabaseSupport;

/**
 * PersistenceUnitInfoImpl
 * @author Andrew Bowley
 * 13/06/2014
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo
{
    public static final String PERSISTENCE_CONFIG_FILENAME = "persistence.xml";
    public static final String PU_NAME_PROPERTY = "persistence-unit-name";
    public static final String CUSTOM_OHC_PROPERTY = "open-helper-class";
    
    private String persistenceUnitName;
    String persistenceProviderClassName = "";
    List<String> managedClassNames;
    Properties properties;
    
    public PersistenceUnitInfoImpl(String persistenceUnitName)
    {
        this.persistenceUnitName = persistenceUnitName;
        managedClassNames = new ArrayList<>();
        properties = new Properties();
        properties.setProperty(DatabaseSupport.JTA_PREFIX + PU_NAME_PROPERTY, persistenceUnitName);
        
    }

    @Override
    public void addTransformer(ClassTransformer classTransformer) 
    {
    }

    @Override
    public boolean excludeUnlistedClasses() 
    {
        return false;
    }

    @Override
    public ClassLoader getClassLoader() 
    {
        return null;
    }

    @Override
    public List<URL> getJarFileUrls() 
    {
        return null;
    }

    @Override
    public DataSource getJtaDataSource() 
    {
        return null;
    }

    @Override
    public List<String> getManagedClassNames() 
    {
        return managedClassNames;
    }

    @Override
    public List<String> getMappingFileNames() 
    {
        return null;
    }

    @Override
    public ClassLoader getNewTempClassLoader() 
    {
        return null;
    }

    @Override
    public DataSource getNonJtaDataSource() 
    {
        return null;
    }

    @Override
    public String getPersistenceProviderClassName() 
    {
        return persistenceProviderClassName;
    }

    @Override
    public String getPersistenceUnitName() 
    {
        return persistenceUnitName;
    }

    @Override
    public URL getPersistenceUnitRootUrl() 
    {
        return null;
    }

    @Override
    public Properties getProperties() 
    {
        return properties;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() 
    {
        return null;
    }
    
    @Override
    public String getPersistenceXMLSchemaVersion() 
    {
        return "2.0";
    }

    @Override
    public SharedCacheMode getSharedCacheMode() 
    {
        return null;
    }

    @Override
    public ValidationMode getValidationMode() 
    {
        return null;
    }
}
