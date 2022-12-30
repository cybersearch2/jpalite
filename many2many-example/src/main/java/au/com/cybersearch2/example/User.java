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

import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.entity.TableJoiner;

/**
 * A user object with a name.
 */
@Entity(name="tableUser")
public class User implements OrmEntity {

	public final static String ID_FIELD_NAME = "id";

	/** Wraps OrmLite ForeignCollection set by Jpalite on the posts field at start up */
	private final TableJoiner<Post, UserPost> tableJoiner;
	
    @Id @GeneratedValue
	int id;

	@Column
	String name;

	@OneToMany(fetch=FetchType.LAZY)
    Collection<UserPost> userPosts;
 
	/**
	 * Construct User object
	 * @param name String
	 */
	public User(String name) 
	{
		this();
		this.name = name;
	}

    /**
     * User default constructor for Ormlite
     */
	User() {
		tableJoiner = new TableJoiner<>() {

			@Override
			public Post fromJoin(UserPost joinRecord) {
				return joinRecord.post;
			}

			@Override
			public UserPost toJoin(Post tableRecord) {
				return new UserPost(User.this, tableRecord);
			}};
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	/**
	 * Returns list of posts owned by this user
	 * @return Post list
	 */
	public List<Post> getPosts() {
		return tableJoiner.getList(userPosts);
	}

	/**
	 * Add given post to this user
	 * @param post Post object
	 * @return resulting number of posts own by this user
	 */
	public int addPost(Post post) {
		return tableJoiner.add(post, userPosts);
	}
}
