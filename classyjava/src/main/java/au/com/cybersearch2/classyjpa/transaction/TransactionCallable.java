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
package au.com.cybersearch2.classyjpa.transaction;

import com.j256.ormlite.support.DatabaseConnection;

/**
 * TransactionCallable
 * Executes Callable object passing open database connection on which transaction is active
 * @author Andrew Bowley
 * 01/08/2014
 */
public interface TransactionCallable
{
    /**
     * Computes a Boolean result to indicate success or failure (rollback required), or throws an exception if unexpected error happens.
     * @param databaseConnection Database connection
     * databaseConnection Open database connection on which transaction is active
     * @return Boolean - Boolean.TRUE indicates success
     * @throws Exception if unable to compute a result
     */
    Boolean call(DatabaseConnection databaseConnection) throws Exception;

}
