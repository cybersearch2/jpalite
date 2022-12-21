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
package au.com.cybersearch2.classyjpa;

import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Interface implemented by the persistence provider.
 * 
 * It is invoked by the container in Java EE environments and by the PersistenceUnitAdmin class in Java SE environments to create an
 * EntityManagerFactory.
 */
public interface PersistenceProvider 
{
	/**
	 * Called by the container when an EntityManagerFactory is to be created.
	 * 
	 * @param info
	 *            metadata for use by the persistence provider
	 * @param map
	 *            a Map of integration-level properties for use by the persistence provider (may be null if no properties are specified). If
	 *            a Bean Validation provider is present in the classpath, the container must pass the ValidatorFactory instance in the map
	 *            with the key "javax.persistence.validation.factory". If the containing archive is a bean archive, the container must pass
	 *            the BeanManager instance in the map with the key "javax.persistence.bean.manager".
	 * @return EntityManagerFactory for the persistence unit specified by the metadata
	 */
	public EntityManagerLiteFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map<?, ?> map);

	/**
	 * Called by PersistenceUnitAdmin class when an EntityManagerFactory is to be created.
	 * 
	 * @param emName
	 *            the name of the persistence unit
	 * @param map
	 *            a Map of properties for use by the persistence provider. These properties may be used to override the values of the
	 *            corresponding elements in the persistence.xml file or specify values for properties not specified in the persistence.xml
	 *            (and may be null if no properties are specified).
	 * @return EntityManagerFactory for the persistence unit, or null if the provider is not the right provider
	 */
	public EntityManagerLiteFactory createEntityManagerFactory(String emName, Map<?, ?> map);

}
