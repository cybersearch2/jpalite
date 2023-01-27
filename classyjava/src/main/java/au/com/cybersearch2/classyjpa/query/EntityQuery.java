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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import com.j256.ormlite.stmt.SelectArg;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;

import com.j256.ormlite.logger.Logger;
import au.com.cybersearch2.classylog.LogManager;

/**
 * EntityQuery Implements javax.persistence.Query using OrmLite query. Only a
 * subset of PersistenceUnitAdmin API 1.0 methods supported.
 * 
 * @author Andrew Bowley 13/05/2014
 */
public class EntityQuery<T extends OrmEntity> extends QueryBase<T> {
	private static Logger logger = LogManager.getLogger(EntityQuery.class);

	/** OrmLite query for generic entity class */
	private final DaoQuery<T> daoQuery;

	public EntityQuery(DaoQuery<T> daoQuery) {
		this.daoQuery = daoQuery;
	}

	/**
	 * Execute an update or delete statement. NOT implemented.
	 * 
	 * @return 0
	 */
	@Override
	public int executeUpdate() {
		release();
		return 0; // Updates and deletes are currently not supported.
	}

	/**
	 * Execute a SELECT query and return the query results as a List.
	 * 
	 * @return a list of the results
	 */
	@Override
	public List<T> getResultList() {
		if (isClosed) // Only perform query once
			return new ArrayList<T>();
		try {
			return daoQuery.getResultList(startPosition, maxResults);
		} finally {
			release();
		}
	}

	/**
	 * Execute a SELECT query that returns a single result.
	 * 
	 * @return The result
	 * @throws NoResultException if there is no result
	 */
	@Override
	public T getSingleResult() {
		T result = null;
		if (isClosed) // Only perform query once
			throw new NoResultException("getSingleResult() called when query already executed");
		try {
			result = daoQuery.getSingleResult();
		} catch (PersistenceException e) {
			String detail = e.getCause() == null ? e.toString() : e.getCause().toString();
			String message = "Named query error: " + detail;
			logger.error(message, e);
			throw new NoResultException(message);
		} finally {
			release();
		}
		if (result == null)
			throw new NoResultException("getSingleResult() query returned null");
		return result;
	}

	/**
	 * Bind an argument to a named parameter.
	 * 
	 * @param param The parameter name
	 * @param value Object
	 * @return The same query instance
	 * @throws IllegalArgumentException if parameter name does not correspond to
	 *                                  parameter in query string
	 */
	@Override
	public TypedQuery<T> setParameter(String param, Object value) {
		SelectArg selectArg = validateParam(param);
		if (selectArg != null)
			selectArg.setValue(value);
		else
			throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
		return this;
	}

	/**
	 * Bind an argument to a positional parameter.
	 * 
	 * @param position Starts at 1
	 * @param value    Object
	 * @return The same query instance
	 * @throws IllegalArgumentException if position does not correspond to
	 *                                  positional parameter of query
	 */
	@Override
	public TypedQuery<T> setParameter(int position, Object value) {
		if (daoQuery.isValidPosition(position)) {
			SelectArg selectArg = daoQuery.get(position);
			selectArg.setValue(value);
		} else
			logInvalidPosition(position);
		return this;
	}

	/**
	 * Bind an instance of java.util.Date to a named parameter.
	 * 
	 * @param param The parameter name
	 * @param value Date
	 * @param type  Not used
	 * @return The same query instance
	 * @throws IllegalArgumentException if parameter name does not correspond to
	 *                                  parameter in query string
	 */
	@Override
	public TypedQuery<T> setParameter(String param, Date value, TemporalType type) {
		SelectArg selectArg = validateParam(param);
		if (selectArg != null)
			selectArg.setValue(value);
		else
			throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
		return this;
	}

	/**
	 * Bind an instance of java.util.Calendar to a named parameter.
	 * 
	 * @param param The parameter name
	 * @param value Calendar
	 * @param type  Not used
	 * @return The same query instance
	 * @throws IllegalArgumentException if parameter name does not correspond to
	 *                                  parameter in query string
	 */
	@Override
	public TypedQuery<T> setParameter(String param, Calendar value, TemporalType type) {
		SelectArg selectArg = validateParam(param);
		if (selectArg != null)
			selectArg.setValue(value.getTime());
		else
			throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
		return this;
	}

	/**
	 * Bind an instance of java.util.Date to a positional parameter.
	 * 
	 * @param position Starts at 1
	 * @param value    Date
	 * @param type     Not used
	 * @return The same query instance
	 * @throws IllegalArgumentException if position does not correspond to
	 *                                  positional parameter of query
	 */
	@Override
	public TypedQuery<T> setParameter(int position, Date value, TemporalType type) {
		if (daoQuery.isValidPosition(position)) {
			SelectArg selectArg = daoQuery.get(position);
			selectArg.setValue(value);
		} else
			logInvalidPosition(position);
		return this;
	}

	/**
	 * Bind an instance of java.util.Calendar to a positional parameter.
	 * 
	 * @param position Starts at 1
	 * @param value    Calendar
	 * @param type     Not used
	 * @return The same query instance
	 * @throws IllegalArgumentException if position does not correspond to
	 *                                  positional parameter of query
	 */
	@Override
	public TypedQuery<T> setParameter(int position, Calendar value, TemporalType type) {
		if (daoQuery.isValidPosition(position)) {
			SelectArg selectArg = daoQuery.get(position);
			selectArg.setValue(value.getTime());
		} else
			logInvalidPosition(position);
		return this;
	}

	/**
	 * Returns selection argument for named parameter
	 * 
	 * @param param The parameter name
	 * @return SelectArg
	 */
	protected SelectArg validateParam(String param) {
		if (param == null) {
			logger.error("Null query parameter encountered for named query");
			return null;
		}
		SelectArg selectArg = daoQuery.get(param);
		if (selectArg == null)
			logger.error("Query parameter '" + param + "' not found for named query");
		return selectArg;
	}

	/**
	 * Log "position out of range" error and throw IllegalArgumentException
	 * 
	 * @param position Invalid value
	 */
	private void logInvalidPosition(int position) {
		logger.error("Query parameter " + position + " out of range for named query");
		throw new IllegalArgumentException("Parameter \"" + position + "\" is invalid");
	}

}
