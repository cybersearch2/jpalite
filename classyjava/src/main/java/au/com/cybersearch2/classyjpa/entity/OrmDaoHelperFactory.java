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
package au.com.cybersearch2.classyjpa.entity;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * OrmDaoHelperFactory
 * @author Andrew Bowley
 * 18/08/2014
 */
public class OrmDaoHelperFactory<T extends OrmEntity>
{
    private Class<T> entityClass;

    public OrmDaoHelperFactory(Class<T> entityClass)
    {
        this.entityClass = entityClass;
    }

    public OrmDaoHelper<T>getOrmDaoHelper(ConnectionSource connectionSource)
    {
        PersistenceDao<T> entityDao = getDao(connectionSource);
        checkTableExists(connectionSource, entityDao);
        return new OrmDaoHelper<T>(entityDao);
    }

    public PersistenceDao<T> getDao(ConnectionSource connectionSource) 
    {
        try
        {
            PersistenceDao<T> dao = createDao(connectionSource);
            dao.setObjectCache(true);
            return dao;
        }
        catch (SQLException e)
        {
            throw new IllegalArgumentException("Error creating DAO for class " + entityClass.getName(), e);
        }
    }

    public boolean checkTableExists(ConnectionSource connectionSource) 
    {
        PersistenceDao<T> entityDao = getDao(connectionSource);
        return checkTableExists(connectionSource, entityDao);
    }
    
    protected boolean checkTableExists(ConnectionSource connectionSource, PersistenceDao<T> entityDao) 
    {
        try
        {
            if (!entityDao.isTableExists())
            {
                createTable(connectionSource);
                return false;
            }
            return true;
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error creating table for class " + entityClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected PersistenceDao<T> createDao(ConnectionSource connectionSource) throws SQLException
    {
        return new PersistenceDao<T>((Dao<T, Integer>) DaoManager.createDao(connectionSource, entityClass));
    }
    
    protected void createTable(ConnectionSource connectionSource) throws SQLException
    {
        TableUtils.createTable(connectionSource, entityClass);
    }
}
