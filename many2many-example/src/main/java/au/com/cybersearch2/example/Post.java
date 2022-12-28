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
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;


/**
 * Post to some blog with String content.
 */
@Entity(name="tablePost")
public class Post implements OrmEntity
{
	public final static String ID_FIELD_NAME = "id";

    @Id @GeneratedValue
 	int id;

	@Column
	String contents;

	@OneToMany(fetch=FetchType.LAZY)
    Collection<UserPost> userPosts;
 
   /**
     * Post default constructor for Ormlite
     */
	Post() {
	}

	/**
	 * Create Post object
	 * @param contents String
	 */
	public Post(String contents) {
		this.contents = contents;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContents() {
		return contents;
	}

	public List<User> getUsers() {
		List<User> userList = new ArrayList<>();
		userPosts.forEach(userPost -> userList.add(userPost.user));
		return userList;
	}

}

