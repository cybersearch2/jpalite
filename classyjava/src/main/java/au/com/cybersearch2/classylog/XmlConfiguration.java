/** Copyright 2023 Andrew J Bowley

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import com.j256.ormlite.logger.Level;

/**
 * Reads XML file in adapted Log4j format and generates a Java Util configuration Properties object
 */
public class XmlConfiguration {

	private static final String CONFIGURATION = "Configuration";
	private static final String APPENDERS = "Appenders";
	private static final String CONSOLE = "Console";
	private static final String FILE = "File";
	private static final String PROPERTY = "Property";
	private static final String PROPERTIES = "Properties";
	private static final String LOGGERS = "Loggers";
	private static final String LOGGER = "Logger";
	private static final String ROOT = "Root";
	private static final String APPENDERREF = "AppenderRef";
	
	private static final String UNKNOWN_ELEMENT = "Logger config unknown or unexpected element %s";
	private static final String MISSING_ATTRIBUTE = "Logger config element %s missing attribute %s";
	private static final String MISSING_ELEMENT ="Logger config missing element %s";
	
    private final XmlPullParser xpp;
    private final Set<String> handlerSet;

    /**
     * Construct XmlConfiguration object
     * @throws XmlPullParserException
     */
    public XmlConfiguration() throws XmlPullParserException  {
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        handlerSet = new HashSet<>();
    }
    
    /**
     * Returns configuration properties
     * @param stream InputStream
     * @return Properties object
     * @throws XmlPullParserException 
     * @throws IOException 
     */
    public Properties parseXmlConfiguration(InputStream stream) throws XmlPullParserException, IOException
    {
    	Properties props = new Properties();
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        xpp.setInput(reader);
        boolean foundConfiguration = false;
        int eventType = xpp.getEventType();
        while(true) {
        	if (eventType == XmlPullParser.END_DOCUMENT)
        		break;
            if (eventType == XmlPullParser.START_DOCUMENT) 
            {
                //System.out.println("Start document");
            } 
            else if (eventType == XmlPullParser.START_TAG) 
            {
            	foundConfiguration = CONFIGURATION.equals(xpp.getName());
                if (foundConfiguration && !parseChildren1(props))
                    break;
            } 
            /*
            else if (eventType == XmlPullParser.END_TAG) 
            {
                System.out.println("End tag "+xpp.getName());
            } 
            else if (eventType == XmlPullParser.TEXT) 
            {
                System.out.println("Text "+xpp.getText());
            }
            */
            eventType = xpp.next();
        }
        if (handlerSet.size() > 0) {
	        StringBuilder builder = new StringBuilder();
	        handlerSet.forEach(handler -> {
	        	if (builder.length() > 0) 
	        		builder.append(",");
	        	builder.append(handler);
	        });
	        props.setProperty("handlers", builder.toString());
        }
        if (!foundConfiguration)
        	System.err.println(String.format(MISSING_ELEMENT, CONFIGURATION));
        return props;
    }

    private boolean parseChildren1(Properties props) throws XmlPullParserException, IOException {
    	boolean ok = false;
        int eventType = xpp.next();
        while(true) {
        	if (eventType == XmlPullParser.END_DOCUMENT)
        		break;
            if (eventType == XmlPullParser.END_TAG) 
            {   // PersistenceUnitAdmin unit end element 
                if (CONFIGURATION.equals(xpp.getName())) {
                    ok = true;
                    break;
                }
            } 
            else if (eventType == XmlPullParser.START_TAG) {
            	boolean appenders = APPENDERS.equals(xpp.getName());
            	boolean loggers = !appenders && LOGGERS.equals(xpp.getName());
            	boolean properties = !appenders && !loggers && PROPERTIES.equals(xpp.getName());
            	if (appenders && !parseAppenders(props)) {
            		ok = false;
            		break;
            	}
            	else if (loggers && !parseLoggers(props)) {
         		    ok = false;
        		    break;
            	}
            	else if (properties && !parseProperties(props)) {
            		ok = false;
            		break;
            	} else if (!appenders && !loggers && !properties)
                	System.err.println(String.format(UNKNOWN_ELEMENT, xpp.getName()));
            }
            eventType = xpp.next();
        }
        return ok;
	}

    private boolean parseAppenders(Properties props) throws XmlPullParserException, IOException {
    	boolean ok = false;
        int eventType = xpp.next();
        while(true) {
        	if (eventType == XmlPullParser.END_DOCUMENT)
        		break;
            if (eventType == XmlPullParser.END_TAG) {    
                if (APPENDERS.equals(xpp.getName())) {
                    ok = true;
                    break;
                }
            } else if (eventType == XmlPullParser.START_TAG) {
            	if (CONSOLE.equals(xpp.getName())) {
                	//System.out.println("Console element found");
            		String handler = "java.util.logging.ConsoleHandler";
                	handlerSet.add(handler);
            		ok = parseTerminals(handler, new String[] {"Filter","Layout","Level","Encoding"}, CONSOLE, props);
            	} else if (FILE.equals(xpp.getName())) {
                	//System.out.println("Console element found");
            		String handler = "java.util.logging.FileHandler";
                	handlerSet.add(handler);
            		ok = parseTerminals(handler, 
            				            new String[] {"Filter","Layout","Level","Encoding","Count","Append","Pattern","Limit","MaxLocks"}, 
            				            FILE, props);
            	} else
                	System.err.println(String.format(UNKNOWN_ELEMENT, xpp.getName()));

            	if (!ok)
                	break;
            }
            eventType = xpp.next();
        } 
        return ok;
	}
    
    private boolean parseTerminals(String prefix, String[] names, String endTag, Properties props) throws XmlPullParserException, IOException {
    	boolean ok = false;
    	if (!prefix.isEmpty())
    	    prefix += ".";
    	List<String> nameList = Arrays.asList(names);
        int eventType = xpp.next();
        while(true) {
        	if (eventType == XmlPullParser.END_DOCUMENT)
        		break;
            if (eventType == XmlPullParser.END_TAG) 
            {   // PersistenceUnitAdmin unit end element 
                if (endTag.equals(xpp.getName())) {
                    ok = true;
                    break;
                }
            } 
            else if (eventType == XmlPullParser.START_TAG) {
            	boolean nameFound = false;
            	for (String name: nameList)
            	    if (name.equals(xpp.getName())) {
            	    	if (name.equals("Layout"))
            	    		name = "formatter";
            	    	else if (name.equals("UseParentHandlers"))
            	    		name = "useParentHandlers";
            	    	else
            	    		name = name.toLowerCase();
                        String value = getText();
                        if ("Level".equals(name))
                        	value = converToJavaUtiltLevel(value);
                        props.setProperty(prefix + name, value);
                        nameFound = true;
                        break;
                	    //System.out.println(String.format("Property %s = %s", prefix + name, value));
            	    }
            	if (!nameFound && APPENDERREF.equals(xpp.getName())) {
            		nameFound = true;
            		String ref = getAttribute("ref");
            		if (ref != null) {
            			String handler;
            			switch (ref) {
            			case "Console": handler = "java.util.logging.ConsoleHandler"; break;
            			case "File": handler = "java.util.logging.FileHandler"; break;
            			default: handler = "";
            			}
            			if (!handler.isEmpty())
            				props.setProperty(prefix + ".handlers", handler);
            		} else
            	        System.err.println(String.format(MISSING_ATTRIBUTE, APPENDERREF, "ref"));
            	}
            	if (!nameFound)
                	System.err.println(String.format(UNKNOWN_ELEMENT, xpp.getName()));
            }
            eventType = xpp.next();
        } 
        return ok;
	}
    
    private boolean parseProperties(Properties props) throws XmlPullParserException, IOException {
    	boolean ok = false;
        int eventType = xpp.next();
        while(true) {
        	if (eventType == XmlPullParser.END_DOCUMENT)
        		break;
            if (eventType == XmlPullParser.END_TAG) {    
                if (PROPERTIES.equals(xpp.getName())) {
                    ok = true;
                    break;
                }
            } else if (eventType == XmlPullParser.START_TAG) {
            	if (PROPERTY.equals(xpp.getName())) {
                	//System.out.println("Property element found");
            		String key = getAttribute("name");
            		if (key != null)
            			props.setProperty(key, getText());
            		else
            	        System.err.println(String.format(MISSING_ATTRIBUTE, PROPERTY, "name"));
            	} else
                	System.err.println(String.format(UNKNOWN_ELEMENT, xpp.getName()));

            }
            eventType = xpp.next();
        } 
        return ok;
	}
    
    private boolean parseLoggers(Properties props) throws XmlPullParserException, IOException {
    	boolean ok = false;
        int eventType = xpp.next();
        while(true) {
        	if (eventType == XmlPullParser.END_DOCUMENT)
        		break;
            if (eventType == XmlPullParser.END_TAG) {    
                if (LOGGERS.equals(xpp.getName())) {
                    ok = true;
                    break;
                }
            } else if (eventType == XmlPullParser.START_TAG) {
		    	if (LOGGER.equals(xpp.getName())) {
		        	String name = getAttribute("name");
		        	if (name == null) 
	           	        System.err.println(String.format(MISSING_ATTRIBUTE, LOGGER, "name"));
		        	else if (!parseTerminals(name, new String[] {"UseParentHandlers","Level"}, LOGGER, props)) {
		     		    ok = false;
		    		    break;
		        	} 
		    	} else if (ROOT.equals(xpp.getName())) {
		        	String level = getAttribute("level");
		        	if (level != null) { 
		        		props.setProperty(".level", converToJavaUtiltLevel(level));
		        		ok = parseTerminals("", new String[] {}, ROOT, props);
		        		if (props.containsKey("handlers")) 
		        			handlerSet.clear();
		     		    if (!ok)
		    		        break;
		        	}
		    	} else
                	System.err.println(String.format(UNKNOWN_ELEMENT, xpp.getName()));

            }
            eventType = xpp.next();
    	}
        return ok;
	}
    
    /**
     * Returns text inside current element
     * @return String
     * @throws XmlPullParserException
     * @throws IOException
     */
    private String getText() throws XmlPullParserException, IOException 
    {
        if (xpp.next() ==  XmlPullParser.TEXT)
            return xpp.getText();
        return "";
    }

    /**
     * Returns attribute value for specified attribute in current element
     * @param name Attribute name
     * @return String
     */
    String getAttribute(String name)
    {
        for (int i = 0; i < xpp.getAttributeCount(); i++)
            if (xpp.getAttributeName(i).equals(name)) 
                return xpp.getAttributeValue(i);
        return null;
    }

    private String converToJavaUtiltLevel(String level) {
		if (level.equals(Level.TRACE.name()))
			return java.util.logging.Level.FINER.getName();
		else if (level.equals(Level.DEBUG.name()))
			return java.util.logging.Level.FINE.getName();
		else if (level.equals(Level.ERROR.name()) ||
		         level.equals(Level.FATAL.name()))
			return java.util.logging.Level.SEVERE.getName();
		return level;
    }
}
