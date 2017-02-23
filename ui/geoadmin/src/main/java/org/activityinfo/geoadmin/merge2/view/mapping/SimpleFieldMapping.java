package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;

/**
 * Maps a root field directly to another root field.
 * 
 */
public class SimpleFieldMapping implements FieldMapping {

    private FieldProfile sourceField;
    private FieldProfile targetField;

    public SimpleFieldMapping(FieldProfile sourceField, FieldProfile targetField) {
        this.targetField = targetField;
        this.sourceField = sourceField;
    }

    public static boolean isSimple(FieldProfile targetField) {
        FieldType fieldType = targetField.getFormField().getType();
        return fieldType instanceof TextType;
    }

    @Override
    public ResourceId getTargetFieldId() {
        return targetField.getId();
    }

    @Override
    public FieldValue mapFieldValue(int sourceIndex) {
        FieldType type = targetField.getFormField().getType();
        if(type instanceof TextType) {
            return TextValue.valueOf(sourceField.getView().getString(sourceIndex));
        } else {
            throw new UnsupportedOperationException("target type: "  + type);
        }
    }
}
