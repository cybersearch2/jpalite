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
package au.com.cybersearch2.classydb;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;

/**
 * DatabaseAdminImplTest
 * @author Andrew Bowley
 * 01/08/2014
 */
public class DatabaseAdminImplTest
{
    public static final String CREATE_SQL_FILENAME = "create.sql";
    public static final String DROP_SQL_FILENAME = "drop.sql";
    public static final String DATA_FILENAME = "data.sql";
    public static final String UPGRADE_DATA_FILENAME = "classyfy-upgrade-v1-v2.sql";
    PersistenceAdmin persistenceAdmin;
    ConnectionSource connectionSource;
    Properties properties;
    ResourceEnvironment resourceEnvironment;

    @Before
    public void setUp()
    {
        properties = new Properties();
        persistenceAdmin = mock(PersistenceAdmin.class);
        connectionSource = mock(ConnectionSource.class);
        when(persistenceAdmin.getConnectionSource()).thenReturn(connectionSource);
        when(persistenceAdmin.getProperties()).thenReturn(properties);
        resourceEnvironment = new JavaTestResourceEnvironment("src/test/resources/sqlite");
    }

    @Test
    public void test_onCreate()
    {
        properties.setProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME, DROP_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.SCHEMA_FILENAME, CREATE_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.DATA_FILENAME, DATA_FILENAME);
        TestDatabaseAdminImpl databaseAdminImpl = new TestDatabaseAdminImpl(TestClassyApplication.PU_NAME, persistenceAdmin, resourceEnvironment);
        databaseAdminImpl.onCreate(connectionSource);
        NativeScriptDatabaseWork processFilesCallable = (NativeScriptDatabaseWork) databaseAdminImpl.processFilesCallable;
        assertThat(processFilesCallable.filenames.length).isEqualTo(3);
        assertThat(processFilesCallable.filenames[0]).isEqualTo(DROP_SQL_FILENAME);
        assertThat(processFilesCallable.filenames[1]).isEqualTo(CREATE_SQL_FILENAME);
        assertThat(processFilesCallable.filenames[2]).isEqualTo(DATA_FILENAME);
    }

    @Test
    public void test_onUpgrade()
    {
        properties.setProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME, DROP_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.SCHEMA_FILENAME, CREATE_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.DATA_FILENAME, DATA_FILENAME);
        TestDatabaseAdminImpl databaseAdminImpl = new TestDatabaseAdminImpl(TestClassyApplication.PU_NAME, persistenceAdmin, resourceEnvironment);
        databaseAdminImpl.onUpgrade(connectionSource, 1,2);
        NativeScriptDatabaseWork processFilesCallable = (NativeScriptDatabaseWork) databaseAdminImpl.processFilesCallable;
        assertThat(processFilesCallable.filenames.length).isEqualTo(1);
        assertThat(processFilesCallable.filenames[0]).isEqualTo(UPGRADE_DATA_FILENAME);
    }
}
