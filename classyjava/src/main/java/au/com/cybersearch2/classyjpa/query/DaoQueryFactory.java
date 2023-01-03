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

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;

/**
 * QueryGenerator
 * Factory for DaoQuery objects.
 * DaoQuery is a OrmLite query for generic entity class.
 * @author Andrew Bowley
 * 13/05/2014
 */
public abstract class DaoQueryFactory<T extends OrmEntity>
{
	/** Entity class */
	protected final Class<T>  entityClass;
    /** Persistence unit */
    protected final PersistenceAdmin persistenceAdmin;

    /**
     * Create DaoQueryFactory object
     * @param entityClass Entity class
     * @param persistenceAdmin Persistence unit
     */
	protected DaoQueryFactory(Class<T> entityClass, PersistenceAdmin persistenceAdmin) {
		this.entityClass = entityClass;
		this.persistenceAdmin = persistenceAdmin;
	}

    /**
     * Returns query object which will execute a prepared statement when required selection arguments are provided
     * @param ormQuery Wraps OrmLite QueryBuilder
     * @return DaoQuery object
     * @throws SQLException if database operation fails
     */
    protected abstract DaoQuery<T> generateQuery(OrmQuery<T> ormQuery) throws SQLException;

    /**
     * Returns query object which will execute a prepared statement when required selection arguments are provided
     * @param connectionSource Open connection source
     * @return DaoQuery object
     * @throws SQLException if database operation fails
     */
    public DaoQuery<T> generateQuery(ConnectionSource connectionSource) throws SQLException {
		PersistenceDao<T> dao = (PersistenceDao<T>) persistenceAdmin.getDao(entityClass, connectionSource);
   	    return generateQuery(new OrmQuery<T>(dao));
    }
}
