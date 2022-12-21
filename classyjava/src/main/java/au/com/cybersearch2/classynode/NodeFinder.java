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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import au.com.cybersearch2.classybean.BeanException;
import au.com.cybersearch2.classybean.BeanUtil;
import au.com.cybersearch2.classybean.BeanUtil.DataPair;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
- * NodeFinder
 * Abstract persistence work overrides doInBackground() to perform find node by primary key.
 * Sub class to override onPostExecute() and onRollback() 
 * @author Andrew Bowley
 * 05/09/2014
 */
public class NodeFinder implements PersistenceWork
{
    public interface Callback
    {
        /**
         * Handle node found in caller's thread
         * @param node Node returned by search is a graph fragment containing all found node ancestors and immediate children
         */
        void onNodeFound(Node node);
        /**
         * Handle node not found in caller's thread. Check if getRollbackException returns non-null in case of failure.
         * @param nodeId Node identity
         */
        void onNodeNotFound(int nodeId);
        /**
         * Handle rollback
         * @param nodeId Node identity
         * @param rollbackException Exception which caused rollback
         */
        void onRollback(int nodeId, Throwable rollbackException);
    }


    protected Callback callback;
    

    protected int nodeId;

    protected Node node;
    
    /**
     * Create NodeFinder object
     * @param nodeId Primary key to search on
     * @param callback Callback to handle completion in caller's thread
     */
    public NodeFinder(int nodeId, Callback callback)
    {
        this.nodeId = nodeId;
        this.callback = callback;
    }

    /**
     * Find node by primary key on background thread
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doTask(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
        NodeEntity nodeEntity = entityManager.find(NodeEntity.class, nodeId);

        node = Node.marshall(nodeEntity);
        // Now get properties of requested node
        Query query = entityManager.createNamedQuery(Node.NODE_BY_PRIMARY_KEY_QUERY + node.getModel()); //
        query.setParameter("node_id", nodeId);
        try
        {
            Object result = query.getSingleResult();
            Set<DataPair> dataSet = BeanUtil.getDataPairSet(result);
            Map<String,Object> propertiesMap = new HashMap<String,Object>(dataSet.size());
            for (DataPair dataPair: dataSet)
                propertiesMap.put(dataPair.getKey(), dataPair.getValue());
            node.setProperties((propertiesMap));
        }
        catch (BeanException e)
        {
            throw new PersistenceException(e.getMessage(), e);
        }
    }

    @Override
    public void onPostExecute(boolean success)
    {
        if (success)
            callback.onNodeFound(node);
        else
            callback.onNodeNotFound(nodeId);
    }

    @Override
    public void onRollback(Throwable rollbackException)
    {
        callback.onRollback(nodeId, rollbackException);
    }
}
