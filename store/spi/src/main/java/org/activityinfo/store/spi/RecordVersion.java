/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.store.spi;

import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Records a change to the record
 */
public class RecordVersion {
    
    private ResourceId recordId;
    private long userId;
    private long time;
    private long version;
    private long formVersion;
    
    private RecordChangeType type;

    private SubFormKind subformKind;
    private String subformKey;

    private final Map<ResourceId, FieldValue> values = new HashMap<>();

    /**
     * @return the id of the record changed.
     */
    public ResourceId getRecordId() {
        return recordId;
    }

    public void setRecordId(ResourceId recordId) {
        this.recordId = recordId;
    }

    /**
     * @return the id of the user changed.
     */
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * 
     * @return the time, in milliseconds since the epoch, of the change.
     */
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the version that this change correspons to.
     */
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(long formVersion) {
        this.formVersion = formVersion;
    }

    /**
     * @return the values of the fields at this change.
     */
    public Map<ResourceId, FieldValue> getValues() {
        return values;
    }


    public RecordChangeType getType() {
        return type;
    }

    public void setType(RecordChangeType type) {
        this.type = type;
    }

    public SubFormKind getSubformKind() {
        return subformKind;
    }

    public void setSubformKind(SubFormKind subformKind) {
        this.subformKind = subformKind;
    }

    public String getSubformKey() {
        return subformKey;
    }

    public void setSubformKey(String subformKey) {
        this.subformKey = subformKey;
    }
}
