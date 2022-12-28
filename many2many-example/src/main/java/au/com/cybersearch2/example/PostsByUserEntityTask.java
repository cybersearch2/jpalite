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

import java.util.Collections;
import java.util.List;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * PostsByUserEntityTask
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class PostsByUserEntityTask implements PersistenceWork
{
    User user1;
    List<Post> posts;

    /**
     * Create PostsByUserEntityTask object
     * @param user1_id User 1 primary key
     */
    public PostsByUserEntityTask(int user1_id)
    {
        user1 = new User();
        user1.id = user1_id;
        posts = Collections.emptyList();
    }


    /**
      * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doTask(au.com.cybersearch2.classyjpa.EntityManagerLite)
      */
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
        entityManager.merge(user1);
        entityManager.refresh(user1);
        posts = Collections.unmodifiableList(user1.getPosts());
     }

    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onPostExecute(boolean)
     */
    @Override
    public void onPostExecute(boolean success) 
    {
        if (!success)
            throw new IllegalStateException("Query " + ManyToManyMain.POSTS_BY_USER + " failed. Check console for error details.");
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onRollback(java.lang.Throwable)
     */
    @Override
    public void onRollback(Throwable rollbackException) 
    {
        throw new IllegalStateException("Query " + ManyToManyMain.POSTS_BY_USER + " failed. Check console for stack trace.", rollbackException);
    }

    /**
     * Returns result of "posts_by_user" query
     * @return List&lt;Post&gt;
     */
    public List<Post> getPosts()
    {
        return posts;
    }
    

}
