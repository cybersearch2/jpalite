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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classybean.BeanException;
import au.com.cybersearch2.classybean.BeanUtil;
import au.com.cybersearch2.classyjpa.entity.EntityClassLoader;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.misc.JavaxPersistenceImpl;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * ClassAnalyser
 * Adds to com.j256.ormlite.misc.JavaxPersistence to support OneToMany and ManyToOne annotations.
 * Duplicates some DatabaseFieldConfig code for processing @ForeignCollection annotation.
 * @author Andrew Bowley
 * 18/07/2014
 */
public class ClassAnalyser
{
    /**
     * Interface for class which maps entity class  to OrmDaoHelper<T,ID> object
     */
    interface ClassRegistry
    {
        /**
         * Register a helper of compound generic type <T,ID>.
         * @param entityClass
         * @param primaryKeyClass
         */
        <T,ID> void registerEntityClass(Class<T> entityClass, Class<ID> primaryKeyClass);
    }

    /**
     * ForeignFieldData
     * Contains foreignFieldMap and foreignCollectionMap used to map foreign field column name to field name and 
     * assign foreignTableConfig to foreignCollection fields
     * @author Andrew Bowley
     * 25/05/2014
     */
    public static class ForeignFieldData
    {   // FieldKey is a composite of field class (or generic class for collections) and column name.
        // JPA specifies mappedBy column name, but OrmLite uses field name for one to many associatiions.
        Map<FieldKey, DatabaseFieldConfig> foreignFieldMap;
        Map<FieldKey, DatabaseFieldConfig> foreignCollectionMap;
        
        public ForeignFieldData()
        {
            foreignFieldMap = new HashMap<FieldKey, DatabaseFieldConfig>();
            foreignCollectionMap = new HashMap<FieldKey, DatabaseFieldConfig>();
        }
    }
    

    public static final String TAG = "ClassAnalyser";
    protected static Log log = JavaLogger.getLogger(TAG);
    

    protected DatabaseType databaseType;

    protected ClassRegistry classRegistry;

    protected EntityClassLoader entityClassLoader;

    /**
     * Construct a ClassAnalyser instance
     * @param databaseType DatabaseType which specifies database feature set
     * @param classRegistry ClassRegistry implementation
     */
    public ClassAnalyser(DatabaseType databaseType, ClassRegistry classRegistry)
    {
        this(databaseType, classRegistry, null);
    }

    /**
     * Construct a ClassAnalyser instance
     * @param databaseType DatabaseType which specifies database feature set
     * @param classRegistry ClassRegistry implementation
     * @param entityClassLoader Class loader to instantiate entity classes (optional)
     */
    public ClassAnalyser(DatabaseType databaseType, ClassRegistry classRegistry, EntityClassLoader entityClassLoader)
    {
        this.databaseType = databaseType;
        this.classRegistry = classRegistry;
        this.entityClassLoader = entityClassLoader;
    }

    /**
     * Returns a list of database table configurations for specified list of class names
     * @param managedClassNames List of class names representing all entities within a single PersistenceUnitAdmin Unit
     * @return List of DatabaseTableConfig
     */
    protected List<DatabaseTableConfig<?>> getDatabaseTableConfigList(List<String> managedClassNames)
    {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        // Report list of failed classes only after consuming managedClassNames list
        List<String> failedList = null;
        // Record data during analysis to resolve foreign field references
        ForeignFieldData foreignFieldData = new ForeignFieldData();
        // Obtain classes from class names
        for (String className: managedClassNames)
        {
            boolean success = false;
            try
            {
            	Class<?> clazz;
            	if (entityClassLoader != null)
            		clazz = entityClassLoader.loadClass(className);
            	else
            		clazz = Class.forName(className);
                classList.add(clazz);
                success = true;
            }
            catch (ClassNotFoundException e)
            {
                log.error(TAG, "Class not found: " + className, e);
            }
            if (!success)
            {
                if (failedList == null)
                    failedList = new ArrayList<String>();
                failedList.add(className);
            }
        }
        if (failedList != null)
            throw new PersistenceException("Failed to load following entity classes: " + failedList.toString());
        // Put database table configurations into a Map instead of a list to support set ForeignTableConfig below
        Map<String, DatabaseTableConfig<?>> tableConfigMap = new HashMap<String, DatabaseTableConfig<?>>();
        for(Class<?> clazz: classList)
        {
            DatabaseTableConfig<?> config = getTableConfiguration(clazz, foreignFieldData);
            if (config != null)
                tableConfigMap.put(clazz.getName(), config);
            else
            {
                if (failedList == null)
                    failedList = new ArrayList<String>();
                failedList.add(clazz.getName());
            }
        }
        if (failedList != null)
            throw new PersistenceException("Failed to extract persistence config following entity classes: " + failedList.toString());
        // Resolve foreign field mapping and foreign table configs
        // Set ForeignCollectionForeignFieldName for foreign collections (OneToMany) 
        for (Entry<FieldKey, DatabaseFieldConfig> entry: foreignFieldData.foreignCollectionMap.entrySet())
        {	// This wires a foreign collection that is not actually foreign, but maps to same table
            // If associated field is defined, the assign it's field name to ForeignCollectionColumnName
            DatabaseFieldConfig fieldConfig = foreignFieldData.foreignFieldMap.get(entry.getKey());
            if (fieldConfig != null)
            	entry.getValue().setForeignCollectionForeignFieldName(fieldConfig.getFieldName());
        }
        // Set ForeignTableConfig for foreign fields (ManyToOne) 
        for (Entry<FieldKey, DatabaseFieldConfig> entry: foreignFieldData.foreignFieldMap.entrySet())
        {
            Class<?> fieldType = entry.getKey().getEntityClass();
            DatabaseTableConfig<?> foreignTableConfig = tableConfigMap.get(fieldType.getName());
            if (foreignTableConfig == null)
                throw new PersistenceException("Table of type " + fieldType.getName() + " not found");
            entry.getValue().setForeignTableConfig(foreignTableConfig);
        }
        // Perform second pass on table configurations to fix foreign fields data type UNKNOWN
        classList.forEach(clazz -> {
        	// For each entity class field...
            DatabaseTableConfig<?> tableConfig = tableConfigMap.get(clazz.getName());
            List<DatabaseFieldConfig> fieldConfigs = tableConfig.getFieldConfigs();
            for (DatabaseFieldConfig fieldConfig: fieldConfigs) {
            	// ... check if it has a foreign reference
            	if (fieldConfig.isForeignAutoRefresh() || fieldConfig.getForeignColumnName() != null) {
            		// If it has, do a pass of the referenced table for fields of data type UNKNOWN
        			DatabaseTableConfig<?> foreignTableConfig = fieldConfig.getForeignTableConfig();
        			if (foreignTableConfig != null) 
        				doSecondTablePass(foreignTableConfig.getDataClass(), foreignTableConfig.getFieldConfigs());
            	}
    		
    		}
        });
        // Return tableConfigMap values converted to a list
        return new ArrayList<DatabaseTableConfig<?>>(tableConfigMap.values());
    }

    /**
     * Fix up any field if data type is UNKNOWN and the field type is byte[].
     * Because of some Ormlite backwards compatibility issues, a byte array won't be detected automatically. 
     * @param dataClass Entity class
     * @param fieldConfigs List of field configurations
     */
    private void doSecondTablePass(Class<?> dataClass, List<DatabaseFieldConfig> fieldConfigs) {
		fieldConfigs.forEach(fieldConfig -> {
			if ((DataType.UNKNOWN == fieldConfig.getDataType()))
				resolveUnknownType(dataClass, fieldConfig);
		});
		
	}

    /**
     * Automatically sets data type for byte array and serializable fields
     * @param dataClass Field type
     * @param fieldConfig Database field configuration
     */
    private void resolveUnknownType(Class<?> dataClass, DatabaseFieldConfig fieldConfig)  {
		// walk up the classes until we find the field
    	DataType dataType = DataType.UNKNOWN;
    	Class<?>[] fieldType = new Class<?>[] {null};
		for (Class<?> classWalk = dataClass; classWalk != null; classWalk = classWalk.getSuperclass()) {
			Field field;
			try {
				field = classWalk.getDeclaredField(fieldConfig.getFieldName());
				fieldType[0] = field.getType();
				if (fieldType[0].isPrimitive())
					break;
				if (byte[].class.isAssignableFrom(fieldType[0])) {
	            	dataType = DataType.BYTE_ARRAY;
				} else if (Serializable.class.isAssignableFrom(fieldType[0])) {
	            	dataType = DataType.SERIALIZABLE;
				} else
					break; // Could be Collection or other special case
			} catch (NoSuchFieldException e) {
				// we ignore this and just loop hopefully finding it in a upper class
			}
			if (dataType != DataType.UNKNOWN) {
				fieldConfig.setDataType(dataType);
				break;
			}
		}
	}
    
    /**
     * Returns DatabaseTableConfig for specified class
     * @param clazz Entity class
     * @param foreignFieldData ForeignFieldData to collect foreign field and foreign collection data
     * @return DatabaseTableConfig of generic type matching entity class or null if error occurs
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected DatabaseTableConfig<?> getTableConfiguration(Class<?> clazz, ForeignFieldData foreignFieldData)
    {
        List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
        // Obtain table name from @Entity annotation if available, otherwise use default name
        String tableName = new JavaxPersistenceImpl().getEntityName(clazz);
        // Allow name to be omitted
        if (tableName == null)
            tableName = "table" + clazz.getSimpleName();
        Class<?> idClass = null;
        // Ascend super class chain to find all relevant annotations
        for (Class<?> working = clazz; working != null; working = working.getSuperclass()) 
        {
            for (Field field : working.getDeclaredFields()) 
            {
                DatabaseFieldConfig fieldConfig = null;
                try
                {   // Try extract field configuration using supplied OrmLite library function
                    fieldConfig = DatabaseFieldConfig.fromField(databaseType, tableName, field);
                }
                catch (SQLException e)
                {   // This exception is not thrown by the OrmLite code, just declared
                }
                if (fieldConfig == null) 
                    // In case nothing found, check for unsupported OneToMany annotation
                    fieldConfig = createOneToManyConfig(databaseType, field);
                else if (fieldConfig.getDataType() == DataType.UNKNOWN) {
                    if (byte[].class.isAssignableFrom(field.getType()))
                	    // Because of some Ormlite backwards compatibility issues, a byte array won't be detected automatically. 
                	    fieldConfig.setDataType(DataType.BYTE_ARRAY);
         			else if (Serializable.class.isAssignableFrom(field.getType()))
         				fieldConfig.setDataType(DataType.SERIALIZABLE);
                }
                if (fieldConfig != null) 
                {
                    fieldConfigs.add(fieldConfig);
                    // Perform further analysis to fill in gaps in OrmLite implementation
                    analyseFieldConfig(fieldConfig, field, foreignFieldData, clazz);
                    // Single out ID field so it's type can be used for helper registration
                    if ((fieldConfig.isId() || fieldConfig.isGeneratedId()) && (idClass == null)) 
                        // Expect only one id field. Catch first if more than one.
                        idClass = field.getType();
                }
            }
        }
        if (fieldConfigs.isEmpty()) 
            log.error(TAG, "Skipping " + clazz + " because no annotated fields found");
        else if (idClass == null)
            log.error(TAG, "Skipping " + clazz + " because no id field found");
        else
        {   
        	// Perform helper registration before returning database table configuration
            classRegistry.registerEntityClass(clazz, idClass);
            return new DatabaseTableConfig(clazz, tableName, fieldConfigs);
        }
        return null;
    }


	/**
     * Perform additional annotation checks
     * @param fieldConfig populated DatabaseFieldConfig object for current field
     * @param field Field object
     * @param foreignFieldData ForeignFieldData to collect foreignCollection data 
     * @param clazz Entity class
     */
    private void analyseFieldConfig(
            DatabaseFieldConfig fieldConfig, 
            Field field, 
            ForeignFieldData foreignFieldData, 
            Class<?> clazz) 
    {
        // For foreign field, set ForeignColumnName and prepare to resolve foreignTableConfig
        // For foreign collection, prepare to resolve ForeignCollectionForeignFieldName
        if (fieldConfig.isForeign() || fieldConfig.isForeignCollection())
        {
            for (Annotation annotation : field.getAnnotations()) 
            {
                Class<?> annotationClass = annotation.annotationType();
                if (annotationClass.getName().equals("javax.persistence.JoinColumn")) 
                {
                    String referencedColumnName = getStringByInvocation(annotation, "referencedColumnName");
                    if ((referencedColumnName.length() > 0) && (fieldConfig.getColumnName() != null))
                    {
                        fieldConfig.setForeignColumnName(referencedColumnName);
                        FieldKey key = new FieldKey(field.getType(), fieldConfig.getColumnName());
                        foreignFieldData.foreignFieldMap.put(key, fieldConfig);
                    }
                }
                else if (annotationClass.getName().equals("javax.persistence.OneToMany")) 
                {
                    if (!Collection.class.isAssignableFrom(field.getType()))
                        throw new PersistenceException(
                                "@OneToMany annotation not applied to Collection type for field " + field);
                    String mappedBy = extractOneToManyField(fieldConfig, annotation, field);
                    if (mappedBy.length() > 0)
                    {
                        FieldKey key = new FieldKey(clazz, mappedBy);
                        foreignFieldData.foreignCollectionMap.put(key, fieldConfig);
                    }
                }

            }
        }
    }

    /**
     * Process OneToMany annotation missing from com.j256.ormlite.misc.JavaxPersistence probably because
     * ForeignCollection annotation serves the same purpose
     * 
     * @param databaseType DatabaseType object
     * @param field Field object
     * @return DatabaseFieldConfig 
     */
    private DatabaseFieldConfig createOneToManyConfig(DatabaseType databaseType, Field field) 
    {
        Annotation oneToManyAnnotation = null;
        for (Annotation annotation : field.getAnnotations()) 
        {
            Class<?> annotationClass = annotation.annotationType();
            if (annotationClass.getName().equals("javax.persistence.OneToMany")) 
            {
                if (!Collection.class.isAssignableFrom(field.getType()))
                    throw new PersistenceException(
                            "@OneToMany annotation not applied to Collection type for field " + field);
                oneToManyAnnotation = annotation;
                break; 
            }
        }
        if (oneToManyAnnotation == null)
            return null;
        DatabaseFieldConfig config = new DatabaseFieldConfig();
        String fieldName = field.getName();
        if (databaseType.isEntityNamesMustBeUpCase())
        {
            fieldName = fieldName.toUpperCase(Locale.US);
        }
        config.setFieldName(fieldName);
        if (config.getDataPersister() == null) 
            config.setDataPersister(DataPersisterManager.lookupForField(field));
        config.setUseGetSet((DatabaseFieldConfig.findGetMethod(field, databaseType, false) != null) &&
                            (DatabaseFieldConfig.findSetMethod(field, databaseType, false) != null));
        // Defaults from ForeignCollectionField
        config.setForeignCollection(true);
        config.setForeignCollectionMaxEagerLevel(1);
        config.setForeignCollectionOrderAscending(true);
        return config;
    }
 
    /**
     * Returns "mappedBy" attribute for OneToMany annotation. Also handles "fetch" value of "EAGER".
     * @param fieldConfig DatabaseFieldConfig object of current field
     * @param annotation Annotation object of current field
     * @param field Field  object
     * @return "mappedBy" value or empty String if not found or empty
     */
    protected String extractOneToManyField(DatabaseFieldConfig fieldConfig, Annotation annotation, Field field)
    {
       // The field that owns the relationship. Required unless the relationship is unidirectional.
        String mappedBy = getStringByInvocation(annotation, "mappedBy");
        String fetchType = getStringByInvocation(annotation, "fetch");
        if (fetchType.toString().equals("EAGER")) 
            fieldConfig.setForeignCollectionEager(true);
        if (mappedBy.length() > 0) 
        {
            fieldConfig.setForeignCollectionForeignFieldName(mappedBy);
            fieldConfig.setForeignCollectionColumnName(mappedBy);
        }
        // With OrmLite foreign collections, column name is expected to match field name 
        fieldConfig.setColumnName(field.getName());
        return mappedBy;
    }
 
    /**
     * Utility method to return unitName of class with PersistenceUnit annotation
     * @param clazz Class which is expect to have PersistenceUnit annotation
     * @return unitName
     * @throws PersistenceException if unitName not specified or empty
     */
    public static String getUnitName(Class<?> clazz) 
    {
        String unitName = null;
        for (Annotation annotation : clazz.getAnnotations())
        {
            Class<?> annotationClass = annotation.annotationType();
            if (annotationClass.getName().equals("javax.persistence.PersistenceUnit"))
            {
                unitName = getStringByInvocation(annotation, "unitName");
                break;
            }
        }
        if (unitName.length() == 0)
            throw new PersistenceException("Unit name not defined in @PersistenceUnit annotation for class " + clazz.getName()); 
        return unitName;
    }

    /**
     * Returns String from object obtained from annotation method invocation
     * @param annotation Annotation object
     * @param methodName Method name
     * @return text or empty string if object returned is null 
     */
    protected static String getStringByInvocation(Annotation annotation, String methodName)
    {
        try
        {
            Object value = BeanUtil.invoke(annotation.getClass().getMethod(methodName), annotation);
            return (value != null) ? value.toString() : "";
        }
        catch (SecurityException e)
        {
            throw new BeanException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new BeanException(e);
        }
    }
    
}
