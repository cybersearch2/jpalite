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
package au.com.cybersearch2.classylog;

import java.util.logging.Level;

/**
 * Log
 * Provides a Java util logging interface similar to that of Android log.
 * This is to ease replacement of Java logging with Android logging if desired. See android.util.Log
 * @author Andrew Bowley
 * 11/06/2014
 */
public interface Log
{
    /**
     * Send a VERBOSE log message. Level = FINEST.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void verbose(String tag, String msg);

    /**
     * Send a VERBOSE log message and log the exception. Level = FINEST.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    void verbose(String tag, String msg, Throwable tr);

    /**
     * Send a DEBUG log message. Level = FINE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void debug(String tag, String msg);

    /**
     * Send a DEBUG log message and log the exception. Level = FINE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    void debug(String tag, String msg, Throwable tr);

    /**
     * Send an INFO log message. Level = INFO. Level = INFO.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void info(String tag, String msg);

    /**
     * Send a INFO log message and log the exception. Level = INFO.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    void info(String tag, String msg, Throwable tr);

    /**
     * Send a WARN log message. Level = WARNING.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void warn(String tag, String msg);

    /**
     * Send a #WARN log message and log the exception. Level = WARNING.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    void warn(String tag, String msg, Throwable tr);
    
    /**
     * Send an ERROR log message. Level = SEVERE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    void error(String tag, String msg);

    /**
     * Send an ERROR log message and log the exception. Level = SEVERE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    void error(String tag, String msg, Throwable tr);
    
    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     * 
     * NOTE IF USING Android Log implementation:    
     * Log.isLoggable() will throw an exception if the length of the tag is greater than
     * 23 characters, so trim it if necessary to avoid the exception.
     *
     * @param tag The tag to check.
     * @param level The level to check.
     * @return Whether or not that this is allowed to be logged.
     */
    boolean isLoggable(String tag, Level level);
    
    /**
     * Set logging level. 
     * NOTE IF USING Android Log implementation, this function is not supported natively by Android.
     * @param level Log level
     */
    void setLevel(Level level);

	/**
	 * Send a VERBOSE log message. Level = FINEST.
	 * @param msg The message you would like logged.
	 * @param args Arguments
	 */
	void verbose(String msg, Object... args);

	/**
	 * Send a DEBUG log message. Level = FINE.
	 * @param msg The message you would like logged.
	 * @param args Arguments
	 */
	void debug(String msg, Object... args);

	/**
	 * Send an INFO log message. Level = INFO. Level = INFO.
	 * @param msg The message you would like logged.
	 * @param args Arguments
	 */
	void info(String msg, Object... args);

	/**
	 * Send a WARN log message. Level = WARNING.
	 * @param msg The message you would like logged.
	 * @param args Arguments
	 */
	void warn(String msg, Object... args);

	/**
	 * Send a #WARN log message and log the exception. Level = WARNING.
	 * @param msg The message you would like logged.
	 * @param throwable An exception to log
	 */
	void warn(String msg, Throwable throwable);

	/**
	 * Send an ERROR log message. Level = SEVERE.
	 * 
	 * @param msg  The message you would like logged.
	 * @param args Optional format arguments
	 */
	void error(String msg, Object... args);

	/**
	 * Send an ERROR log message and log the exception. Level = SEVERE.
	 * @param msg The message you would like logged.
	 * @param throwable An exception to log
	 */
	void error(String msg, Throwable throwable);

}
