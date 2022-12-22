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
package au.com.cybersearch2.classyjpa;

import java.sql.SQLException;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.query.DaoQuery;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;

import com.j256.ormlite.stmt.QueryBuilder;

/**
 * QueryForAllGenerator
 * Query factory to find all objects belonging to a particular Entity class.
 * @author Andrew Bowley
 * 01/06/2014
 */
public class QueryForAllGenerator implements DaoQueryFactory
{
    /** Interface for JPA Support */
    PersistenceAdmin persistenceAdmin;

    /**
     * ForAllQuery
     * The query object produced each time generateQuery() is called on containing class
     * @author Andrew Bowley
     * 23 Sep 2014
     */
    class ForAllQuery<T extends OrmEntity> extends DaoQuery<T>
    {
        /**
         * Create ForAllQuery object
         * @param dao OrmLite data access object of generic type matching Entity class to be retrieved
         * @throws SQLException
         */
        public ForAllQuery(PersistenceDao<T> dao) throws SQLException
        {
            // The super class executes the prepared statement
            super(dao);
        }

        /**
         * Construct a query using supplied QueryBuilder.
         * @see au.com.cybersearch2.classyjpa.query.DaoQuery#buildQuery(com.j256.ormlite.stmt.QueryBuilder)
         */
        @Override
        protected QueryBuilder<T, Integer> buildQuery(
                QueryBuilder<T, Integer> statementBuilder) throws SQLException 
        {
            // Query for all objects in database by leaving out where clause
            return statementBuilder;
        }

    }

    /**
     * Create QueryForAllGenerator object
     * @param persistenceAdmin Interface for JPA Support
     */
    public QueryForAllGenerator(PersistenceAdmin persistenceAdmin)
    {
        this.persistenceAdmin = persistenceAdmin;
    }
    
    /**
     * Returns query object which will execute a prepared statement with a primary key selection argument
     * @see au.com.cybersearch2.classyjpa.query.DaoQueryFactory#generateQuery(au.com.cybersearch2.classyjpa.entity.PersistenceDao)
     */
    @Override
    public <T extends OrmEntity> DaoQuery<T> generateQuery(PersistenceDao<T> dao)
            throws SQLException 
    {
        return new ForAllQuery<T>(dao);
    }

}
