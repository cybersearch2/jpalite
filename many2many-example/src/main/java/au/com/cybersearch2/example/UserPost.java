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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;

/**
 * Join table which links users to their posts.
 * 
 * <p>
 * For more information about foreign objects, see the <a href="http://ormlite.com/docs/foreign" >online docs</a>
 * </p>
 */
@Entity(name="tableUserPost")
public class UserPost implements OrmEntity
{
	/**
	 * This id is generated by the database and set on the object when it is passed to the create method. An id is
	 * needed in case we need to update or delete this object in the future.
	 */
    @Id @GeneratedValue
	int id;

    // Column name of this foreign field defaults to "user_id
    @OneToOne
	User user;

    // Column name of this foreign field defaults to "post_id
    @OneToOne
 	Post post;

    /**
     * UserPost default constructor for ormlite
     */
	UserPost() {
	}

	/**
	 * Create UserPost object
	 * @param user User
	 * @param post Post
	 */
	public UserPost(User user, Post post) {
		this.user = user;
		this.post = post;
	}
}
