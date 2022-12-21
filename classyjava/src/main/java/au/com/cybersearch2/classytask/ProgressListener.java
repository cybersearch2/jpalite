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
 * Interface for a task progress callback. This allows progress updates
 * to be published and setting the work status to "FAILED". Applicable
 * only to asynchronous execution, ie. where the task is running on a
 * background thread.
 *
 * @param <Progress> Type used to report progress updates
 */
public interface ProgressListener<Progress> {

	/**
	 * Publish progress update
	 * @param progress Progress data
	 * @throws InterruptedException if interrupted
	 */
	void publishProgress(Progress progress) throws InterruptedException;
	
}
