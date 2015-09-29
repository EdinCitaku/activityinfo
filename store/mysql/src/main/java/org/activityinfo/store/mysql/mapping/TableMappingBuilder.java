package org.activityinfo.store.mysql.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.model.type.geo.GeoPoint;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class TableMappingBuilder {
    private String tableName;
    private String baseFilter;
    private List<FieldMapping> mappings = Lists.newArrayList();
    private PrimaryKeyMapping primaryKeyMapping;
    private FormClass formClass;
    private DeleteMethod deleteMethod = DeleteMethod.SOFT_BY_DATE;
    
    private Map<String, Object> insertDefaults = new HashMap<>();

    private TableMappingBuilder(ResourceId formClassId, String tableName) {
        this.tableName = tableName;
        this.formClass = new FormClass(formClassId);
    }

    public static TableMappingBuilder newMapping(ResourceId formClassId, String tableName) {
        return new TableMappingBuilder(formClassId, tableName);
    }

    public void setPrimaryKeyMapping(char domain, String columnName) {
        this.primaryKeyMapping = new PrimaryKeyMapping(domain, columnName);
    }

    public void setFormLabel(String name) {
        this.formClass.setLabel(name);
    }

    public void setOwnerId(ResourceId rootId) {
        formClass.setOwnerId(rootId);
    }
    
    public void setBaseFilter(String baseFilter) {
        this.baseFilter = baseFilter;
    }

    public void add(FieldMapping fieldMapping) {
        formClass.addElement(fieldMapping.getFormField());
        mappings.add(fieldMapping);
    }
    
    public void addTextField(FormField field, String columnName) {
        add(new FieldMapping(field, columnName, Mapping.TEXT));
    }

    public void addDateField(FormField field, String columnName) {
        add(new FieldMapping(field, columnName, Mapping.DATE));
    }

    public void addReferenceField(FormField field, final char domain, String columnName) {
        add(new FieldMapping(field, columnName, new ForeignKeyMapping(domain)));
    }
    
    public void addGeoAreaField(FormField field) {
        add(new FieldMapping(field, Arrays.asList("x1", "y1", "x2", "y2"), new FieldValueMapping() {
            @Override
            public FieldValue extract(ResultSet rs, int index) throws SQLException {
                double x1 = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                }
                double y1 = rs.getDouble(index + 1);
                if (rs.wasNull()) {
                    return null;
                }
                double x2 = rs.getDouble(index + 2);
                if (rs.wasNull()) {
                    return null;
                }
                double y2 = rs.getDouble(index + 3);
                if (rs.wasNull()) {
                    return null;
                }

                return new GeoArea(Extents.create(x1, y1, x2, y2), "FIXME");
            }

            @Override
            public Collection<? extends Object> toParameters(FieldValue value) {
                GeoArea area = (GeoArea) value;
                Extents bbox = area.getEnvelope();
                return Arrays.asList(bbox.getX1(), bbox.getY1(), bbox.getX2(), bbox.getY2());
            }
        }));
    }

    public void addGeoPoint(FormField field) {
        add(new FieldMapping(field, Arrays.asList("x1", "y1"), new FieldValueMapping() {
            @Override
            public FieldValue extract(ResultSet rs, int index) throws SQLException {

                double lat = rs.getDouble(index + 1);
                if (rs.wasNull()) {
                    return null;
                }

                double lon = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                }

                return new GeoPoint(lat, lon);
            }

            @Override
            public Collection<Double> toParameters(FieldValue value) {
                GeoPoint pointValue = (GeoPoint) value;
                return Arrays.asList(pointValue.getLongitude(), pointValue.getLatitude());
            }
        }));
    }
    
    public void defaultValueOnInsert(String fieldName, Object sqlValue) {
        insertDefaults.put(fieldName, sqlValue);
    }
    
    public void setDeleteMethod(DeleteMethod deleteMethod) {
        this.deleteMethod = Preconditions.checkNotNull(deleteMethod);
    }


    public TableMapping build() {
        Preconditions.checkState(primaryKeyMapping != null, tableName + ": Primary key is not set");
        Preconditions.checkState(formClass != null, tableName + ": FormClass is not set");
        Preconditions.checkState(formClass.getOwnerId() != null, tableName + ": ownerId is not set");
        return new TableMapping(tableName, tableName + " base", baseFilter, primaryKeyMapping, mappings, formClass,
                deleteMethod, insertDefaults);
    }
}
