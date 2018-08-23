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
package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.primitive.HasStringValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A level within a hierarchy of lookup choices.
 */
public class LookupKey {

    private int keyIndex;

    List<ParentKey> parentLevels = new ArrayList<>();

    private ResourceId formId;

    private String formLabel;

    /**
     * The name of the field that serves as a key, or {@code null} if this is
     * the raw unique record ID.
     */
    private final String fieldLabel;

    /**
     * This key's field id.
     */
    private SymbolNode fieldId;

    private List<LookupKey> childLevels = new ArrayList<>();

    /**
     * This key's distinctive label. This is set by LookupKeySet after all
     * LookupKeys are constructed.
     */
    String keyLabel;

    protected static class ParentKey {

        private LookupKey key;
        private SymbolNode fieldId;

        public ParentKey(LookupKey key) {
            this.key = key;
        }

        public ParentKey(LookupKey key, String fieldId) {
            this(key);
            this.fieldId = fieldId == null ? null : new SymbolNode(fieldId);
        }

        public LookupKey getKey() {
            return key;
        }

        public SymbolNode getFieldId() {
            return fieldId;
        }
    }

    private LookupKey(int keyIndex,
              String parentFieldId,
              LookupKey parentLevel,
              FormClass formClass,
              SymbolNode keyFormula,
              String fieldLabel) {
        this.keyIndex = keyIndex;
        if (parentLevel != null) {
            parentLevels.add(new ParentKey(parentLevel, parentFieldId));
        }
        this.formId = formClass.getId();
        this.formLabel = formClass.getLabel();
        this.fieldLabel = fieldLabel;
        this.fieldId = keyFormula;

        if(parentLevel != null) {
            parentLevel.childLevels.add(this);
        }
    }

    /**
     * Constructs a new LookupKey for the given parent, form, and key field.
     */
    LookupKey(int keyIndex,
              String parentFieldId,
              LookupKey parentLevel,
              FormClass formClass,
              FormField formField) {

        this(keyIndex, parentFieldId, parentLevel, formClass, new SymbolNode(formField.getId()), formField.getLabel());
    }

    LookupKey(int keyIndex, List<ParentKey> parentKeys, FormClass formClass, FormField formField) {
        this(keyIndex, null, null, formClass, new SymbolNode(formField.getId()), formField.getLabel());
        this.parentLevels.addAll(parentKeys);
    }

    /**
     * Constructs a new LookupKey for the given form and key field.
     */
    LookupKey(int keyIndex, FormClass formClass, FormField field) {
        this(keyIndex, null, null, formClass, field);
    }


    /**
     * Constructs a new LookupKey for the given form using the raw record id.
     */
    LookupKey(int keyIndex, FormClass formClass) {
        this(keyIndex, null, null, formClass, new SymbolNode(ColumnModel.RECORD_ID_SYMBOL), "ID");
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public boolean isRoot() {
        return parentLevels.isEmpty();
    }

    public boolean isLeaf() {
        return childLevels.isEmpty();
    }

    public String getFormLabel() {
        return formLabel;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public SymbolNode getKeyField() {
        return fieldId;
    }

    public List<LookupKey> parentLevels() {
        assert !parentLevels.isEmpty();
        return parentLevels.stream().map(ParentKey::getKey).collect(Collectors.toList());
    }

    public List<LookupKey> getParentLevels() {
        List<LookupKey> parents = new ArrayList<>();
        collectParents(parents);
        return parents;
    }

    private void collectParents(List<LookupKey> parents) {
        if(this.isRoot()) {
            return;
        }
        for (LookupKey parent : parentLevels()) {
            parents.add(parent);
            parent.collectParents(parents);
        }
    }

    public List<LookupKey> getChildLevels() {
        return childLevels;
    }

    @Override
    public String toString() {
        return "[" + formLabel + "." + fieldLabel + ": " +  formId + "." + fieldId + "]";
    }

    private void collectKeys(@Nullable FormulaNode baseField, Map<LookupKey, FormulaNode> keys) {
        keys.put(this, join(baseField, fieldId));

        if(!isRoot()) {
            parentLevels.forEach(parentLevel -> {
                parentLevel.getKey().collectKeys(join(baseField, parentLevel.getFieldId()), keys);
            });
        }
    }

    private FormulaNode join(@Nullable FormulaNode base, @Nullable SymbolNode field) {
        if(base == null && field == null) {
            return null;
        } else if (base == null) {
            return field;
        } else if (field == null) {
            return base;
        } else {
            return new CompoundExpr(base, field);
        }
    }

    public Map<LookupKey, FormulaNode> getKeyFormulas() {
        return getKeyFormulas(null);
    }

    public Map<LookupKey, FormulaNode> getKeyFormulas(FormulaNode baseField) {
        Map<LookupKey, FormulaNode> keys = new HashMap<>();
        collectKeys(baseField, keys);
        return keys;
    }

    public String label(FormInstance record) {
        if(fieldId.getName().equals(ColumnModel.RECORD_ID_SYMBOL)) {
            return record.getId().asString();
        }

        FieldValue fieldValue = record.get(ResourceId.valueOf(fieldId.getName()));
        if(fieldValue == null) {
            return record.getId().asString();
        }

        if(fieldValue instanceof SerialNumber) {
            SerialNumberType type = new SerialNumberType();
            return type.format(((SerialNumber) fieldValue));

        } else {
            return ((HasStringValue) fieldValue).asString();
        }
    }

    public String getKeyLabel() {
        return keyLabel;
    }

    public void addParentLevel(ParentKey parent) {
        this.parentLevels.add(parent);
    }

    public void addParentLevels(List<ParentKey> parents) {
        this.parentLevels.addAll(parents);
    }

}
