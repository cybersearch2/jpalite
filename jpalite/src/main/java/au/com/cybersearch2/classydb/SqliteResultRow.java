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
package au.com.cybersearch2.classydb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.j256.ormlite.support.DatabaseResults;

import au.com.cybersearch2.classyjpa.query.ResultRow;
import com.j256.ormlite.logger.Logger;
import au.com.cybersearch2.classylog.LogManager;

/**
 * SqliteResultRow Converts SQLite DatabaseResults to generic ResultRow
 * 
 * @author Andrew Bowley 30/07/2014
 */
public class SqliteResultRow implements ResultRow {
	private static Logger logger = LogManager.getLogger(SqliteResultRow.class);

	protected final int position;
	protected final DatabaseResults results;

	/**
	 * Construct a SqliteResultRow object
	 * 
	 * @param position Current position of the cursor in the row set
	 * @param results  DatabaseResults object
	 */
	public SqliteResultRow(int position, DatabaseResults results) {
		this.position = position;
		this.results = results;
	}

	/**
	 * Returns the current position of the cursor in the row set. The value is
	 * zero-based. When the row set is first returned the cursor will be at position
	 * -1, which is before the first row. After the last row is returned another
	 * call to next() will leave the cursor past the last entry, at a position of
	 * count().
	 *
	 * @return The current cursor position.
	 */
	@Override
	public int getPosition() {
		return position;
	}

	/**
	 * Returns the zero-based index for the given column name, or -1 if the column
	 * doesn't exist.
	 *
	 * @param columnName The name of the target column.
	 * @return The zero-based column index for the given column name, or -1 if the
	 *         column name does not exist.
	 */
	@Override
	public int getColumnIndex(String columnName) {
		FunctionSpec functionSpec = new FunctionSpec(op.getColumnIndex);
		functionSpec.setColumnName(columnName);
		return (Integer) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns the zero-based index for the given column name, or throws
	 * {@link IllegalArgumentException} if the column doesn't exist. If you're not
	 * sure if a column will exist or not use {@link #getColumnIndex(String)} and
	 * check for -1, which is more efficient than catching the exceptions.
	 *
	 * @param columnIndex The zero-based column index
	 * @return The column name
	 * @see #getColumnIndex(String)
	 * @throws IllegalArgumentException if the column does not exist
	 */
	@Override
	public String getColumnName(int columnIndex) {
		String[] columnNames = getColumnNames();
		if ((columnIndex >= columnNames.length) || (columnIndex < 0))
			throw new IllegalArgumentException("Column " + columnIndex + " invalid");
		return columnNames[columnIndex];
	}

	/**
	 * Returns a string array holding the names of all of the columns in the result
	 * set in the order in which they were listed in the result.
	 *
	 * @return The names of the columns returned in this query.
	 */
	@Override
	public String[] getColumnNames() {
		FunctionSpec functionSpec = new FunctionSpec(op.getColumnNames);
		return (String[]) sqlFunction(functionSpec, results);
	}

	/**
	 * Return total number of columns
	 * 
	 * @return number of columns
	 */
	@Override
	public int getColumnCount() {
		FunctionSpec functionSpec = new FunctionSpec(op.getColumnCount);
		return (Integer) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns the value of the requested column as a byte array.
	 *
	 * <p>
	 * The result and whether this method throws an exception when the column value
	 * is null or the column type is not a blob type is implementation-defined.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return The value of that column as a byte array.
	 */
	@Override
	public byte[] getBlob(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.getBlob);
		functionSpec.setColumnIndex(columnIndex);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		InputStream inStream = (InputStream) sqlFunction(functionSpec, results);
		int nRead;
		byte[] data = new byte[16384];
		boolean success = false;
		try {
			while ((nRead = inStream.read(data, 0, data.length)) != -1) {
				outStream.write(data, 0, nRead);
			}
			outStream.flush();
			success = true;
			inStream.close();
		} catch (IOException e) {
			logger.error("BlobStream error", e);
		}

		return success ? outStream.toByteArray() : new byte[] {};
	}

	/**
	 * Returns the value of the requested column as a String.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return The value of that column as a String.
	 */
	@Override
	public String getString(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.getString);
		functionSpec.setColumnIndex(columnIndex);
		return (String) sqlFunction(functionSpec, results);
	}

	/**
	 * Retrieves the requested column text and stores it in the buffer provided. If
	 * the buffer size is not sufficient, a new char buffer will be allocated and
	 * assigned to CharArrayBuffer.data
	 * 
	 * @param columnIndex The zero-based index of the target column. if the target
	 *                    column is null, return buffer
	 * @param buffer      the buffer to copy the text into.
	 */
	@Override
	public void copyStringToBuffer(int columnIndex, StringBuffer buffer) {
		buffer.append(getString(columnIndex));
	}

	/**
	 * Returns the value of the requested column as a short.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return The value of that column as a short.
	 */
	@Override
	public short getShort(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.getShort);
		functionSpec.setColumnIndex(columnIndex);
		return (Short) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns the value of the requested column as an int.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return The value of that column as an int.
	 */
	@Override
	public int getInt(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.getInt);
		functionSpec.setColumnIndex(columnIndex);
		return (Integer) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns the value of the requested column as a long.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return The value of that column as a long.
	 */
	@Override
	public long getLong(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.getLong);
		functionSpec.setColumnIndex(columnIndex);
		return (Long) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns the value of the requested column as a float.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return The value of that column as a float.
	 */
	@Override
	public float getFloat(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.getFloat);
		functionSpec.setColumnIndex(columnIndex);
		return (Float) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns the value of the requested column as a double.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return The value of that column as a double.
	 */
	@Override
	public double getDouble(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.getDouble);
		functionSpec.setColumnIndex(columnIndex);
		return (Double) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns <code>true</code> if the last object returned with the column index
	 * is null.
	 *
	 * @param columnIndex The zero-based index of the target column.
	 * @return Whether the column value is null.
	 */
	@Override
	public boolean isNull(int columnIndex) {
		FunctionSpec functionSpec = new FunctionSpec(op.isNull);
		functionSpec.setColumnIndex(columnIndex);
		return (Boolean) sqlFunction(functionSpec, results);
	}

	/**
	 * Returns Object from Database results according to type of operation specified
	 * 
	 * @param functionSpec FunctionSpec indicating type of operation and related
	 *                     parameters
	 * @param results      Open DatabaseResults object
	 * @return Object
	 */
	protected static Object sqlFunction(FunctionSpec functionSpec, DatabaseResults results) {
		try {
			switch (functionSpec.getOperation()) {
			case getColumnIndex:
				return Integer.valueOf(results.findColumn(functionSpec.getColumnName()));
			case getColumnNames:
				return results.getColumnNames();
			case getColumnCount:
				return Integer.valueOf(results.getColumnCount());
			case getBlob:
				return results.getBlobStream(functionSpec.getColumnIndex());
			case getString:
				return results.getString(functionSpec.getColumnIndex());
			case getShort:
				return Short.valueOf(results.getShort(functionSpec.getColumnIndex()));
			case getInt:
				return Integer.valueOf(results.getInt(functionSpec.getColumnIndex()));
			case getLong:
				return Long.valueOf(results.getLong(functionSpec.getColumnIndex()));
			case getFloat:
				return results.getFloat(functionSpec.getColumnIndex());
			case getDouble:
				return results.getDouble(functionSpec.getColumnIndex());
			case isNull:
				return Boolean.valueOf(results.wasNull(functionSpec.getColumnIndex()));
			default:
				throw new IllegalArgumentException(functionSpec.getOperation() + " not allowed for sqlFunction()");
			}
		} catch (SQLException e) {
			if (functionSpec.getOperation() == op.getColumnIndex)
				return Integer.valueOf(-1);
			throw new PersistenceException(functionSpec.getOperation() + " failed", e);
		}
	}
}
