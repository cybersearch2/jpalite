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
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseSupportBase.ConnectionPair;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.QueryInfo.RowMapper;
import au.com.cybersearch2.classyjpa.query.ResultRow;
import au.com.cybersearch2.classyjpa.query.ResultRow.FunctionSpec;
import au.com.cybersearch2.classyjpa.query.ResultRow.op;

/**
 * SQLiteDatabaseSupportTest
 * @author Andrew Bowley
 * 02/07/2014
 */
public class SQLiteDatabaseSupportTest
{
    static final String COLUMN_NAME = "ID";
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
    static final FieldType[] fieldTypes = new FieldType[] {};
    
    SQLiteDatabaseSupport sqLiteDatabaseSupport;
    ConnectionSource connectionSource;
    DatabaseConnection dbConnection;
    DatabaseResults results;
    SQLException sqlException;
    
    @Before
    public void setUp() throws SQLException
    {
        sqLiteDatabaseSupport = new SQLiteDatabaseSupport(ConnectionType.memory);
        connectionSource = mock(ConnectionSource.class);
        dbConnection = mock(DatabaseConnection.class);
        sqLiteDatabaseSupport.connectionSourceMap.put(DATABASE_NAME, new ConnectionPair(connectionSource, dbConnection));
        results = mock(DatabaseResults.class);
        sqlException = new SQLException("Database error");
        when(connectionSource.getReadWriteConnection(any(String.class))).thenReturn(dbConnection);
    }

    @Test
    public void test_SQLiteDatabaseSupport_getResultList() throws SQLException
    {
        Integer RESULT1 = Integer.valueOf(97);
        Integer RESULT2 = Integer.valueOf(4320);
        QueryInfo queryInfo = getTestQueryInfo();
        CompiledStatement compiledStatement = mock(CompiledStatement.class);
        when(dbConnection.compileStatement(
                isA(String.class), 
                eq(StatementType.SELECT_RAW), 
                eq(fieldTypes),
                eq(DatabaseConnection.DEFAULT_RESULT_FLAGS),
                eq(true))).thenReturn(compiledStatement);
        when(compiledStatement.runQuery(isNull())).thenReturn(results);
        when(compiledStatement.getColumnCount()).thenReturn(2);
        when(results.first()).thenReturn(true);
        when(results.next()).thenReturn(true, false);
        ArgumentCaptor<ResultRow> resultRowArg = ArgumentCaptor.forClass(ResultRow.class);
        when(queryInfo.getRowMapper().mapRow(resultRowArg.capture())).thenReturn(RESULT1, RESULT2);
        List<Object> resultList = sqLiteDatabaseSupport.getResultList(connectionSource, queryInfo, 0, 0);
        assertThat(resultList.size()).isEqualTo(2);
        assertThat(resultList.get(0)).isEqualTo(RESULT1);
        assertThat(resultList.get(1)).isEqualTo(RESULT2);
        assertThat(resultRowArg.getAllValues().get(0).getPosition()).isEqualTo(0);
        assertThat(resultRowArg.getAllValues().get(1).getPosition()).isEqualTo(1);
    }
    
    @Test
    public void test_SQLiteDatabaseSupport_getResultList_empty() throws SQLException
    {
        QueryInfo queryInfo = getTestQueryInfo();
        CompiledStatement compiledStatement = mock(CompiledStatement.class);
        when(dbConnection.compileStatement(
                isA(String.class), 
                eq(StatementType.SELECT_RAW), 
                eq(fieldTypes),
                eq(DatabaseConnection.DEFAULT_RESULT_FLAGS),
                eq(true))).thenReturn(compiledStatement);
        when(compiledStatement.runQuery(isNull())).thenReturn(results);
        when(compiledStatement.getColumnCount()).thenReturn(2);
        when(results.first()).thenReturn(false);
         List<Object> resultList = sqLiteDatabaseSupport.getResultList(connectionSource, queryInfo, 0, 0);
        assertThat(resultList.size()).isEqualTo(0);
    }
 
    @Test
    public void test_SQLiteDatabaseSupport_getSingleResult() throws SQLException
    {
        Integer RESULT1 = Integer.valueOf(809584);
        QueryInfo queryInfo = getTestQueryInfo();
        queryInfo.setLimit(null);
        CompiledStatement compiledStatement = mock(CompiledStatement.class);
        when(dbConnection.compileStatement(
                isA(String.class), 
                eq(StatementType.SELECT_RAW), 
                eq(fieldTypes),
                eq(DatabaseConnection.DEFAULT_RESULT_FLAGS),
                eq(true))).thenReturn(compiledStatement);
        when(compiledStatement.runQuery(isNull())).thenReturn(results);
        when(compiledStatement.getColumnCount()).thenReturn(2);
        when(results.first()).thenReturn(true);
        when(results.next()).thenReturn(false);
        ArgumentCaptor<ResultRow> resultRowArg = ArgumentCaptor.forClass(ResultRow.class);
        when(queryInfo.getRowMapper().mapRow(resultRowArg.capture())).thenReturn(RESULT1);
        Object resultObject = sqLiteDatabaseSupport.getSingleResult(connectionSource, queryInfo);
        assertThat(resultObject).isEqualTo(RESULT1);
        assertThat(resultRowArg.getValue().getPosition()).isEqualTo(0);
    }
    
    @Test
    public void test_SQLiteDatabaseSupport_getSingleResult_empty() throws SQLException
    {
        QueryInfo queryInfo = getTestQueryInfo();
        queryInfo.setLimit(null);
        CompiledStatement compiledStatement = mock(CompiledStatement.class);
        when(dbConnection.compileStatement(
                isA(String.class), 
                eq(StatementType.SELECT_RAW), 
                eq(fieldTypes),
                eq(DatabaseConnection.DEFAULT_RESULT_FLAGS),
                eq(true))).thenReturn(compiledStatement);
        when(compiledStatement.runQuery(isNull())).thenReturn(results);
        when(compiledStatement.getColumnCount()).thenReturn(2);
        when(results.first()).thenReturn(false);
        Object resultObject = sqLiteDatabaseSupport.getSingleResult(connectionSource, queryInfo);
        assertThat(resultObject).isNull();
    }
    
    @Test
    public void test_SQLiteDatabaseSupport_getDatabaseResults() throws SQLException
    {
        DatabaseResults databaseResults = mock(DatabaseResults.class);
        QueryInfo queryInfo = getTestQueryInfo();
        CompiledStatement compiledStatement = mock(CompiledStatement.class);
        String sqlStatement = SQL_STATEMENT + " LIMIT " + SQL_LIMIT;
        ArgumentCaptor<String> statementArg = ArgumentCaptor.forClass(String.class);
        when(dbConnection.compileStatement(
                statementArg.capture(), 
                eq(StatementType.SELECT_RAW), 
                eq(fieldTypes),
                eq(DatabaseConnection.DEFAULT_RESULT_FLAGS),
                eq(true))).thenReturn(compiledStatement);

        when(compiledStatement.runQuery(isNull())).thenReturn(databaseResults);
        when(compiledStatement.getColumnCount()).thenReturn(2);
        sqLiteDatabaseSupport.getDatabaseResults(dbConnection, queryInfo, 0, 0);
        assertThat(statementArg.getValue()).isEqualTo(sqlStatement);
        ArgumentCaptor<String> arguments = ArgumentCaptor.forClass(String.class);
        verify(compiledStatement, times(2)).setObject(anyInt(), arguments.capture(), eq(SqlType.STRING));
        assertThat(arguments.getAllValues().get(0)).isEqualTo("Brown");
        assertThat(arguments.getAllValues().get(1)).isEqualTo("Smith");
    }

    @Test
    public void test_SQLiteDatabaseSupport_wrapDatabaseResults() throws SQLException
    {
        ResultRow resultRow = new SqliteResultRow(5, results);
        assertThat(resultRow.getPosition()).isEqualTo(5);
        when(results.findColumn(COLUMN_NAME)).thenReturn(1);
        assertThat(resultRow.getColumnIndex(COLUMN_NAME)).isEqualTo(1);
        String[] COLUMN_NAMES = new String[] { COLUMN_NAME, "Description" };
        when(results.getColumnNames()).thenReturn(COLUMN_NAMES);
        assertThat(resultRow.getColumnNames()).isEqualTo(COLUMN_NAMES);
        when(results.getColumnCount()).thenReturn(2);
        assertThat(resultRow.getColumnCount()).isEqualTo(2);
        byte[] TEST_BLOB = "This is a test 123!".getBytes();
        InputStream inStream = new ByteArrayInputStream(TEST_BLOB);
        when(results.getBlobStream(3)).thenReturn(inStream);
        assertThat(resultRow.getBlob(3)).isEqualTo(TEST_BLOB);
        String DB_TEXT = "Acme Roadrunner Pty Ltd";
        when(results.getString(7)).thenReturn(DB_TEXT);
        assertThat(resultRow.getString(7)).isEqualTo(DB_TEXT);
        when(results.getShort(3)).thenReturn((short)1234);
        assertThat(resultRow.getShort(3)).isEqualTo((short)1234);
        when(results.getInt(3)).thenReturn(Integer.MAX_VALUE);
        assertThat(resultRow.getInt(3)).isEqualTo(Integer.MAX_VALUE);
        when(results.getLong(3)).thenReturn(Long.MAX_VALUE);
        assertThat(resultRow.getLong(3)).isEqualTo(Long.MAX_VALUE);
        when(results.getFloat(3)).thenReturn(Float.MAX_VALUE);
        assertThat(resultRow.getFloat(3)).isEqualTo(Float.MAX_VALUE);
        when(results.getDouble(3)).thenReturn(Double.MAX_VALUE);
        assertThat(resultRow.getDouble(3)).isEqualTo(Double.MAX_VALUE);
        when(results.wasNull(3)).thenReturn(Boolean.valueOf(false));
        assertThat(resultRow.isNull(3)).isEqualTo(false);
        when(results.wasNull(3)).thenThrow(sqlException);
        try
        {
            resultRow.isNull(3);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo(op.isNull.toString() + " failed");
            assertThat(e.getCause()).isEqualTo(sqlException);
        }
    }
    
    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getColumnIndex() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getColumnIndex);
        functionSpec.setColumnName(COLUMN_NAME);
        when(results.findColumn(COLUMN_NAME)).thenReturn(1);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Integer.class);
        Integer intValue = (Integer)object;
        assertThat(intValue).isEqualTo(1);
        when(results.findColumn(COLUMN_NAME)).thenThrow(sqlException);
        assertThat(SqliteResultRow.sqlFunction(functionSpec, results)).isEqualTo(-1);
    }
    
    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getColumnNames() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getColumnNames);
        String[] COLUMN_NAMES = new String[] { COLUMN_NAME, "Description" };
        when(results.getColumnNames()).thenReturn(COLUMN_NAMES);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(String[].class);
        String[] arrayValue = (String[])object;
        assertThat(arrayValue).isEqualTo(COLUMN_NAMES);
        when(results.getColumnNames()).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getColumnCount() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getColumnCount);
        when(results.getColumnCount()).thenReturn(2);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Integer.class);
        Integer intValue = (Integer)object;
        assertThat(intValue).isEqualTo(2);
        when(results.getColumnCount()).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getBlob() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getBlob);
        functionSpec.setColumnIndex(3);
        InputStream inStream = mock(InputStream.class);
        when(results.getBlobStream(3)).thenReturn(inStream);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(InputStream.class);
        InputStream value = (InputStream)object;
        assertThat(value).isEqualTo(inStream);
        when(results.getBlobStream(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getString() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getString);
        functionSpec.setColumnIndex(3);
        String DB_TEXT = "Acme Roadrunner Pty Ltd";
        when(results.getString(3)).thenReturn(DB_TEXT);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(String.class);
        String value = (String)object;
        assertThat(value).isEqualTo(DB_TEXT);
        when(results.getString(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getShort() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getShort);
        functionSpec.setColumnIndex(3);
        when(results.getShort(3)).thenReturn((short)1234);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Short.class);
        Short value = (Short)object;
        assertThat(value).isEqualTo((short)1234);
        when(results.getShort(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getInt() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getInt);
        functionSpec.setColumnIndex(3);
        when(results.getInt(3)).thenReturn(Integer.MAX_VALUE);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Integer.class);
        Integer value = (Integer)object;
        assertThat(value).isEqualTo(Integer.MAX_VALUE);
        when(results.getInt(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getLong() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getLong);
        functionSpec.setColumnIndex(3);
        when(results.getLong(3)).thenReturn(Long.MAX_VALUE);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Long.class);
        Long value = (Long)object;
        assertThat(value).isEqualTo(Long.MAX_VALUE);
        when(results.getLong(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getFloat() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getFloat);
        functionSpec.setColumnIndex(3);
        when(results.getFloat(3)).thenReturn(Float.MAX_VALUE);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Float.class);
        Float value = (Float)object;
        assertThat(value).isEqualTo(Float.MAX_VALUE);
        when(results.getFloat(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_isNull() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.isNull);
        functionSpec.setColumnIndex(3);
        when(results.wasNull(3)).thenReturn(Boolean.valueOf(false));
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Boolean.class);
        Boolean value = (Boolean)object;
        assertThat(value).isEqualTo(Boolean.valueOf(false));
        when(results.wasNull(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    @Test
    public void test_SQLiteDatabaseSupport_sqlFunction_getDouble() throws SQLException
    {
        FunctionSpec functionSpec = new FunctionSpec(op.getDouble);
        functionSpec.setColumnIndex(3);
        when(results.getDouble(3)).thenReturn(Double.MAX_VALUE);
        Object object = SqliteResultRow.sqlFunction(functionSpec, results);
        assertThat(object).isInstanceOf(Double.class);
        Double value = (Double)object;
        assertThat(value).isEqualTo(Double.MAX_VALUE);
        when(results.getDouble(3)).thenThrow(sqlException);
        doTestSqlException(functionSpec);
    }

    protected void doTestSqlException(FunctionSpec functionSpec)
    {
        try
        {
            SqliteResultRow.sqlFunction(functionSpec, results);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo(functionSpec.getOperation().toString() + " failed");
            assertThat(e.getCause()).isEqualTo(sqlException);
        }
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
