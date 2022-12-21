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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * NoNameEntity
 * @author Andrew Bowley
 * 12/05/2014
 */
@Entity
public class NoNameEntity
{
        @Id @GeneratedValue
        protected int _id;
        
        @Column(nullable = false)
        protected int node_id;
        
        @Column(nullable = false)
        protected String description;
        
        @Column(nullable = false)
        protected Date created;

        @Column(nullable = false)
        protected String creator;
        
        @Column
        protected Date modified;

        @Column
        protected String modifier;
     
        @Column(nullable = false)
        protected String identifier;

        public int get_id() {
            return _id;
        }

        public void set_id(int _id) {
            this._id = _id;
        }

        public int getNode_id() {
            return node_id;
        }

        public void setNode_id(int node_id) {
            this.node_id = node_id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public Date getModified() {
            return modified;
        }

        public void setModified(Date modified) {
            this.modified = modified;
        }

        public String getModifier() {
            return modifier;
        }

        public void setModifier(String modifier) {
            this.modifier = modifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
}
