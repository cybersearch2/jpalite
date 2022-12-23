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

import javax.persistence.TypedQuery;

/**
 * NamedSqlQuery
 * Named native query generator
 * @author Andrew Bowley
 * 30/05/2014
 */
public class NamedSqlQuery implements Comparable<NamedSqlQuery>
{
    /** Name of query */
    private String name;
    /** Native query information */
    private QueryInfo queryInfo;
    /** Native query generator */
    protected SqlQueryFactory sqlQueryFactory;
    
    /**
     * Create NamedSqlQuery object
     * @param name Name of query
     * @param queryInfo Native query information
     * @param sqlQueryFactory Native query generator
     */
    public NamedSqlQuery(String name, QueryInfo queryInfo, SqlQueryFactory sqlQueryFactory)
    {
        this.name = name;
        this.queryInfo = queryInfo;
        this.sqlQueryFactory = sqlQueryFactory;
    }

    /**
     * Returns native query
     * @return Query
     */
    @SuppressWarnings("unchecked")
	public <T> TypedQuery<T> createQuery(Class<T> clazz)
    {
        return (TypedQuery<T>) sqlQueryFactory.createSqlQuery(queryInfo);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @param   another The object to be compared.
     * @return  A negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(NamedSqlQuery another) 
    {
        return name.compareTo(another.name);
    }
    
    /**
     * Returns a hash code value for the object.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other)
    {
        if ((other != null) && ! (other instanceof NamedSqlQuery))
            return name.equals(((NamedSqlQuery)other).name);
        return false;
    }
    
}
