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
import javax.persistence.PersistenceException;

/**
 * SetRollbackTransaction
 * Transaction to allow user to rollback when not in User Transaction mode
 * @author Andrew Bowley
 * 10/06/2014
 */
public class SetRollbackTransaction implements EntityTransaction
{
    /** Wrapped transaction */
    protected EntityTransaction entityTransaction;
   
    /**
     * Create SetRollbackTransaction object
     * @param entityTransaction Wrapped transaction
     */
    public SetRollbackTransaction(EntityTransaction entityTransaction)
    {
        this.entityTransaction = entityTransaction;
    }

    /**
     * Ignore any call to start the resource transaction.
     */
    @Override
    public void begin() 
    {
    }
    
    /**
     * Ignore any call to commit the current transaction
     */
    @Override
    public void commit() 
    {
    }
    
    /**
     * Determine whether the current transaction has been marked
     * for rollback.
     * @throws IllegalStateException if {@link #isActive()} is false.
     */
    @Override
    public boolean getRollbackOnly() 
    {
        return entityTransaction.getRollbackOnly();
    }
    
    /**
     * Indicate whether a transaction is in progress.
     * @throws PersistenceException if an unexpected error condition is encountered.
     */
    @Override
    public boolean isActive() 
    {
        return entityTransaction.isActive();
    }
    
    /**
     * Ignore any call to roll back the current transaction
     */
    @Override
    public void rollback() 
    {
    }
    
    /**
     * Mark the current transaction so that the only possible
     * outcome of the transaction is for the transaction to be
     * rolled back.
     * @throws IllegalStateException if {@link #isActive()} is false.
     */
    @Override
    public void setRollbackOnly() 
    {
        entityTransaction.setRollbackOnly();;
    }
}
