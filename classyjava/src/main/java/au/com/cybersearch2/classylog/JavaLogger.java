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

import java.util.logging.*;

/**
 * JavaLogger
 * Logger readily interchangeable with Android android.util.Log, implemented using java.util.logging.Logger
 * Mapping Android levels to Java levels:
 * VERBOSE = FINEST
 * DEBUG = FINE
 * INFO = INFO
 * WARN = WRNING
 * ERROR = SEVERE
 * @author Andrew Bowley
 * 11/06/2014
 */
public class JavaLogger implements Log
{
    /** Use java.util.logging package for actual log implementation */
    private Logger logger;
    /** Tag Used to identify the source of a log message */
    private String name;
    
    /**
     * Create JavaLogger object. Call static getLogger() to invoke constructor.
     * @param name Tag Used to identify the source of a log message
     */
    protected JavaLogger(String name)
    {
        this.name = name;
        logger = Logger.getLogger(name);
    }

    /**
     * JavaLogger class factory
     * @param tagClass Class used to identify the source of a log message
     * @return JavaLogger
     */
    public static JavaLogger getLogger(Class<?> tagClass)
    {
        return new JavaLogger(tagClass.getName());
    }
    
    /**
     * JavaLogger class factory
     * @param name Tag Used to identify the source of a log message
     * @return JavaLogger
     */
    public static JavaLogger getLogger(String name)
    {
        return new JavaLogger(name);
    }
    
    /**
     * Send a VERBOSE log message. Level = FINEST.
     * @param msg The message you would like logged.
     */
 	@Override
	public void verbose(String msg, Object... args)
    {
		if ((msg != null) && !msg.isEmpty()) {
			logger.logp(Level.FINEST, name, null, formatText(msg, args));
		}
    }

    /**
     * Send a DEBUG log message. Level = FINE.
     * @param msg The message you would like logged.
     */
	@Override
	public void debug(String msg, Object... args)
    {
		if ((msg != null) && !msg.isEmpty()) {
			logger.logp(Level.FINE, name, null, formatText(msg, args));
		}
    }

    /**
     * Send an INFO log message. Level = INFO. Level = INFO.
     * @param msg The message you would like logged.
     */
	@Override
	public void info(String msg, Object... args)
    {
		if ((msg != null) && !msg.isEmpty()) {
			logger.logp(Level.INFO, name, null, formatText(msg, args));
		}
    }

    /**
     * Send a WARN log message. Level = WARNING.
     * @param msg The message you would like logged.
     */
	@Override
	public void warn(String msg, Object... args)
    {
		if ((msg != null) && !msg.isEmpty()) {
			logger.logp(Level.WARNING, name, null, formatText(msg, args));
		}
    }

    /**
     * Send a #WARN log message and log the exception. Level = WARNING.
     * @param msg The message you would like logged.
     * @param throwable An exception to log
     */
	@Override
    public void warn(String msg, Throwable throwable) 
    {
		if ((msg != null) && !msg.isEmpty()) {
			logger.logp(Level.WARNING, name, null, msg, throwable);
		}
    }

    /**
	 * Send an ERROR log message. Level = SEVERE.
	 * 
	 * @param msg  The message you would like logged.
	 * @param args Optional format arguments
	 */
	@Override
	public void error(String msg, Object... args)
    {
		if ((msg != null) && !msg.isEmpty()) {
			logger.logp(Level.SEVERE, name, null, formatText(msg, args));
		}
    }

    /**
     * Send an ERROR log message and log the exception. Level = SEVERE.
     * @param msg The message you would like logged.
     * @param throwable An exception to log
     */
 	@Override
    public void error(String msg, Throwable throwable) 
    {
        logger.logp(Level.SEVERE, name, null, msg, throwable);
    }

    /**
     * Send a VERBOSE log message. Level = FINEST.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void verbose(String tag, String msg) 
    {
        logger(tag).logp(Level.FINEST, tag, null, msg);
    }

    /**
     * Send a VERBOSE log message and log the exception. Level = FINEST.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void verbose(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.FINEST, tag, null, msg, tr);
    }

    /**
     * Send a DEBUG log message. Level = FINE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void debug(String tag, String msg) 
    {
        logger(tag).logp(Level.FINE, tag, null, msg);
    }

    /**
     * Send a DEBUG log message and log the exception. Level = FINE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void debug(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.FINE, tag, null, msg, tr);
    }

    /**
     * Send an INFO log message. Level = INFO. Level = INFO.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void info(String tag, String msg) 
    {
        logger(tag).logp(Level.INFO, tag, null, msg);
    }

    /**
     * Send a INFO log message and log the exception. Level = INFO.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void info(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.INFO, tag, null, msg, tr);
    }

    /**
     * Send a WARN log message. Level = WARNING.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void warn(String tag, String msg) 
    {
        logger(tag).logp(Level.WARNING, tag, null, msg);
    }

    /**
     * Send a #WARN log message and log the exception. Level = WARNING.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void warn(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.WARNING, tag, null, msg, tr);
    }

    /**
     * Send an ERROR log message. Level = SEVERE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void error(String tag, String msg) 
    {
        logger(tag).logp(Level.SEVERE, tag, null, msg);
    }

    /**
     * Send an ERROR log message and log the exception. Level = SEVERE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void error(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.SEVERE, tag, null, msg, tr);
    }

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
    @Override
    public boolean isLoggable(String tag, Level level) 
    {
        return logger(tag).isLoggable(level);
    }
    
    /**
     * Set logging level. 
     * NOTE IF USING Android Log implementation, this function is not supported natively by Android.
     */
    @Override
    public void setLevel(Level level) 
    {
        logger.setLevel(level);
    }

    /**
     * Get logger referenced by tag. Handle mismatch of tag to this logger's name gracefully.
     * @param tag Used to identify the source of a log message. 
     * @return Logger This logger if tag matches name or tag is empty, otherwise logger obtained by Logger.getLogger(tag).
     */
    protected Logger logger(String tag)
    {
        if (name.equals(tag) || (tag == null) || (tag.length() == 0))
            return logger;
        return Logger.getLogger(tag);
    }

	protected String formatText(String msg, Object... args) {
		if ((args == null) || (args.length == 0)) {
			return msg;
		} else {
			return String.format(msg, args);
		}
	}

}
