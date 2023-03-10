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

import java.sql.SQLException;
import java.util.List;

import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * DaoQueryTest
 * Note: selectionArguments methods tested in EntityQueryTest
 * @author Andrew Bowley
 * 10/07/2014
 */
public class DaoQueryTest
{
    private static final int OFFSET = 17;
    private static final int LIMIT = 100;
    protected OrmQuery<RecordCategory> ormQuery;
    protected PreparedQuery<RecordCategory> preparedQuery;
    protected QueryBuilder<RecordCategory, Integer> statementBuilder;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
    {
    	ormQuery = mock(OrmQuery.class);
        preparedQuery = mock(PreparedQuery.class);
        statementBuilder = mock(QueryBuilder.class);
    }
    
    @Test
    public void test_getResultList() throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = new DaoQuery<RecordCategory>(ormQuery){

            @Override
            public QueryBuilder<RecordCategory,Integer> buildQuery(
                    QueryBuilder<RecordCategory, Integer> statementBuilder)
                    throws SQLException {
                return statementBuilder;
            }};
            
            int startPosition = OFFSET;
            int maxResults = LIMIT;
            List<RecordCategory> records = new ArrayList<>();
            for (int i = 1; i < maxResults; ++i) {
            	RecordCategory record = new RecordCategory();
            	record.set_id(i);
            	records.add(record);
            }
            when(ormQuery.getResultList(startPosition, maxResults, daoQuery)).thenReturn(records);
            List<RecordCategory> result = daoQuery.getResultList(startPosition, maxResults);  
            assertThat(result).isEqualTo(records);
    }

    @Test
    public void test_prepare_sql_exception() throws SQLException
    {
        SQLException sqlException = new SQLException("Offset out of bounds");
    	PersistenceException persistenceException = new PersistenceException("Error preparing query", sqlException);
        DaoQuery<RecordCategory> daoQuery = new DaoQuery<RecordCategory>(ormQuery){

            @Override
            public QueryBuilder<RecordCategory, Integer> buildQuery(
                    QueryBuilder<RecordCategory, Integer> statementBuilder)
                    throws SQLException {
                return statementBuilder;
            }};
        int startPosition = OFFSET;
        int maxResults = LIMIT;
        when(ormQuery.getResultList(startPosition, maxResults, daoQuery)).thenThrow(persistenceException);
        try
        {
        	daoQuery.getResultList(startPosition, maxResults);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error preparing query");
            assertThat(e.getCause()).isEqualTo(sqlException);
        }
    }
  
    @Test
    public void test_getResultList_default() throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = prepareQuery(0, 0);
        List<RecordCategory> testList = Collections.singletonList(new RecordCategory());
        when(ormQuery.getResultList(0, 0, daoQuery)).thenReturn(testList);
        List<RecordCategory> result = daoQuery.getResultList(0, 0);
        assertThat(result).isEqualTo(testList);
    }
 
    @Test
    public void test_getSingleResult() throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = prepareQuery(0, 1);
        RecordCategory recordCategory = new RecordCategory();
        when(ormQuery.getSingleResult(daoQuery)).thenReturn(recordCategory);
        RecordCategory result = daoQuery.getSingleResult();
        assertThat(result).isEqualTo(recordCategory);
    }
    
    protected DaoQuery<RecordCategory> prepareQuery(int startPosition, int maxResults) throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = new DaoQuery<RecordCategory>(ormQuery){

            @Override
            public QueryBuilder<RecordCategory, Integer> buildQuery(
                    QueryBuilder<RecordCategory, Integer> statementBuilder)
                    throws SQLException {
                return statementBuilder;
            }};
            return daoQuery;
    }

}
