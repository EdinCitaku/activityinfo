package org.activityinfo.model.type.expr;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.activityinfo.model.resource.IsRecord;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * A FieldValue containing a symbolic expression such as "A + B"
 */
public class ExprValue implements FieldValue, IsRecord {

    private final String expression;

    public ExprValue(String expression) {
        this.expression = expression;
    }

    @JsonValue
    public String getExpression() {
        return expression;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return ExprFieldType.TYPE_CLASS;
    }

    @Override
    public JsonElement toJsonElement() {
        return new JsonPrimitive(expression);
    }

    @Override
    public Record asRecord() {
        return new Record()
                .set(FieldValue.TYPE_CLASS_FIELD_NAME, getTypeClass().getId())
                .set("value", expression);
    }

    public static ExprValue valueOf(String value) {
        return new ExprValue(value);
    }

    public static ExprValue fromRecord(Record record) {
        return new ExprValue(record.getString("value"));
    }
}
