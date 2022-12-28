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
package au.com.cybersearch2.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * UsersByPostTask
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class UsersByPostTask implements PersistenceWork
{
    Post post1;
    Post post2;
    List<List<User>> resultsList;

    /**
     * Create UsersByPostTask object
     * @param post1_id Post 1 primary key
     * @param post2_id Post 2 primary key
     */
    public UsersByPostTask(int post1_id, int post2_id)
    {
        post1 = new Post();
        post1.id = post1_id;
        post2 = new Post();
        post2.id = post2_id;
        resultsList = new ArrayList<>(2);
    }

    /**
     * Returns result of "users_by_post" query for post 1
     * @return List&lt;User&gt;
     */
    public List<User> getUsersByPost1()
    {
        return resultsList.size() == 2 ? resultsList.get(0) : new ArrayList<>();
    }
    
    /**
     * Returns result of "users_by_post" query for post 2
     * @return List&lt;User&gt;
     */
    public List<User> getUsersByPost2()
    {
        return resultsList.size() == 2 ? resultsList.get(1) : new ArrayList<>();
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doTask(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
        entityManager.merge(post1);
        entityManager.merge(post2);
        entityManager.refresh(post1);
        entityManager.refresh(post2);
        resultsList.add(Collections.unmodifiableList(post1.getUsers()));
        resultsList.add(Collections.unmodifiableList(post2.getUsers()));
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onPostExecute(boolean)
     */
    @Override
    public void onPostExecute(boolean success) 
    {
        if (!success)
            throw new IllegalStateException("Query " + ManyToManyMain.USERS_BY_POST + " failed. Check console for error details.");
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onRollback(java.lang.Throwable)
     */
    @Override
    public void onRollback(Throwable rollbackException) 
    {
        throw new IllegalStateException("Query " + ManyToManyMain.USERS_BY_POST + " failed. Check console for stack trace.", rollbackException);
   }
}
