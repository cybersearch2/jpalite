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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.log.LogRecordHandler;
import au.com.cybersearch2.log.TestLogHandler;


/**
 * NativeQueryTest
 * @author Andrew Bowley
 * 17/07/2014
 */
@RunWith(MockitoJUnitRunner.class)
public class NativeQueryTest
{
    enum ParamType
    {
        date,
        calendar
    }

    static final Integer PRIMARY_KEY = 23;
    static final String ID_COLUMN_NAME = "_ID";
    static final Date   CREATED = new Date();
    static final String CREATED_COLUMN_NAME = "Created";
    static Calendar CREATED_CALENDAR;
    private static final int OFFSET = 17;
    private static final int LIMIT = 100;
	static LogRecordHandler logRecordHandler;
	
	@Mock
    private SqlQuery<RecordCategory> sqlQuery;
    private NativeQuery<RecordCategory> nativeQuery;

  
	@BeforeClass public static void onlyOnce() {
		logRecordHandler = TestLogHandler.logRecordHandlerInstance();
	}

	@Before
    public void setUp()
    {
		nativeQuery = new NativeQuery<>(sqlQuery);
		TestLogHandler.getLogRecordHandler().clear();
        if (CREATED_CALENDAR == null)
        {
            CREATED_CALENDAR = GregorianCalendar.getInstance();
            CREATED_CALENDAR.setTime(CREATED);
        }
    }
    
    @Test
    public void test_getResultList() throws SQLException
    {
        RecordCategory recordCategory = new RecordCategory();
        nativeQuery.setMaxResults(LIMIT);
        nativeQuery.setFirstResult(OFFSET);
        assertThat(nativeQuery.isClosed).isFalse();
        when((List<RecordCategory>)sqlQuery.getResultObjectList(OFFSET, LIMIT)).thenReturn(Collections.singletonList(recordCategory));
        List<RecordCategory> result = nativeQuery.getResultList();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(recordCategory);
        assertThat(nativeQuery.isClosed).isTrue();
    }

    @Test
    public void test_getResultList_closed()
    {
        nativeQuery.release();
        assertThat(nativeQuery.getResultList()).isNotNull();
        assertThat(nativeQuery.getResultList().size()).isEqualTo(0);
        assertThat(nativeQuery.isClosed).isTrue();
    }

    @Test
    public void test_getSingleResult() throws SQLException
    {
        RecordCategory recordCategory = new RecordCategory();
        when(sqlQuery.getResultObject()).thenReturn(recordCategory);
        Object result = nativeQuery.getSingleResult();
        assertThat(result).isEqualTo(recordCategory);
        assertThat(nativeQuery.isClosed).isTrue();
    }

    @Test
    public void test_getSingleResult_null()
    {
        when(sqlQuery.getResultObject()).thenReturn(null);
        try
        {
            nativeQuery.getSingleResult();
            failBecauseExceptionWasNotThrown(NoResultException.class);
        }
        catch(NoResultException e)
        {
            assertThat(e.getMessage()).isEqualTo(sqlQuery.toString());
        }
    }

    @Test
    public void test_getSingleResult_closed()
    {
        nativeQuery.release();
        try
        {
            nativeQuery.getSingleResult();
            failBecauseExceptionWasNotThrown(NoResultException.class);
        }
        catch(NoResultException e)
        {
            assertThat(e.getMessage()).isEqualTo("getSingleResult() called when query already executed");
        }
    }

    @Test
    public void test_getSingleResult_exception_thrown()
    {
        PersistenceException persistenceException = new PersistenceException("No row matched on primary key");
        when(sqlQuery.getResultObject()).thenThrow(persistenceException);
        try
        {
            nativeQuery.getSingleResult();
            failBecauseExceptionWasNotThrown(NoResultException.class);
        }
        catch(NoResultException e)
        {
            assertThat(e.getMessage()).contains(persistenceException.toString());
        }
        assertThat(logRecordHandler.match(0, "sqlQuery: javax.persistence.PersistenceException: No row matched on primary key")).isTrue();
    }


    @Test
    public void test_executeUpdate() throws SQLException
    {
        assertThat(nativeQuery.executeUpdate()).isEqualTo(0);
        assertThat(nativeQuery.isClosed).isTrue();
        Mockito.verifyNoInteractions(sqlQuery);
    }

    @Test
    public void test_setParameterByPosition() throws SQLException
    {
        when(sqlQuery.setParam(1, Integer.MAX_VALUE)).thenReturn(true);
        assertThat(nativeQuery.setParameter(1, Integer.MAX_VALUE)).isEqualTo(nativeQuery);
    }
    
    @Test
    public void test_setParameterByName() throws SQLException
    {
        when(sqlQuery.setParam("key", Integer.MAX_VALUE)).thenReturn(true);
        assertThat(nativeQuery.setParameter("key", Integer.MAX_VALUE)).isEqualTo(nativeQuery);
    }
    
    @Test
    public void test_setParameter_position_invalid() throws SQLException
    {
        when(sqlQuery.setParam(1, Integer.MAX_VALUE)).thenReturn(false);
        try
        {
            assertThat(nativeQuery.setParameter(1, Integer.MAX_VALUE)).isEqualTo(nativeQuery);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Position \"1\" is invalid");
        }
    }

    @Test
    public void test_setParameter_name_invalid() throws SQLException
    {
        when(sqlQuery.setParam("key", Integer.MAX_VALUE)).thenReturn(false);
        try
        {
            assertThat(nativeQuery.setParameter("key", Integer.MAX_VALUE)).isEqualTo(nativeQuery);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"key\" is invalid");
        }
    }

    @Test
    public void test_nativeQuery_setParameter_string()
    {
        do_test_nativeQuery_setParameter_string(ParamType.date);
        do_test_nativeQuery_setParameter_string(ParamType.calendar);
    }

    @Test
    public void test_nativeQuery_setParameter_string_invalid()
    {
        do_test_nativeQuery_setParameter_string_invalid(ParamType.date);
        do_test_nativeQuery_setParameter_string_invalid(ParamType.calendar);
    }

    @Test
    public void test_nativeQuery_setParameter_index()
    {
        do_test_nativeQuery_setParameter_index(ParamType.date);
        do_test_nativeQuery_setParameter_index(ParamType.calendar);
    }

    @Test
    public void test_nativeQuery_setParameter_index_invalid()
    {
        do_test_nativeQuery_setParameter_index_invalid(ParamType.date);
        do_test_nativeQuery_setParameter_index_invalid(ParamType.calendar);
    }

    public void do_test_nativeQuery_setParameter_index_0()
    {
        try
        {
            nativeQuery.setParameter(0, PRIMARY_KEY);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"0\" is invalid");
        }
        
    }

    public void do_test_nativeQuery_setParameter_string(ParamType paramType)
    {
        Query query = null;
        when(sqlQuery.setParam(CREATED_COLUMN_NAME, CREATED)).thenReturn(true);
        
        switch (paramType)
        {
        case date:      query = nativeQuery.setParameter(CREATED_COLUMN_NAME, CREATED, TemporalType.DATE); break;
        case calendar:  query = nativeQuery.setParameter(CREATED_COLUMN_NAME, CREATED_CALENDAR, TemporalType.DATE); break;
        default:
        }
        assertThat(query).isEqualTo(nativeQuery);
    }

    public void do_test_nativeQuery_setParameter_string_invalid(ParamType paramType)
    {
        when(sqlQuery.setParam("xxxx", CREATED)).thenReturn(false);
        try
        {
            switch (paramType)
            {
            case date:     nativeQuery.setParameter("xxxx", CREATED, TemporalType.DATE); break;
            case calendar: nativeQuery.setParameter("xxxx", CREATED_CALENDAR, TemporalType.DATE); break;
            default:
            }
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"xxxx\" is invalid");
        }
        
    }

    public void do_test_nativeQuery_setParameter_index(ParamType paramType)
    {
        Query query = null;
        when(sqlQuery.setParam(1, CREATED)).thenReturn(true);
        
        switch (paramType)
        {
        case date:      query = nativeQuery.setParameter(1, CREATED, TemporalType.DATE); break;
        case calendar:  query = nativeQuery.setParameter(1, CREATED_CALENDAR, TemporalType.DATE); break;
        default:
        }
        assertThat(query).isEqualTo(nativeQuery);
    }

    public void do_test_nativeQuery_setParameter_index_invalid(ParamType paramType)
    {
        when(sqlQuery.setParam(3, CREATED)).thenReturn(false);
        try
        {
            switch (paramType)
            {
            case date:      nativeQuery.setParameter(3, CREATED, TemporalType.DATE); break;
            case calendar:  nativeQuery.setParameter(3, CREATED_CALENDAR, TemporalType.DATE); break;
            default:
            }
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Position \"3\" is invalid");
        }
        
    }
}
