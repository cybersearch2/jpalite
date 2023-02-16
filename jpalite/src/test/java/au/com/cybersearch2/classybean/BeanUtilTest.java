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
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classybean.BeanUtil.DataPair;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * BeanUtilTest
 * @author Andrew Bowley
 * 12/06/2014
 */
public class BeanUtilTest
{
    static class TestModule2
    {
    	public TestModule2() {
    		
    	}
    	
        Object provideObject() 
        {
            return new Object();
        }
    }

    static class Injectee
    {
        Object object;
        
        Object get()
        {
            return object;
        }
    }
    
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
        Set<DataPair> datapairSet = BeanUtil.getDataPairSet(recordCategory);
        int size = datapairSet.size();
        assertThat(size).isGreaterThan(6);
        Map<String,Object> shadowMap = new HashMap<>();
        Iterator<DataPair> iterator = datapairSet.iterator();
        while (iterator.hasNext())
        {
            DataPair entry = iterator.next();
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

    @Test
    public void test_newClassInstance()
    {
        Object module = BeanUtil.newClassInstance(TestModule2.class.getName());
        assertThat(module).isNotNull();
        String badClassName = TestModule2.class.getName()+"X";
        try
        {
            BeanUtil.newClassInstance(badClassName);
            failBecauseExceptionWasNotThrown(BeanException.class);
        }
        catch(BeanException e)
        {
            assertThat(e.getMessage()).isEqualTo("Class " + badClassName + " not found");
        }
    }
    
    @Test
    public void test_newClassInstance_null_classname()
    {
        try
        {
            BeanUtil.newClassInstance(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter className is null");
        }
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
