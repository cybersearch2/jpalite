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
 * QueryInfo
 * Holds information to be used to build an SQLite query and map each result set row to an object.
 *
 * The query components are as follows (ones marked with "*" mandatory):
 * 
 * table* 
 *      The table name to compile the query against.
 * columns*
 *      A list of which columns to return.
 * selection 
 *      A filter declaring which rows to return, formatted as an
 *            SQL WHERE clause (excluding the WHERE itself). 
 *            By default, all rows for the given table are returned.
 * selectionArgs 
 *      You may include ?s in selection, which will be
 *            replaced by the values from selectionArgs, in order that they
 *            appear in the selection. The values will be bound as Strings.
 * groupBy 
 *      A filter declaring how to group rows, formatted as an SQL
 *            GROUP BY clause (excluding the GROUP BY itself).
 *            By default, rows are not grouped.
 * having 
 *      A filter declare which row groups to include in the cursor,
 *            if row grouping is being used, formatted as an SQL HAVING
 *            clause (excluding the HAVING itself). By default,
 *            all row groups will be included, and is required when row
 *            grouping is not being used.
 * orderBy 
 *      How to order the rows, formatted as an SQL ORDER BY clause
 *            (excluding the ORDER BY itself). 
 *            The default sort order may be unordered.
 * limit 
 *      Limits the number of rows returned by the query,
 *            formatted as LIMIT clause. Default is no limit.
 * parameterNames
 *      The parameter names mapped to selection arguments in same order.
 *            
 * @author Andrew Bowley
 * 30/05/2014
 */
public class QueryInfo
{
    /**
     * RowMapper
     * An interface to create an Object from a query result row
     * @author Andrew Bowley
     * 28/08/2014
     */
    public interface RowMapper
    {
        /**
         * Returns object populated from query result row
         * @param resultRow ResultRow
         * @return Object
         */
        Object mapRow(ResultRow resultRow);
    }
    
    /** Mandatory. Maps a Cursor position to an object to be returned by the query */
    protected RowMapper rowMapper;
    /** Mandatory. The table name to compile the query against. */
    protected String table;
    /** Mandatory. A list of which columns to return. */
    protected String[] columns;
    /** Optional. A filter declaring which rows to return, formatted as an
     *  SQL WHERE clause (excluding the WHERE itself). 
     *  The default is to return all rows for the given table. 
     *  You may include ?s in selection, which will be replaced by the Query setParameter() values */
    protected String selection;
    /** Optional. The parameter names mapped to selection arguments in same order. 
     *  If omitted, only set parameter by index is supported. */
    protected String[] parameterNames;
    /** Optional. The selection arguments. You may include ?s in selection, which will be
     *  replaced by the values from selectionArgs, in order that they
     *  appear in the selection. The values will be bound as Strings.  */
    protected String[] selectionArgs;
    /** Optional. A filter declaring how to group rows, formatted as an SQL
     *  GROUP BY clause (excluding the GROUP BY itself). */
    protected String groupBy;
    /** Optional. A filter declaring which row groups to include in the cursor,
     *  if row grouping is being used, formatted as an SQL HAVING
     *  clause (excluding the HAVING itself). */
    protected String having;
    /** Optional. How to order the rows, formatted as an SQL ORDER BY clause
     *  (excluding the ORDER BY itself). */
    protected String orderBy;
    /** Optional. Limits the number of rows returned by the query,
     *  formatted as either a single 'count' value or 'skip', 'count' combination. 
     *  The default sort order may be unordered */
    protected String limit;
    
    public QueryInfo(RowMapper rowMapper, String table, String... columns)
    {
        if (table == null)
            throw new IllegalArgumentException("Parameter table is null");
        this.table = table;
        if (columns == null)
            throw new IllegalArgumentException("Parameter columns is null");
        this.columns = columns;
        this.rowMapper = rowMapper;
    }

    /**
     * Returns The table name to compile the query against.
     * @return String
     */
    public String getTable() {
        return table;
    }

    /**
     * Returns a list of which columns to return
     * @return String
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * Returns a filter declaring which rows to return
     * @return String formatted as an SQL WHERE clause 
     *            (excluding the WHERE itself)
     */
    public String getSelection() {
        return selection;
    }

    /**
     * Sets a filter declaring which rows to return 
     * @param selection String formatted as an SQL WHERE clause 
     *                    (excluding the WHERE itself)
     */
    public void setSelection(String selection) {
        this.selection = selection;
    }

    /**
     * Get the parameter names mapped to selection arguments in same order
     * @return String[]
     */
    public String[] getParameterNames() {
        return parameterNames;
    }

    /**
     * Set the parameter names mapped to selection arguments in same order
     * @param parameterNames String[]
     */
    public void setParameterNames(String[] parameterNames) {
        this.parameterNames = parameterNames;
    }

    /**
     * Returns a filter declaring how to group rows, formatted as an SQL GROUP BY clause
     * @return String formatted as an SQL HAVING clause 
     *            (excluding the HAVING itself) 
     */
    public String getGroupBy() {
        return groupBy;
    }

    /**
     * Sets a filter declaring how to group rows, formatted as an SQL GROUP BY clause
     * @param groupBy String formatted as an SQL HAVING clause 
     *                   (excluding the HAVING itself)
     */
    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * Returns a filter declaring which row groups to include in the cursor
     * @return String formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself)
     */
    public String getHaving() {
        return having;
    }

    /**
     * Set a filter declaring which row groups to include in the cursor 
     * @param having String formatted as an SQL ORDER BY clause
     *                 (excluding the ORDER BY itself)
     */
    public void setHaving(String having) {
        this.having = having;
    }

    /**
     * Returns how to order the rows
     * @return String formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). The
     *            default sort order may be unordered.
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Set how to order the rows
     * @param orderBy String formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Default null will use the
     *            default sort order, which may be unordered.
     */
    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * Returns a limit clause for the number of rows returned by the query
     * @return String formatted as either a single 'count' value or 'skip', 'count' combination
     */
    public String getLimit() {
        return limit;
    }

    /**
     * Set a limit clause for the number of rows returned by the query
     * @param limit String formatted as either a single 'count' value or 'skip', 'count' combination
     */
    public void setLimit(String limit) {
        this.limit = limit;
    }

    /**
     * Returns agent which maps a query result row to an object
     * @return RowMapper
     */
    public RowMapper getRowMapper() {
        return rowMapper;
    }

    /**
     * Returns the selection arguments
     * @return String[]
     */
    public String[] getSelectionArgs() 
    {
        return selectionArgs == null ? new String[]{} : selectionArgs;
    }

    /**
     * Sets the selection arguments. You may include ?s in selection, which will be
     *  replaced by the values from selectionArgs, in order that they
     *  appear in the selection. The values will be bound as Strings.
     * @param selectionArgs String[]
     */
    public void setSelectionArgs(String[] selectionArgs) 
    {
        this.selectionArgs = selectionArgs;
    }

}
