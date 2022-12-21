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

import java.util.concurrent.ExecutorService;

public interface TaskExecutor {

    /**
     * Returns executor service. The service should implement a thread pool.
     * @return ExecutorService
     */
	ExecutorService getExecutorService(); 


    /**
     * Initiates a shutdown. The implementation-should attempt to stop all actively executing tasks and not wait for previously submitted tasks to
     * complete execution.
     * @throws SecurityException if security violation detected
     */
    void shutdown();
}
