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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import au.com.cybersearch2.classylog.*;

/**
 * NativeQuery
 * Implements javax.persistence.Query using native query. Only a subset of PersistenceUnitAdmin API 1.0 methods supported.
 * @author Andrew Bowley
 * 30/05/2014
 */
public class NativeQuery<T> extends QueryBase<T>
{
    public static final String TAG = "NativeQuery";
    protected static Log log = JavaLogger.getLogger(TAG);
    /** Query invoked using Android SQLite interface */
    protected SqlQuery sqlQuery;
  
    /**
     * Create a NativeQuery object
     * @param sqlQuery Query invoked using Android SQLite interface
     */
    public NativeQuery(SqlQuery sqlQuery)
    {
        this.sqlQuery = sqlQuery;
    }

    /**
     * Execute an update or delete statement. NOT implemented.
     * @return 0
     */
    @Override
    public int executeUpdate() 
    {
        release();
        return 0; // Updates and deletes are not currently supported.
    }

    /**
     * Execute a SELECT query and return the query results as a List.
     * @return List of objects
     */   
    @SuppressWarnings("unchecked")
    @Override
    public List<T> getResultList() 
    {
        if (isClosed) // Only perform query once
            return new ArrayList<>();
        try
        {
            return (List<T>) sqlQuery.getResultObjectList(startPosition, maxResults);
        }
        finally
        {
            release();
        }
    }

    /**
     * Execute a SELECT query that returns a single result.
     * @return Object
     * @throws NoResultException if there is no result
     */
    @SuppressWarnings("unchecked")
	@Override
    public T getSingleResult() 
    {
        T result = null;
        if (isClosed) // Only perform query once
            throw new NoResultException("getSingleResult() called when query already executed");
        String message = sqlQuery.toString();
        try
        {
             result = (T) sqlQuery.getResultObject();
        }
        catch (PersistenceException e)
        {
            message += ": " + ((e.getCause() != null) ? e.getCause().toString() : e.toString());
            log.error(TAG, message, e);
        }
        finally
        {
            release();
        }
        if (result == null)
        {
            throw new NoResultException(message);
        }
        return result;
    }

   
    /**
     * Bind an argument to a named parameter.
     * @param param The parameter name
     * @param value Object
     * @return The same query instance
     * @throws IllegalArgumentException if parameter name does not
     *    correspond to parameter in query string
     */
    @Override
    public TypedQuery<T> setParameter(String param, Object value) 
    {
        if (!sqlQuery.setParam(param, value))
            throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
        return this;
    }

    /**
     * Bind an argument to a positional parameter.
     * @param position  Starts at 1
     * @param value Object
     * @return The same query instance
     * @throws IllegalArgumentException if position does not
     *    correspond to positional parameter of query
     */
    @Override
    public TypedQuery<T> setParameter(int position, Object value) 
    {
        if (!sqlQuery.setParam(position, value))
            throw new IllegalArgumentException("Position \"" + position + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Date to a named parameter.
     * @param param The parameter name
     * @param value Date
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if parameter name does not
     *    correspond to parameter in query string
     */
    @Override
    public TypedQuery<T> setParameter(String param, Date value, TemporalType type) 
    {
        if (!sqlQuery.setParam(param, value))
            throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Calendar to a named parameter.
     * @param param The parameter name
     * @param value Calendar
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if parameter name does not
     *    correspond to parameter in query string
     */
    @Override
    public TypedQuery<T> setParameter(String param, Calendar value, TemporalType type) 
    {
        if (!sqlQuery.setParam(param, value.getTime()))
            throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Date to a positional parameter.
     * @param position  Starts at 1
     * @param value Date
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if position does not
     *    correspond to positional parameter of query
     */
    @Override
    public TypedQuery<T> setParameter(int position, Date value, TemporalType type) 
    {
        if (!sqlQuery.setParam(position, value))
            throw new IllegalArgumentException("Position \"" + position + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Calendar to a positional parameter.
     * @param position  Starts at 1
     * @param value Calendar
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if position does not
     *    correspond to positional parameter of query
     */
    @Override
    public TypedQuery<T> setParameter(int position, Calendar value, TemporalType type) 
    {
        if (!sqlQuery.setParam(position, value.getTime()))
            throw new IllegalArgumentException("Position \"" + position + "\" is invalid");
        return this;
    }

}
