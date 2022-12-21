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
package au.com.cybersearch2.classyjpa.persist;

import com.j256.ormlite.db.BaseSqliteDatabaseType;

/**
 * SqliteDatabaseType
 * @author Andrew Bowley
 * 14/06/2014
 */
public class SqliteDatabaseType extends BaseSqliteDatabaseType
{
    @Override
    public String getDatabaseName() 
    {
        return "SQLite";
    }

    @Override
    public boolean isDatabaseUrlThisType(String url, String dbTypePart) 
    {
        return "sqlite".equals(dbTypePart);
    }

    @Override
    protected String[] getDriverClassNames() 
    {
        return new String[]{"org.sqlite.JDBC"};
    }
}
