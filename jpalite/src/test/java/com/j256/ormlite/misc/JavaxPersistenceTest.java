/**
ISC License (https://opensource.org/licenses/ISC)

Copyright 2021, Gray Watson

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. */
package com.j256.ormlite.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.EnumIntegerType;
import com.j256.ormlite.field.types.EnumStringType;
import com.j256.ormlite.h2.H2DatabaseType;

public class JavaxPersistenceTest extends BaseCoreTest {

	@Test
	public void testConversions() throws Exception {
		for (Field field : Javax.class.getDeclaredFields()) {
			DatabaseFieldConfig config = new JavaxPersistenceImpl().createFieldConfig(databaseType, field);
			if (field.getName().equals("generatedId")) {
				assertFalse(config.isId());
				assertTrue(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("id")) {
				assertTrue(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("stuff")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertEquals(field.getName(), config.getFieldName());
				assertEquals(Javax.STUFF_FIELD_NAME, config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("unknown")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("foreignManyToOne")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isForeignCollection());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("foreignOneToOne")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isForeignCollection());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("foreignOneToMany")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertTrue(config.isForeignCollection());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getForeignCollectionForeignFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("mappedByField")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertTrue(config.isForeignCollection());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertEquals(Javax.MAPPED_BY_FIELD_NAME, config.getForeignCollectionForeignFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("joinFieldName")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isForeignCollection());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertEquals(Javax.JOIN_FIELD_NAME, config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("columnDefinition")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isVersion());
				assertTrue(config.isCanBeNull());
				assertEquals(Javax.COLUMN_DEFINITION, config.getColumnDefinition());
			} else if (field.getName().equals("uniqueColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertTrue(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("nullableColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("uniqueJoinColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isForeignCollection());
				assertTrue(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("nullableJoinColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isForeignCollection());
				assertFalse(config.isUnique());
				assertFalse(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("ourEnumOrdinal")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isVersion());
				assertTrue(config.isCanBeNull());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
				assertTrue(config.getDataPersister() instanceof EnumIntegerType);
			} else if (field.getName().equals("ourEnumString")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isVersion());
				assertTrue(config.isCanBeNull());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
				assertTrue(config.getDataPersister() instanceof EnumStringType);
			} else if (field.getName().equals("version")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertTrue(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("basic")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("basicNotOptional")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} //else {
			//	System.err.println("\n\n\nUnknown field: " + field.getName());
			//}
		}
	}

	@Test
	public void testTableName() {
		JavaxPersistenceConfigurer configurer = new JavaxPersistenceImpl();
		assertEquals(Javax.JAVAX_ENTITY_NAME, configurer.getEntityName(Javax.class));
		assertNull(configurer.getEntityName(EntityNoName.class));
	}

	@Test
	public void testUpperCaseFieldNames() throws Exception {
		UpperCaseFieldDatabaseType ucDatabaseType = new UpperCaseFieldDatabaseType();
		for (Field field : Javax.class.getDeclaredFields()) {
			DatabaseFieldConfig config = new JavaxPersistenceImpl().createFieldConfig(ucDatabaseType, field);
			if (field.getName().equals("id")) {
				assertTrue(config.isId());
				assertFalse(config.isGeneratedId());
				assertEquals("ID", config.getFieldName());
			}
		}
	}

	@Test
	public void testSerializableClass() throws SQLException {
		Dao<SerializableWrapper, Integer> dao = createDao(SerializableWrapper.class, true);
		SerializableStuff stuff = new SerializableStuff();
		stuff.field1 = 12345;
		stuff.field2 = "oejwepfjw";
		SerializableWrapper wrapper = new SerializableWrapper();
		wrapper.stuff = stuff;

		assertEquals(1, dao.create(wrapper));

		SerializableWrapper result = dao.queryForId(wrapper.id);
		assertNotNull(result);
		assertEquals(wrapper.id, result.id);
		assertEquals(wrapper.stuff, result.stuff);
	}

	/* ======================================================================================================= */


	@Entity
	protected static class EntityNoName {
	}

	protected static class SerialField implements Serializable {
		private static final long serialVersionUID = -3883857119616908868L;
		String stuff;

		public SerialField() {
		}
	}

	private static class UpperCaseFieldDatabaseType extends H2DatabaseType {
		public UpperCaseFieldDatabaseType() throws SQLException {
			super();
		}

		@Override
		public boolean isEntityNamesMustBeUpCase() {
			return true;
		}
	}

	@Entity
	private static class SerializableWrapper {
		@Id
		@GeneratedValue
		int id;
		@Column
		SerializableStuff stuff;
	}

	@Entity
	private static class SerializableStuff implements Serializable {
		private static final long serialVersionUID = -6203522605272351584L;
		@Column
		int field1;
		@Column
		String field2;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + field1;
			result = prime * result + ((field2 == null) ? 0 : field2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			SerializableStuff other = (SerializableStuff) obj;
			if (field1 != other.field1) {
				return false;
			}
			if (field2 == null) {
				return (other.field2 == null);
			} else {
				return (field2.equals(other.field2));
			}
		}
	}
}
