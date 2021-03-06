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
package org.activityinfo.model.form;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class FieldValueChange {
    private String fieldId;
    private String fieldLabel;
    private String oldValueLabel;
    private String newValueLabel;
    private String subFormKind;
    private String subFormKey;

    public FieldValueChange() {
    }

    @JsOverlay
    public String getFieldId() {
        return fieldId;
    }

    @JsOverlay
    public String getFieldLabel() {
        return fieldLabel;
    }

    @JsOverlay
    public String getOldValueLabel() {
        return oldValueLabel;
    }

    @JsOverlay
    public String getNewValueLabel() {
        return newValueLabel;
    }

    @JsOverlay
    public String getSubFormKind() {
        return subFormKind;
    }

    @JsOverlay
    public String getSubFormKey() {
        return subFormKey;
    }

    public static class Builder {

        private FieldValueChange change = new FieldValueChange();

        public Builder setFieldId(String fieldId) {
            change.fieldId = fieldId;
            return this;
        }

        /**
         * Sets the fieldLabel.
         *
         * @param fieldLabel the current label of the field changed
         */
        public Builder setFieldLabel(String fieldLabel) {
            change.fieldLabel = fieldLabel;
            return this;
        }

        /**
         * Sets the oldValueLabel.
         *
         * @param oldValueLabel human-readable string of the old value of the field
         */
        public Builder setOldValueLabel(String oldValueLabel) {
            change.oldValueLabel = oldValueLabel;
            return this;
        }

        /**
         * Sets the newValueLabel.
         *
         * @param newValueLabel human-readable string of the new value of the field
         */
        public Builder setNewValueLabel(String newValueLabel) {
            change.newValueLabel = newValueLabel;
            return this;
        }

        /**
         * Sets the subFormKind.
         *
         * @param subFormKind subform kind
         */
        public Builder setSubFormKind(String subFormKind) {
            change.subFormKind = subFormKind;
            return this;
        }

        /**
         * Sets the subFormKey.
         *
         * @param subFormKey subform key
         */
        public Builder setSubFormKey(String subFormKey) {
            change.subFormKey = subFormKey;
            return this;
        }

        public FieldValueChange build() {
            return change;
        }
    }
}
