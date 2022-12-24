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
import java.util.List;

import javax.persistence.PersistenceException;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

/**
 * Wraps OrmLite QueryBuilder
 */
public class OrmQuery<T extends OrmEntity> {

	private static final String BUILDER_ERROR = "Error preparing query";

	/** Wraps OrmList DAO mapped to Entity class */
	private final PersistenceDao<T> ormDao;

	/**
	 * Construct OrmQuery object
	 * @param ormDao  Wraps OrmList DAO mapped to Entity class
	 */
	public OrmQuery(PersistenceDao<T> ormDao) {
		this.ormDao = ormDao;
	}

    /**
     * Returns object from executing prepared query
     * @param ormQueryBuilder Query builder
     * @return Entity object or null if nothing returned by query
     */
	public T getSingleResult(OrmQueryBuilder<T> ormQueryBuilder) 
    {
        try
        {
            return ormDao.queryForFirst(ormQueryBuilder.buildQuery(getBuilder(0, 1)).prepare());
        }
        catch (SQLException e)
        {
            throw new PersistenceException(BUILDER_ERROR, e);
        }
    }

    /**
     * Returns list of objects from executing prepared query
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @param ormQueryBuilder Query builder
     * @return List of Entity objects
     */
	public List<T> getResultList(int startPosition, int maxResults, OrmQueryBuilder<T> ormQueryBuilder) 
    {
        try
        {
        	return ormDao.query(ormQueryBuilder.buildQuery(getBuilder(startPosition, maxResults)).prepare());
        }
        catch (SQLException e)
        {
            throw new PersistenceException(BUILDER_ERROR, e);
        }
    }
	
    /**
     * Returns prepared query with given builder
     * queryBuilder Query builder
     * @return PreparedQuery object
     */
	public PreparedQuery<T> prepare(QueryBuilder<T,Integer> queryBuilder)
    {
        try
        {
        	return queryBuilder.prepare();
        }
        catch (SQLException e)
        {
            throw new PersistenceException(BUILDER_ERROR, e);
        }
    }
	
   /**
     * Returns query builder with start position and maxResults as given
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @return QueryBuilder object
     */
	public QueryBuilder<T,Integer> getBuilder(int startPosition, int maxResults)
    {
		QueryBuilder<T, Integer> statementBuilder = null;
        try
        {
            statementBuilder = ormDao.queryBuilder();
            if (startPosition > 0)
                statementBuilder.offset(Long.valueOf(startPosition));
            if (maxResults > 0)
                statementBuilder.limit(Long.valueOf(maxResults));
        }
        catch (SQLException e)
        {
            throw new PersistenceException(BUILDER_ERROR, e);
        }
        return statementBuilder;
    }
}
