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
package au.com.cybersearch2.classytask;

import java.util.concurrent.TimeUnit;

/**
 * Holds WorkUnit execution status and provides method to block caller
 * pending task completion
 *
 */
public class TaskStatus {

	private volatile WorkStatus workStatus;

	/**
	 * Construct TaskStatus object
	 */
	public TaskStatus() {
        workStatus = WorkStatus.PENDING;
	}
	
	public WorkStatus getWorkStatus() {
		return workStatus;
	}
 
    /**
     * Returns flag set true if execution has started
     * @return boolean
     */
    public boolean wasTaskInvoked() 
    {
        return workStatus != WorkStatus.PENDING;
    }

    /**
     * Block caller pending completion of execution
     * @param duration Number of units
     * @param unit Unit interval
     * @throws InterruptedException if interrupted
     */
    public void await(long duration, TimeUnit unit) throws InterruptedException {
    	synchronized(this) {
    		if ((workStatus != WorkStatus.FINISHED) && (workStatus != WorkStatus.FAILED))
    			wait(TimeUnit.MILLISECONDS.convert(duration, unit));
    	}
    }
 
    /**
     * Set current work status
     * @param workStatus WorkStatus enum
     */
    public void setWorkStatus(WorkStatus workStatus) {
    	this.workStatus = workStatus;
    	synchronized(this) {
    		if ((workStatus == WorkStatus.FINISHED) || (workStatus == WorkStatus.FAILED))
    			this.notifyAll();
    	}
    }
}
