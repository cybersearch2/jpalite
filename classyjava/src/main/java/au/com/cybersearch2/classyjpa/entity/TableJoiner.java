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
package au.com.cybersearch2.classyjpa.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;

/**
 * Wraps OrmLite ForeignCollection set by Jpalite on a @OmeTpMany join table field at start up
 *
 * @param <T> Entity type of associated table
 * @param <J> Entity type of join table
 */
public abstract class TableJoiner<T,J> {

	/**
	 * Returns table object extracted from join table record
	 * @param joinRecord Join table row
	 * @return entity object
	 */
	public abstract T fromJoin(J joinRecord);

	/**
	 * Returns join table object created to incorporate give table entity object
	 * @param tableRecord Table entity object
	 * @return join table entity object
	 */
	public abstract J toJoin(T tableRecord);

	/**
	 * Returns list of table entity objects
	 * @param foreignCollection OrmLite foreign collection or java.util collection
	 * @return Entity object list
	 */
	public List<T> getList(Collection<J> foreignCollection) {
		if (foreignCollection == null)
			return Collections.emptyList();
		if (foreignCollection instanceof ForeignCollection)
			return getOrmList((ForeignCollection<J>)foreignCollection);
		List<T> list = new ArrayList<>();
		foreignCollection.forEach(element -> list.add(fromJoin(element)));
		return list;
	}

	/**
	 * Add given element to given foreign collection
	 * @param element Table entity object to add
	 * @param foreignCollection Foreign collection
	 * @return resulting count of items in the collection
	 */
	public int add(T element, Collection<J> foreignCollection) {
		if (foreignCollection == null)
			throw new IllegalArgumentException("Parameter \"foreignCollection\" is null");
		if (element == null)
			throw new IllegalArgumentException("Parameter \"element\" is null");
		foreignCollection.add(toJoin(element));
		return foreignCollection.size();
	}
	
	private List<T> getOrmList(ForeignCollection<J> foreignCollection) {
		List<T> list = new ArrayList<>();
		CloseableIterator<J> iterator = foreignCollection.closeableIterator();
		try {
			while (iterator.hasNext())
				list.add(fromJoin(iterator.next()));
		} finally {
			// must always close our iterators otherwise connections to the database are held open
			try {
				iterator.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
