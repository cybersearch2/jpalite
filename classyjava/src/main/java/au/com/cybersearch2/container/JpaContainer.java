/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.container;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import javax.persistence.PersistenceException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.j256.ormlite.field.DataPersisterManager;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseType;
import au.com.cybersearch2.classydb.H2DatabaseSupport;
import au.com.cybersearch2.classydb.OpenHelper;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupport;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.JavaDoubleType;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfo;
import au.com.cybersearch2.classyjpa.persist.PrimitiveJavaDoubleType;

/**
 * Container for Jpalite persistence implementation
 */
public class JpaContainer {

	public static final String RESOURCE_PATH_NAME = "jpalite.resource-path";
	public static final String JSON_FILENAME = "jpalite.json";
	public static final String DATABASE_TYPE = "databaseType";
	public static final String CONNECTION_TYPE = "connectionType";

	/** All persistence units mapped by name */
	private final Map<String, PersistenceUnit> persistenceUnits;
	/** Database type enumeration based on Ormlite types */
	private DatabaseType databaseType;
	/** Connection type enumeration - memory, file or pooled */
	private ConnectionType connectionType;
	/** Flag set true if one-time initialization performed */
	private boolean isInitialized;
	/** Resource environment which allows customization away from standard defaults */
	private ResourceEnvironment resourceEnvironment;
    /** Native support. */
    private DatabaseSupport databaseSupport;
    /** Global options */
    private EnumSet<JpaOption> jpaOptions;
    /** First, possibly only, persistence unit defined in configuration */
    private String primeUnit;
	
    /**
     * Construct JpaContainer. No parameters supports singleton implementation
     */
	public JpaContainer() {
		// Default type values intended for testing only
		databaseType = DatabaseType.H2;
		connectionType = ConnectionType.memory;
		persistenceUnits = new HashMap<>();
		jpaOptions = EnumSet.noneOf(JpaOption.class);
		primeUnit = "";
	}
	
	/**
	 * Establish persistence units configured in jpalite.json file
	 */
	public void initialize() {
		if (isInitialized)
			return;
		File jsonFile = getJpaliteJson();
		JSONObject jsonObject = parseJpaliteJson(jsonFile);
		analyseDatabaseType(getString(DATABASE_TYPE, jsonObject));
		analyseConnectionType(getString(CONNECTION_TYPE, jsonObject));
		analyseOptions(jsonObject);
        databaseSupport = getDatabaseSupport(databaseType, connectionType);
        resourceEnvironment = createResourceEnvironment(jsonFile.getParent());
        Map<String, PersistenceUnitInfo> puInfoMap = parsePersistenceJson(jsonObject);
        createPersistenceUnits(puInfoMap);
        databaseSupport.initialize();
		isInitialized = true;
	}

	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	/**
	 * Execute given persistence work on prime persistence unit
	 * @param jpaliteWork Function to perform with entity manager
	 * @return JpaProcess object
	 */
	public JpaProcess execute(PersistenceWork jpaliteWork) {
		JpaRunner jpaRunner = new JpaRunner(persistenceUnits.get(primeUnit));
		return jpaRunner.execute(jpaliteWork);
	}

	/**
	 * Create all persistence units using information from jpalite.json
	 * @param puInfoMap Persistence unit configurations mapped by name
	 */
	private void createPersistenceUnits(Map<String, PersistenceUnitInfo> puInfoMap) {
		puInfoMap.forEach((key, value) -> {
			persistenceUnits.put(key, createPersistenceUnit(value));
		});
	}

	/**
	 * Returns resource environment which co-locates resources with jpalite.json
	 * @param resourcePath Directory path of jpalite.json
	 */
	private ResourceEnvironment createResourceEnvironment(String resourcePath) {
		return new ResourceEnvironment() {
      			 @Override
    			 public InputStream openResource(String resourceName) throws IOException {
    			     return openFile(new File(resourcePath, resourceName));
    			 }};
	}

	/**
	 * Validate connection type string and convert case-insensitive to enum
	 * @param string Connection type
	 * @throws JpaliteException if string is invalid
	 */
	private void analyseConnectionType(String string) {
		if (string.isEmpty())
			return;
		for (ConnectionType type: Arrays.asList(ConnectionType.values())) {
			if (type.name().equalsIgnoreCase(string)) {
				connectionType = type;
				return;
			}
		}
		throw new JpaliteException(String.format("Unknown %s - '%s'", CONNECTION_TYPE, string));
	}

	/**
	 * Validate database type string and convert case-insensitive to enum
	 * @param string Database type
	 * @throws JpaliteException if string is invalid
	 */
	private void analyseDatabaseType(String string) {
		if (string.isEmpty())
			return;
		for (DatabaseType type: Arrays.asList(DatabaseType.values())) {
			if (type.name().equalsIgnoreCase(string)) {
				databaseType = type;
				return;
			}
		}
		throw new JpaliteException(String.format("Unknown %s - '%s'", DATABASE_TYPE, string));
	}

	/**
	 * Validate option strings and convert each case-insensitive to enum
	 * @param string Database type
	 * @throws JpaliteException if string is invalid
	 */
	private void analyseOptions(JSONObject jsonObject) {
		List<JpaOption> optionList = new ArrayList<>();
		forEach(jsonObject, "options", option -> { 
			for (JpaOption optionEnum: JpaOption.values())
			    if (optionEnum.getKey().equalsIgnoreCase(option.toString())) {
			    	optionList.add(optionEnum);
			    	break;
			    }
			// TOD)- Report unknown options
		});
		if (!optionList.isEmpty()) {
			jpaOptions = EnumSet.copyOf(optionList);
			if (jpaOptions.contains(JpaOption.use_double_long_bits)) {
		        DataPersisterManager.registerDataPersisters(
		        		JavaDoubleType.getSingleton(), PrimitiveJavaDoubleType.getSingleton());
			}
		}
	}

	/**
	 * Returns path to jpalite.json using a heuristic approach to find it 
	 * @return
	 */
	private File getJpaliteJson() {
		File jsonFile = null;
		URL resourceUrl = null;
		// First look at system property
		String resourcePath = System.getProperty(RESOURCE_PATH_NAME, "");
		if (resourcePath.isEmpty()) {
			// Next look at jpalite home location
            String homePath = System.getProperty("user.home");
            if (homePath == null)
                throw new Error("Can't find user.home ??");
            resourcePath = Paths.get(homePath, ".jpalite")
                    .toAbsolutePath().normalize().toString();
            jsonFile = new File(JSON_FILENAME, JSON_FILENAME);
            if (!jsonFile.exists()) {
            	// Next try to find the file on the classpath 
            	jsonFile = null;
				resourceUrl = this.getClass().getClassLoader().getResource("jpalite");
				if (resourceUrl != null) {
					try {
						jsonFile = new File(resourceUrl.toURI().getPath());
					} catch (URISyntaxException e) {
					}
					resourcePath = jsonFile.getParent();
					if (resourcePath == null)
						// This is not expected
		            	jsonFile = null;
				}
            }
		} else 
			jsonFile = new File(resourcePath, JSON_FILENAME);
		if (jsonFile == null)
			throw new JpaliteException(String.format("Resource path not found in System property %s", RESOURCE_PATH_NAME));
		return jsonFile;
	}

	/**
	 * Parse jpalite.json file
	 * @param jsonFile Path to file
	 * @return JSONObject object
	 */
	private JSONObject parseJpaliteJson(File jsonFile) {
		JSONParser parser = new JSONParser();
		Object jsonObj = null;
		try (Reader reader = new FileReader(jsonFile)) {
			jsonObj = parser.parse(reader);
		} catch (FileNotFoundException e) {
			throw new JpaliteException(String.format("%s not found in %s", JSON_FILENAME, jsonFile.toString()));
		} catch (IOException e) {
			throw new JpaliteException(String.format("IO error reading file %s in %s", JSON_FILENAME, jsonFile.toString()), e);
		} catch (ParseException e) {
			throw new JpaliteException(String.format("Json parser error reading file %s in %s", JSON_FILENAME, jsonFile.toString()), e);
		}
		return (JSONObject) jsonObj;
	}

	/**
	 * Returns DatabaseSupport object specific to given database and connect types
	 * @param databaseType
	 * @param connectionType
	 * @return DatabaseSupport object
	 */
    private DatabaseSupport getDatabaseSupport(DatabaseType databaseType, ConnectionType connectionType) {
		switch (databaseType) {
		case H2: return new H2DatabaseSupport(connectionType);
		case SQLite: return new SQLiteDatabaseSupport(connectionType);
		default:
		}
		throw new PersistenceException(String.format("Unsupported database type %s", databaseType.name()));
	}

	private Map<String, PersistenceUnitInfo> parsePersistenceJson(JSONObject jsonObject) {
		Map<String, PersistenceUnitInfo> puMap = new HashMap<>();
		forEach(jsonObject, "units", persistenceUnit -> parseUnit((JSONObject)persistenceUnit, puMap));
	    if (puMap.isEmpty())
	    	throw new JpaliteException(String.format("File %s has no persistence unit configured", JSON_FILENAME));
		return puMap;
	}

	private void parseUnit(JSONObject puJson, Map<String, PersistenceUnitInfo> puMap) {
		String name = (String) puJson.get("name");
		if (name == null)
			throw new JpaliteException(String.format("File %s persistence unit %d name missing", JSON_FILENAME, puMap.size() + 1));
		PersistenceUnitInfo pu = new PersistenceUnitInfo(name);
		puMap.put(name, pu);
		forEach(puJson, "classes", entityClass -> 
			pu.addClassName(entityClass.toString()));
		if (pu.getManagedClassNames().isEmpty())
	    	throw new JpaliteException(String.format("File %s persistence unit $s has no entity classes", JSON_FILENAME, name));
		forEach(puJson, "properties", property -> {
			String key;
			String value;
			int pos = property.toString().indexOf('=');
			if (pos != -1) {
				key = property.toString().substring(0, pos);
				value = property.toString().substring(pos +1);
			} else {
				key = property.toString();
				value = "true";
			}
			pu.setProperty(key, value);
		});
	}

    /**
     * Initialize persistence unit implementations based on persistence.xml configuration
     * @throws PersistenceException
     */
    private PersistenceUnit createPersistenceUnit(PersistenceUnitInfo configuration) {
    	String name = configuration.getPersistenceUnitName();
        // Create configuration object and initialize it according to PU info read from persistence.xml
        // This includes setting up DAOs for all entity classes
        PersistenceConfig persistenceConfig = new PersistenceConfig(databaseSupport.getDatabaseType());
        persistenceConfig.setEntityClassLoader(resourceEnvironment.getEntityClassLoader(name));
        persistenceConfig.setPuInfo(configuration);
        // Create objects for JPA and native support which are accessed using PersistenceFactory
        PersistenceAdminImpl persistenceAdmin = new PersistenceAdminImpl(name, databaseSupport, persistenceConfig);
        OpenHelper openHelper = getOpenHelperCallbacks(persistenceConfig.getPuInfo().getProperties());
        DatabaseAdminImpl databaseAdmin = new DatabaseAdminImpl(persistenceAdmin, resourceEnvironment, openHelper);
    	databaseAdmin.initializeDatabase(persistenceConfig, databaseSupport);
    	if (primeUnit.isEmpty())
    		primeUnit = name;
    	return new PersistenceUnit(name, databaseAdmin, persistenceAdmin, persistenceConfig);
    }

    /**
     * Returns OpenHelperCallbacks object, if defined in the PU properties
     * @param properties Properties object
     * @return OpenHelperCallbacks or null if not defined
     */
    private OpenHelper getOpenHelperCallbacks(Properties properties)
    {
        // Property "open-helper-callbacks-classname"
        String openHelperCallbacksClassname = properties.getProperty(DatabaseSupport.JTA_PREFIX + PersistenceUnitInfo.CUSTOM_OHC_PROPERTY);
        if (openHelperCallbacksClassname != null)
        {
            // Custom
            for (OpenHelper openHelper: databaseSupport.getOpenHelperCallbacksList())
                if (openHelper.getClass().getName().equals(openHelperCallbacksClassname))
                    return openHelper;
            throw new PersistenceException(openHelperCallbacksClassname + " object not registered");
        }
        // Mo match
        return null;
    }
    
	private String getString(String key, JSONObject jsonObject) {
		String value = (String) jsonObject.get(key); 
		return (value == null) ? "" : value;
	}

	@SuppressWarnings("unchecked")
	private void forEach(JSONObject puJson, String key, Consumer<?> action) {
		JSONArray jsonArray = (JSONArray)puJson.get(key);
		if (jsonArray != null)
			jsonArray.forEach(action);
	}

}
