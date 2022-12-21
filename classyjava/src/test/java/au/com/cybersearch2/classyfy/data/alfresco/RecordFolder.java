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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * RecordFolder
 * @author Andrew Bowley
 * 12/05/2014
 */
@Entity(name = "folders")
public class RecordFolder extends ManagedRecord
{

    private static final long serialVersionUID = -116543269110722658L;

    protected String location;
    
    @Column(nullable = false)
    protected boolean hasDispositionSchedule;
    
    @Column(nullable = true)
    protected String dispositionInstructions;
    
    @Column(nullable = true)
    protected String dispositionAuthority;
    
    public RecordFolder()
    {
    }

    @Override
    public void set_id(int _id) 
    {
        this._id = _id;
    }

    @Override
    public void setDescription(String description) 
    {
        this.description = description;
    }

    @Override
    public void setCreated(Date created) 
    {
        this.created = created;
    }

    @Override
    public void setCreator(String creator) 
    {
        this.creator = creator;
    }

    @Override
    public void setModified(Date modified) 
    {
        this.modified = modified;
    }

    @Override
    public void setModifier(String modifier) 
    {
        this.modifier = modifier;
    }

    @Override
    public void setIdentifier(String identifier) 
    {
        this.identifier = identifier;
    }

    public String getDescription() 
    {
        return description;
    }

    public Date getCreated() 
    {
        return created;
    }

    public String getCreator() 
    {
        return creator;
    }

    public Date getModified() 
    {
        return modified;
    }

    public String getModifier() 
    {
        return modifier;
    }

    public String getIdentifier() 
    {
        return identifier;
    }

    @Override
    public void set_nodeId(int node_id) 
    {
        this.node_id = node_id;
    }

    public String getLocation() 
    {
        return location;
    }

    public void setLocation(String location) 
    {
        this.location = location;
    }

    public boolean isHasDispositionSchedule() 
    {
        return hasDispositionSchedule;
    }

    public void setHasDispositionSchedule(boolean hasDispositionSchedule) 
    {
        this.hasDispositionSchedule = hasDispositionSchedule;
    }

    public String getDispositionInstructions() 
    {
        return dispositionInstructions;
    }

    public void setDispositionInstructions(String dispositionInstructions) 
    {
        this.dispositionInstructions = dispositionInstructions;
    }

    public String getDispositionAuthority() 
    {
        return dispositionAuthority;
    }

    public void setDispositionAuthority(String dispositionAuthority) 
    {
        this.dispositionAuthority = dispositionAuthority;
    }

}
