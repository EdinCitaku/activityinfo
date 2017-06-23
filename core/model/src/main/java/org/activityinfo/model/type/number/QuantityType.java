package org.activityinfo.model.type.number;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.JsonParsing;
import org.activityinfo.model.type.*;

import static com.google.common.base.Strings.nullToEmpty;
import static org.activityinfo.json.Json.createObject;

/**
 * A value types that describes a real-valued quantity and its units.
 */
public class QuantityType implements ParametrizedFieldType {


    public static class TypeClass implements ParametrizedFieldTypeClass, RecordFieldTypeClass {

        private TypeClass() {}

        @Override
        public String getId() {
            return "quantity";
        }

        @Override
        public QuantityType createType() {
            return new QuantityType()
                    .setUnits(I18N.CONSTANTS.defaultQuantityUnits());
        }

        @Override
        public FieldType deserializeType(JsonObject parametersObject) {
            return new QuantityType(JsonParsing.toNullableString(parametersObject.get("units")));
        }

    }

    public static final TypeClass TYPE_CLASS = new TypeClass();

    private String units;

    public QuantityType() {
    }

    public QuantityType(String units) {
        this.units = units;
    }

    public String getUnits() {
        return units;
    }

    public QuantityType setUnits(String units) {
        this.units = units;
        return this;
    }

    /**
     * @return new QuantitType with the given {@code updatedUnits}
     */
    public QuantityType withUnits(String updatedUnits) {
        return new QuantityType(updatedUnits);
    }

    @Override
    public ParametrizedFieldTypeClass getTypeClass() {
        return TYPE_CLASS;
    }

    @Override
    public FieldValue parseJsonValue(JsonValue value) {
        double doubleValue = value.asNumber();
        if(Double.isNaN(doubleValue)) {
            throw new IllegalArgumentException();
        }
        return new Quantity(doubleValue, units);
    }

    @Override
    public <T> T accept(FieldTypeVisitor<T> visitor) {
        return visitor.visitQuantity(this);
    }

    @Override
    public boolean isUpdatable() {
        return true;
    }

    @Override
    public org.activityinfo.json.JsonObject getParametersAsJson() {
        JsonObject object = createObject();
        object.put("units", nullToEmpty(units));
        return object;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return "QuantityType";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuantityType type = (QuantityType) o;

        return units != null ? units.equals(type.units) : type.units == null;

    }

    @Override
    public int hashCode() {
        return units != null ? units.hashCode() : 0;
    }
}
