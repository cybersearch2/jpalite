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
package au.com.cybersearch2.example;

import java.util.ArrayList;
import java.util.List;

public class Transcript {

	private int errorCount;
    private List<Observation> observations;

    public Transcript()
    {
    	observations = new ArrayList<>();
    }

    public void add(String report, boolean status) 
    {
    	observations.add(new Observation(report, status));
    	if (!status)
    		++errorCount;
    }

    public int getErrorCount() {
		return errorCount;
	}

    public int getTotalCount() {
    	return observations.size();
    }
    
	public List<String> getReports() {
    	List<String> contents = new ArrayList<>();
    	observations.forEach(ob -> contents.add(ob.getReport()));
    	return contents;
    }
}
