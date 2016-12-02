package org.activityinfo.model.type.attachment;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

import java.util.List;

/**
 * @author yuriyz on 8/6/14.
 */
public class AttachmentValue implements FieldValue {

    private final List<Attachment> values = Lists.newArrayList();

    @Override
    public FieldTypeClass getTypeClass() {
        return AttachmentType.TYPE_CLASS;
    }


    public AttachmentValue() {
    }

    public AttachmentValue(Attachment imageRowValue) {
        values.add(imageRowValue);
    }

    public List<Attachment> getValues() {
        return values;
    }

    public boolean hasValues() {
        return !values.isEmpty();
    }

    public List<Record> getValuesAsRecords() {
        final List<Record> result = Lists.newArrayList();
        for (Attachment value : values) {
            result.add(value.asRecord());
        }
        return result;
    }

    @Override
    public JsonElement toJsonElement() {
        JsonArray array = new JsonArray();
        for (Attachment value : values) {
            array.add(value.toJsonElement());
        }
        return array;
    }

    public static AttachmentValue fromRecord(Record record) {
        AttachmentValue value = new AttachmentValue();
        List<Record> recordList = record.getRecordList("values");
        for (Record r : recordList) {
            value.getValues().add(Attachment.fromRecord(r));
        }
        return value;
    }

    public static AttachmentValue fromJson(String json) {
        return fromRecord(Resources.recordFromJson(json));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AttachmentValue that = (AttachmentValue) o;

        return !(values != null ? !values.equals(that.values) : that.values != null);

    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }

    public static AttachmentValue fromJsonSilently(String json) {
        try {
            return fromJson(json);
        } catch (Exception e) {
            return null;
        }
    }


}
