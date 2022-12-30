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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.EnumIntegerType;
import com.j256.ormlite.field.types.EnumStringType;
import com.j256.ormlite.misc.Javax;
import com.j256.ormlite.table.DatabaseTableConfig;

import au.com.cybersearch2.classyfy.data.alfresco.NoNameEntity;
import au.com.cybersearch2.classyfy.data.alfresco.NonEntity;
import au.com.cybersearch2.classyfy.data.alfresco.NonIdEntity;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.persist.ClassAnalyser.ClassRegistry;
import au.com.cybersearch2.classyjpa.persist.ClassAnalyser.ForeignFieldData;

/**
 * ClassAnalyserTest
 * @author Andrew Bowley
 * 18/07/2014
 */
public class ClassAnalyserTest
{
    static class TestClassRegistry implements ClassRegistry
    {
        Class<?> entityClass;
        
        public TestClassRegistry(Class<?> entityClass)
        {
            this.entityClass = entityClass;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T extends OrmEntity> void registerEntityClass(Class<T> entityClass) 
        {
            assertThat(entityClass).isEqualTo((Class<T>) this.entityClass);
       }
    }
 
    @PersistenceUnit(unitName=PU_NAME)
    static class PU
    {
    }

    @PersistenceUnit()
    static class PU_null
    {
    }

    @PersistenceUnit(unitName="")
    static class PU_empty
    {
    }
    
    static final String PU_NAME = "classy-persist";
    
    ForeignFieldData foreignFieldData;

    
    @Before
    public void setUp() throws Exception 
    {
        foreignFieldData = new ForeignFieldData();
    }
    
    @Test
    public void test_getTableConfiguration()
    {
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(RecordCategory.class));
        DatabaseTableConfig<?> config = classAnalyser.getTableConfiguration(RecordCategory.class, foreignFieldData);
        assertThat(config).isNotNull();
        assertThat(config.getTableName().equals("categories"));
        assertThat(config.getFieldConfigs().size()).isGreaterThan(0);
        assertThat(foreignFieldData.foreignFieldMap.isEmpty()).isTrue();
    }

    @Test
    public void test_getTableConfiguration_no_name()
    {
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(NoNameEntity.class));
        DatabaseTableConfig<? extends OrmEntity> config = classAnalyser.getTableConfiguration(NoNameEntity.class, foreignFieldData);
        assertThat(config).isNotNull();
        assertThat(config.getTableName().equals("tableNoNameEntity"));
        assertThat(config.getFieldConfigs().size()).isGreaterThan(0);
    }
    
    @Test
    public void test_getTableConfiguration_non_entity()
    {
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(NonEntity.class));
        DatabaseTableConfig<? extends OrmEntity> config = classAnalyser.getTableConfiguration(NonEntity.class, foreignFieldData);
        assertThat(config).isNull();
    }
    
    @Test
    public void test_getDatabaseTableConfigList()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add(RecordCategory.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(RecordCategory.class));
        List<DatabaseTableConfig<?>> configList = classAnalyser.getDatabaseTableConfigList(managedClassNames);
        assertThat(configList).isNotNull();
        assertThat(configList.size()).isEqualTo(1);
        assertThat(configList.get(0).getTableName()).isEqualTo("categories");
    }
    
    @Test
    public void test_getDatabaseTableConfigList_non_id_entity()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add(RecordCategory.class.getName());
        managedClassNames.add(NonIdEntity.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(RecordCategory.class));
        try
        {
            classAnalyser.getDatabaseTableConfigList(managedClassNames);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(NonIdEntity.class.getName());
            assertThat(e.getMessage()).doesNotContain(RecordCategory.class.getName());
        }
    }

    @Test
    public void test_getDatabaseTableConfigList_class_not_found()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add("x" + RecordCategory.class.getName());
        managedClassNames.add(NonIdEntity.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(RecordCategory.class));
        try
        {
            classAnalyser.getDatabaseTableConfigList(managedClassNames);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Failed to load following entity classes: [xau.com.cybersearch2.classyfy.data.alfresco.RecordCategory]");
        }
    }
    

    @Test
    public void test_getDatabaseTableConfigList_one_to_many()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add(Employee.class.getName());
        managedClassNames.add(Department.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new ClassRegistry(){

            @Override
            public <T extends OrmEntity> void registerEntityClass(Class<T> entityClass) {
                //if (!((entityClass == Employee.class) || (entityClass == Department.class)))
                //    throw new IllegalArgumentException(entityClass.getName() + " not valid");
            }});
        List<DatabaseTableConfig<?>> configList = classAnalyser.getDatabaseTableConfigList(managedClassNames);
        assertThat(configList).isNotNull();
        assertThat(configList.size()).isEqualTo(2);
        for (DatabaseTableConfig<?> tableConfig: configList)
            if ("tableDepartment".equals(tableConfig.getTableName()))
            {
                for (DatabaseFieldConfig fieldConfig: tableConfig.getFieldConfigs())
                {
                    if ("employees".equals(fieldConfig.getFieldName()))
                    {
                        assertThat(fieldConfig.isForeignCollection()).isTrue();
                        assertThat(fieldConfig.getForeignCollectionForeignFieldName()).isEqualTo("dept_id");
                        assertThat(fieldConfig.getForeignCollectionMaxEagerLevel()).isEqualTo(1);
                        assertThat(fieldConfig.isForeignCollectionOrderAscending()).isTrue();
                        assertThat(fieldConfig.isForeignCollectionEager()).isFalse();
                        break;
                    }
                }
            }
            else if ("tableEmployee".equals(tableConfig.getTableName()))
            {
                for (DatabaseFieldConfig fieldConfig: tableConfig.getFieldConfigs())
                {
                    if ("department".equals(fieldConfig.getFieldName()))
                    {
                        assertThat(fieldConfig.isForeign()).isTrue();
                        DatabaseTableConfig<?> foreignTableConfig = fieldConfig.getForeignTableConfig();
                        assertThat(foreignTableConfig).isNotNull();
                        assertThat(foreignTableConfig.getTableName()).isEqualTo("tableDepartment");
                        break;
                        
                    }
                }
            }
    }

    @Test
    public void test_Javax()
    {
    	List<Field> fieldList = new ArrayList<>();
    	for (Field field : Javax.class.getDeclaredFields())
    		fieldList.add(field);
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new ClassRegistry(){

            @Override
            public <T extends OrmEntity> void registerEntityClass(Class<T> entityClass) {
                //f (!(entityClass == Javax.class))
                //    throw new IllegalArgumentException(entityClass.getName() + " not valid");
            }});
        List<DatabaseTableConfig<?>> configList = classAnalyser.getDatabaseTableConfigList(Collections.singletonList("com.j256.ormlite.misc.Javax"));
        assertThat(configList).isNotNull();
        assertThat(configList.size()).isEqualTo(1);
        Iterator<Field> iterator = fieldList.iterator();
           for (DatabaseFieldConfig config: configList.get(0).getFieldConfigs()) {
            	String field = config.getFieldName();
            	boolean fieldFound = false;
            	while (iterator.hasNext()) {
            		String name = iterator.next().getName();
            		if (name.equals(field)) {
            			fieldFound = true;
            			break;
            		}
            	}
            	assertTrue(field, fieldFound);
    			if (field.equals("generatedId")) {
    				assertFalse(config.isId());
    				assertTrue(config.isGeneratedId());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("id")) {
    				assertTrue(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("stuff")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertEquals(Javax.STUFF_FIELD_NAME, config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("unknown")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getDataPersister());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("foreignManyToOne")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertTrue(config.isForeign());
    				assertFalse(config.isForeignCollection());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getDataPersister());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("foreignOneToOne")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertTrue(config.isForeign());
    				assertFalse(config.isForeignCollection());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getDataPersister());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("foreignOneToMany")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertTrue(config.isForeignCollection());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getDataPersister());
    				assertNull(config.getForeignCollectionForeignFieldName());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("mappedByField")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertTrue(config.isForeignCollection());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getDataPersister());
    				assertEquals(Javax.MAPPED_BY_FIELD_NAME, config.getForeignCollectionForeignFieldName());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("joinFieldName")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertTrue(config.isForeign());
    				assertFalse(config.isForeignCollection());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getDataPersister());
    				assertEquals(Javax.JOIN_FIELD_NAME, config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("columnDefinition")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertFalse(config.isUnique());
    				assertFalse(config.isVersion());
    				assertTrue(config.isCanBeNull());
    				assertEquals(Javax.COLUMN_DEFINITION, config.getColumnDefinition());
    			} else if (field.equals("uniqueColumn")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertTrue(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("nullableColumn")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertFalse(config.isUnique());
    				assertFalse(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("uniqueJoinColumn")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertTrue(config.isForeign());
    				assertFalse(config.isForeignCollection());
    				assertTrue(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("nullableJoinColumn")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertTrue(config.isForeign());
    				assertFalse(config.isForeignCollection());
    				assertFalse(config.isUnique());
    				assertFalse(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("ourEnumOrdinal")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertFalse(config.isUnique());
    				assertFalse(config.isVersion());
    				assertTrue(config.isCanBeNull());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    				assertTrue(config.getDataPersister() instanceof EnumIntegerType);
    			} else if (field.equals("ourEnumString")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertFalse(config.isUnique());
    				assertFalse(config.isVersion());
    				assertTrue(config.isCanBeNull());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    				assertTrue(config.getDataPersister() instanceof EnumStringType);
    			} else if (field.equals("version")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertTrue(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("basic")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertFalse(config.isUnique());
    				assertTrue(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else if (field.equals("basicNotOptional")) {
    				assertFalse(config.isId());
    				assertFalse(config.isGeneratedId());
    				assertFalse(config.isForeign());
    				assertFalse(config.isUnique());
    				assertFalse(config.isCanBeNull());
    				assertFalse(config.isVersion());
    				assertNull(config.getColumnName());
    				assertNull(config.getColumnDefinition());
    			} else {
    				System.err.println("\n\n\nUnknown field: " + field);
    			}
        }
    }
 
    @Test
    public void test_many_to_many()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add(User.class.getName());
        managedClassNames.add(Post.class.getName());
        managedClassNames.add(UserPost.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new ClassRegistry(){

            @Override
            public <T extends OrmEntity> void registerEntityClass(Class<T> entityClass) {
                if (!((entityClass == User.class) || (entityClass == Post.class) || (entityClass == UserPost.class)))
                    throw new IllegalArgumentException(entityClass.getName() + " not valid");
            }});
        List<DatabaseTableConfig<?>> configList = classAnalyser.getDatabaseTableConfigList(managedClassNames);
        assertThat(configList).isNotNull();
        assertThat(configList.size()).isEqualTo(3);
        int count = 0;
        for (DatabaseTableConfig<?> tableConfig: configList)
            if ("tableUser".equals(tableConfig.getTableName())) {
                for (DatabaseFieldConfig config: tableConfig.getFieldConfigs()) {
                    if ("posts".equals(config.getFieldName())) {
        				assertFalse(config.isId());
        				assertFalse(config.isGeneratedId());
        				assertFalse(config.isForeign());
        				assertTrue(config.isForeignCollection());
        				assertFalse(config.isUnique());
        				assertTrue(config.isCanBeNull());
        				assertFalse(config.isVersion());
        				assertNull(config.getDataPersister());
        				assertNull(config.getForeignCollectionForeignFieldName());
        				assertNull(config.getColumnName());
        				assertNull(config.getColumnDefinition());
                    	++count;
                        break;
                        
                    }
                 }
            } else if ("tablePost".equals(tableConfig.getTableName())) {
                for (DatabaseFieldConfig config: tableConfig.getFieldConfigs()) {
                    if ("user".equals(config.getFieldName())) {
        				assertFalse(config.isId());
        				assertFalse(config.isGeneratedId());
        				assertTrue(config.isForeign());
        				assertFalse(config.isForeignCollection());
        				assertFalse(config.isUnique());
        				assertTrue(config.isCanBeNull());
        				assertFalse(config.isVersion());
        				assertNull(config.getDataPersister());
        				assertEquals(Post.USER_ID_FIELD_NAME, config.getColumnName());
        				assertNull(config.getColumnDefinition());
                    	++count;
                        break;
                        
                    }
                }
            } else if ("tableUserPost".equals(tableConfig.getTableName())) {
            	int target = count + 2;
                for (DatabaseFieldConfig config: tableConfig.getFieldConfigs()) {
                    if ("user".equals(config.getFieldName())) {
        				assertThat(config.getForeignTableConfig()).isNotNull();
        				assertThat(config.getForeignTableConfig().getTableName()).isEqualTo("tableUser");
        				assertFalse(config.isId());
        				assertFalse(config.isGeneratedId());
        				assertTrue(config.isForeign());
        				assertFalse(config.isForeignCollection());
        				assertFalse(config.isUnique());
        				assertTrue(config.isCanBeNull());
        				assertFalse(config.isVersion());
        				assertNull(config.getDataPersister());
        				assertThat(config.getColumnName()).isEqualTo(UserPost.USER_ID_FIELD_NAME);
        				assertNull(config.getColumnDefinition());
                   	++count;
                        
                    } else if ("post".equals(config.getFieldName())) {
        				assertThat(config.getForeignTableConfig()).isNotNull();
        				assertThat(config.getForeignTableConfig().getTableName()).isEqualTo("tablePost");
        				assertFalse(config.isId());
        				assertFalse(config.isGeneratedId());
        				assertTrue(config.isForeign());
        				assertFalse(config.isForeignCollection());
        				assertFalse(config.isUnique());
        				assertTrue(config.isCanBeNull());
        				assertFalse(config.isVersion());
        				assertNull(config.getDataPersister());
        				assertThat(config.getColumnName()).isEqualTo(UserPost.POST_ID_FIELD_NAME);
        				assertNull(config.getColumnDefinition());
                    	++count;
                    }
                    if (count == target)
                    	break;
                }
            }
        assertThat(count).isEqualTo(4);
    }
       
    @Test
    public void test_one_to_many_fetch()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add(EagerEmployee.class.getName());
        managedClassNames.add(EagerDepartment.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new ClassRegistry(){

            @Override
            public <T extends OrmEntity> void registerEntityClass(Class<T> entityClass) {
                //if (!((entityClass == EagerEmployee.class) || (entityClass == EagerDepartment.class)))
                //    throw new IllegalArgumentException(entityClass.getName() + " not valid");
            }});
        List<DatabaseTableConfig<?>> configList = classAnalyser.getDatabaseTableConfigList(managedClassNames);
        assertThat(configList).isNotNull();
        assertThat(configList.size()).isEqualTo(2);
        for (DatabaseTableConfig<?> tableConfig: configList)
            if ("tableDepartment".equals(tableConfig.getTableName()))
            {
                for (DatabaseFieldConfig fieldConfig: tableConfig.getFieldConfigs())
                {
                    if ("employees".equals(fieldConfig.getFieldName()))
                    {
                        assertThat(fieldConfig.isForeignCollection()).isTrue();
                        assertThat(fieldConfig.getForeignCollectionForeignFieldName()).isEqualTo("dept_id");
                        assertThat(fieldConfig.getForeignCollectionMaxEagerLevel()).isEqualTo(1);
                        assertThat(fieldConfig.isForeignCollectionOrderAscending()).isTrue();
                        assertThat(fieldConfig.isForeignCollectionEager()).isTrue();
                        break;
                    }
                }
            }
            else if ("tableEmployee".equals(tableConfig.getTableName()))
            {
                for (DatabaseFieldConfig fieldConfig: tableConfig.getFieldConfigs())
                {
                    if ("department".equals(fieldConfig.getFieldName()))
                    {
                        assertThat(fieldConfig.isForeign()).isTrue();
                        DatabaseTableConfig<?> foreignTableConfig = fieldConfig.getForeignTableConfig();
                        assertThat(foreignTableConfig).isNotNull();
                        assertThat(foreignTableConfig.getTableName()).isEqualTo("tableDepartment");
                        break;
                        
                    }
                }
            }
    }

    @Test
    public void test_one_to_many_no_foreign_collection()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add(Employee.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(Employee.class));
        try
        {
            classAnalyser.getDatabaseTableConfigList(managedClassNames);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Table of type au.com.cybersearch2.classyjpa.persist.Department not found");
        }
    }
    
    @Test
    public void test_getDatabaseTableConfigList_non_entity()
    {
        List<String> managedClassNames = new ArrayList<>();
        managedClassNames.add(RecordCategory.class.getName());
        managedClassNames.add(NonEntity.class.getName());
        managedClassNames.add(NonIdEntity.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(RecordCategory.class));
        try
        {
            classAnalyser.getDatabaseTableConfigList(managedClassNames);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(NonEntity.class.getName());
            assertThat(e.getMessage()).doesNotContain(RecordCategory.class.getName());
        }
    }

    @Test 
    public void test_getUnitName()
    {
        assertThat(ClassAnalyser.getUnitName(PU.class)).isEqualTo(PU_NAME);
    }

    @Test 
    public void test_getUnitName_null()
    {
        try
        {
            ClassAnalyser.getUnitName(PU_null.class);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Unit name not defined in @PersistenceUnit annotation for class au.com.cybersearch2.classyjpa.persist.ClassAnalyserTest$PU_null");
        }
    }

    @Test 
    public void test_getUnitName_empty()
    {
        try
        {
            ClassAnalyser.getUnitName(PU_empty.class);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Unit name not defined in @PersistenceUnit annotation for class au.com.cybersearch2.classyjpa.persist.ClassAnalyserTest$PU_empty");
        }
    }
}
