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
// License for OrmLite Many to Many Example in test package 
// com.j256.ormlite.jdbc.examples.manytomany
/*
ISC License (https://opensource.org/licenses/ISC)

Copyright 2019, Gray Watson

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. */
package au.com.cybersearch2.example;

import java.util.List;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.container.JpaContainer;
import au.com.cybersearch2.container.JpaProcess;
import au.com.cybersearch2.container.WorkStatus;

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
 * JUPALITE COMMENTS:
 * </p>
 * <p>
 * This example shows how a many-to-many table relationship with a join table can be performed in lightweight JPA.
 * OrmLite does not support the use of @ManyToMany or @JoinTable annotations so a workaround is to have back to back
 * OneToMany associations with the join table in the middle.
 * </p>
 */
public class ManyToManyMain 
{
    static public final String POSTS_BY_USER = "posts_by_user";
    static public final String USERS_BY_POST = "users_by_post";
    static public final String PU_NAME = "manytomany";

    private final boolean quietMode;
    
	private JpaContainer jpaContainer;

    User user1;
    User user2;
    Post post1;
    Post post2;

    public ManyToManyMain() {
    	quietMode = false;
    }
    
    public ManyToManyMain(boolean quietMode) {
    	this.quietMode = quietMode;
    }
    
    /**
     * Test ManyToMany association
     * @param args Not used
     */
	public static void main(String[] args) {
		ManyToManyMain manyToManyMain = null;
		int returnCode = 0;
     	try {
     		manyToManyMain = new ManyToManyMain();
     		if (manyToManyMain.setUp()) {
     		   if (!manyToManyMain.runApplication(new Transcript()))
     			   returnCode = 1;
     		}
     	} catch (Throwable t) {
     		t.printStackTrace();
     	} finally {
     		try {
     			manyToManyMain.close();
     		} catch (InterruptedException e) {}
      		System.exit(returnCode);
     	}
	}
	
    /**
     * Populate entity tables. Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException if interrupted
     */
    public boolean setUp() throws InterruptedException {
		try {
			jpaContainer = new JpaContainer();
			jpaContainer.initialize();
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
        // PersistenceUnitAdmin work adds 2 users and 2 posts to the database using JPA.
        // Hence there will be an enclosing transaction to ensure data consistency.
        // Any failure will result in an IllegalStateExeception being thrown from
        // the calling thread.
        PersistenceWork setUpWork = new PersistenceWork() {

            @Override
            public void doTask(EntityManagerLite entityManager) {
                // create our 1st user
                user1 = new User("Jim Coakley)");
                entityManager.persist(user1);
                // have user1 post something
                post1 = new Post("Wow is it cold outside!!");
                entityManager.persist(post1);
                user1.addPost(post1);
                // have user1 post a second post
                post2 = new Post("Now it's a bit warmer thank goodness.");
                entityManager.persist(post2);
                user1.addPost(post2);
                // create another user
                user2 = new User("Gandalf Gray");
                entityManager.persist(user2);
                // have the 2nd user also say the 2nd post
                user2.addPost(post2);
                // Database updates committed upon exit
            }

            @Override
            public void onPostExecute(boolean success) {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException) {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
    	JpaProcess process = jpaContainer.execute(setUpWork);
		return process.exitValue() == WorkStatus.FINISHED;
    }

    public void close() throws InterruptedException {
    	jpaContainer.close();
    }

    /**
     * Returns Jim Coakley's User
     * @return User
     */
    public User getUser1() {
        return user1;
    }

    /**
     * Returns Gandalf Gray's User
     * @return User
     */
    public User getUser2() {
        return user2;
    }

    /**
     * Returns "Wow is it cold outside!" Post
     * @return Post
     */
    public Post getPost1() {
        return post1;
    }

    /**
     * Returns "Now it's a bit warmer thank goodness." Post
     * @return Post
     */
    public Post getPost2() {
        return post2;
    }
 
    /**
     * Verify posts retrieved by "posts_by_user" named query
     * @param posts List&lt;Post&gt; retrieved for user1
     */
    public void verifyPostsByUser(List<Post> posts, Transcript transcript) {
        // user1 should have 2 posts
    	boolean status = posts.size() == 2;
    	transcript.add("2 posts by user = " + status, status);
    	status = post1.id == posts.get(0).id;
    	transcript.add("First post id of " + posts.get(0).id + " = " + status, status);
    	status = post1.contents.equals(posts.get(0).contents);
    	transcript.add("First post contents match = " + status, status);
       	status = post2.id == posts.get(1).id;
    	transcript.add("Second post id of " + posts.get(1).id + " = " + status, status);
    	status = post2.contents.equals(posts.get(1).contents);
    	transcript.add("Second post contents match = " + status, status);
    }
 
    /**
     * Verify users retrieved by "users_by_post" named query
     * @param usersByPost1 List&lt;User&gt; retrieved for post1
     * @param usersByPost2 List&lt;User&gt; retrieved for post2
     */
    public void verifyUsersByPost(List<User> usersByPost1, List<User> usersByPost2, Transcript transcript) {
        // Examine all of the users that have a post.
        // post1 should only have 1 corresponding user
        boolean status = usersByPost1.size() == 1;
        transcript.add("One user for post1 = " + status, status);
        status = user1.id ==  usersByPost1.get(0).id;
        transcript.add("User1 ids match = " + status, status);

        // post2 should have 2 corresponding users
        status = usersByPost2.size() == 2;
        transcript.add("Two users for post2 = " + status, status);
        status = user1.id ==  usersByPost2.get(0).id;
        transcript.add("User1 ids match = " + status, status);
        status = user1.name.equals(usersByPost2.get(0).name);
        transcript.add("User1 namess match = " + status, status);
        status = user2.id ==  usersByPost2.get(1).id;
        transcript.add("User1 ids match = " + status, status);
        status = user2.name.equals(usersByPost2.get(1).name);
        transcript.add("User2 namess match = " + status, status);
    }
 
    protected boolean runApplication(Transcript transcript) {
        User user1 = getUser1();
        User user2 = getUser2();
        Post post1 = getPost1();
        Post post2 = getPost2();
        PostsByUserEntityTask postsByUserEntityTask = new PostsByUserEntityTask(
                user1.id);
        JpaProcess process = jpaContainer.execute(postsByUserEntityTask);
        if (process.exitValue() != WorkStatus.FINISHED)
        	return false;
        List<Post> posts = postsByUserEntityTask.getPosts();
        verifyPostsByUser(posts, transcript);
        if (!quietMode) {
        	System.out.println("PostsByUser: ");
        	System.out.println(user1.name + " posted \"" + posts.get(0).contents + "\" & \"" + posts.get(1).contents + "\"");
        }
        UsersByPostTask usersByPostTask= new UsersByPostTask(
                post1.id,
                post2.id);
        process = jpaContainer.execute(usersByPostTask);
        if (process.exitValue() != WorkStatus.FINISHED)
        	return false;

        verifyUsersByPost(usersByPostTask.getUsersByPost1(), usersByPostTask.getUsersByPost2(), transcript);
 		transcript.getObservations().forEach(entry -> { 
 			if (entry.isStatus())
 				System.out.println(entry.getReport());
 			else
 				System.err.println(entry.getReport());
 		});
 		if (transcript.getErrorCount() == 0) {
 			if (!quietMode) {
	            System.out.println("UsersByPosts: ");
	            System.out.println("Only " + user1.name + " posted \"" + post1.contents + "\"");
	            System.out.println("Both " + user1.name + " and " + user2.name +
	                    " posted \"" + post2.contents + "\"");
	 			System.out.println("Success");
 			}
 			return true;
 		} else {
 			System.err.println("Failed");
 		}
		return false;
    }

}
