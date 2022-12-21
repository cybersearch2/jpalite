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
package au.com.cybersearch2.classyfy.data;

/**
 * Model
 * @author Andrew Bowley
 * 14/04/2014
 */
public enum Model
{
   root, 
   recordCategory, // Alfresco Records managemement
   recordFolder ;  // Alfresco Records managemement
   
   public static Model getModelByName(String name)
   {
       if (name == null)
           throw new IllegalArgumentException("Parameter \"name\" is null");
       return valueOf(Model.class, name);
   }
}
