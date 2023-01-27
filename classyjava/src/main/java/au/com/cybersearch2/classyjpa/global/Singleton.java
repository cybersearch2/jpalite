/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.classyjpa.global;

import java.lang.reflect.InvocationTargetException;

import au.com.cybersearch2.classylog.LogManager;

public enum Singleton {

	log_manager(LogManager.class);

	private final Object object;
	private final Class<?> myClass;
	
	private Singleton(Class<?> clazz) {
		myClass = clazz;
		try {
			object = clazz.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new JpaliteException(String.format("Error constructing singleton class %s",clazz), e);
		}
	}

	public Object getObject() {
		return object;
	}

	public Class<?> getMyClass() {
		return myClass;
	}
}
