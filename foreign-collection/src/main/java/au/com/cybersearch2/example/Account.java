/*
ISC License (https://opensource.org/licenses/ISC)

Copyright 2019, Gray Watson

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
package au.com.cybersearch2.example;

import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.j256.ormlite.table.DatabaseTable;

import au.com.cybersearch2.classyjpa.entity.ForeignCollector;
import au.com.cybersearch2.classyjpa.entity.OrmEntity;

/**
 * Example account entity object
 */
@DatabaseTable(tableName = "accounts")
public class Account implements OrmEntity {

	// for QueryBuilder to be able to find the fields
	public static final String NAME_FIELD_NAME = "name";
	public static final String PASSWORD_FIELD_NAME = "passwd";

	/** Wraps OrmLite ForeignCollection set by Jpalite on the orders field at start up */
	private final ForeignCollector<Order> foreignCollector;

    @Id @GeneratedValue
	private int id;

    @Column(name = NAME_FIELD_NAME, nullable = false)
	private String name;

    @Column(name = PASSWORD_FIELD_NAME)
	private String password;

	@OneToMany(fetch=FetchType.LAZY)
	private Collection<Order> orders;

	Account() {
		// all persisted classes must define a no-arg constructor with at least package visibility
		foreignCollector = new ForeignCollector<>();
	}

	public Account(String name) {
		this();
		this.name = name;
	}

	public Account(String name, String password) {
		this();
		this.name = name;
		this.password = password;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Returns list of orders owned by this account
	 * @return Order list
	 */
	public List<Order> getOrders() {
		return foreignCollector.getList(orders);
	}

	/**
	 * Add given order to this Account
	 * @param order Order object
	 * @return resulting number of orders own by this Account
	 */
	public int add(Order order) {
		return foreignCollector.add(order, orders);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || other.getClass() != getClass()) {
			return false;
		}
		return name.equals(((Account) other).name);
	}
}
