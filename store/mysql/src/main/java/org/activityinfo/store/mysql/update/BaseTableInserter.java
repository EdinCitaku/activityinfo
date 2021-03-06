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
package org.activityinfo.store.mysql.update;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.FieldMapping;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.spi.TypedRecordUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds an update to an existing base table row.
 * 
 */
public class BaseTableInserter {
    private TableMapping mapping;
    
    private List<String> columns = new ArrayList<>();
    private List<Object> parameters = new ArrayList<>();

    private final int siteId;

    public BaseTableInserter(TableMapping mapping, ResourceId siteId) {
        this.mapping = mapping;
        this.siteId = CuidAdapter.getLegacyIdFromCuid(siteId);
        columns.add(mapping.getPrimaryKey().getColumnName());
        parameters.add(this.siteId);
    }

    public void set(ResourceId fieldId, FieldValue value) {
        FieldMapping fieldMapping = mapping.getMapping(fieldId);
        Preconditions.checkArgument(fieldMapping != null, "No such field: %s", fieldId.asString());

        if(value != null) {
            addValue(fieldMapping, value);
        }
    }

    private void addValue(FieldMapping fieldMapping, FieldValue value) {
        columns.addAll(fieldMapping.getColumnNames());
        parameters.addAll(fieldMapping.getConverter().toParameters(value));
    }
    
    public void addValue(String fieldName, Object value) {
        columns.add(fieldName);
        parameters.add(value);
    }


    public void insert(QueryExecutor executor, TypedRecordUpdate update) {
        Preconditions.checkArgument(update.getRecordId().getDomain() == mapping.getPrimaryKey().getDomain(),
                "Resource Id mismatch, expected domain '%c', got id '%s'",
                mapping.getPrimaryKey().getDomain(),
                update.getRecordId().asString());

        // Add default values for inserts
        for (Map.Entry<String, Object> entry : mapping.getInsertDefaults().entrySet()) {
            columns.add(entry.getKey());
            parameters.add(entry.getValue());
        }
        
        // Describe all the updates
        for (Map.Entry<ResourceId, FieldValue> change : update.getChangedFieldValues().entrySet()) {
            set(change.getKey(), change.getValue());
        }

        executeInsert(executor);   
    }
    

    public void executeInsert(QueryExecutor executor) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(mapping.getBaseTable()).append(" (");
        Joiner.on(", ").appendTo(sql, columns);
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if(i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        System.out.println(sql.toString());
        System.out.println(parameters.toString());

        int rowsUpdated = executor.update(sql.toString(), parameters);
        
        Preconditions.checkState(rowsUpdated == 1);
    }


}
