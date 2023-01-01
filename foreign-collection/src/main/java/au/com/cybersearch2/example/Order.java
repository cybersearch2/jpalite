package au.com.cybersearch2.example;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;

/**
 * Example order entity object
 */
@Entity(name = "orders")
public class Order implements OrmEntity {

    @Id @GeneratedValue
	private int id;

    @OneToOne
	private Account account;
	@Column
	private int itemNumber;
	@Column
	private int quantity;
	@Column
	private float price;

	Order() {
		// all persisted classes must define a no-arg constructor with at least package visibility
	}

	public Order(Account account, int itemNumber, float price, int quantity) {
		this.account = account;
		this.itemNumber = itemNumber;
		this.price = price;
		this.quantity = quantity;
	}

	public int getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public int getItemNumber() {
		return itemNumber;
	}

	public void setItemNumber(int itemNumber) {
		this.itemNumber = itemNumber;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
