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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

/**
 * QueryBase
 * Abstract implementation of javax.persistence.Query for both OrmLite and native queries
 * @author Andrew Bowley
 * 08/07/2014
 */
abstract public class QueryBase implements Query
{    
    private static final String GET_PARAM_NO_SUPPORT = "getParameter() not supported";
    private static final String PARAM_NO_SUPPORT = "Parameter not supported";
    /** Maximum number of objects to return */
    protected int maxResults;
    /** The start position of the first result, numbered from 0 */
    protected int startPosition;
    /** Flag for query closed */
    protected volatile boolean isClosed;

    /**
     * Set the maximum number of results to retrieve.
     * @param maxResults Maximum number of objects to return, 0 means unlimited
     * @return The same query instance
     * @throws IllegalArgumentException if argument is negative
     */
    @Override
    public Query setMaxResults(int maxResults) 
    {
        if (maxResults < 0)
            throw new IllegalArgumentException("Parameter \"maxResults\" is negative: " + maxResults);
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Set the position of the first result to retrieve.
     * @param startPosition The start position of the first result, numbered from 0
     * @return The same query instance
     * @throws IllegalArgumentException if argument is negative
     */
     @Override
     public Query setFirstResult(int startPosition) 
     {
         if (startPosition < 0)
             throw new IllegalArgumentException("Parameter \"startPosition\" is negative: " + startPosition);
         this.startPosition = startPosition;
         return this;
     }

    /**
     * Set the flush mode type to be used for the query execution not supported.
     * @param type Not used
     * @return The same query instance
     */
    @Override
    public Query setFlushMode(FlushModeType type) 
    {
        return this;
    }

    /**
     * Set an implementation-specific hint not supported
     * @param hintName Hint
     * @param value Value
     * @return The same query instance
     */
    @Override
    public Query setHint(String hintName, Object value) 
    {
        return this;
    }

    /**
     * The position of the first result the query object was set to retrieve. Returns 0 if setFirstResult was not applied to the query
     * object.
     * 
     * @return position of the first result
     */
    @Override
    public int getFirstResult() 
    {
        return startPosition;
    }

    /**
     * Get the flush mode in effect for the query execution. Always returns the flush mode
     * in effect for the entity manager.
     * 
     * @return flush mode
     */
    @Override
    public FlushModeType getFlushMode() 
    {
        return FlushModeType.COMMIT;
    }

    /**
     * Get the properties and hints and associated values that are in effect for the query instance not supported
     * 
     * @return empty map
     */
    @Override
    public Map<String, Object> getHints() 
    {
        return Collections.emptyMap();
    }

    /**
     * Get the current lock mode for the query not supported.
     * @return LockModeType
     * @throws UnsupportedOperationException unconditionally
     */
    @Override
    public LockModeType getLockMode() 
    {
        throw new UnsupportedOperationException("LockMode not supported");
    }

    /**
     * The maximum number of results the query object was set to retrieve. Returns Integer.MAX_VALUE if setMaxResults was not applied to the
     * query object.
     * 
     * @return maximum number of results
     */
    @Override
    public int getMaxResults()
    {
        return maxResults;
    }

    /**
     * Get the parameter object corresponding to the declared positional parameter with the given position not supported 
     * @param position Position
     */
    @Override
    public Parameter<?> getParameter(int position) {
        throw new IllegalStateException(GET_PARAM_NO_SUPPORT);
    }

    /**
     * Get the parameter object corresponding to the declared parameter of the given name not supported
     * @param name Parameter name
     */
    @Override
    public Parameter<?> getParameter(String name) {
        throw new IllegalStateException(GET_PARAM_NO_SUPPORT);
    }

    /**
     * Get the parameter object corresponding to the declared positional parameter with the given position and type not supported
     * @param position Position
     * @param type Class
     * @param <X> Parameter type
     */
    @Override
    public <X> Parameter<X> getParameter(int position, Class<X> type) {
        throw new IllegalStateException(GET_PARAM_NO_SUPPORT);
    }

    /**
     * Get the parameter object corresponding to the declared parameter of the given name and type not supported
     * @param name Parameter name
     * @param type Class
     * @param <X> Parameter type
     * @return parameter
     */
    @Override
    public <X> Parameter<X> getParameter(String name, Class<X> type) {
        throw new IllegalStateException(GET_PARAM_NO_SUPPORT);
    }

    /**
     * Return the input value bound to the positional parameter not supported
     * @param position Position
     */
    @Override
    public Object getParameterValue(int position) {
        throw new IllegalStateException(GET_PARAM_NO_SUPPORT);
    }

    /**
     * Return the input value bound to the parameter not supported
     * @param param parameter
     * @param <X> Parameter type
     */
    @Override
    public <X> X getParameterValue(Parameter<X> param) {
        throw new IllegalStateException(GET_PARAM_NO_SUPPORT);
    }

    /**
     * Return the input value bound to the named parameter not supported
     * @param param parameter
     */
    @Override
    public Object getParameterValue(String param) {
        throw new IllegalStateException(GET_PARAM_NO_SUPPORT);
    }

    /**
     * Get the parameter objects corresponding to the declared parameters of the query not supported
     */
    @Override
    public Set<Parameter<?>> getParameters() {
        throw new IllegalStateException(PARAM_NO_SUPPORT);
    }

    /**
     * Return a boolean indicating whether a value has been bound to the parameter not supported
     * @param param parameter
     */
    @Override
    public boolean isBound(Parameter<?> param) {
        throw new UnsupportedOperationException(PARAM_NO_SUPPORT);
    }

    /**
     * Set the lock mode type to be used for the query execution not supported
     * @param lockMode Lock mode
     */
    @Override
    public Query setLockMode(LockModeType lockMode) {
        throw new UnsupportedOperationException("LockMode not supported");
    }

    /**
     * Bind the value of a Parameter object not supported
     * @param param parameter
     * @param value Value
     */
    @Override
    public <X> Query setParameter(Parameter<X> param, X value) {
        throw new UnsupportedOperationException(PARAM_NO_SUPPORT);
    }

    /**
     * Bind an instance of java.util.Calendar to a Parameter object not supported
     * @param param parameter
     * @param value Value
     * @param temporalType Temporal type
     */
    @Override
    public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        throw new UnsupportedOperationException(PARAM_NO_SUPPORT);
    }

    /**
     * Bind an instance of java.util.Date to a Parameter object not supported
     * @param param parameter
     * @param value Value
     * @param temporalType Temporal type
     */
    @Override
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        throw new UnsupportedOperationException(PARAM_NO_SUPPORT);
    }

    /**
     * Return an object of the specified type to allow access to the provider-specific API. If the provider's query implementation does not
     * support the specified class, the PersistenceException is thrown.
     * 
     * @param cls
     *            the class of the object to be returned. This is normally either the underlying query implementation class or an interface
     *            that it implements.
     * @return an instance of the specified class
     * @throws PersistenceException
     *             if the provider does not support the call
     */
    @Override
    public <X> X unwrap(Class<X> cls) {
        throw new PersistenceException("unwrap not supported");
    }

    /**
     * Close
     */
    protected synchronized void release() 
    {
        notifyAll(); // Signal to any waiting EntityManager this query is over
        isClosed = true;
    }
}
