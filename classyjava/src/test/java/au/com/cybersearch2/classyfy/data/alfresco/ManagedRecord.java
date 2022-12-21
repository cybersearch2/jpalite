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
package au.com.cybersearch2.classyfy.data.alfresco;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * ManagedRecord
 * @author Andrew Bowley
 * 01/05/2014
 */
public abstract class ManagedRecord implements Serializable
{
    private static final long serialVersionUID = 8577297186634353771L;

    @Id @GeneratedValue
    protected int _id;
   
    @Column
    protected int node_id;
    
    @Column
    protected String description;
    
    @Column
    protected Date created;

    @Column
    protected String creator;
    
    @Column(nullable = true)
    protected Date modified;

    @Column(nullable = true)
    protected String modifier;
 
    @Column(nullable = true)
    protected String identifier;

    public abstract void set_id(int _id);

    public abstract void set_nodeId(int node_id);

    public abstract void setDescription(String description);

    public abstract void setCreated(Date created);

    public abstract void setCreator(String creator);

    public abstract void setModified(Date modified);

    public abstract void setModifier(String modifier);

    public abstract void setIdentifier(String identifier);

    public int get_id() 
    {
        return _id;
    }

    public int get_node_id() 
    {
        return node_id;
    }
    
}
