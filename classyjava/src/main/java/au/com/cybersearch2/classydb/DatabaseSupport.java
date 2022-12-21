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
package au.com.cybersearch2.classydb;

import java.util.List;
import java.util.Properties;

import au.com.cybersearch2.classyjpa.query.QueryInfo;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

/**
 * DatabaseSupport
 * Interface for direct database access to allow native operations to be performed
 * @author Andrew Bowley
 * 16/06/2014
 */
public interface DatabaseSupport
{

    public enum ConnectionType
    {
        memory,
        file,
        pooled
    }

    /**
     * Perform any inititialization required prior to creating first database connection
     */
    void initialize();

    /**
     * Perform any clean up required on database shutdown
     */
    void close();

    /**
     * Returns database type
     * @return DatabaseType
     */
    DatabaseType getDatabaseType();
 
    /**
     * Returns list result of native query in Android SQLite API format
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo
     * @param startPosition int
     * @param maxResults int
     * @return List of Objects
     */
    List<Object> getResultList(ConnectionSource connectionSource, QueryInfo queryInfo, int startPosition, int maxResults);

    /**
     * Returns single result of native query in Android SQLite API format
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo
     * @return Object
     */
    Object getSingleResult(ConnectionSource connectionSource, QueryInfo queryInfo);
    
    /**
     * Gets the database version.
     * @param  connectionSource Open ConnectionSource object of database. Can be null for Android SQLite. 
     * @param properties Properties defined in persistence.xml
     * @return the database version
     */
    int getVersion(ConnectionSource connectionSource, Properties properties);

    /**
     * Sets the database version.
     * @param  connectionSource Open ConnectionSource object of database. Can be null for Android SQLite.
     * @param properties Properties defined in persistence.xml
     * @param version the new database version
     */
    void setVersion(int version, Properties properties, ConnectionSource connectionSource);
    
    void registerOpenHelperCallbacks(OpenHelperCallbacks openHelperCallbacks);
    List<OpenHelperCallbacks> getOpenHelperCallbacksList();

}
