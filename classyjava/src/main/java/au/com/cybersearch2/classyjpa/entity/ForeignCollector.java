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

public class ForeignCollector<T> {

	public List<T> getList(Collection<T> foreignCollection) {
		if (foreignCollection == null)
			return Collections.emptyList();
		if (foreignCollection instanceof ForeignCollection)
			return getOrmList((ForeignCollection<T>)foreignCollection);
		List<T> list = new ArrayList<>();
		foreignCollection.forEach(element -> list.add(element));
		return list;
	}

	public int add(T element, Collection<T> foreignCollection) {
		if (foreignCollection == null)
			throw new IllegalArgumentException("Parameter \"foreignCollection\" is null");
		if (element == null)
			throw new IllegalArgumentException("Parameter \"element\" is null");
		foreignCollection.add(element);
		return foreignCollection.size();
	}
	
	private List<T> getOrmList(ForeignCollection<T> foreignCollection) {
		List<T> list = new ArrayList<>();
		CloseableIterator<T> iterator = foreignCollection.closeableIterator();
		try {
			while (iterator.hasNext())
				list.add(iterator.next());
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
