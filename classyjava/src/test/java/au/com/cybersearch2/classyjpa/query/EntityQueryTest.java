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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.stmt.QueryBuilder;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.query.DaoQuery.SimpleSelectArg;

/**
 * EntityQueryTest
 * @author Andrew Bowley
 * 08/07/2014
 */
public class EntityQueryTest
{
    enum ParamType
    {
        object,
        date,
        calendar
    }
    
    static final Integer PRIMARY_KEY = 23;
    static final String ID_COLUMN_NAME = "_ID";
    static final Date   CREATED = new Date();
    static final String CREATED_COLUMN_NAME = "Created";
    static Calendar CREATED_CALENDAR;
    
    static class TestReadyQuery extends DaoQuery<RecordCategory>
    {
        RuntimeException doThrowException;
        RecordCategory recordCategory;
        
        @SuppressWarnings({ "unchecked" })
        public TestReadyQuery()
        {
            super(mock(OrmQuery.class), getSelectionArguments());
        }

        @SuppressWarnings({ "unchecked" })
		public TestReadyQuery(SimpleSelectArg[] selectionArguments)
        {
            super(mock(OrmQuery.class), selectionArguments);
        }

        @Override
        protected List<RecordCategory> getResultList(int startPosition, int maxResults) 
        {
            if (doThrowException != null)
                throw doThrowException;
            return results;
        }

        @Override
        protected RecordCategory getSingleResult()
        {
            if (doThrowException != null)
                throw doThrowException;
            return recordCategory;
        }

        @Override
        public QueryBuilder<RecordCategory, Integer> buildQuery(
                QueryBuilder<RecordCategory, Integer> statementBuilder)
                throws SQLException 
        {
            return null;
        }

        public void setRecordCategory(RecordCategory recordCategory)
        {
            this.recordCategory = recordCategory;
        }
    }
    protected EntityQuery<RecordCategory> entityQuery;
    protected DaoQuery<RecordCategory> daoQuery;
    protected static List<RecordCategory> results;
    protected static RecordCategory testItem;

    
    @Before
    public void setUp()
    {
        if (results == null)
        {
            testItem = new RecordCategory();
            results = new ArrayList<>();
            results.add(testItem);
            CREATED_CALENDAR = GregorianCalendar.getInstance();
            CREATED_CALENDAR.setTime(CREATED);
        }
        TestReadyQuery testReadyQuery = new TestReadyQuery();
        testReadyQuery.setRecordCategory(testItem);
        daoQuery = testReadyQuery;
        entityQuery = new EntityQuery<RecordCategory>(daoQuery);
        
    }
    
    @Test
    public void test_ReadQuery_constructor()
    {
        assertThat(daoQuery.argumentMap.size()).isEqualTo(2);
        SimpleSelectArg selectArg = (SimpleSelectArg) daoQuery.argumentMap.get(ID_COLUMN_NAME);
        assertThat(selectArg.getColumnName()).isEqualTo(ID_COLUMN_NAME);
        assertThat(selectArg.getValue()).isNull();
        selectArg = (SimpleSelectArg) entityQuery.daoQuery.get(CREATED_COLUMN_NAME);
        assertThat(selectArg.getColumnName()).isEqualTo(CREATED_COLUMN_NAME);
        assertThat(selectArg.getValue()).isNull();
    }

    @Test
    public void test_ReadyQuery_constructor_select_arg_recycle()
    {
        SimpleSelectArg[] selectionArguments = getSelectionArguments();
        TestReadyQuery testReadyQuery = new TestReadyQuery(selectionArguments);
        SimpleSelectArg selectArg = (SimpleSelectArg) testReadyQuery.argumentMap.get(ID_COLUMN_NAME);
        assertThat(selectArg.getColumnName()).isEqualTo(ID_COLUMN_NAME);
        assertThat(selectArg.getValue()).isNull();
        selectArg = (SimpleSelectArg) testReadyQuery.get(CREATED_COLUMN_NAME);
        assertThat(selectArg.getColumnName()).isEqualTo(CREATED_COLUMN_NAME);
        assertThat(selectArg.getValue()).isNull();
    }

    @Test
    public void test_EntityQuery_constructor_select_arg_null()
    {
        TestReadyQuery testReadyQuery = new TestReadyQuery(null);
        assertThat(testReadyQuery.argumentMap.size()).isEqualTo(0);
    }

    @Test
    public void test_EntityQuery_constructor_select_arg_empty()
    {
        TestReadyQuery testReadyQuery = new TestReadyQuery(new SimpleSelectArg[0]);
        assertThat(testReadyQuery.argumentMap.size()).isEqualTo(0);
    }
    
    @Test
    public void test_EntityQuery_executeUpdate()
    {
        assertThat(entityQuery.executeUpdate()).isEqualTo(0);
    }

    @Test
    public void test_EntityQuery_getResultList_closed()
    {
        entityQuery.release();
        assertThat(entityQuery.getResultList()).isNotNull();
        assertThat(entityQuery.getResultList().size()).isEqualTo(0);
        assertThat(entityQuery.isClosed).isTrue();
    }

    @Test
    public void test_EntityQuery_getResultList()
    {
        List<RecordCategory> list = entityQuery.getResultList();
        assertThat(list).isNotNull();
        assertThat(list).isEqualTo(results);
        assertThat(entityQuery.isClosed).isTrue();
    }

    @Test
    public void test_EntityQuery_getSingleResult()
    {
        Object result = entityQuery.getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testItem);
        assertThat(entityQuery.isClosed).isTrue();
    }

    @Test
    public void test_EntityQuery_getSingleResult_null()
    {
        ((TestReadyQuery)daoQuery).setRecordCategory(null);
        try
        {
            entityQuery.getSingleResult();
            failBecauseExceptionWasNotThrown(NoResultException.class);
        }
        catch(NoResultException e)
        {
            assertThat(e.getMessage()).isEqualTo("getSingleResult() query returned null");
        }
    }

    @Test
    public void test_EntityQuery_getSingleResult_closed()
    {
        entityQuery.release();
        try
        {
            entityQuery.getSingleResult();
            failBecauseExceptionWasNotThrown(NoResultException.class);
        }
        catch(NoResultException e)
        {
            assertThat(e.getMessage()).isEqualTo("getSingleResult() called when query already executed");
        }
    }

    @Test
    public void test_EntityQuery_getSingleResult_exception_thrown()
    {
        PersistenceException persistenceException = new PersistenceException("No row matched on primary key");
        ((TestReadyQuery)daoQuery).doThrowException = persistenceException;
        try
        {
            entityQuery.getSingleResult();
            failBecauseExceptionWasNotThrown(NoResultException.class);
        }
        catch(NoResultException e)
        {
            assertThat(e.getMessage()).isEqualTo("Named query error: " + persistenceException.toString());
        }
    }

    @Test
    public void test_EntityQuery_getSingleResult_sqlexception_thrown()
    {
        SQLException sqlException = new SQLException("No row matched on primary key");
        PersistenceException persistenceException = new PersistenceException("Error", sqlException);
        ((TestReadyQuery)daoQuery).doThrowException = persistenceException;
        try
        {
            entityQuery.getSingleResult();
            failBecauseExceptionWasNotThrown(NoResultException.class);
        }
        catch(NoResultException e)
        {
            assertThat(e.getMessage()).isEqualTo("Named query error: " + sqlException.toString());
        }
    }
    
    @Test
    public void test_EntityQuery_setParameter_string()
    {
        do_test_EntityQuery_setParameter_string(ParamType.object);
        do_test_EntityQuery_setParameter_string(ParamType.date);
        do_test_EntityQuery_setParameter_string(ParamType.calendar);
    }

    @Test
    public void test_EntityQuery_setParameter_string_null()
    {
        do_test_EntityQuery_setParameter_string_null(ParamType.object);
        do_test_EntityQuery_setParameter_string_null(ParamType.date);
        do_test_EntityQuery_setParameter_string_null(ParamType.calendar);
    }

    @Test
    public void test_EntityQuery_setParameter_string_no_match()
    {
        do_test_EntityQuery_setParameter_string_no_match(ParamType.object);
        do_test_EntityQuery_setParameter_string_no_match(ParamType.date);
        do_test_EntityQuery_setParameter_string_no_match(ParamType.calendar);
    }

    @Test
    public void test_EntityQuery_setParameter_index()
    {
        do_test_EntityQuery_setParameter_index(ParamType.object);
        do_test_EntityQuery_setParameter_index(ParamType.date);
        do_test_EntityQuery_setParameter_index(ParamType.calendar);
    }

    @Test
    public void test_EntityQuery_setParameter_index_bounds()
    {
        do_test_EntityQuery_setParameter_index_bounds(ParamType.object);
        do_test_EntityQuery_setParameter_index_bounds(ParamType.date);
        do_test_EntityQuery_setParameter_index_bounds(ParamType.calendar);
    }

    @Test
    public void test_EntityQuery_setParameter_index_0()
    {
        do_test_EntityQuery_setParameter_index_0(ParamType.object);
        do_test_EntityQuery_setParameter_index_0(ParamType.date);
        do_test_EntityQuery_setParameter_index_0(ParamType.calendar);
    }
    
    public void do_test_EntityQuery_setParameter_index_0()
    {
        try
        {
            entityQuery.setParameter(0, PRIMARY_KEY);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"0\" is invalid");
        }
        
    }

    public void do_test_EntityQuery_setParameter_string()
    {
        Query query = entityQuery.setParameter(ID_COLUMN_NAME, PRIMARY_KEY);
        SimpleSelectArg selectArg = (SimpleSelectArg) daoQuery.argumentMap.get(ID_COLUMN_NAME);
        assertThat(selectArg.getValue()).isEqualTo(PRIMARY_KEY);
        assertThat(query).isEqualTo(entityQuery);
    }

    public void do_test_EntityQuery_setParameter_string(ParamType paramType)
    {
        Query query = null;
        Object value = null;
        switch (paramType)
        {
        case object:    query = entityQuery.setParameter(ID_COLUMN_NAME, PRIMARY_KEY); value = PRIMARY_KEY; break;
        case date:      query = entityQuery.setParameter(CREATED_COLUMN_NAME, CREATED, TemporalType.DATE); value = CREATED; break;
        case calendar:  query = entityQuery.setParameter(CREATED_COLUMN_NAME, CREATED_CALENDAR, TemporalType.DATE); value = CREATED; break;
        default:
        }
        String name = (paramType == ParamType.object) ? ID_COLUMN_NAME : CREATED_COLUMN_NAME;
        SimpleSelectArg selectArg = (SimpleSelectArg) daoQuery.argumentMap.get(name);
        assertThat(selectArg.getValue()).isEqualTo(value);
        assertThat(query).isEqualTo(entityQuery);
    }

    public void do_test_EntityQuery_setParameter_string_null(ParamType paramType)
    {
        try
        {
            switch (paramType)
            {
            case object:    entityQuery.setParameter((String)null, PRIMARY_KEY); break;
            case date:     entityQuery.setParameter((String)null, CREATED, TemporalType.DATE); break;
            case calendar: entityQuery.setParameter((String)null, CREATED_CALENDAR, TemporalType.DATE); break;
            default:
            }
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"null\" is invalid");
        }
        
    }

    public void do_test_EntityQuery_setParameter_string_no_match(ParamType paramType)
    {
        try
        {
            switch (paramType)
            {
            case object:    entityQuery.setParameter("xxxx", PRIMARY_KEY); break;
            case date:     entityQuery.setParameter("xxxx", CREATED, TemporalType.DATE); break;
            case calendar: entityQuery.setParameter("xxxx", CREATED_CALENDAR, TemporalType.DATE); break;
            default:
            }
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"xxxx\" is invalid");
        }
        
    }

    public void do_test_EntityQuery_setParameter_index(ParamType paramType)
    {
        Query query = null;
        Object value = null;
        switch (paramType)
        {
        case object:    query = entityQuery.setParameter(1, PRIMARY_KEY); value = PRIMARY_KEY; break;
        case date:      query = entityQuery.setParameter(1, CREATED, TemporalType.DATE); value = CREATED; break;
        case calendar:  query = entityQuery.setParameter(1, CREATED_CALENDAR, TemporalType.DATE); value = CREATED; break;
        default:
        }
        
        SimpleSelectArg selectArg = (SimpleSelectArg) daoQuery.argumentMap.get(ID_COLUMN_NAME);
        assertThat(selectArg.getValue()).isEqualTo(value);
        assertThat(query).isEqualTo(entityQuery);
    }

    public void do_test_EntityQuery_setParameter_index_bounds(ParamType paramType)
    {
        try
        {
            switch (paramType)
            {
            case object:    entityQuery.setParameter(3, PRIMARY_KEY); break;
            case date:     entityQuery.setParameter(3, CREATED, TemporalType.DATE); break;
            case calendar: entityQuery.setParameter(3, CREATED_CALENDAR, TemporalType.DATE); break;
            default:
            }
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"3\" is invalid");
        }
        
    }
    
    public void do_test_EntityQuery_setParameter_index_0(ParamType paramType)
    {
        try
        {
            switch (paramType)
            {
            case object:    entityQuery.setParameter(0, PRIMARY_KEY); break;
            case date:     entityQuery.setParameter(0, CREATED, TemporalType.DATE); break;
            case calendar: entityQuery.setParameter(0, CREATED_CALENDAR, TemporalType.DATE); break;
            default:
            }
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"0\" is invalid");
        }
        
    }

    static SimpleSelectArg[] getSelectionArguments()
    {
        SimpleSelectArg[] selectionArguments = new SimpleSelectArg[2];
        selectionArguments[0] = new SimpleSelectArg();
        //selectionArguments[0].setValue(PRIMARY_KEY);
        selectionArguments[0].setMetaInfo(ID_COLUMN_NAME);
        selectionArguments[1] = new SimpleSelectArg();
        //selectionArguments[1].setValue(CREATED);
        selectionArguments[1].setMetaInfo(CREATED_COLUMN_NAME);
        return selectionArguments;
    }
    
}
