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
package au.com.cybersearch2.pp.jpa;

import java.util.ArrayList;
import java.util.List;

public class Transcript {
	
    protected List<String> textList;

    public Transcript()
    {
        textList = new ArrayList<>();
    }

    public void add(String text) 
    {
        textList.add(text);
    }


    public List<String> getContents() {
    	List<String> contents = textList;
    	textList = new ArrayList<>();
    	return contents;
    }
}
