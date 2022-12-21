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
package au.com.cybersearch2.classynode;

import java.sql.SQLException;

import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.query.DaoQuery;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.DaoQuery.SimpleSelectArg;

import com.j256.ormlite.stmt.QueryBuilder;

/**
 * EntityByNodeIdGenerator
 * Generate query to find Node by primary key
 * @author Andrew Bowley
 * 09/06/2014
 */
public class EntityByNodeIdGenerator implements DaoQueryFactory
{
    @Override
    /**
     * Generate query to find Node by primary key
     * @see au.com.cybersearch2.classyjpa.query.DaoQueryFactory#generateQuery(au.com.cybersearch2.classyjpa.entity.PersistenceDao)
     */
    public <T> DaoQuery<T> generateQuery(PersistenceDao<T, ?> dao)
            throws SQLException 
    {   // Only one select argument required for primary key 
        final SimpleSelectArg nodeIdArg = new SimpleSelectArg();
        // Set primary key column name
        nodeIdArg.setMetaInfo("node_id");
        return new DaoQuery<T>(dao, nodeIdArg){

            /**
             * Update supplied QueryBuilder object to add where clause
             * @see au.com.cybersearch2.classyjpa.query.DaoQuery#buildQuery(com.j256.ormlite.stmt.QueryBuilder)
             */
            @Override
            protected QueryBuilder<T, ?> buildQuery(QueryBuilder<T, ?> queryBuilder)
                    throws SQLException {
                // build a query with the WHERE clause set to 'node_id = ?'
                queryBuilder.where().eq("node_id", nodeIdArg);
                return queryBuilder;
            }};
    }
}
