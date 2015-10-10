package org.activityinfo.store.mysql.mapping;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.Join;

import java.util.Collections;
import java.util.List;


/**
 * Maps a {@code FormField} to one or more MySQL columns
 */
public class FieldMapping {

    private final FormField formField;
    private List<String> columnNames = Lists.newArrayList();
    private FieldValueConverter valueExtractor;
    private Join join;

    public FieldMapping(FormField formField, String columnName, FieldValueConverter valueExtractor) {
        this.formField = formField;
        this.columnNames = Collections.singletonList(columnName);
        this.valueExtractor = valueExtractor;
    }

    public FieldMapping(FormField formField, List<String> columnName, FieldValueConverter valueExtractor) {
        this.formField = formField;
        this.columnNames = columnName;
        this.valueExtractor = valueExtractor;
    }

    public FieldMapping(FormField formField, String columnName, Join join, FieldValueConverter valueExtractor) {
        this.formField = formField;
        this.columnNames = Collections.singletonList(columnName);
        this.valueExtractor = valueExtractor;
        this.join = join;
    }


    public ResourceId getFieldId() {
        return formField.getId();
    }

    public FormField getFormField() {
        return formField;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public FieldValueConverter getValueExtractor() {
        return valueExtractor;
    }

    public Join getJoin() {
        return join;
    }
}
