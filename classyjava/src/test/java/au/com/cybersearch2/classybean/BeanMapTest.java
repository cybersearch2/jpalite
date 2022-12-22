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
package au.com.cybersearch2.classybean;

import static org.assertj.core.api.Assertions.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * BeanMapTest
 * @author Andrew Bowley
 * 12/06/2014
 */
public class BeanMapTest
{
    Date created;
    Date modified;

    @Before
    public void setUp() throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", new Locale("en", "AU"));
        created = sdf.parse("2014-02-05 18:45:46.000000");
        modified = sdf.parse("2014-02-12 11:55:23.000000");
    }
    
    @Test
    public void test_BeanMap() throws Exception
    {
        RecordCategory recordCategory = new RecordCategory();
        populateRecordCategory(recordCategory);
        BeanMap beanMap = new BeanMap(recordCategory);
        assertThat(beanMap.containsKey("created")).isTrue();
        assertThat(beanMap.containsKey("modified")).isTrue();
        assertThat(beanMap.containsKey("creator")).isTrue();
        assertThat(beanMap.containsKey("modifier")).isTrue();
        assertThat(beanMap.containsKey("description")).isTrue();
        assertThat(beanMap.containsKey("identifier")).isTrue();
        assertThat(beanMap.get("created")).isEqualTo(created);
        assertThat(beanMap.get("modified")).isEqualTo(modified);
        assertThat(beanMap.get("creator")).isEqualTo("admin");
        assertThat(beanMap.get("modifier")).isEqualTo("prole");
        assertThat(beanMap.get("description")).isEqualTo("Information Technology");
        assertThat(beanMap.get("identifier")).isEqualTo("2014-1391586274589");
        Set<Entry<String, Object>> entrySet = beanMap.entrySet();
        int size = entrySet.size();
        assertThat(size).isGreaterThan(6);
        try
        {
            entrySet.remove(entrySet.iterator().next());
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        }
        catch (UnsupportedOperationException e)
        {
        }
        Map<String,Object> shadowMap = new HashMap<>();
        Iterator<Entry<String, Object>> iterator = entrySet.iterator();
        while (iterator.hasNext())
        {
            Entry<String, Object> entry = iterator.next();
            shadowMap.put(entry.getKey(), entry.getValue());
        }
        assertThat(shadowMap.containsKey("created")).isTrue();
        assertThat(shadowMap.containsKey("modified")).isTrue();
        assertThat(shadowMap.containsKey("creator")).isTrue();
        assertThat(shadowMap.containsKey("modifier")).isTrue();
        assertThat(shadowMap.containsKey("description")).isTrue();
        assertThat(shadowMap.containsKey("identifier")).isTrue();
        assertThat(shadowMap.get("created")).isEqualTo(created);
        assertThat(shadowMap.get("modified")).isEqualTo(modified);
        assertThat(shadowMap.get("creator")).isEqualTo("admin");
        assertThat(shadowMap.get("modifier")).isEqualTo("prole");
        assertThat(shadowMap.get("description")).isEqualTo("Information Technology");
        assertThat(shadowMap.get("identifier")).isEqualTo("2014-1391586274589");
   }
        

    
    protected void populateRecordCategory(RecordCategory recordCategory)
    {
        recordCategory.setCreated(created);
        recordCategory.setModified(modified);
        recordCategory.setCreator("admin");
        recordCategory.setModifier("prole");
        recordCategory.setDescription("Information Technology");
        recordCategory.setIdentifier("2014-1391586274589");
    }
}
