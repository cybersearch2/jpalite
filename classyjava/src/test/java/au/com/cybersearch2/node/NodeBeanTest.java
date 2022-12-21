package au.com.cybersearch2.node;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyapp.TestClassyApplicationModule;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.entity.TestPersistenceWork;
import au.com.cybersearch2.classyjpa.entity.TestPersistenceWork.Callable;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.TestPersistenceFactory;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classynode.NodeEntity;
import au.com.cybersearch2.classytask.TaskStatus;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyutil.Transcript;

public class NodeBeanTest {
    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
    }

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
    	// Set ORMLite system property to select local Logger
        System.setProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY, LogBackendType.LOG4J2.name());
        persistenceContext = createObjectGraph().persistenceContext();
        testPersistenceFactory = new TestPersistenceFactory(persistenceContext);
        transcript = new Transcript();
    }

    @After
    public void shutdown()
    {
        testPersistenceFactory.onShutdown();
    }
 
    @Test @Ignore
    public void testNodeEntity() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) throws Exception
            {
            	EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
            	@SuppressWarnings("unchecked")
				PersistenceDao<NodeEntity, Integer> dao = (PersistenceDao<NodeEntity, Integer>) delegate.getDaoForClass(NodeEntity.class);
            	NodeEntity rootNode = new NodeEntity();
            	rootNode.setName(Node.ROOT);
            	dao.assignEmptyForeignCollection(rootNode, "_children");
            	entityManager.persist(rootNode);
            	rootNode.setParent(rootNode);
            	entityManager.merge(rootNode);
            	NodeEntity parentNode = new NodeEntity();
            	parentNode.setName("Parent");
            	parentNode.setParent(rootNode);
               	dao.assignEmptyForeignCollection(parentNode, "_children");
            	entityManager.persist(parentNode);
            	NodeEntity testNode = new NodeEntity();
            	testNode.setName("Under test");
            	testNode.setParent(parentNode);
            	//dao.assignEmptyForeignCollection(testNode, "_children");
            	entityManager.persist(testNode);
            	parentNode = entityManager.find(NodeEntity.class, 2);
            	/*
        		//CloseableIterator<NodeEntity> iterator = parentNode.get_children().closeableIterator();
        		try {
        			if (!iterator.hasNext()) {
        				System.err.println("Parent children not updated");
        				return Boolean.FALSE;
        			}
        		} finally {
        			// must always close our iterators otherwise connections to the database are held open
        			iterator.close();
        		}
        		*/
            	NodeEntity underTest = entityManager.find(NodeEntity.class, 3);
            	if (underTest != null) {
            		System.out.println("Under test name = " + underTest.getName());
            		Node underTestNode = Node.marshall(underTest);
            		Node underTestParentNode = underTestNode.getParent();
            		System.out.println("Under test parent name = " + underTestParentNode.getName());
            		System.out.println("Root name = " + underTestParentNode.getParent().getName());
             	}
            	
            	return Boolean.TRUE;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        persistenceWorkModule = new PersistenceWorkModule(TestClassyApplication.PU_NAME, persistenceWork);
        TaskStatus taskStatus = persistenceWorkModule.doTask(component.persistenceContext());
        taskStatus.await(5, TimeUnit.SECONDS);
        //transcript.assertEventsSoFar("background task", "entityManager.query() completed", "onPostExecute true");
        assertThat(taskStatus.getWorkStatus()).isEqualTo(WorkStatus.FINISHED);
    	
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

}
