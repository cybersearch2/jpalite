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

import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

/**
 * QueryGenerator
 * Factory for DaoQuery objects.
 * DaoQuery is a OrmLite query for generic entity class.
 * @author Andrew Bowley
 * 13/05/2014
 */
public interface DaoQueryFactory
{
    /**
     * Returns query object which will execute a prepared statement when required selection arguments are provided
     * @param dao OrmLite data access object of generic type matching Entity class to be retrieved
     * @param <T> Dao type
     * @return DaoQuery
     * @throws SQLException if database operation fails
     */
    <T extends OrmEntity> DaoQuery<T> generateQuery(PersistenceDao<T> dao) throws SQLException;
}
