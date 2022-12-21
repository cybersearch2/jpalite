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

/**
 * Performs a task implemented as a sub-class of this class. Method execute() is
 * called to perform the task. The work status is maintained in the TaskStatus
 * super class.
 */
public abstract class WorkUnit<Progress> extends TaskStatus
{
    /**
     * Task implementation. Execution can, by arrangement, take place on a background thread. Any
     * required parameters will need to be provided as fields belonging to the sub class.
     * @param progressListener Progress listener
     * @return Boolean object to flag success (true), failure (false) or cancel (null)
     */
    public abstract boolean doTask(ProgressListener<Progress> progressListener);

    /**
     * Perform task
     * @param progressListener Progress listener. Use is optional.
     * @return Boolean object to report success
     */
    public Boolean execute(ProgressListener<Progress> progressListener) 
    {
    	setWorkStatus(WorkStatus.RUNNING);
        boolean result = doTask(progressListener);
        return Boolean.valueOf(result);
    }


}
