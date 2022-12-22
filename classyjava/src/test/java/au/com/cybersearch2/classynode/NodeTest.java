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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import au.com.cybersearch2.classyfy.data.Model;

/**
 * NodeTest
 * @author Andrew Bowley
 * 10/09/2014
 */
public class NodeTest
{
    final int NODE_ID = 123;
    final int CHILD_ID = 124;
    final int PARENT_NODE_ID = 97;
    final int NODE_LEVEL = 1;
    final String NODE_NAME = "Information_Technology";
    final String NODE_TITLE = "Information Technology";
    final String CHILD_NAME = "Mobile_Plans";
    final String CHILD_TITLE = "Mobile Plans";
    
    @Test
    public void test_rootNodeNewInstance()
    {
        Node node = Node.rootNodeNewInstance();
        assertThat(Model.values()[node.getModel()]).isEqualTo(Model.root);
        assertThat(node.getParent()).isEqualTo(node);
        assertThat(node.getLevel()).isEqualTo(1);
        assertThat(node.getId()).isEqualTo(0);
        assertThat(node.getParentId()).isEqualTo(0);
    }
    
    @Test
    public void test_NodeEntity_constructor_null_parent()
    {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setModel(Model.recordCategory.ordinal());
        nodeEntity.set_id(NODE_ID);
        nodeEntity.set_parent_id(PARENT_NODE_ID);
        nodeEntity.setName(NODE_NAME);
        nodeEntity.setTitle(NODE_TITLE);
        nodeEntity.setLevel(NODE_LEVEL);
        NodeEntity parent = new NodeEntity();
        parent.setModel(Model.recordCategory.ordinal());
        parent.set_id(PARENT_NODE_ID);
        nodeEntity.setParent(parent);
        NodeEntity child = new NodeEntity();
        child.setModel(Model.recordFolder.ordinal());
        child.set_id(CHILD_ID);
        child.setName(CHILD_NAME);
        child.setTitle(CHILD_TITLE);
        List<NodeEntity> childList = new ArrayList<>();
        childList.add(child);
        childList.add(Node.rootNodeNewInstance().nodeEntity); // This one should be ignored
        nodeEntity.set_children(childList);
        Node node = new Node(nodeEntity, null);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(node.getName()).isEqualTo(NODE_NAME);
        assertThat(node.getTitle()).isEqualTo(NODE_TITLE);
        assertThat(node.getId()).isEqualTo(NODE_ID);
        assertThat(node.getParentId()).isEqualTo(PARENT_NODE_ID);
        assertThat(node.getParent().getModel()).isEqualTo(Model.root.ordinal());
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getProperties()).isNotNull();
        assertThat(node.getProperties().size()).isEqualTo(0);
        assertThat(node.getParent().getLevel()).isEqualTo(NODE_LEVEL - 1);
        List<Node> children = node.getChildren();
        assertThat(children.size()).isEqualTo(1);
        Node childNode = children.get(0);
        assertThat(childNode.getId()).isEqualTo(CHILD_ID);
        assertThat(childNode.getModel()).isEqualTo(Model.recordFolder.ordinal());
        assertThat(childNode.getName()).isEqualTo(CHILD_NAME);
        assertThat(childNode.getTitle()).isEqualTo(CHILD_TITLE);
        assertThat(childNode.getParent()).isEqualTo(node);
    }
    
    @Test
    public void test_NodeEntity_constructor()
    {
        Node parent = mock(Node.class);
        when(parent.getId()).thenReturn(PARENT_NODE_ID);
        List<Node> childList = new ArrayList<>();
        when(parent.getChildren()).thenReturn(childList);
        when(parent.getLevel()).thenReturn(NODE_LEVEL - 1);
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setModel(Model.recordCategory.ordinal());
        nodeEntity.set_id(NODE_ID);
        nodeEntity.set_parent_id(PARENT_NODE_ID);
        nodeEntity.setName(NODE_NAME);
        nodeEntity.setTitle(NODE_TITLE);
        nodeEntity.setLevel(NODE_LEVEL);
        NodeEntity entityParent = new NodeEntity();
        entityParent.setModel(Model.recordCategory.ordinal());
        entityParent.set_id(PARENT_NODE_ID);
        nodeEntity.setParent(entityParent);
        Node node = new Node(nodeEntity, parent);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(node.getName()).isEqualTo(NODE_NAME);
        assertThat(node.getTitle()).isEqualTo(NODE_TITLE);
        assertThat(node.getId()).isEqualTo(NODE_ID);
        assertThat(node.getParentId()).isEqualTo(PARENT_NODE_ID);
        assertThat(node.getParent()).isEqualTo(parent);
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getProperties()).isNotNull();
        assertThat(node.getProperties().size()).isEqualTo(0);
        assertThat(childList.get(0)).isEqualTo(node);
    }

    @Test
    public void test_NodeEntity_constructor_existing_child()
    {
        Node parent = mock(Node.class);
        when(parent.getId()).thenReturn(PARENT_NODE_ID);
        List<Node> childList = new ArrayList<>();
        Node childNode = mock(Node.class);
        when(childNode.getId()).thenReturn(NODE_ID);
        childList.add(childNode);
        when(parent.getChildren()).thenReturn(childList);
        when(parent.getLevel()).thenReturn(NODE_LEVEL - 1);
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setModel(Model.recordCategory.ordinal());
        nodeEntity.set_id(NODE_ID);
        nodeEntity.set_parent_id(PARENT_NODE_ID);
        nodeEntity.setName(NODE_NAME);
        nodeEntity.setTitle(NODE_TITLE);
        nodeEntity.setLevel(NODE_LEVEL);
        NodeEntity entityParent = new NodeEntity();
        entityParent.setModel(Model.recordCategory.ordinal());
        entityParent.set_id(PARENT_NODE_ID);
        nodeEntity.setParent(entityParent);
        Node node = new Node(nodeEntity, parent);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(node.getName()).isEqualTo(NODE_NAME);
        assertThat(node.getTitle()).isEqualTo(NODE_TITLE);
        assertThat(node.getId()).isEqualTo(NODE_ID);
        assertThat(node.getParentId()).isEqualTo(PARENT_NODE_ID);
        assertThat(node.getParent()).isEqualTo(parent);
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getProperties()).isNotNull();
        assertThat(node.getProperties().size()).isEqualTo(0);
        assertThat(childList.get(0)).isEqualTo(node);
    }
    
    @Test
    public void test_NodeEntity_constructor_null_entity()
    {
        try
        {
            new Node(null, null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter nodeEntity is null");
        }
    }
    
    @Test
    public void test_model_constructor()
    {
        Node parent = mock(Node.class);
        List<Node> childList = new ArrayList<>();
        when(parent.getChildren()).thenReturn(childList);
        when(parent.getLevel()).thenReturn(NODE_LEVEL - 1);
        Node node = new Node(Model.recordCategory.ordinal(), parent);
        assertThat(childList.size()).isEqualTo(1);
        assertThat(childList.get(0)).isEqualTo(node);
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
    }
    
    @Test
    public void test_get_property()
    {
        Node node = Node.rootNodeNewInstance();
        assertThat(Node.getProperty(node, "key", null)).isEqualTo("null");
        assertThat(Node.getProperty(node, "key", "value")).isEqualTo("'value'");
        node.setProperties(new HashMap<>());
        assertThat(Node.getProperty(node, "key", null)).isEqualTo("null");
        assertThat(Node.getProperty(node, "key", "value")).isEqualTo("'value'");
        node.getProperties().put("key", "superlative");
        assertThat(Node.getProperty(node, "key", "value")).isEqualTo("'superlative'");
        assertThat(Node.getProperty(node, "key", null)).isEqualTo("'superlative'");
        node.getProperties().put("level", Integer.valueOf(NODE_LEVEL));
        assertThat(Node.getProperty(node, "level", null)).isEqualTo("'" + NODE_LEVEL + "'");
    }
    
    @Test
    public void test_marshall()
    {
        NodeEntity parent = new NodeEntity();
        parent.setModel(Model.recordCategory.ordinal());
        parent.set_id(PARENT_NODE_ID);
        Node parentNode = new Node(parent, null);
        NodeEntity level2Entity = new NodeEntity();
        level2Entity.setModel(Model.recordFolder.ordinal());
        level2Entity.set_id(NODE_ID);
        level2Entity.setName(NODE_NAME);
        level2Entity.setTitle(NODE_TITLE);
        level2Entity.setLevel(NODE_LEVEL);
        Node level2Node = new Node(level2Entity, parentNode);
        assert(level2Node.getParentId() == parentNode.getId());
        NodeEntity child1 = new NodeEntity();
        child1.setModel(Model.recordFolder.ordinal());
        child1.set_id(CHILD_ID + 1);
        child1.setName(CHILD_NAME);
        child1.setTitle(CHILD_TITLE);
        List<NodeEntity> childList1 = new ArrayList<>();
        childList1.add(child1);
        //child1._parent_id = parent.get_id();
        parent.set_children(childList1);
        NodeEntity child2 = new NodeEntity();
        child2.setModel(Model.recordFolder.ordinal());
        child2.set_id(CHILD_ID + 2);
        child2.setName(CHILD_NAME);
        child2.setTitle(CHILD_TITLE);
        List<NodeEntity> childList2 = new ArrayList<>();
        childList2.add(child2);
        child2.set_parent_id(child1.get_id());
        child1.set_children(childList2);
        NodeEntity child = new NodeEntity();
        child.setModel(Model.recordFolder.ordinal());
        child.set_id(CHILD_ID);
        child.setName(CHILD_NAME);
        child.setTitle(CHILD_TITLE);
        List<NodeEntity> childList = new ArrayList<>();
        childList.add(child);
        child.set_parent_id(level2Entity.get_id());
        level2Entity.set_children(childList);
        Node testNode = Node.marshall(level2Entity);
        assertThat(testNode.getId()).isEqualTo(NODE_ID);
        assertThat(testNode.getModel()).isEqualTo(Model.recordFolder.ordinal());
        assertThat(testNode.getChildren().get(0).getId()).isEqualTo(CHILD_ID);
        Node testParentNode = (Node)testNode.getParent();
        assertThat(testParentNode.getId()).isEqualTo(PARENT_NODE_ID);
        assertThat(testParentNode.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(testParentNode.getChildren().get(0).getId()).isEqualTo(NODE_ID);
        assertThat(testParentNode.getChildren().size()).isEqualTo(1);
        Node root = (Node)testParentNode.getParent();
        assertThat(root.getModel()).isEqualTo(Model.root.ordinal());
    }
}
