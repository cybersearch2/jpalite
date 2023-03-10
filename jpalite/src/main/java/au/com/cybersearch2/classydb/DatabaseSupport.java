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

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.query.QueryInfo;

/**
 * DatabaseSupport
 * Interface for direct database access to allow native operations to be performed
 * @author Andrew Bowley
 * 16/06/2014
 */
public interface DatabaseSupport
{
	/** Prefix to distinguish Jtalite property names from others */
	public static final String JTA_PREFIX = "JTA_";

    /** Default location for file database. Intended only for testing purposes. */
    public static final String DEFAULT_FILE_LOCATION = System.getProperty("user.home") + "/.jpalite/resources/db";

    /** Connection type is implementation-specific */
    public enum ConnectionType
    {
        memory,
        file,
        pooled
    }

    /**
     * Perform any initialization required prior to creating first database connection
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
	 * 
	 * @param connectionSource Open ConnectionSource object of database.
	 * @param puName       Persistence unit name
	 * @return the database version
	 */
    int getVersion(ConnectionSource connectionSource, String puName);

	/**
	 * Sets the database version.
	 * 
	 * @param connectionSource Open ConnectionSource object of database.
	 * @param puName       Persistence unit name
	 * @param version          the new database version
	 */
    void setVersion(int version, String puName, ConnectionSource connectionSource);

}
