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

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.table.DatabaseTableConfig;

import au.com.cybersearch2.classyfy.data.alfresco.NoNameEntity;
import au.com.cybersearch2.classyfy.data.alfresco.NonEntity;
import au.com.cybersearch2.classyfy.data.alfresco.NonIdEntity;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
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
        public <T, ID> void registerEntityClass(Class<T> entityClass,
                Class<ID> primaryKeyClass) 
        {
            assertThat(entityClass).isEqualTo((Class<T>) this.entityClass);
            assertThat(primaryKeyClass).isEqualTo((Class<ID>) int.class);
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
        assertThat(foreignFieldData.foreignCollectionMap.isEmpty()).isTrue();
        assertThat(foreignFieldData.foreignFieldMap.isEmpty()).isTrue();
    }

    @Test
    public void test_getTableConfiguration_no_name()
    {
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(NoNameEntity.class));
        DatabaseTableConfig<?> config = classAnalyser.getTableConfiguration(NoNameEntity.class, foreignFieldData);
        assertThat(config).isNotNull();
        assertThat(config.getTableName().equals("tableNoNameEntity"));
        assertThat(config.getFieldConfigs().size()).isGreaterThan(0);
    }
    
    @Test
    public void test_getTableConfiguration_non_entity()
    {
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new TestClassRegistry(NonEntity.class));
        DatabaseTableConfig<?> config = classAnalyser.getTableConfiguration(NonEntity.class, foreignFieldData);
        assertThat(config).isNull();
    }
    
    @Test
    public void test_getDatabaseTableConfigList()
    {
        List<String> managedClassNames = new ArrayList<String>();
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
        List<String> managedClassNames = new ArrayList<String>();
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
        List<String> managedClassNames = new ArrayList<String>();
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
        List<String> managedClassNames = new ArrayList<String>();
        managedClassNames.add(Employee.class.getName());
        managedClassNames.add(Department.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new ClassRegistry(){

            @Override
            public <T, ID> void registerEntityClass(Class<T> entityClass,
                    Class<ID> primaryKeyClass) {
                if (!((entityClass == Employee.class) || (entityClass == Department.class)))
                    throw new IllegalArgumentException(entityClass.getName() + " not valid");
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
                        assertThat(fieldConfig.getForeignCollectionForeignFieldName()).isEqualTo("department");
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
    public void test_one_to_many_fetch()
    {
        List<String> managedClassNames = new ArrayList<String>();
        managedClassNames.add(EagerEmployee.class.getName());
        managedClassNames.add(EagerDepartment.class.getName());
        ClassAnalyser classAnalyser = new ClassAnalyser(new SqliteDatabaseType(), new ClassRegistry(){

            @Override
            public <T, ID> void registerEntityClass(Class<T> entityClass,
                    Class<ID> primaryKeyClass) {
                if (!((entityClass == EagerEmployee.class) || (entityClass == EagerDepartment.class)))
                    throw new IllegalArgumentException(entityClass.getName() + " not valid");
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
                        assertThat(fieldConfig.getForeignCollectionForeignFieldName()).isEqualTo("department");
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
        List<String> managedClassNames = new ArrayList<String>();
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
        List<String> managedClassNames = new ArrayList<String>();
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
