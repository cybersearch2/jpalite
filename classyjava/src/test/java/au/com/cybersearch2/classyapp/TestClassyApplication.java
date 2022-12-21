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
package au.com.cybersearch2.classyapp;

import au.com.cybersearch2.classyfy.data.Model;
import au.com.cybersearch2.classynode.Node;

/**
 * TestClassyApplication
 * @author Andrew Bowley
 * 13/06/2014
 */
public class TestClassyApplication
{
    public static final String PU_NAME = "classyfy";
    public static final String DATABASE_NAME = "classyfy.db";
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + Model.recordCategory.ordinal();
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + Model.recordFolder.ordinal();
}
