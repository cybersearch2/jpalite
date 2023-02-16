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
package au.com.cybersearch2.node;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

@Entity(name = "tableNode")
public class NodeBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue
    int _id;
    @Column(nullable = false)
    String name;
	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "_parent_id")
    NodeBean parent;
	@ForeignCollectionField
	private ForeignCollection<NodeBean> _children;

    
    /*
    @OneToOne
    @JoinColumn(name="_parent_id", referencedColumnName="_id")
     */
    
    NodeBean() {
    }
    
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
     * Returns parent node or, if root node, self
     * @return NodeEntity
     */
    public NodeBean getParent() 
    {
        return parent;
    }
    
    /**
     * Sets parent node
     * @param parent NodeEntity
     */
    public void setParent(NodeBean parent) 
    {
        this.parent = parent;
    }
    
    /**
     * Returns child nodes
     * @return Collection&lt;NodeEntity&gt;
     */
    public ForeignCollection<NodeBean> get_children() 
    {
         return _children;
    }
    
    /**
     * Sets child nodes
     * @param _children Collection&lt;NodeEntity&gt;
     */
    public void set_children(Collection<NodeBean> _children) 
    {
    	get_children().addAll(_children);
    }
}
