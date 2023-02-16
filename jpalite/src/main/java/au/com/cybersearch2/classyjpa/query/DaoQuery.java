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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.stmt.SelectArg;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;

/**
 * DaoQuery
 * OrmLite query returning an entity class. This is an abstract class as it contains abstract method buildQuery() 
 * for derived class to construct a query using an object of class com.j256.ormlite.stmt.QueryBuilder. 
 * @author Andrew Bowley
 * 01/06/2014
 */
public abstract class DaoQuery<T extends OrmEntity> implements OrmQueryBuilder<T>
{
    /**
     * SimpleSelectArg
     * Makes value accessible without potential SQLException
     * @see com.j256.ormlite.stmt.SelectArg#getValue()
     * @author Andrew Bowley
     * 26/08/2014
     */
    public static class SimpleSelectArg extends SelectArg
    {
        /**
         * Returns value 
         * @return Object
         * @see com.j256.ormlite.stmt.SelectArg#getValue()
         */
        @Override
        public Object getValue() {
            return super.getValue();
        }
    }
    
    /** Wraps OrmLite QueryBuilder */
    protected OrmQuery<T> ormQuery;
    /** Maps selection argument to name ie. columnName attribute */
    protected Map<String, SelectArg> argumentMap;
    /** Selection arguments which are used to construct the WHERE clause */
    protected SelectArg[] argumentArray;
 
    /**
     * Create new DaoQuery object
     * @param ormQuery Wraps OrmLite QueryBuilder
     * @param selectionArguments Selection arguments which are used to construct the WHERE clause
     */
    public DaoQuery(OrmQuery<T> ormQuery, SimpleSelectArg... selectionArguments)
    {
        this.ormQuery = ormQuery;
        argumentMap = new HashMap<>();
        if ((selectionArguments != null) && (selectionArguments.length > 0))
        {
            // Copy arguments to local array
            argumentArray = new SelectArg[selectionArguments.length];
            System.arraycopy(selectionArguments, 0, argumentArray, 0, selectionArguments.length);
            // Set up map to access arguments by name
            for (SelectArg selectArg: selectionArguments)
            {
                selectArg.setValue(null); // Reset to avoid using stale data
                argumentMap.put(selectArg.getColumnName(), selectArg);
            }
        }
        else
            argumentArray = new SelectArg[]{};
    }

    /**
     * Returns list of objects from executing prepared query
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @return List of Entity objects
     */
    protected List<T> getResultList(int startPosition, int maxResults) 
    {
        return ormQuery.getResultList(startPosition, maxResults, this);
    }

    /**
     * Returns object from executing prepared query
     * @return Entity object or null if nothing returned by query
     */
    protected T getSingleResult() 
    {
        return ormQuery.getSingleResult(this);
    }

    /**
     * Returns true if position value is in range of 1 to number of arguments
     * @param position Position
     * @return boolean
     */
    public boolean isValidPosition(int position)
    {
        return (position <= argumentArray.length) && (position > 0);
    }
 
    /**
     * Returns selection argument at specified position
     * @param position int
     * @return SelectArg
     */
    public SelectArg get(int position)
    {
        return argumentArray[position - 1];
    }

    /**
     * Returns selection argument for specified name
     * @param param Name of parameter
     * @return SelectArg
     */
    public SelectArg get(String param)
    {
        return argumentMap.get(param);
    }
}
