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
 * Interface for task message handler
 */
public interface TaskMessenger<Progress,Result>
{
    /**
     * Post task success flag
     * @param taskMonitor Task monitor
     * @param result Success flag
     * @throws InterruptedException if interrupted
     */
    void sendResult(TaskMonitor<Progress,Result> taskMonitor, Result result) throws InterruptedException;
    
    /**
	 * Post progress update
     * @param taskMonitor Task monitor
     * @param progress Progress object
     * @throws InterruptedException if interrupted
    */
    void sendProgress(TaskMonitor<Progress,Result> taskMonitor, Progress progress) throws InterruptedException;

    /**
     * Shutdown
     */
    void shutdown();

}
