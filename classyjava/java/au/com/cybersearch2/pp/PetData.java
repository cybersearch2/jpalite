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
package au.com.cybersearch2.pp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import au.com.cybersearch2.pp.api.Pet;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
@Entity(name="Pets")
public class PetData implements Pet 
{

    @Id @GeneratedValue
	int id;

    @Column
    String name;
    
	@Column
	long timestamp;
	
	public PetData() 
	{
		// needed by ormlite
	}

	public PetData(String name) 
	{
		this.name = name;
		this.timestamp = System.currentTimeMillis() / 1000;
	}

	@Override
	public long getTimestamp() 
	{
		return timestamp;
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public int getId() 
	{
		return id;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder(name);
		sb.append(": id=").append(id);
		return sb.toString();
	}
}
