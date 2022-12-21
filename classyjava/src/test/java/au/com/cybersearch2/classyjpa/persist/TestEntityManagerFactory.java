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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.persistence.EntityTransaction;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.entity.EntityManagerImpl;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;

/**
 * TestEntityManagerFactory
 * @author Andrew Bowley
 * 14/06/2014
 */
public class TestEntityManagerFactory implements EntityManagerLiteFactory
{
    public static EntityManagerLite entityManager;
    
    @Override
    public void close() {
    }

    @Override
    public EntityManagerLite createEntityManager() {
        return entityManager;
    }

    @Override
    public EntityManagerLite createEntityManager(Map<String, Object>  arg0) {
        return entityManager;
    }

    @Override
    public boolean isOpen() {
        return true;
    }
    
    public static EntityManagerLite getEntityManager()
    {
        return entityManager;
    }
    
    public static EntityTransactionImpl setEntityManagerInstance()
    {
        EntityTransactionImpl transaction = mock(EntityTransactionImpl.class);
        setEntityManagerInstance(transaction);
        return transaction;
    }
    
    public static void setEntityManagerInstance(EntityTransaction transaction)
    {

        entityManager = mock(EntityManagerImpl.class);
        when(entityManager.getTransaction()).thenReturn(transaction);
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }
}
