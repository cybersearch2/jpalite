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
package au.com.cybersearch2.classyapp;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.cybersearch2.classydb.NativeScriptDatabaseWorkTest;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupportTest;
import au.com.cybersearch2.classydb.SQLiteSupportTest;
import au.com.cybersearch2.classyjpa.entity.EntityManagerImplTest;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContextTest;
import au.com.cybersearch2.classyjpa.entity.ObjectMonitorTest;
import au.com.cybersearch2.classyjpa.entity.OrmDaoHelperFactoryTest;
import au.com.cybersearch2.classyjpa.entity.OrmDaoHelperTest;
import au.com.cybersearch2.classyjpa.entity.PersistenceDaoTest;
import au.com.cybersearch2.classyjpa.persist.ClassAnalyserTest;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfigTest;
import au.com.cybersearch2.classyjpa.persist.PersistenceXmlParserTest;
import au.com.cybersearch2.classyjpa.query.DaoQueryTest;
import au.com.cybersearch2.classyjpa.query.EntityQueryTest;
import au.com.cybersearch2.classyjpa.query.NativeQueryTest;
import au.com.cybersearch2.classyjpa.query.SqlQueryTest;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImplTest;
import au.com.cybersearch2.classyjpa.transaction.TransactionStateTest;
import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;

/**
 * JUnitTestSuite
 * @author Andrew Bowley
 * 19/06/2014
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SQLiteSupportTest.class,
    OrmDaoHelperTest.class,
    OrmDaoHelperFactoryTest.class,
    ObjectMonitorTest.class,
    EntityManagerImplTest.class,
    PersistenceConfigTest.class,
    PersistenceXmlParserTest.class,
    TransactionStateTest.class,
    PersistenceDaoTest.class,
    JavaPersistenceContextTest.class,
    SQLiteDatabaseSupportTest.class,
    NativeScriptDatabaseWorkTest.class,
    DaoQueryTest.class,
    EntityQueryTest.class,
    NativeQueryTest.class,
    SqlQueryTest.class,
    EntityTransactionImplTest.class,
    ClassAnalyserTest.class
})
public class JUnitTestSuite 
{   
	static LogRecordHandler logRecordHandler;

	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}


}


