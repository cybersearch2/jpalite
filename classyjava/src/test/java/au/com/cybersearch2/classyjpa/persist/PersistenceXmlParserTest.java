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
package au.com.cybersearch2.classyjpa.persist;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.junit.Test;

/**
 * PersistenceXmlParserTest
 * @author Andrew Bowley
 * 11/05/2014
 */
public class PersistenceXmlParserTest
{
    static final String PERSISTENCE_XML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"" +
                   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                   "version=\"2.0\">" +
        "<persistence-unit name=\"classyfy\">" +
           "<provider>au.com.cybersearch2.ClassyFyProvider</provider>" + 
           "<class>au.com.cybersearch2.data.alfresco.RecordCategory</class>" +
        "</persistence-unit>" +
      "</persistence>";
    
    @Test
    public void test_PersistenceXmlParser() throws Exception
    {
        PersistenceXmlParser parser = new PersistenceXmlParser();
        Map<String, PersistenceUnitInfo> result = parser.parsePersistenceXml(new ByteArrayInputStream(PERSISTENCE_XML.getBytes()));
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        PersistenceUnitInfo info = result.get("classyfy");
        assertThat(info).isNotNull();
        assertThat(info.getPersistenceUnitName()).isEqualTo("classyfy");
        List<String> managed = info.getManagedClassNames();
        assertThat(managed.size()).isEqualTo(1);
        assertThat(managed.get(0)).isEqualTo("au.com.cybersearch2.data.alfresco.RecordCategory");
    }

}
