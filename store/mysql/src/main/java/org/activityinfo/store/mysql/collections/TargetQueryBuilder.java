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
package org.activityinfo.store.mysql.collections;

import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.metadata.DatabaseTargetForm;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.Cursor;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class TargetQueryBuilder implements ColumnQueryBuilder {
    
    private QueryExecutor executor;
    private DatabaseTargetForm target;

    private Map<Integer, ValueEmitter> valueEmitters = Maps.newHashMap();

    private MySqlCursorBuilder baseCursorBuilder;
    
    private double[] valueBuffer;
    
    public TargetQueryBuilder(QueryExecutor executor, DatabaseTargetForm target, TableMapping mapping) {
        this.executor = executor;
        this.target = target;
        this.baseCursorBuilder = new MySqlCursorBuilder(mapping, executor);
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException();     
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        baseCursorBuilder.addResourceId(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        if(fieldId.getDomain() == CuidAdapter.TARGET_INDICATOR_FIELD_DOMAIN) {
            int indicatorId = CuidAdapter.getLegacyIdFromCuid(fieldId);
            FormField indicatorField = target.getIndicatorField(indicatorId);
            if(indicatorField == null) {
                throw new IllegalArgumentException("No such indicator " + indicatorId + " in database " + 
                                 target.getDatabaseId());
            }
            int index = valueEmitters.size();
            valueEmitters.put(indicatorId, new ValueEmitter(index, indicatorField, observer));
        } else {
            baseCursorBuilder.addField(fieldId, observer);
        }
        
    }

    @Override
    public void execute() {
        
        // First do base columns (like dates, project, partner, etc)
        if(baseCursorBuilder.hasObservers()) {
            Cursor open = baseCursorBuilder.open();
            while(open.next()) {
            }
        }
        
        if(!valueEmitters.isEmpty()) {
            valueBuffer = new double[valueEmitters.size()];
            Arrays.fill(valueBuffer, Double.NaN);

            try (ResultSet rs = executor.query("SELECT  " +
                    "T.TargetId, " +        // (1)
                    "V.IndicatorId, " +     // (2)
                    "V.Value " +            // (3)
                    "FROM target T " +
                    "LEFT JOIN targetvalue V ON (T.targetId = V.targetId) " +
                    "WHERE T.DatabaseId = " + target.getDatabaseId() + " " +
                    "ORDER BY T.TargetId")) {

                int lastTargetId = -1;

                while (rs.next()) {
                    int targetId = rs.getInt(1);

                    // If this is the first row for a set of TargetValues,
                    // then emit the base columns (Date1, Date2, ProjectId, PartnerId)
                    if (lastTargetId != targetId) {
                        if (lastTargetId > 0) {
                            flushValues();
                        }
                        lastTargetId = targetId;
                    }
                    
                    int indicatorId = rs.getInt(2);
                    double value = rs.getDouble(3);
                    if(!rs.wasNull()) {
                        ValueEmitter valueEmitter = valueEmitters.get(indicatorId);
                        if(valueEmitter != null) {
                            valueEmitter.set(value);
                        }
                    }
                }
                if(lastTargetId > 0) {
                    flushValues();
                }

                for (ValueEmitter valueEmitter : valueEmitters.values()) {
                    valueEmitter.observer.done();
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void flushValues() {
        for (ValueEmitter valueEmitter : valueEmitters.values()) {
            valueEmitter.flush();
        }

        // Reset buffer
        Arrays.fill(valueBuffer, Double.NaN);
    }
    
    private class ValueEmitter  {
        private int bufferIndex;
        private String units;
        private CursorObserver<FieldValue> observer;


        public ValueEmitter(int index, FormField indicatorField, CursorObserver<FieldValue> observer) {
            this.bufferIndex = index;
            this.observer = observer;
            this.units = ((QuantityType) indicatorField.getType()).getUnits();
        }
        
        public void set(double value) {
            valueBuffer[bufferIndex] = value;
        }
        
        public void flush() {
            double value = valueBuffer[bufferIndex];
            if(Double.isNaN(value)) {
                observer.onNext(null);
            } else {
                observer.onNext(new Quantity(value));
            }
        }
        
    }
}
