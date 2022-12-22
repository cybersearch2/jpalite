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
package au.com.cybersearch2.classyutil;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Transcript - Partial replacement for Robolectric Transcript
 * @author Andrew Bowley
 * 13/06/2014
 */
public class Transcript
{
    protected List<String> textList;

    public Transcript()
    {
        textList = new ArrayList<>();
    }

    public void add(String text) 
    {
        textList.add(text);
    }

    public void assertEventsSoFar(String... textSequence) 
    {
        int index = 0;
        for (String text: textSequence)
        {
            assertThat(index).isLessThan(textList.size());
            assertThat(textList.get(index)).isEqualTo(text);
            ++index;
        }
    }

    public void assertEventsInclude(String text) 
    {
        assertThat(textList.size()).isGreaterThan(0);
        int index = 0;
        Iterator<String> iterator = textList.iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().equals(text))
                break;
            ++index;
        }
        assertThat(index).isLessThan(textList.size());
    }

}
