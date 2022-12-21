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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Interface for task executor to call back to task monitor
 *
 * @param <Result> Type returned from task
 */
public interface TaskListener<Result> {

	/**
	 * Handle start of execution event
	 */
	void onStart();
	
	/**
	 * Post result on task termination
	 * @param result Result data
	 */
	void postResult(Result result);
	
	/**
	 * Set future created to return result to the caller
	 * @param futureFesult Future to get result when available
	 */
	void setFutureResult(Future<Result> futureFesult);
    /**
     * Set Exception thrown when the worker task fails due to a uncaught RuntimeException  
     * @param executionException ExecutionException
     */
    public void setExecutionException(ExecutionException executionException);
}
