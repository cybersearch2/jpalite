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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.DefaultTaskExecutor;
import au.com.cybersearch2.classytask.DefaultTaskMessenger;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.TaskMessenger;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * ORIGINAL COMMENTS:
 * Main sample routine to show how to do many-to-many type relationships. It also demonstrates how we user inner queries
 * as well foreign objects.
 * 
 * <p>
 * <b>NOTE:</b> We use asserts in a couple of places to verify the results but if this were actual production code, we
 * would have proper error handling.
 * </p>
 * <p>
 * CLASSYTOOLS COMMENTS:
 * </p>
 * <p>
 * This example shows JPA in action. The application code exempifies use of a standard persistence interface. 
 * The OrmLite implementation is mostly hidden in library code, but does show up in named queries where, to keep things
 * lightweight, OrmLite QueryBuilder is employed in place of a JQL implementation @see ManyToManyGenerator.
 * </p>
 * <p>
 * Also notable is dependency injection using Dagger @see ManyToManyModule. If one studies the details, what the
 * dependency inject allows is flexibility in 3 ways:
 * </p>
 * <ol>
 * <li>Choice of database - the PersistenceContext binding</li>
 * <li>Location of resource files such as persistence.xml (and Locale too) - the ResourceEnvironment binding</li>
 * <li>How to reduce background thread priority - the ThreadHelper binding</li>
 * </ol>
 */
public class ManyToManyMain 
{


    static public final String POSTS_BY_USER = "posts_by_user";

    static public final String USERS_BY_POST = "users_by_post";

    static public final String PU_NAME = "manytomany";

    private static TaskExecutor taskExecutor;


    User user1;

    User user2;

    Post post1;

    Post post2;

    protected PersistenceContext persistenceContext;
    protected ManyToManyFactory manyToManyFactory;
    protected TaskMessenger<Void,Boolean> taskMessenger;

    /**
     * Create ManyToManyMain object
     * This creates and populates the database using JPA, provides verification logic and runs a test from main().
     */
    public ManyToManyMain() 
    {
        // Set up dependency injection, which creates an ObjectGraph from a ManyToManyModule configuration object
        taskMessenger = new DefaultTaskMessenger<Void>(Void.class);
        persistenceContext = createFactory();
        // Note that the table for each entity class will be created in the following step (assuming database is in memory).
        // To populate these tables, call setUp().
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(PU_NAME);
        // Create named queries to exploit UserPost join table.
        // Note ManyToManyGenerator class is reuseable as it allows any Many to Many association to be queried.
        ManyToManyGenerator manyToManyPostsByUser = 
                new ManyToManyGenerator(persistenceAdmin, "tableUserPost", UserPost.USER_ID_FIELD_NAME, UserPost.POST_ID_FIELD_NAME, Post.ID_FIELD_NAME);
        ManyToManyGenerator manyToManyUsersByPost = 
                new ManyToManyGenerator(persistenceAdmin, "tableUserPost", UserPost.POST_ID_FIELD_NAME, UserPost.USER_ID_FIELD_NAME, User.ID_FIELD_NAME);
        persistenceAdmin.addNamedQuery(Post.class, POSTS_BY_USER, manyToManyPostsByUser);
        persistenceAdmin.addNamedQuery(User.class, USERS_BY_POST, manyToManyUsersByPost);
    }

    /**
     * Test ManyToMany association
     * @param args Not used
     */
	public static void main(String[] args)
	{
     	taskExecutor = new DefaultTaskExecutor();
     	try {
            new ManyToManyMain().runApplication();
     	} finally {
     		taskExecutor.shutdown();
     		System.exit(0);
     	}
	}
	
    /**
     * Populate entity tables. Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException if interrupted
     */
    public void setUp() throws InterruptedException
    {
        // PersistenceUnitAdmin work adds 2 users and 2 posts to the database using JPA.
        // Hence there will be an enclosing transaction to ensure data consistency.
        // Any failure will result in an IllegalStateExeception being thrown from
        // the calling thread.
        PersistenceWork setUpWork = new PersistenceWork(){

            @Override
            public void doTask(EntityManagerLite entityManager)
            {
                // create our 1st user
                user1 = new User("Bilbo Baggins");
                entityManager.persist(user1);
                // have user1 post something
                post1 = new Post("Wow is it cold outside!!");
                entityManager.persist(post1);
                // link the user and the post together in the join table
                UserPost user1Post1 = new UserPost(user1, post1);
                entityManager.persist(user1Post1);
                // have user1 post a second post
                post2 = new Post("Now it's a bit warmer thank goodness.");
                entityManager.persist(post2);
                UserPost user1Post2 = new UserPost(user1, post2);
                entityManager.persist(user1Post2);
                // create another user
                user2 = new User("Gandalf Gray");
                entityManager.persist(user2);
                // have the 2nd user also say the 2nd post
                UserPost user2Post1 = new UserPost(user2, post2);
                entityManager.persist(user2Post1);
                // Database updates committed upon exit
            }

            @Override
            public void onPostExecute(boolean success)
            {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
        execute(setUpWork);
    }

    protected PersistenceContext createFactory()
    {
        manyToManyFactory = new ManyToManyFactory(taskExecutor, taskMessenger);
        return manyToManyFactory.getPersistenceContext();
    }
    
    protected WorkStatus execute(PersistenceWork persistenceWork) throws InterruptedException
    {
        TaskStatus taskStatus = manyToManyFactory.doTask(persistenceWork);
        taskStatus.await(500, TimeUnit.SECONDS);
        return taskStatus.getWorkStatus();
    }

    public void close()
    {
    	taskMessenger.shutdown();
        persistenceContext.close();
    }

    /**
     * Returns Bilbo Baggins' User
     * @return User
     */
    public User getUser1()
    {
        return user1;
    }

    /**
     * Returns Gandalf Gray's User
     * @return User
     */
    public User getUser2()
    {
        return user2;
    }

    /**
     * Returns "Wow is it cold outside!" Post
     * @return Post
     */
    public Post getPost1()
    {
        return post1;
    }

    /**
     * Returns "Now it's a bit warmer thank goodness." Post
     * @return Post
     */
    public Post getPost2()
    {
        return post2;
    }
 
    /**
     * Verify posts retrieved by "posts_by_user" named query
     * @param posts List&lt;Post&gt; retrieved for user1
     */
    public void verifyPostsByUser(List<Post> posts)
    {
        // user1 should have 2 posts
        assertEquals(2, posts.size());
        assertEquals(post1.id, posts.get(0).id);
        assertEquals(post1.contents, posts.get(0).contents);
        assertEquals(post2.id, posts.get(1).id);
        assertEquals(post2.contents, posts.get(1).contents);
    }
 
    /**
     * Verify users retrieved by "users_by_post" named query
     * @param usersByPost1 List&lt;User&gt; retrieved for post1
     * @param usersByPost2 List&lt;User&gt; retrieved for post2
     */
    public void verifyUsersByPost(List<User> usersByPost1, List<User> usersByPost2)
    {
        // Examine all of the users that have a post.
        // post1 should only have 1 corresponding user
        assertEquals(1, usersByPost1.size());
        assertEquals(user1.id, usersByPost1.get(0).id);

        // post2 should have 2 corresponding users
        assertEquals(2, usersByPost2.size());
        assertEquals(user1.id, usersByPost2.get(0).id);
        assertEquals(user1.name, usersByPost2.get(0).name);
        assertEquals(user2.id, usersByPost2.get(1).id);
        assertEquals(user2.name, usersByPost2.get(1).name);
    }
 
    protected void runApplication()
    {
        try
        {
            setUp();
            User user1 = getUser1();
            User user2 = getUser2();
            Post post1 = getPost1();
            Post post2 = getPost2();
            PostsByUserEntityTask postsByUserEntityTask = new PostsByUserEntityTask(
                    user1.id,
                    user2.id,
                    post1.id,
                    post2.id);
            execute(postsByUserEntityTask);
            List<Post> posts = postsByUserEntityTask.getPosts();
            verifyPostsByUser(posts);
            System.out.println("PostsByUser: ");
            System.out.println(user1.name + " posted \"" + posts.get(0).contents + "\" & \"" + posts.get(1).contents + "\"");
            UsersByPostTask usersByPostTask= new UsersByPostTask(
                    user1.id,
                    user2.id,
                    post1.id,
                    post2.id);
            execute(usersByPostTask);
            verifyUsersByPost(usersByPostTask.getUsersByPost1(), usersByPostTask.getUsersByPost2());
            System.out.println("UsersByPosts: ");
            System.out.println("Only " + user1.name + " posted \"" + post1.contents + "\"");
            System.out.println("Both " + user1.name + " and " + user2.name +
                    " posted \"" + post2.contents + "\"");
            System.out.println("Test completed successfully");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    

}
