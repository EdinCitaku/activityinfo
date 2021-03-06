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
package org.activityinfo.ui.client.input.viewModel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.analysis.FieldReference;
import org.activityinfo.analysis.FormulaValidator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.Formulas;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.AndFunction;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;

import java.util.*;

/**
 * Calculates the filters that apply to individual fields based on form-level permissions.
 */
public class PermissionFilters {

    private final Map<ResourceId, FormulaNode> fieldFilters = new HashMap<>();

    public static PermissionFilters create(FormTree formTree) {
        return new PermissionFilters(formTree, Operation.CREATE_RECORD);
    }

    public static PermissionFilters edit(FormTree formTree) {
        return new PermissionFilters(formTree, Operation.EDIT_RECORD);
    }

    public PermissionFilters(FormTree formTree, Operation operation) {
        this(formTree, formTree.getRootMetadata().getPermissions(), operation);
    }

    @VisibleForTesting
    PermissionFilters(FormTree formTree, FormPermissions permissions, Operation operation) {

        /*
         * Create a set of independent boolean permission criteria.
         */
        Set<FormulaNode> criteria = new HashSet<>();
        if(permissions.hasVisibilityFilter()) {
            criteria.addAll(parsePermission(permissions.getViewFilter()));
        }
        if(permissions.isFiltered(operation)) {
            criteria.addAll(parsePermission(permissions.getFilter(operation)));
        }

        /*
         * Now map each of the criteria to a field, if possible
         */
        Multimap<ResourceId, FormulaNode> fieldCriteria = HashMultimap.create();
        for (FormulaNode criterium : criteria) {
            FormulaValidator validator = new FormulaValidator(formTree);
            validator.validate(criterium);
            if(validator.isValid()) {
                Optional<ResourceId> rootField = findUniqueFieldReference(validator.getReferences(), formTree.getRootFormClass());
                if(rootField.isPresent()) {
                    fieldCriteria.put(rootField.get(), criterium);
                }
            }
        }

        /*
         * Finally combine all the separate filters into an expression per field.
         */
        for (ResourceId fieldId : fieldCriteria.keySet()) {
            fieldFilters.put(fieldId, Formulas.allTrue(fieldCriteria.get(fieldId)));
        }
    }

    private List<FormulaNode> parsePermission(String filter) {
        FormulaNode formulaNode = FormulaParser.parse(filter);
        return Formulas.findBinaryTree(formulaNode, AndFunction.INSTANCE);
    }

    private Optional<ResourceId> findUniqueFieldReference(List<FieldReference> references, FormClass rootFormClass) {
        Set<ResourceId> rootFields = new HashSet<>();

        for (FieldReference reference : references) {
            switch (reference.getMatch().getType()) {
                case RECORD_ID:
                    findFormReferenceField(reference, rootFormClass).transform(rootFields::add);
                    break;
                case FORM_ID:
                    return Optional.absent();
                case FIELD:
                    rootFields.add(reference.getMatch().getFieldNode().getPath().getRoot());
                    break;
            }
        }
        if(rootFields.size() == 1) {
            return Optional.of(rootFields.iterator().next());
        } else {
            return Optional.absent();
        }
    }

    private Optional<ResourceId> findFormReferenceField(FieldReference reference, FormClass rootFormClass) {
        for (FormField field : rootFormClass.getFields()) {
            if (!(field.getType() instanceof ReferenceType)) {
                continue;
            }
            ReferenceType refType = (ReferenceType) field.getType();
            if (refType.getRange().contains(reference.getMatch().getFormClass().getId())) {
                return Optional.of(field.getId());
            }
        }
        return Optional.absent();
    }

    public boolean isFiltered(ResourceId fieldId) {
        return fieldFilters.containsKey(fieldId);
    }

    /**
     * Returns a filter for records referenced by the given field.
     */
    public Optional<FormulaNode> getReferenceBaseFilter(ResourceId fieldId) {
        FormulaNode filter = fieldFilters.get(fieldId);
        if(filter == null) {
            return Optional.absent();
        }

        SymbolNode fieldExpr = new SymbolNode(fieldId);

        return Optional.of(filter.transform(x -> {
            if(x.equals(fieldExpr)) {
                return new SymbolNode(ColumnModel.RECORD_ID_SYMBOL);
            } else {
                return x;
            }
        }));
    }
}
