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

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SQLiteSupportTest
 * @author Andrew Bowley
 * 15/06/2014
 */
public class SQLiteSupportTest
{
    private static final String IN_MEMORY_PATH = "jdbc:sqlite::memory:";

    @Test
    public void test_SQLiteConnection_open_close() throws Exception
    {
        ConnectionSource connectionSource = null;
        boolean success = false;
        try
        {
             // Single connection source example for a database URI
             connectionSource = new JdbcConnectionSource(IN_MEMORY_PATH );
        }
        finally
        {
            if (connectionSource != null)
            {
                connectionSource.close();
                success = true;
             }
        }
        assertThat(success).isTrue();
    }
}
