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

import java.sql.SQLException;
import java.util.concurrent.Callable;
import static org.mockito.Mockito.*;

import com.j256.ormlite.support.ConnectionSource;

/**
 * TestClassyEntityTransaction
 * @author Andrew Bowley
 * 18/07/2014
 */
public class TestClassyEntityTransaction extends EntityTransactionImpl
{
    TransactionState mockTransactionState = mock(TransactionState.class);
    SQLException sqlException;
    
    public TestClassyEntityTransaction(ConnectionSource connectionSource)
    {
        super(connectionSource);

    }

    public TestClassyEntityTransaction(ConnectionSource connectionSource,
            TransactionCallable onPreCommit)
    {
        super(connectionSource, onPreCommit);

    }

    public TestClassyEntityTransaction(ConnectionSource connectionSource,
            TransactionCallable onPreCommit, Callable<Boolean> onPostCommit)
    {
        super(connectionSource, onPreCommit, onPostCommit);

    }

    protected TransactionState getTransactionState() throws SQLException 
    {
        if (sqlException != null)
            throw sqlException;
        return mockTransactionState;
    }

}
