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

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.TypedQuery;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.QueryForAllGenerator;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.DefaultTaskExecutor;
import au.com.cybersearch2.classytask.DefaultTaskMessenger;
import au.com.cybersearch2.classytask.TaskExecutor;
import au.com.cybersearch2.classytask.TaskMessenger;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * Demonstrates a one-to-many table relationship.
 * Shows how type java.util.Collection is applied to a @OneToMany-annotated field.
 */
public class ForeignCollectionMain {

	public static final String ACCOUNTS_PU = "account";
	private static final String ALL_ORDERS = "all_orders";
    private static TaskExecutor taskExecutor;

    private PersistenceContext persistenceContext;
    private ForeignCollectionFactory foreignCollectionFactory;
    private TaskMessenger<Void,Boolean> taskMessenger;

    public ForeignCollectionMain() {
        taskMessenger = new DefaultTaskMessenger<Void>(Void.class);
    }

	public static void main(String[] args) throws Exception {
     	taskExecutor = new DefaultTaskExecutor();
     	try {
     		ForeignCollectionMain foreignCollectionMain = new ForeignCollectionMain();
     		foreignCollectionMain.setUp();
     	    // read and write some data
     		Transcript transcript = foreignCollectionMain.readWriteData();
     		transcript.getReports().forEach(entry -> System.out.println(entry));
     		if (transcript.getErrorCount() == 0)
     			System.out.println("Success");
     		else
     			System.err.println("Failed");
     	} finally {
     		taskExecutor.shutdown();
     		System.exit(0);
     	}
	}

    public void close() {
    	taskMessenger.shutdown();
        persistenceContext.close();
    }

    /**
     * Initialize application and database and return flag set true if successful
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @return boolean
     */
    public boolean setUp()
    {
    	persistenceContext = createFactory();
		try {
			initializeDatabase();
		} catch (InterruptedException e) {
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
		return true;
     }

    public void shutdown()
    {
    	taskMessenger.shutdown();
    	if (persistenceContext != null) {
	        persistenceContext.getPersistenceAdmin(ACCOUNTS_PU).close();
	        persistenceContext = null;
    	}
    }

	private Transcript readWriteData() throws Exception {
		Transcript transcript = new Transcript();

        PersistenceWork mainWork = new PersistenceWork() {

            @Override
            public void doTask(EntityManagerLite entityManager) {
				// create an instance of Account
				String name = "Buzz Lightyear";
				Account account = new Account(name);
		
				// persist the account object to the database
				entityManager.persist(account);
		
				// create an associated Order for the Account
				// Buzz bought 2 of item #21312 for a price of $12.32
				int quantity1 = 2;
				int itemNumber1 = 21312;
				float price1 = 12.32F;
				Order order1 = new Order(account, itemNumber1, price1, quantity1);
				entityManager.persist(order1);
		
				// create another Order for the Account
				// Buzz also bought 1 of item #785 for a price of $7.98
				int quantity2 = 1;
				int itemNumber2 = 785;
				float price2 = 7.98F;
				Order order2 = new Order(account, itemNumber2, price2, quantity2);
				entityManager.persist(order2);
		
				Account accountResult = entityManager.find(Account.class, account.getId());
		        List<Order> orderList = accountResult.getOrders();
				// sanity checks
				if (orderList.isEmpty()) {
					transcript.add("No orders returned by find all query", false);
					return;
				}
				Order order = orderList.get(0);
				boolean status = itemNumber1 == order.getItemNumber();
				transcript.add("Item number match is " + status, status);
				status = accountResult.equals(order.getAccount());
				transcript.add("Account objects same = " + status, status);
				if (orderList.size() < 2) {
					transcript.add("Order 2 not returned by for all query", false);
					return;
				}
				order = orderList.get(1);
				status = itemNumber2 == order.getItemNumber();
				transcript.add("Item numbers match = " + status, status);
				status = orderList.size() == 2;
				transcript.add("At end = " + status, status);
		
				// create another Order for the Account
				// Buzz also bought 1 of item #785 for a price of $7.98
				int quantity3 = 50;
				int itemNumber3 = 78315;
				float price3 = 72.98F;
				Order order3 = new Order(account, itemNumber3, price3, quantity3);
		
				// now let's add this order via the foreign collection
				int size = accountResult.add(order3);
				// now there are 3 of them in there
				transcript.add("Number of account object orders = " + size, size == 3);
		        TypedQuery<Order> query = entityManager.createNamedQuery(ALL_ORDERS, Order.class);
				orderList = query.getResultList();
				// and 3 in the database
				size = orderList.size();
				transcript.add("Number of database orders = " +  size, size == 3);
           }

            @Override
            public void onPostExecute(boolean success) {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException) {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
        try {
            execute(mainWork);
         } catch (Throwable t) {
        	t.printStackTrace();
        } finally {
        	shutdown();
        }
		return transcript;
	}
	
    private PersistenceContext createFactory() {
    	foreignCollectionFactory = new ForeignCollectionFactory(taskExecutor, taskMessenger);
        return foreignCollectionFactory.getPersistenceContext();
    }
    
    private WorkStatus execute(PersistenceWork persistenceWork) throws InterruptedException {
        TaskStatus taskStatus = foreignCollectionFactory.doTask(persistenceWork);
        taskStatus.await(500, TimeUnit.SECONDS);
        return taskStatus.getWorkStatus();
    }

    private void initializeDatabase() throws InterruptedException
    {
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistenceContext.getPersistenceAdmin(ACCOUNTS_PU);
        // Create named queries to find all objects of an entity class.
        // Note QueryForAllGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator<Order> allOrderObjects = 
                new QueryForAllGenerator<Order>(Order.class, persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(Order.class, ALL_ORDERS, allOrderObjects);
    }

}