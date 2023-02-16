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

/**
 * Enumerates settings
 */
public enum JpaSetting {

	schema_filename("schemaFilename", "SQL file to create database"),
	drop_schema_filename("dropSchemaFilename", "SQL file to drop database"),
	data_filename("dataFilename", "SQL file to populate database"),
	upgrade_filename("upgradeFilename", "SQL file to upgrade database"),
	database_version("databaseVersion", "Database version"),
	database_name("databaseName", "Database name"),
	user_transactions("userTransations", "Transactions performed by user"),
	open_helper_class("openHelperClass", "Open helper callback classname");
	
	private final String key;
	private final String description;
	
	private JpaSetting(String key, String description) {
		this.key = key;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}
}
