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
package au.com.cybersearch2.classyjpa.persist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.j256.ormlite.logger.Logger;
import au.com.cybersearch2.classylog.LogManager;

/**
 * PersistenceXmlParser Parses persistence.xml to create object which maps each
 * persistence unit data to it's name. NOTE: Only data used by ClassyTools is
 * extracted
 * 
 * @author Andrew Bowley 11/05/2014
 */
public class PersistenceXmlParser {
	private static Logger logger = LogManager.getLogger(PersistenceXmlParser.class);

	private XmlPullParser xpp;

	/**
	 * Create PersistenceXmlParser object
	 * 
	 * @throws XmlPullParserException - unexpected error creating parser instance
	 */
	public PersistenceXmlParser() throws XmlPullParserException {
		XmlPullParserFactory factory;
		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		xpp = factory.newPullParser();
	}

	/**
	 * Returns object to which input stream for persistence.xml is unmarshalled
	 * 
	 * @param stream InputStream
	 * @return Map&lt;String, PersistenceUnitInfo&gt; - maps each persistence unit
	 *         data to it's name
	 */
	public Map<String, PersistenceUnitInfo> parsePersistenceXml(InputStream stream) {
		Map<String, PersistenceUnitInfo> result = new HashMap<>();
		Reader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			xpp.setInput(reader);
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					// System.out.println("Start document");
				} else if (eventType == XmlPullParser.START_TAG) {
					if ("persistence-unit".equals(xpp.getName())) { // PersistenceUnitAdmin unit element
						String name = getAttribute("name");
						if (name != null)
							result.put(name, parsePersistenceUnit(name));
					}
				}
				/*
				 * else if (eventType == XmlPullParser.END_TAG) {
				 * System.out.println("End tag "+xpp.getName()); } else if (eventType ==
				 * XmlPullParser.TEXT) { System.out.println("Text "+xpp.getText()); }
				 */
				eventType = xpp.next();
			}
		} catch (XmlPullParserException e) {
			logger.error("Error parsing persistence.xml", e);
		} catch (IOException e) {
			logger.error("Error reading persistence.xml", e);
		}
		return result;
	}

	/**
	 * Returns persistence unit data
	 * 
	 * @param puName PersistenceUnitAdmin unit name
	 * @return PersistenceUnitInfo
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private PersistenceUnitInfo parsePersistenceUnit(String puName) throws XmlPullParserException, IOException {
		PersistenceUnitInfoImpl pu = new PersistenceUnitInfoImpl(puName);
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.END_TAG) { // PersistenceUnitAdmin unit end element
				if ("persistence-unit".equals(xpp.getName()))
					return pu;
			} else if (eventType == XmlPullParser.START_TAG) {
				if ("provider".equals(xpp.getName())) { // Provider class name
					pu.persistenceProviderClassName = getText();
				} else if ("class".equals(xpp.getName())) { // Entity class name
					String className = getText();
					if (className.length() > 0)
						pu.managedClassNames.add(className);
				} else if ("property".equals(xpp.getName())) { // Property
					String name = getAttribute("name");
					if ((name != null) && (name.length() > 0))
						pu.getProperties().setProperty(name, getAttribute("value"));
				}
			}
			eventType = xpp.next();
		}
		return pu;
	}

	/**
	 * Returns text inside current element
	 * 
	 * @return String
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private String getText() throws XmlPullParserException, IOException {
		if (xpp.next() == XmlPullParser.TEXT)
			return xpp.getText();
		return "";
	}

	/**
	 * Returns attribute value for specified attribute in current element
	 * 
	 * @param name Attribute name
	 * @return String
	 */
	String getAttribute(String name) {
		for (int i = 0; i < xpp.getAttributeCount(); i++)
			if (xpp.getAttributeName(i).equals(name))
				return xpp.getAttributeValue(i);
		return null;
	}

}
