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

import java.sql.SQLException;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

/**
 * NamedDaoQuery
 * Named OrmLite query generator
 * @author Andrew Bowley
 * 13/05/2014
 */
public class NamedDaoQuery<T extends OrmEntity> implements Comparable<NamedDaoQuery<T>>
{
    /** Entity class */
    protected Class<T> clazz;
    /** Name of query */
    protected String name;
    /** Query generator which incorporates selection arguments */
    protected DaoQueryFactory daoQueryFactory;
    
    /**
     * Create NamedDaoQuery object
     * @param clazz Entity class
     * @param name Name of query
     * @param daoQueryFactory Query generator which incorporates selection arguments
     */
    public NamedDaoQuery(Class<T> clazz, String name, DaoQueryFactory daoQueryFactory)
    {
        this.clazz = clazz;
        this.name = name;
        this.daoQueryFactory = daoQueryFactory;
    }

    /**
     * Returns OrmLite query
     * @param dao Entity DAO containing open ConnectionSource
     * @return Query
     */
    public TypedQuery<? extends OrmEntity> createQuery(PersistenceDao<? extends OrmEntity> dao)
    {
        try
        {
        	if (!dao.getDataClass().isAssignableFrom(getEntityClass()))
                throw new PersistenceException(String.format("Named query \"%s\" cannot be created with dao of class %x", name, getEntityClass().getName()));
			@SuppressWarnings("unchecked")
			DaoQuery<T> daoQuery = (DaoQuery<T>) daoQueryFactory.generateQuery(dao);
            return new EntityQuery<T>(daoQuery);
        }
        catch (SQLException e)
        {
            throw new PersistenceException(String.format("Named query \"%s\" failed on start", name), e);
        }
    }

    /**
     * Returns Entity class
     * @return Class
     */
    public Class<T> getEntityClass()
    {
        return clazz;
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
    public int compareTo(NamedDaoQuery<T> another) 
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
        if ((other instanceof NamedDaoQuery)) {
        	NamedDaoQuery<?> otherQuery = (NamedDaoQuery<?>)other;
        	return (clazz == otherQuery.getEntityClass()) && 
                    name.equals(otherQuery.name);
        }
        return false;
    }
    
}
