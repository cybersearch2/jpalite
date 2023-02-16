package au.com.cybersearch2.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.junit.Test;

import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseType;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

public class JpaContainerTest {

	private static final String[] JPALITE_JASON = {
			"{",
			"  \"databaseType\": \"H2\",",
			"  \"connectionType\": \"memory\"",
			"  \"units\":[",
			"    {\"name\": \"organizations\",",
			"     \"provider\": \"org.h2.Driver\",",
			"     \"classes\":[",
			"      \"au.com.cybersearch2.classyfy.data.alfresco.RecordCategory\"",
			"     ]",
			"     \"settings\":[",
			"      \"databaseName = categories\"",
			"     ]",
			"    }",
			"]}"
	};
	private static String RESOURCE_FILE = createTempFile();
	
	@Test
	public void test_system_resource_path() {
		System.setProperty("jpalite.resource-path", RESOURCE_FILE);
		JpaContainer jpaContainer = new JpaContainer();
		jpaContainer.initialize();
		assertThat(jpaContainer.getDatabaseType()).isEqualTo(DatabaseType.H2);
		assertThat(jpaContainer.getConnectionType()).isEqualTo(ConnectionType.memory);
	}

	@Test
	public void test_execute() {
		System.setProperty("jpalite.resource-path", RESOURCE_FILE);
		JpaContainer jpaContainer = new JpaContainer();
		jpaContainer.initialize();
		assertThat(jpaContainer.getDatabaseType()).isEqualTo(DatabaseType.H2);
		assertThat(jpaContainer.getConnectionType()).isEqualTo(ConnectionType.memory);
		PersistenceWork persistenceWork = new PersistenceWork() {

			@Override
			public void doTask(EntityManagerLite entityManager) {
				System.out.println("Hello world from Jpalite!");
			}

			@Override
			public void onPostExecute(boolean success) {
			}

			@Override
			public void onRollback(Throwable rollbackException) {
			}};
		JpaProcess process = jpaContainer.execute(persistenceWork);
		assertThat(process.exitValue()).isEqualTo(WorkStatus.FINISHED);
	}
	
	@Test
	public void test_no_resource_path() {
		try {
		   new JpaContainer();
           failBecauseExceptionWasNotThrown(JpaliteException.class);
       }
       catch(JpaliteException e)
       {
           assertThat(e.getMessage()).contains("Resource path not found in System property jpalite.resource-path");
       }
	}

	private static String createTempFile() {
	    Path tempFile;
	    Path resourcePath = null;
		try {
		    if (RESOURCE_FILE != null) {
		    	Path filePath = Paths.get(RESOURCE_FILE);
		    	Files.deleteIfExists(filePath);
		    }
			tempFile = Files.createTempFile("jpalite", ".json");
			resourcePath = tempFile.getParent().resolve("jpalite.json");
			Files.move(tempFile, resourcePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Path jsonPath = resourcePath;
	    Arrays.asList(JPALITE_JASON).forEach(line -> {
			try {
				Files.writeString(jsonPath, line, StandardOpenOption.APPEND);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	    return tempFile.getParent().toString();
	}}
