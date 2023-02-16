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

import au.com.cybersearch2.classyjpa.EntityManagerLite;

/**
 * UserTransactionSupport
 * Interface to extend javax.persistence.EntityManager to support user transactions
 * @author Andrew Bowley
 * 10/06/2014
 */
public interface UserTransactionSupport extends EntityManagerLite
{
    /**
     * Set user transaction flag
     * @param value boolean
     */
    void setUserTransaction(boolean value);
}
