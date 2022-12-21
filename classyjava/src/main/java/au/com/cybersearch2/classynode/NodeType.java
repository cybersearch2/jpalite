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
package au.com.cybersearch2.classynode;

/**
 * NodeType
 * A Node is the anchor for a component of a graph. 
 * Each node has a model which identifies what the node contains.
 * The top node of a graph has a special model called "root".
 * @author Andrew Bowley
 * 05/09/2014
 */
public interface NodeType<T extends Enum<T>>
{
    static final int ROOT = 0;
    static final String ROOT_NAME = "root";
    
    /**
     * Returns the root mode, which has name defined as ROOT static constant 
     * @return The an enum constant
     */
    T root(); 
    
    /**
     * Returns the model with the specified name
     *
     * @param name The name of the constant value to find.
     * @return An enum constant
     * @throws IllegalArgumentException
     *             if {@code name} is {@code null} or does not
     *             have a constant value called {@code name}
     */
    T valueOf(final String name);
    
    /**
     * Returns the model with the specified ordinal value
     *
     * @param ordinal The ordinal of the constant value to find.
     * @return An enum constant
     * @throws IllegalArgumentException
     *             if {@code name} is {@code null} or does not
     *             have a constant value called {@code name}
     */
    T valueOf(final int ordinal);

}
