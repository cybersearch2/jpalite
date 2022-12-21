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
package au.com.cybersearch2.classyjpa.persist;

import com.j256.ormlite.field.SqlType;

/**
 * Type that persists a double primitive as a bit-encoded long value
 * 
 * @author Andrew Bowley
 */
public class PrimitiveJavaDoubleType extends JavaDoubleType {

	private static final PrimitiveJavaDoubleType singleTon = new PrimitiveJavaDoubleType();

	public static PrimitiveJavaDoubleType getSingleton() {
		return singleTon;
	}

	private PrimitiveJavaDoubleType() {
		super(SqlType.LONG, new Class<?>[] { double.class });
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}
}
