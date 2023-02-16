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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import au.com.cybersearch2.service.WorkerService;

/**
 * Container for Jpalite persistence implementation
 */
public class JpaContainer {

	private static class Property {
		
		private final String key;
		private final String value;
		
		public Property(String key, String value) {
			this.key = key;
			this.value = value;
		}

		protected String getKey() {
			return key;
		}

		protected String getValue() {
			return value;
		}
	}
	
	public static final String RESOURCE_PATH_NAME = "jpalite.resource-path";
	public static final String JSON_FILENAME = "jpalite.json";
	public static final String DATABASE_TYPE = "databaseType";
	public static final String CONNECTION_TYPE = "connectionType";

	/** List of persistence unit names in order of appearance in jpalite.json */
	private final List<String> puNames;
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
    /** jpalite version number. Default value of zero is initial version. */
    private int version;
	
    /**
     * Construct JpaContainer. No parameters supports singleton implementation
     */
	public JpaContainer() {
		// Default type values intended for testing only
		databaseType = DatabaseType.H2;
		connectionType = ConnectionType.memory;
		persistenceUnits = new HashMap<>();
		puNames = new ArrayList<>();
		jpaOptions = EnumSet.noneOf(JpaOption.class);
		primeUnit = "";
	}
	
	/**
	 * Establish persistence units configured in given version of jpalite.json file
	 */
	public void initialize(int version) {
	    this.version = version;
	    if (version < 1)
			throw new JpaliteException(String.format("Invalid version '%d'. It must be greater than zero", version));
	    initialize();
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
		if (jpaOptions.contains(JpaOption.use_double_long_bits))
	        DataPersisterManager.registerDataPersisters(
	        		JavaDoubleType.getSingleton(), PrimitiveJavaDoubleType.getSingleton());

        databaseSupport = getDatabaseSupport(databaseType, connectionType);
        resourceEnvironment = createResourceEnvironment(jsonFile.getParent());
        List<PersistenceUnitInfo> puInfoList = parsePersistenceJson(jsonObject);
        puInfoList.forEach(item -> puNames.add(item.getPersistenceUnitName()));
        createPersistenceUnits(puInfoList);
        databaseSupport.initialize();
		isInitialized = true;
	}

	public void forEach(Consumer<PersistenceUnit> action) {
		puNames.forEach(item -> action.accept(persistenceUnits.get(item)));
	}
	
	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public PersistenceUnit getUnit(String name) {
		if (!persistenceUnits.containsKey(name))
			throw new JpaliteException(String.format("Persistence unit named '%s' not found", name));
		return persistenceUnits.get(name);
	}
	
	public PersistenceUnit getPrimeUnit() {
		return persistenceUnits.get(primeUnit);
	}

	/**
	 * Execute given persistence work on prime persistence unit
	 * @param unitName Persistence unit name
	 * @param jpaliteWork Function to perform with entity manager
	 * @return JpaProcess object
	 */
	public JpaProcess execute(String unitName, PersistenceWork jpaliteWork) {
		PersistenceUnit unit = getUnit(unitName);
		boolean isSyncMode = jpaOptions.contains(JpaOption.synchronous_mode);
		boolean isUserTransactions = 
				unit.getPersistenceAdmin().hasSetting(JpaSetting.user_transactions);
		JpaProcess jpaProcess = 
			new JpaProcess(unit, jpaliteWork, isSyncMode);
		if (isUserTransactions)
			jpaProcess.setUserTransactions(true);
		if (isSyncMode) 
		    return jpaProcess.waitFor();
		else
		    return execute(new JpaProcess(unit, jpaliteWork));
	}
	
	/**
	 * Execute given persistence work on prime persistence unit
	 * @param jpaliteWork Function to perform with entity manager
	 * @return JpaProcess object
	 */
	public JpaProcess execute(PersistenceWork jpaliteWork) {
		return execute(primeUnit, jpaliteWork);
	}

    /**
     * Close all database connections
     * @throws InterruptedException 
     */
    public void close() throws InterruptedException
    {
   	    persistenceUnits.values().forEach(unit -> unit.close());
        WorkerService.await();
    }
    
	private JpaProcess execute(JpaProcess jpaProcess) {
	    CompletableFuture<JpaProcess> processFuture = jpaProcess.onExit();
	    try {
			return processFuture.get(30, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
	    	throw new PersistenceException("Jpa process failed to terminate normally", e);
		}
	}
	
	/**
	 * Create all persistence units using information from jpalite.json
	 * @param puInfoMap Persistence unit configurations mapped by name
	 */
	private void createPersistenceUnits(List<PersistenceUnitInfo> puInfoList) {
		puInfoList.forEach(item -> {
			persistenceUnits.put(item.getPersistenceUnitName(), createPersistenceUnit(item));
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
		String segment = version < 1 ? JSON_FILENAME : String.format("v%d/%s", version, JSON_FILENAME);
		// First look at system property
		String resourcePath = System.getProperty(RESOURCE_PATH_NAME, "");
		if (resourcePath.isEmpty()) {
			// Next look at jpalite home location
            String homePath = System.getProperty("user.home");
            if (homePath == null)
                throw new Error("Can't find user.home ??");
            resourcePath = Paths.get(homePath, ".jpalite")
                    .toAbsolutePath().normalize().toString();
            jsonFile = new File(resourcePath, segment);
            if (!jsonFile.exists()) {
            	// Next try to find the file on the classpath 
            	jsonFile = null;
				resourceUrl = this.getClass().getResource("/");
				if (resourceUrl != null) {
					try {
						jsonFile = new File(resourceUrl.toURI().getPath(), segment);
					} catch (URISyntaxException e) {
					}
					resourcePath = jsonFile.getParent();
					if (resourcePath == null)
						// This is not expected
		            	jsonFile = null;
				}
            }
		} else 
			jsonFile = new File(resourcePath, segment);
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

	private List<PersistenceUnitInfo> parsePersistenceJson(JSONObject jsonObject) {
		List<PersistenceUnitInfo> puList = new ArrayList<>();
		forEach(jsonObject, "units", persistenceUnit -> parseUnit((JSONObject)persistenceUnit, puList));
	    if (puList.isEmpty())
	    	throw new JpaliteException(String.format("File %s has no persistence unit configured", JSON_FILENAME));
		return puList;
	}

	private void parseUnit(JSONObject puJson, List<PersistenceUnitInfo> puList) {
		String name = (String) puJson.get("name");
		if (name == null)
			throw new JpaliteException(String.format("File %s persistence unit %d name missing", JSON_FILENAME, puList.size() + 1));
		PersistenceUnitInfo pu = new PersistenceUnitInfo(name);
		puList.add(pu);
		forEach(puJson, "classes", entityClass -> 
			pu.addClassName(entityClass.toString()));
		if (pu.getManagedClassNames().isEmpty())
	    	throw new JpaliteException(String.format("File %s persistence unit %s has no entity classes", JSON_FILENAME, name));
		forEach(puJson, "settings", item -> {
			int count = pu.getSettingsMap().size();
			Property property = getProperty(item.toString());
			for (JpaSetting jpaSetting: JpaSetting.values()) {
				if (jpaSetting.getKey().equalsIgnoreCase(property.getKey())) {
					pu.getSettingsMap().put(jpaSetting, property.getValue());
					break;
				}
			}
			if (count == pu.getSettingsMap().size())
				throw new JpaliteException(
					String.format("File %s persistence unit %s has unknown setting %s", JSON_FILENAME, name, property.getKey()));
		});
		forEach(puJson, "properties", item -> {
			Property property = getProperty(item.toString());
			pu.setProperty(property.getKey(), property.getValue());
		});
	}

    private Property getProperty(String string) {
		String key;
		String value;
		int pos = string.indexOf('=');
		if (pos != -1) {
			key = string.substring(0, pos);
			value = string.substring(pos +1);
		} else {
			key = string;
			value = "true";
		}
		return new Property(key.trim(), value.trim());
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
        OpenHelper openHelper = getOpenHelperCallbacks(persistenceConfig.getPuInfo().getSettingsMap());
        DatabaseAdminImpl databaseAdmin = new DatabaseAdminImpl(persistenceAdmin, resourceEnvironment);
        PersistenceUnit unit = new PersistenceUnit(name, databaseAdmin, persistenceAdmin, persistenceConfig);
        if (openHelper != null) {
            openHelper.setPersistenceUnit(unit);
            databaseAdmin.setOpenHelper(openHelper);
        }
    	databaseAdmin.initializeDatabase(persistenceConfig, databaseSupport);
    	if (primeUnit.isEmpty())
    		primeUnit = name;
    	return unit;
    }

    /**
     * Returns OpenHelperCallbacks object, if defined in the PU properties
     * @param properties Properties object
     * @return OpenHelperCallbacks or null if not defined
     */
    private OpenHelper getOpenHelperCallbacks(SettingsMap settingsMap)
    {
        if (settingsMap.hasSetting(JpaSetting.open_helper_class))
        {
        	String openHelperCallbacksClassname = settingsMap.get(JpaSetting.open_helper_class);
        	Class<?> openHelperClass;
			try {
				openHelperClass = Class.forName(openHelperCallbacksClassname);
            	if (openHelperClass.isAssignableFrom(OpenHelper.class)) 
        	        return (OpenHelper)openHelperClass.getConstructor().newInstance();
        	} catch (Throwable e) {
                throw new JpaliteException(String.format("Error instanciating open helper class %s", openHelperCallbacksClassname), e);
			}
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
