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

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongObjectType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a Double object using long bits
 * 
 * @author Andrew Bowley
 */

public class JavaDoubleType extends LongObjectType {

	private static final JavaDoubleType singleTon = new JavaDoubleType();

	public static JavaDoubleType getSingleton() {
		return singleTon;
	}

	private JavaDoubleType() {
		super(SqlType.LONG, new Class<?>[] { Double.class });
	}

	protected JavaDoubleType(SqlType sqlType, Class<?>[] classes) {
		super(SqlType.LONG, classes);
	}

	/**
	 * @throws SQLException
	 *             If there are problems with the conversion.
	 */
	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
		return Double.doubleToLongBits((Double)javaObject);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return Double.longBitsToDouble(Long.parseLong(defaultStr));
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return Double.longBitsToDouble(((Long) results.getLong(columnPos)));
	}

	@Override
	public Object convertIdNumber(Number number) {
		// by default the type cannot convert an id number
		return null;
	}

	@Override
	public boolean isValidGeneratedType() {
		return false;
	}

	@Override
	public boolean isValidForVersion() {
		return false;
	}

	/**
	 * Move the current-value to the next value. Used for the version field.
	 */
	@Override
	public Object moveToNextValue(Object currentValue) {
		return null;
	}
}
