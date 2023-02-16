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
package au.com.cybersearch2.classyjpa.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.query.QueryInfo.RowMapper;
import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;

/**
 * SqlQueryTest
 * @author Andrew Bowley
 * 17/07/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class SqlQueryTest
{
	static final class Employee {
		
	}

    static final String PU_NAME = "acme-enterprise";
    static final String DATABASE_NAME = "acme-enterprise.db";
    static final String SQL_STATEMENT = 
    "SELECT Employees.LastName, COUNT(Orders.OrderID) AS NumberOfOrders FROM Orders " +
    "INNER JOIN Employees " +
    "ON Orders.EmployeeID=Employees.EmployeeID " +
    "WHERE LastName=? OR LastName=? " +
    "GROUP BY LastName " +
    "HAVING COUNT(Orders.OrderID) > 25 " +
    "ORDER BY NumberOfOrders";
    static final String SQL_TABLES = "Orders INNER JOIN Employees ON Orders.EmployeeID=Employees.EmployeeID";
    static final String[] SQL_COLUMNS = { "Employees.LastName", "COUNT(Orders.OrderID) AS NumberOfOrders" }; 
    static final String SQL_SELECTION = "LastName=? OR LastName=?";
    static final String SQL_GROUP_BY = "LastName";
    static final String SQL_HAVING = "COUNT(Orders.OrderID) > 25";
    static final String SQL_ORDER_BY = "NumberOfOrders";
    static final String SQL_LIMIT = "20";
    static Date CREATED;
	static LogRecordHandler logRecordHandler;

    @Mock
    private PersistenceAdmin persistenceAdmin;
    private SqlQuery<Employee> sqlQuery;
    private QueryInfo queryInfo;

	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}

	@Before
	public void setUp() {
		TestLogHandler.getLogRecordHandler().clear();
        queryInfo = getTestQueryInfo();
        sqlQuery = new SqlQuery<Employee>(persistenceAdmin, queryInfo);
        Calendar cal = GregorianCalendar.getInstance(Locale.US);
        cal.set(2014, 5, 25, 5, 17, 23);
        CREATED = cal.getTime();
    }

    @Test
    public void test_getResultObjectList()
    {
        sqlQuery.getSelectionArgs().add(queryInfo.selectionArgs[0]);
        sqlQuery.getSelectionArgs().add(queryInfo.selectionArgs[1]);
        queryInfo.selectionArgs = null;
        Employee employee = new Employee();
        when(persistenceAdmin.getResultList(queryInfo, 0, 0)).thenReturn(Collections.singletonList(employee));
        List<Employee> result = sqlQuery.getResultObjectList();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(employee);
        assertThat(queryInfo.selectionArgs.length).isEqualTo(2);
        assertThat(queryInfo.selectionArgs[0]).isEqualTo("Brown");
        assertThat(queryInfo.selectionArgs[1]).isEqualTo("Smith");
    }
    
    @Test
    public void test_getResultObject()
    {
        sqlQuery.getSelectionArgs().add(queryInfo.selectionArgs[0]);
        sqlQuery.getSelectionArgs().add(queryInfo.selectionArgs[1]);
        queryInfo.selectionArgs = null;
        Employee employee = new Employee();
        when(persistenceAdmin.getSingleResult(queryInfo)).thenReturn(employee);
        Object result = sqlQuery.getResultObject();
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(employee);
        assertThat(queryInfo.selectionArgs.length).isEqualTo(2);
        assertThat(queryInfo.selectionArgs[0]).isEqualTo("Brown");
        assertThat(queryInfo.selectionArgs[1]).isEqualTo("Smith");
    }

    @Test
    public void test_setParam_by_index()
    {
        assertThat(sqlQuery.setParam(1, "Jones")).isEqualTo(true);
        assertThat(sqlQuery.setParam(2, "Ng")).isEqualTo(true);
        assertThat(sqlQuery.setParam(0, "Xerces")).isEqualTo(false);
        assertThat(sqlQuery.setParam(3, "Xenon")).isEqualTo(false);
        assertThat(sqlQuery.setParam(2, CREATED)).isEqualTo(true);
        assertThat(sqlQuery.getSelectionArgs().get(1)).isEqualTo("2014-06-25 05:17:23.000000");
        assertThat(logRecordHandler.match(0, "Query parameter 0 out of range for Named query for 'LastName=? OR LastName=?'")).isTrue();
        assertThat(logRecordHandler.match(1, "Query parameter 3 out of range for Named query for 'LastName=? OR LastName=?'")).isTrue();
    }
    
    @Test
    public void test_setParam_by_name()
    {
        assertThat(sqlQuery.setParam("lastname1", "Jones")).isEqualTo(true);
        assertThat(sqlQuery.setParam("lastname2", "Ng")).isEqualTo(true);
        assertThat(sqlQuery.setParam("XXXX", "Xerces")).isEqualTo(false);
        assertThat(sqlQuery.setParam("lastname2", CREATED)).isEqualTo(true);
        assertThat(sqlQuery.getSelectionArgs().get(1)).isEqualTo("2014-06-25 05:17:23.000000");
        assertThat(logRecordHandler.match(0, "Query parameter 'XXXX' not found for named query 'LastName=? OR LastName=?'")).isTrue();
    }
 
    @Test
    public void test_toString()
    {
        assertThat(sqlQuery.toString()).isEqualTo("Named query for '" + SQL_SELECTION + "'");
    }
    
    protected QueryInfo getTestQueryInfo()
    {
        RowMapper rowMapper = mock(RowMapper.class);
        QueryInfo queryInfo = new QueryInfo(rowMapper, SQL_TABLES, SQL_COLUMNS);
        queryInfo.setGroupBy(SQL_GROUP_BY);
        queryInfo.setHaving(SQL_HAVING);
        queryInfo.setLimit(SQL_LIMIT);
        queryInfo.setOrderBy(SQL_ORDER_BY);
        queryInfo.setParameterNames(new String[]{ "lastname1", "lastname2" });
        queryInfo.setSelection(SQL_SELECTION);
        queryInfo.setSelectionArgs(new String[]{ "Brown", "Smith" });
        return queryInfo;
    }
}
