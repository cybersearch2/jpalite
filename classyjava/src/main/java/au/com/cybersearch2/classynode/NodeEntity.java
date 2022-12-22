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

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;

/**
 * NodeEntity
 * Persistent anchor for a component of a graph. 
 * Each node has a model which identifies what the node contains and is mapped by ordinal to an enumeration constant.
 * The top node of a graph has a special model called "root".
 * Graph parent and child relationships are expressed as OneToOne and OneToMany respectively.
 * @author Andrew Bowley
 * 05/09/2014
 */
@Entity(name = "nodes")
public class NodeEntity implements OrmEntity, Serializable
{
    private static final long serialVersionUID = -5476538034094591968L;
    
    @Id @GeneratedValue
    int _id;
    @Column(nullable = false)
    int model;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    String title;
    @Column(nullable = false)
    int level;
    @Column(nullable = false)
    int _parent_id;
    @OneToOne
    @JoinColumn(name="_parent_id", referencedColumnName="_id", unique=true)
    NodeEntity parent;
    @OneToMany(mappedBy="_parent_id", fetch=FetchType.EAGER)
    // With foreign collections, OrmLite always uses primary id on the "one" side
    // and on the "many" side, defaults to to first field with type matching collection generic type.
    // If "mappedby" is specifed, this overrides the default, but the field type must still match.
    Collection<NodeEntity> _children;

    /**
     * Returns primary key
     * @return int
     */
    public int get_id()
    {
        return _id;
    }
    /**
     * Set primary key
     * @param _id int
     */
    public void set_id(int _id)
    {
        this._id = _id;
    }
    
    /**
     * Returns model ordinal value
     * @return int
     */
    public int getModel() 
    {
        return model;
    }
    
    /**
     * Set model ordinal value
     * @param model int
     */
    public void setModel(int model) 
    {
        this.model = model;
    }
    
    /**
     * Returns node name
     * @return String
     */
    public String getName() 
    {
        return name;
    }
    
    /**
     * Set node name (computer friendly)
     * @param name String
     */
    public void setName(String name) 
    {
        this.name = name;
    }
    
    /**
     * Returns node title (human readable)
     * @return String
     */
    public String getTitle() 
    {
        return title;
    }
    
    /**
     * Set node title (human readable)
     * @param title String
     */
    public void setTitle(String title) 
    {
        this.title = title;
    }
    
    /**
     * Returns depth in graph, starting at 1 for the solitary root node
     * @return int
     */
    public int getLevel() 
    {
        return level;
    }
    
    /**
     * Sets depth in graph, starting at 1 for the solitary root node
     * @param level int
     */
    public void setLevel(int level) 
    {
        this.level = level;
    }
    
    /**
     * Returns parent node or, if root node, self
     * @return NodeEntity
     */
    public NodeEntity getParent() 
    {
        return parent;
    }
    
    /**
     * Sets parent node
     * @param parent NodeEntity
     */
    public void setParent(NodeEntity parent) 
    {
        this.parent = parent;
    }
    
    /**
     * Returns child nodes
     * @return Collection&lt;NodeEntity&gt;
     */
    public Collection<NodeEntity> get_children() 
    {
        return _children;
    }
    
    /**
     * Sets child nodes
     * @param _children Collection&lt;NodeEntity&gt;
     */
    public void set_children(Collection<NodeEntity> _children) 
    {
        this._children = _children;
    }
    
    /**
     * Returns parent primary key
     * @return int
     */
    public int get_parent_id()
    {
        return _parent_id;
    }
    
    /**
     * Sets parent primary key
     * @param _parent_id int
     */
    public void set_parent_id(int _parent_id)
    {
        this._parent_id = _parent_id;
    }
    
}
