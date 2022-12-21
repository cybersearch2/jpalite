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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * SqlQuery
 * Implements javax.persistence.Query invoked using an Android SQLite interface.
 * The SQL is executed with OrmLite JDBC when not running on Android.
 * @author Andrew Bowley
 * 10/07/2014
 */
public class SqlQuery
{
    public static final String TAG = "SqlQuery";
    protected static Log log = JavaLogger.getLogger(TAG);

    protected final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected PersistenceAdmin persistenceAdmin;

    protected QueryInfo queryInfo;

    protected List<String> selectionArgs;

    /**
     * Create SqlQuery object
     * @param persistenceAdmin JPA Support
     * @param queryInfo Native query information
     */
    public SqlQuery(PersistenceAdmin persistenceAdmin, QueryInfo queryInfo)
    {
        this.persistenceAdmin = persistenceAdmin;
        this.queryInfo = queryInfo;
        selectionArgs = new ArrayList<String>();
     }

    /**
     * Execute query and return results as a list of Objects
     * @return Object list
     */
    public List<?> getResultObjectList() 
    {
        return getResultObjectList(0,0);
    }
    
    /**
     * Execute query and return results as a list of Objects
     * @param startPosition Start position
     * @param maxResults Maximum results limit
     * @return Object list
     */
    public List<?> getResultObjectList(int startPosition, int maxResults) 
    {
        queryInfo.setSelectionArgs(selectionArgs.toArray(new String[selectionArgs.size()]));
        return persistenceAdmin.getResultList(queryInfo, startPosition, maxResults);
    }

    /**
     * Execute query and return a single Object result
     * @return Object or null if nothing returned by query
     */
    public Object getResultObject() 
    {
        queryInfo.setSelectionArgs(selectionArgs.toArray(new String[selectionArgs.size()]));
        return persistenceAdmin.getSingleResult(queryInfo);
    }

    /**
     * Set parameter value referenced by position
     * @param position Starts at 1
     * @param value Object
     * @return boolean - true if position is valid, otherwise false
     */
    public boolean setParam(int position, Object value)
    {
        if (position > 0)
        {
            if ((queryInfo.getParameterNames() != null) && (position > queryInfo.getParameterNames().length))
                logInvalidIndex(position);
            else
            {
                selectionArgs.add(position - 1, value == null ? null : formatObject(value));
                return true;
            }
        }
        else
            logInvalidIndex(position);
        return false;
    }
    
    /**
     * Set parameter referenced by name
     * @param param Parameter to set
     * @param value Object
     * @return boolean - true if name is valid, otherwise false
     */
    public boolean setParam(String param, Object value)
    {
        if (param == null)
        {
            log.error(TAG, "Null query parameter encountered for named query '" + queryInfo.getSelection() + "'");
            return false;
        }
        if (queryInfo.getParameterNames() == null)
        {
            log.error(TAG, "Query parameters not supported for named query '" + queryInfo.getSelection() + "'");
            return false;
        }
        String[] parameterNames = queryInfo.getParameterNames();
        int index = 0;
        for (; index < parameterNames.length; index++)
            if (param.equals(parameterNames[index]))
                break;
         if (index == parameterNames.length)
         {
            log.error(TAG, "Query parameter '" + param + "' not found for named query '" + queryInfo.getSelection() + "'");
            return false;
        }
        selectionArgs.add(index, value == null ? null : formatObject(value));
        return true;
    }

    /**
     * Returns object value as String
     * @param value Object
     * @return String
     */
    protected String formatObject(Object value) 
    {
        if (value instanceof Date)
        {   // Dates have to be of standard format for SQLite
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            String dateValue = sdf.format((Date)value); 
            // Append ".SSSSSS" part of format as zeros as non-zero values are not converted correctly
            return dateValue + ".000000";
        }
        return value.toString();
    }

    /**
     * Log "position out of range" message
     * @param position Invalid position value
     */
    protected void logInvalidIndex(int position)
    {
        log.error(TAG, "Query parameter " + position + " out of range for " + toString());
        //throw new IllegalArgumentException("Parameter \"" + position + "\" is invalid");
    }

    /**
     * Returns a string representation of the object.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() 
    {
        return "Named query for '" + queryInfo.getSelection() + "'";
    }
}
