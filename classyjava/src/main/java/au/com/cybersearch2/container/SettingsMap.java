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
package au.com.cybersearch2.container;

import java.util.EnumSet;

public class SettingsMap {

	private final EnumSet<JpaSetting> settingsSet;
	private String schemaFilename;
	private String dropSchemaFilename;
	private String dataFilename;
	private String upgradeFilename;
	private String databaseVersion;
	private String databaseName;
	private String userTransations;
	private String openHelperClass;
	
	public SettingsMap() {
	    this.settingsSet = EnumSet.noneOf(JpaSetting.class);
	}

	public void put(JpaSetting key, String value) {
		if ((value == null) || value.isEmpty())
			throw new IllegalArgumentException("Value parameter is null or empty");
		switch(key) {
		case schema_filename: schemaFilename = value; break;
		case drop_schema_filename: dropSchemaFilename = value; break;
		case data_filename: dataFilename = value; break;
		case upgrade_filename: upgradeFilename = value; break;
		case database_version: databaseVersion = value; break;
		case database_name: databaseName = value; break;
		case user_transactions: userTransations = value; break;
		case open_helper_class: openHelperClass = value; break;
		}
		if (!settingsSet.contains(key))
		    settingsSet.add(key);
	}

	public boolean hasSetting(JpaSetting key) {
		return settingsSet.contains(key);
	}
	
	public String get(JpaSetting key) {
		if (hasSetting(key)) // Avoid returning null
			switch(key) {
			case schema_filename: return schemaFilename;
			case drop_schema_filename: return dropSchemaFilename;
			case data_filename: return dataFilename;
			case upgrade_filename: return upgradeFilename;
			case database_version: return databaseVersion;
			case database_name: return databaseName;
			case user_transactions: return userTransations;
			case open_helper_class: return openHelperClass;
			}
		return "";
	}
	
	public EnumSet<JpaSetting> getKeySet() {
		return EnumSet.complementOf(settingsSet);
	}

	public int size() {
		return settingsSet.size();
	}
}
