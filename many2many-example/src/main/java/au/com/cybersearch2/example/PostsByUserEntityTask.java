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
import java.util.List;

import javax.persistence.TypedQuery;

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
    User user2;
    Post post1;
    Post post2;
    UserPost user1Post1;
    UserPost user1Post2;
    UserPost user2Post1;
    List<Post> posts;

    /**
     * Create PostsByUserEntityTask object
     * @param user1_id User 1 primary key
     * @param user2_id User 2 primary key
     * @param post1_id Post 1 primary key
     * @param post2_id Post 2 primary key
     */
    public PostsByUserEntityTask(int user1_id, int user2_id, int post1_id, int post2_id)
    {
        user1 = new User();
        user1.id = user1_id;
        user2 = new User();
        user2.id = user2_id;
        post1 = new Post();
        post1.id = post1_id;
        post2 = new Post();
        post2.id = post2_id;
        posts = new ArrayList<>();
    }


    /**
      * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doTask(au.com.cybersearch2.classyjpa.EntityManagerLite)
      */
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
        entityManager.merge(user1);
        entityManager.merge(user2);
        entityManager.merge(post1);
        entityManager.merge(post2);
        entityManager.refresh(user1);
        entityManager.refresh(user2);
        entityManager.refresh(post1);
        entityManager.refresh(post2);
        // Perform query to get all posts by user1
        TypedQuery<Post> query = 
        	entityManager.createNamedQuery(
        		ManyToManyMain.POSTS_BY_USER, Post.class);
        query.setParameter(UserPost.USER_ID_FIELD_NAME, user1.id);
        posts.addAll(query.getResultList());
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
