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
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.stmt.QueryBuilder;

import au.com.cybersearch2.classyjpa.entity.OrmEntity;
import au.com.cybersearch2.classyjpa.query.DaoQuery;
import au.com.cybersearch2.classyjpa.query.DaoQuery.SimpleSelectArg;
import au.com.cybersearch2.classyjpa.query.OrmQuery;

/**
 * Node
 * Anchor for a component of a graph. 
 * Each node has a model which identifies what the node contains.
 * The top node of a graph has a special model called "root".
 * @author Andrew Bowley
 * 05/09/2014
 */
public class Node implements Serializable 
{
	public static final String ROOT = "Root";
    /** Prefix for name of query to fetch node by primary key */
    public static final String NODE_BY_PRIMARY_KEY_QUERY = "NodeByPrimaryKey";
	private static final long serialVersionUID = -2122221453077225002L;
	
    /** Node properties defined by model */
    Map<String,Object> properties;
    /** Persistence object */
    NodeBean nodeBean;
    Node parent;
    /** Child nodes list. When this node is fetched from a database, the list may contain place holders with only primary key set */
    List<Node> children;
    /** Flag set true if this node is included in the trunk of a marshalled node */
    boolean isFragment;
    
    /**
     * Create root node. Private default constructor prevents creation of node orphans.
     * To begin constructing a graph, create a top node by calling 
     * static method rootNodeNewInstance(). The graph then grows
     * by using the Node(model, parent) constructor.
     */
    private Node()
    {
    	nodeBean = new NodeBean();
        //nodeBean.setModel(NodeType.ROOT);
        nodeBean.setParent(nodeBean);
        //nodeBean.setLevel(0);
        nodeBean.set_id(0);
        nodeBean.setName(ROOT);
        //nodeBean.setTitle(ROOT);
        parent = this;
    }
 
    /**
     * Returns primary key
     * @return int
     */
    public int getId()
    {
        return nodeBean.get_id();
    }
    
     /**
     * Returns model ordinal value
     * @return int
     */
    public int getModel() 
    {
        return 0; //nodeBean.getModel();
    }
    
    /**
     * Returns node name
     * @return String
     */
    public String getName() 
    {
        return nodeBean.getName();
    }
    
    /**
     * Returns node title (human readable)
     * @return String
     */
    public String getTitle() 
    {
        return ""; //nodeBean.getTitle();
    }
    
    /**
     * Returns depth in graph, starting at 1 for the solitary root node
     * @return int
     */
    public int getLevel() 
    {
        return 0; //nodeBean.getLevel();
    }
    
    

    /**
     * Returns parent node or, if root node, self
     * @return NodeBean
     */
    public Node getParent() 
    {
        return parent;
    }
    
    /**
     * Returns parent primary key
     * @return int
     */
    public int getParentId()
    {
        return nodeBean.getParent().get_id();
    }
    

    /**
     * Construct a Node from its persisted state. 
     * @param nodeBean The persisted object
     * @param parent The parent on the graph under construction or null if this is the first node of the graph
     */
    public Node(NodeBean nodeBean, Node parent)
    {
        if (nodeBean  == null)
            throw new IllegalArgumentException("Parameter nodeBean is null");
        this.nodeBean = nodeBean;
        if (parent != null)
        {
            // Check if a Node with same id already in children list
            // Replace it, if found, as it only a placeholder
            Node existingNode = null;
            for (Node childNode: parent.getChildren())
            {   
                if (childNode.getId() == nodeBean.get_id())
                {
                    existingNode = childNode;
                    break;
                }
            }
            if (existingNode != null)
                parent.getChildren().remove(existingNode);
            // Now add this node to the parent's children list
            parent.getChildren().add(this);
            // Set level to one more than parent's
            setLevel(parent.getLevel() + 1);
        }
        else
        {   // No parent specified, so create a root node to be parent
            parent = new Node();
            parent.setId(nodeBean.getParent().get_id());
            parent.getChildren().add(this);
            //parent.setLevel(nodeBean.level - 1);
        }
    	this.parent = parent;
    	nodeBean.parent = parent.getNodeBean();
        // Transfer nodeBean's chidren to this Node 
        if ((nodeBean.get_children() != null))
        {
            for (NodeBean childEntity: nodeBean.get_children())
            {
                if (childEntity._id != childEntity.getParent().get_id()) // Never add top node as a child
                    new Node(childEntity, this);
            }
        }
    }
    
    /**
     * Create an empty Node object and attach to an existing graph
     * @param model Ordinal of model enum type
     * @param parent Parent node - use Node.rootNodeNewInstance() for first Node in graph
     */
    public Node(int model, Node parent)
    {
        if (parent == null)
            throw new IllegalArgumentException("Parameter parent is null");
    	nodeBean = new NodeBean();
        //nodeBean.setModel(model);
        nodeBean.setParent(parent.getNodeBean());
        //nodeBean.setLevel(parent.getLevel() + 1);
        this.parent = parent;
        parent.getChildren().add(this);
    }
    
    /**
     * Set properties
     * @param properties Map&lt;String, Object*gt;
     */
    public void setProperties(Map<String, Object> properties) 
    {
        this.properties = properties;
    }
   
    /** 
     * Returns children list
     * @return List&lt;Node&gt;
     */
    public List<Node> getChildren()
    {
        if (isFragment && (children != null) && children.size() > 1)
        {
        	
        	for (Node child: children)
        		if (child.isFragment) {
        			ArrayList<Node> fragmentList = new ArrayList<>();
        			fragmentList.add(child);
        			return fragmentList;
        		}
        	// Continue if child fragment not found. 
        }
        if (children == null)
            children = new ArrayList<>();
        return children;
    }
    
    /**
     * Returns properties
     * @return Map&lt;String,Object&gt;
     */
    public Map<String,Object> getProperties()
    {
        if (properties == null)
            properties = new HashMap<>();
        return properties;
    }

    protected NodeBean getNodeBean()
    {
    	return nodeBean;
    }

    /**
     * Sets depth in graph, starting at 1 for the solitary root node
     * @param level int
     */
    protected void setLevel(int level) 
    {
        //nodeBean.setLevel(level);
    }

    /**
     * Set primary key
     * @param id int
     */
    protected void setId(int id)
    {
        nodeBean.set_id(id);
    }

    /**
     * Returns root Node object
     * @return Node
     */
    public static Node rootNodeNewInstance()
    {
        return new Node();
    }
   
    /**
     * Returns property value as quote-delimited text for logging perposes 
     * @param node Node from which to extract property
     * @param key Property name
     * @param defaultValue Value to return if property not found 
     * @return String
     */
    public static String getProperty(Node node, String key, String defaultValue)
    {
        Object object = (node.properties == null ? null : node.properties.get(key));
        if (object == null)
        {
            if (defaultValue == null)
                return "null";
            else
                return "'" + defaultValue + "'";
        }
        return "'" + object.toString() + "'";
    }
    
    /**
     * Marshall a nodeBean object into a graph fragment containing all ancestors and immediate children.
     * Deletes children of other Nodes in graph to prevent triggering lazy fetches and thus potentially fetching the entire graph
     * @param nodeBean The object to marshall
     * @return Root node of graph
     */
    public static Node marshall(NodeBean nodeBean)
    {
        Deque<NodeBean> nodeBeanDeque = new ArrayDeque<NodeBean>();
        // Walk up to top node
        while (nodeBean != null)
        {
             nodeBeanDeque.add(nodeBean);
             NodeBean parentBean = nodeBean.getParent();
             if (parentBean == null) { // Encountered root node pending merge commit
            	 nodeBean.setParent(nodeBean);
            	 break;
             }
             if  (nodeBean.get_id() == parentBean.get_id())// Top of tree indicated by self parent
                break;
             nodeBean = nodeBean.getParent();
        }
        // Now build graph fragment
        Node node = Node.rootNodeNewInstance();
        node.isFragment = true;
        Iterator<NodeBean> nodeBeanIterator = nodeBeanDeque.descendingIterator();
        while (nodeBeanIterator.hasNext())
        {
            nodeBean = nodeBeanIterator.next();
            node = new Node(nodeBean, node);
            node.isFragment = true;
        }
        if (nodeBean.get_children().isEmpty()) {
        }
        	 
        return node;
    }

    public <T extends OrmEntity> DaoQuery<T> generateQuery(OrmQuery<T> ormQuery, Integer parentId)
            throws SQLException 
    {   // Only one select argument required for primary key 
        final SimpleSelectArg nodeIdArg = new SimpleSelectArg();
        nodeIdArg.setValue(parentId);
        // Set primary key column name
        nodeIdArg.setMetaInfo("_parent_id");
        return new DaoQuery<T>(ormQuery, nodeIdArg){

            /**
             * Update supplied QueryBuilder object to add where clause
             * @see au.com.cybersearch2.classyjpa.query.DaoQuery#buildQuery(com.j256.ormlite.stmt.QueryBuilder)
             */
            @Override
            public QueryBuilder<T, Integer> buildQuery(QueryBuilder<T, Integer> queryBuilder)
                    throws SQLException {
                // build a query with the WHERE clause set to 'node_id = ?'
                queryBuilder.where().eq("_parent_id", nodeIdArg);
                return queryBuilder;
            }};
    }
}
