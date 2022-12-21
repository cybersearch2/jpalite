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
package au.com.cybersearch2.classyjpa.query;

/**
 * ResultRow
 * Subset of Cursor interface to use for row mapping
 * @author Andrew Bowley
 * 30/05/2014
 */
public interface ResultRow
{
    /**
     * op
     * Enumeration of operation to perform on query result row
     * @author Andrew Bowley
     * 30/07/2014
     */
    public enum op
    {
        getColumnIndex,
        getColumnName,
        getColumnNames,
        getColumnCount,
        getBlob,
        getString,
        copyStringToBuffer,
        getShort,
        getInt,
        getLong,
        getFloat,
        getDouble,
        isNull
    }
    /**
     * FunctionSpec
     * Indicates type of operation and related parameters
     * @author Andrew Bowley
     * 30/07/2014
     */
    public class FunctionSpec
    {
        public FunctionSpec(op operation)
        {
            this.operation = operation;
        }
        
        op operation;
        int columnIndex;
        String columnName;
        StringBuffer buffer;
        
        public op getOperation() {
            return operation;
        }
        public int getColumnIndex() {
            return columnIndex;
        }
        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public StringBuffer getBuffer() {
            return buffer;
        }
        public void setBuffer(StringBuffer buffer) {
            this.buffer = buffer;
        }
    }
    
    /**
     * Returns the current position of the cursor in the row set.
     * The value is zero-based. When the row set is first returned the cursor
     * will be at positon -1, which is before the first row. After the
     * last row is returned another call to next() will leave the cursor past
     * the last entry, at a position of count().
     *
     * @return The current cursor position.
     */
    int getPosition();

    /**
     * Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
     *
     * @param columnName the name of the target column.
     * @return The zero-based column index for the given column name, or -1 if
     * the column name does not exist.
     */
    int getColumnIndex(String columnName);

    /**
     * Returns the column name at the given zero-based column index.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The column name for the given column index.
     */
    String getColumnName(int columnIndex);

    /**
     * Returns a string array holding the names of all of the columns in the
     * result set in the order in which they were listed in the result.
     *
     * @return The names of the columns returned in this query.
     */
    String[] getColumnNames();

    /**
     * Return total number of columns
     * @return number of columns 
     */
    int getColumnCount();
    
    /**
     * Returns the value of the requested column as a byte array.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null or the column type is not a blob type is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The value of that column as a byte array.
     */
    byte[] getBlob(int columnIndex);

    /**
     * Returns the value of the requested column as a String.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null or the column type is not a string type is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The value of that column as a String.
     */
    String getString(int columnIndex);
    
    /**
     * Retrieves the requested column text and stores it in the buffer provided.
     * If the buffer size is not sufficient, a new char buffer will be allocated 
     * and assigned to CharArrayBuffer.data
     * @param columnIndex the zero-based index of the target column.
     *        if the target column is null, return buffer
     * @param buffer the buffer to copy the text into. 
     */
    void copyStringToBuffer(int columnIndex, StringBuffer buffer);
    
    /**
     * Returns the value of the requested column as a short.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The value of that column as a short.
     */
    short getShort(int columnIndex);

    /**
     * Returns the value of the requested column as an int.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The value of that column as an int.
     */
    int getInt(int columnIndex);

    /**
     * Returns the value of the requested column as a long.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The value of that column as a long.
     */
    long getLong(int columnIndex);

    /**
     * Returns the value of the requested column as a float.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The value of that column as a float.
     */
    float getFloat(int columnIndex);

    /**
     * Returns the value of the requested column as a double.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return The value of that column as a double.
     */
    double getDouble(int columnIndex);

    /**
     * (Android SQLite only) Returns <code>true</code> if the value in the indicated column is null.
     * Returns <code>true</code> if the last object returned with the column index is null.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return Whether the column value is null.
     */
    boolean isNull(int columnIndex);

}
