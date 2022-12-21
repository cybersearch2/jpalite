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

import javax.persistence.EntityTransaction;

/**
 * TransactionInfo
 * Java bean containing transaction context of persistence container. Essential for managing and reporting rollback. 
 * @author Andrew Bowley
 * 15/05/2014
 */
public class TransactionInfo
{
 
    private Throwable rollbackException;
 
    private boolean isUserTransaction;

    private EntityTransaction entityTransaction;
    
    /**
     * Set enclosing entity transaction
     * @param entityTransaction EntityTransaction
     */
    public void setEntityTransaction(EntityTransaction entityTransaction)
    {
        this.entityTransaction = entityTransaction;
    }
    
    /**
     * Returns enclosing entity transaction
     * @return EntityTransaction
     */
    public EntityTransaction getTransaction()
    {
        return entityTransaction;
    }
    
    /**
     * Set Exception responsible for rollback if one is thrown while transaction is active
     * @param rollbackException Throwable
     */
    public void setRollbackException(Throwable rollbackException) 
    {
        this.rollbackException = rollbackException;
    }

    /**
     * Returns Exception responsible for rollback if one is thrown while transaction is active
     * @return Throwable
     */
    public Throwable getRollbackException() 
    {
        return rollbackException;
    }

    /**
     * Returns true if user transaction
     * @return boolean
     */
    public boolean isUserTransaction() 
    {
        return isUserTransaction;
    }

    /**
     * Set user transaction flag
     * @param isUserTransaction boolean
     */
    public void setUserTransaction(boolean isUserTransaction) 
    {
        this.isUserTransaction = isUserTransaction;
    }

}
