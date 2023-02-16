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

/**
 * FieldKey
 * Used by ClassAnalyser to configure foreign collections and foreign fields
 * @author Andrew Bowley
 * 25/05/2014
 */
public class FieldKey implements Comparable<FieldKey>
{
    /** Entity class */
    protected Class<?> entityClass;
    /** Name of field */
    protected String fieldName;
 
    /**
     * Construct a FieldKey Instance
     * @param entityClass Entity class
     * @param fieldName Name of field 
     */
    public FieldKey(Class<?> entityClass, String fieldName)
    {
        this.entityClass = entityClass;
        this.fieldName = fieldName;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param   another The object to be compared.
     * @return  A negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this object.
     */
    @Override
    public int compareTo(FieldKey another) 
    {
        int compareClasses = entityClass.getName().compareTo(another.entityClass.getName());
        return (compareClasses != 0) ? compareClasses : (fieldName.compareTo(another.fieldName));
    }

    /**
     * Returns Class of entity with one or more OneToMany or ManyToOne annotations
     * @return Class
     */
    public Class<?> getEntityClass() 
    {
        return entityClass;
    }

    /**
     * Set class of entity with one or more OneToMany or ManyToOne annotations 
     * @param entityClass Class
     */
    public void setEntityClass(Class<?> entityClass) 
    {
        this.entityClass = entityClass;
    }

    /**
     * Returns name of field
     * @return String
     */
    public String getFieldName() 
    {
        return fieldName;
    }

    /**
     * Set name of  field
     * @param fieldName String
     */
    public void setFieldName(String fieldName) 
    {
        this.fieldName = fieldName;
    }

    /**
     * Returns a hash code value for the object. This method is 
     * supported for the benefit of hashtables such as those provided by 
     * <code>java.util.Hashtable</code>. 
     * @see java.lang.Object#hashCode()
     */ 
    @Override
    public int hashCode()
    {
        return entityClass.hashCode() ^ fieldName.hashCode();
    }
  
    /**
     * Indicates whether some other object is "equal to" this one.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object another)
    {
        if (another instanceof FieldKey)
            return entityClass.equals(((FieldKey) another).entityClass) && 
            		fieldName.equals(((FieldKey) another).fieldName);
        return false;
    }
}
