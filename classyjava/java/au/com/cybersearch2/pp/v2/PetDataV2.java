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
package au.com.cybersearch2.pp.v2;

import javax.persistence.Column;
import javax.persistence.Entity;

import au.com.cybersearch2.pp.PetData;

/**
 * A simple demonstration object we are creating and persisting to the database.
 */
@Entity(name="Pets")
public class PetDataV2  extends PetData
{
	@Column 
	String quote;
	
	PetDataV2() 
	{
		super();
	}

	public PetDataV2(String name, String quote) 
	{
		super(name);
		this.quote = quote;
	}
	
	public void setQuote(String value)
	{
		quote = value;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(": \"").append(quote).append("\"");
		return sb.toString();
	}
}
