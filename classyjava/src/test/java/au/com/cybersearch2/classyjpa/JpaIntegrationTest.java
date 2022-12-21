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

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyapp.TestClassyApplicationModule;
import au.com.cybersearch2.classyfy.data.Model;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.entity.TestPersistenceWork;
import au.com.cybersearch2.classyjpa.entity.TestPersistenceWork.Callable;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.TestPersistenceFactory;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classynode.NodeEntity;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * JpaIntegrationTest
 * @author Andrew Bowley
 * 13/06/2014
 */
public class JpaIntegrationTest
{
    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
    }

    private static final String TOP_TITLE = "Cybersearch2 Records";
    
    protected Transcript transcript;
    protected TestPersistenceFactory testPersistenceFactory; 
    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    
    
    protected PersistenceContext persistenceContext;

    @BeforeClass
    public static void before() {
    	// Set ORMLite system property to select local Logger
        System.setProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY, LogBackendType.LOG4J2.name());
    }
    
    @Before
    public void setup() throws Exception
    {
        persistenceContext = createObjectGraph().persistenceContext();
        testPersistenceFactory = new TestPersistenceFactory(persistenceContext);
        transcript = new Transcript();
    }

    @After
    public void shutdown()
    {
        testPersistenceFactory.onShutdown();
    }
 
	/**
	 * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object.
	 * Override to run with different database and/or platform. 
	 */
	protected ApplicationComponent createObjectGraph()
	{
        component = new ApplicationComponent() {

        	TestClassyApplicationModule module = new TestClassyApplicationModule(new JavaTestResourceEnvironment("src/test/resources/h2"));
        	
			@Override
			public PersistenceContext persistenceContext() {
				return module.providePersistenceContext();
			}

        };
        return component;
	}

    @Test 
    public void test_findNodeById() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        final NodeEntity[] entityHolder = new NodeEntity[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) throws Exception
            {
                entityHolder[0] = entityManager.find(NodeEntity.class, 34);
                transcript.add("entityManager.find() completed");
                return entityHolder[0].get_id() == 34;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(persistenceContext);
        taskStatus.await(5, TimeUnit.SECONDS);
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
        Node node = Node.marshall(entityHolder[0]);
        assertThat(node).isNotNull();
        assertThat(node.getId() == 34);
        assertThat(node.getParentId()== 1);
        assertThat(node.getChildren()).isNotNull();
        assertThat(node.getChildren().size()).isEqualTo(8);
        assertThat(node.getParent()).isNotNull();
        assertThat(node.getParent() instanceof Node).isTrue();
        //assertThat(node.getLevel()).isEqualTo(4);
        Node parent = (Node)node.getParent();
        assertThat(parent.getId() == 1);
        assertThat(parent.getParentId() == 0);
        assertThat(parent.getChildren().size()).isEqualTo(1);
        assertThat(parent.getChildren().contains(node)).isTrue();
        assertThat(parent.getTitle()).isEqualTo(TOP_TITLE);
        assertThat(parent.getModel()).isEqualTo(Model.recordCategory.ordinal());
        //assertThat(parent.getLevel()).isEqualTo(3);
        Node root = (Node)parent.getParent();
        assertThat(root.getChildren().size()).isEqualTo(1);
        assertThat(root.getChildren().contains(parent)).isTrue();
        assertThat(root.getModel()).isEqualTo(Model.root.ordinal());
        assertThat(root.getId() == 0);
        assertThat(root.getParentId() == 0);
        //assertThat(root.getLevel()).isEqualTo(1);
    }

    @Test
    public void test_PersistenceEnvironment()
    {
        assertThat(testPersistenceFactory).isNotNull();
        PersistenceContext testPersistenceContext = testPersistenceFactory.getPersistenceEnvironment();
        assertThat(testPersistenceContext).isNotNull();
        PersistenceAdmin persistenceAdmin = testPersistenceContext.getPersistenceAdmin(TestClassyApplication.PU_NAME);
        assertThat(persistenceAdmin).isNotNull();
        EntityManagerLiteFactory entityManagerFactory = persistenceAdmin.getEntityManagerFactory();
        assertThat(entityManagerFactory).isNotNull();
        EntityManagerLite em = entityManagerFactory.createEntityManager();
        assertThat(em).isNotNull();
        EntityTransaction transaction = em.getTransaction();
        assertThat(transaction).isNotNull();
        transaction.begin();
        em.close();
        
    }

    @Test 
    public void test_find_node() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);

        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) 
            {
                entityHolder[0] = entityManager.find(RecordCategory.class, Integer.valueOf(1));
                transcript.add("entityManager.find() completed");
                return true;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(persistenceContext);
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "entityManager.find() completed", "onPostExecute true");
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
    }
   
    @Test 
    public void test_find_entity() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);

        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) 
            {
                entityHolder[0] = entityManager.find(RecordCategory.class, Integer.valueOf(1));
                transcript.add("entityManager.find() completed");
                return true;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(persistenceContext);
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "entityManager.find() completed", "onPostExecute true");
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
     }
         
    @Test 
    public void test_find_entity_by_query() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);

        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) throws Exception
            {
                EntityManagerDelegate delegate = (EntityManagerDelegate)entityManager.getDelegate();
                @SuppressWarnings("unchecked")
                PersistenceDao<RecordCategory, Integer> recordCategoryDao = 
                        (PersistenceDao<RecordCategory, Integer>) delegate.getDaoForClass(RecordCategory.class);
                QueryBuilder<RecordCategory, Integer> statementBuilder = recordCategoryDao.queryBuilder();
                SelectArg selectArg = new SelectArg();
                // build a query with the WHERE clause set to 'name = ?'
                statementBuilder.where().eq("node_id", selectArg);
                PreparedQuery<RecordCategory> preparedQuery = statementBuilder.prepare();
                // now we can set the select arg (?) and run the query
                selectArg.setValue(Integer.valueOf(2));
                List<RecordCategory> results = recordCategoryDao.query(preparedQuery);
                entityHolder[0] = results.get(0);
                transcript.add("entityManager.find() completed");
                return true;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(persistenceContext);
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "entityManager.find() completed", "onPostExecute true");
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
    }
       
    @Test 
    public void test_find_entity_by_named_query() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) throws Exception
            {
                Query query = entityManager.createNamedQuery(TestClassyApplication.CATEGORY_BY_NODE_ID);
                query.setParameter("node_id", Integer.valueOf(2));
                entityHolder[0] = (RecordCategory) query.getSingleResult();
                transcript.add("entityManager.query() completed");
                return entityHolder[0].get_node_id() == 2;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(persistenceContext);
        taskStatus.await(5, TimeUnit.SECONDS);
        transcript.assertEventsSoFar("background task", "entityManager.query() completed", "onPostExecute true");
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
    }

}
