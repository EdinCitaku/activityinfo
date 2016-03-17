package org.activityinfo.ui.client.component.form;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 1. Single form model:
 * a) definition : rootFormClass
 * b) instance level: workingRootInstance
 * <p/>
 * <p/>
 * 2. Subform model:
 * <p/>
 * a) definition:
 * rootFormClass:
 * - FormField -> SubFormType -> Sub FormClass
 *
 * subFormClass:
 * - key field - references SubFormKind (type of subformFormClass) : Collection, Partner, LocationType, Period
 * b) each subform can have multiple instances:
 * <p/>
 * b.1) valueInstance - instance that keeps values for sub form
 * valueInstance.id - generated id
 * valueInstance.classId - references subFormClass
 * valueInstance.ownerId - referenced root working instance
 * valueInstance.keyId - referenced key instance
 * <p/>
 * b.2) keyInstance - instance that keeps values for key, like period value or partner
 * keyInstance.id - generated
 * keyInstance.classId - definition class id : ClassType.getDefinition().getId();
 *
 * @author yuriyz on 02/13/2015.
 */
public class FormModel {

    /**
     * Key for subform value instance.
     */
    public static class SubformValueKey {

        private final FormClass subForm;
        private final FormInstance instance;

        public SubformValueKey(FormClass subForm, FormInstance instance) {
            this.subForm = subForm;
            this.instance = instance;
        }

        public FormClass getSubForm() {
            return subForm;
        }

        public FormInstance getInstance() {
            return instance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubformValueKey that = (SubformValueKey) o;

            return !(subForm != null ? !subForm.equals(that.subForm) : that.subForm != null) && !(instance != null ? !instance.equals(that.instance) : that.instance != null);

        }

        @Override
        public int hashCode() {
            int result = subForm != null ? subForm.hashCode() : 0;
            result = 31 * result + (instance != null ? instance.hashCode() : 0);
            return result;
        }
    }

    private final ResourceLocator locator;
    private final StateProvider stateProvider;
    private final EventBus eventBus = new SimpleEventBus();

    /**
     * FormField.SubFormType -> sub FormClass referenced in SubFormType
     */
    private final Map<ResourceId, FormClass> ownerFormFieldToSubFormClass = HashBiMap.create();

    /**
     * Keeps formfieldId to owner FormClass
     */
    private final Map<ResourceId, FormClass> formFieldToFormClass = Maps.newHashMap();

    /**
     * Keeps selected instance (tab) for given sub form class.
     */
    private final BiMap<FormClass, FormInstance> selectedInstances = HashBiMap.create();

    private final Set<ResourceId> persistedInstanceToRemoveByLocator = Sets.newHashSet();

    private FormClass rootFormClass;

    // validation form class is used to refer to "top-level" form class.
    // For example "Properties panel" renders current type-formClass but in order to validate expression we need
    // reference to formClass that is currently editing on FormDesigner.
    // it can be null.
    private FormClass validationFormClass = null;

    /**
     * The original, unmodified instance
     */
    private FormInstance originalRootInstance;

    /**
     * A new version of the root instance, being updated by the user
     */
    private FormInstance workingRootInstance;

    /**
     * Keeps reporting instance to value instance (e.g. Period instance to values of subform for this period).
     */
    private final BiMap<SubformValueKey, FormInstance> subFormInstances = HashBiMap.create();

    /**
     * Keeps formClass to create FieldContainers map.
     */
    private final Map<ResourceId, Set<FieldContainer>> classToFields = Maps.newHashMap();

    public FormModel(ResourceLocator locator, StateProvider stateProvider) {
        this.locator = locator;
        this.stateProvider = stateProvider;
    }

    public Promise<Void> loadFormClassWithDependentSubForms(ResourceId rootClassId) {
        return locator.getFormClass(rootClassId).then(new Function<FormClass, FormClass>() {
            @Nullable
            @Override
            public FormClass apply(FormClass input) {
                FormModel.this.rootFormClass = input;
                for (FormField formField : input.getFields()) {
                    formFieldToFormClass.put(formField.getId(), input);
                }
                return input;
            }
        }).join(new Function<FormClass, Promise<Void>>() {
            @Override
            public Promise<Void> apply(FormClass rootFormClass) {
                List<Promise<FormClass>> promises = Lists.newArrayList();
                for (final FormField formField : rootFormClass.getFields()) {
                    if (formField.getType() instanceof SubFormReferenceType) {
                        SubFormReferenceType subFormType = (SubFormReferenceType) formField.getType();
                        Promise<FormClass> promise = locator.getFormClass(subFormType.getClassId());
                        promise.then(new Function<FormClass, Object>() {
                            @Nullable
                            @Override
                            public Object apply(FormClass subForm) {
                                ownerFormFieldToSubFormClass.put(formField.getId(), subForm);
                                for (FormField field : subForm.getFields()) {
                                    formFieldToFormClass.put(field.getId(), subForm);
                                }
                                return null;
                            }
                        });
                        promises.add(promise);
                    }
                }
                return Promise.waitAll(promises);
            }
        });
    }

    public List<FormField> getAllFormsFields() {
        List<FormField> formFields = Lists.newArrayList(getRootFormClass().getFields());
        for (FormClass subForm : ownerFormFieldToSubFormClass.values()) {
            formFields.addAll(subForm.getFields());
        }
        return formFields;
    }

    public FormClass getClassByField(ResourceId formFieldId) {
        FormClass formClass = formFieldToFormClass.get(formFieldId);

        Preconditions.checkNotNull(formClass, "Unknown field, id:" + formFieldId);

        return formClass;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public FormClass getSubFormByOwnerFieldId(ResourceId formFieldId) {
        return ownerFormFieldToSubFormClass.get(formFieldId);
    }

    public ResourceLocator getLocator() {
        return locator;
    }

    public FormClass getRootFormClass() {
        return rootFormClass;
    }

    public FormClass getValidationFormClass() {
        return validationFormClass;
    }

    public void setValidationFormClass(FormClass validationFormClass) {
        this.validationFormClass = validationFormClass;
    }

    public FormInstance getWorkingRootInstance() {
        return workingRootInstance;
    }

    public void setWorkingRootInstance(FormInstance workingRootInstance) {
        this.originalRootInstance = workingRootInstance.copy();
        this.workingRootInstance = workingRootInstance;
    }

    public Optional<FormInstance> getWorkingInstance(ResourceId formFieldId) {
        FormClass classByField = getClassByField(formFieldId);
        if (classByField.equals(rootFormClass)) {
            return Optional.of(getWorkingRootInstance());
        }
        FormInstance selectedTab = selectedInstances.get(classByField);

        if (selectedTab == null) {
            return Optional.absent(); // on initial form creation we may have nothing selected
        }

        return Optional.of(createSubFormValueInstanceIfAbsent(new SubformValueKey(classByField, selectedTab)));
    }

    public BiMap<SubformValueKey, FormInstance> getSubFormInstances() {
        return subFormInstances;
    }

    /**
     * Returns list of tabs mentioned in subFormInstances map. Or in other words involved in interaction by user during the session.
     *
     * @return list of tabs mentioned in subFormInstances map
     */
    public List<FormInstance> getSubformPresentTabs() {
        List<FormInstance> result = Lists.newArrayList();

        for (SubformValueKey key : subFormInstances.keySet()) {
            result.add(key.getInstance());
        }
        return result;
    }

    public Set<SubformValueKey> getKeysBySubForm(FormClass subForm) {
        Set<SubformValueKey> set = Sets.newHashSet();

        for (SubformValueKey key : subFormInstances.keySet()) {
            if (key.getSubForm().equals(subForm)) {
                set.add(key);
            }
        }

        return set;
    }

    public void setSelectedInstance(FormInstance tabInstance, FormClass subForm) {
        selectedInstances.put(subForm, tabInstance);
        createSubFormValueInstanceIfAbsent(new SubformValueKey(subForm, tabInstance));
    }

    private FormInstance createSubFormValueInstanceIfAbsent(SubformValueKey key) {
        FormInstance valueInstance = subFormInstances.get(key);
        if (valueInstance == null) {
            valueInstance = new FormInstance(ResourceId.generateId(), key.getSubForm().getId());
            valueInstance.setOwnerId(getWorkingRootInstance().getId());
            valueInstance.setKeyId(key.getInstance().getId());
            subFormInstances.put(key, valueInstance);
        }
        return valueInstance;
    }

    public Set<FieldContainer> getContainersOfClass(ResourceId classId) {
        Set<FieldContainer> containers = classToFields.get(classId);
        return containers != null ? containers : Collections.<FieldContainer>emptySet();
    }

    public FormModel addContainerOfClass(ResourceId classId, FieldContainer fieldContainer) {
        Set<FieldContainer> containers = classToFields.get(classId);
        if (containers == null) {
            containers = Sets.newHashSet();
            classToFields.put(classId, containers);
        }
        containers.add(fieldContainer);
        return this;
    }

    public FormInstance getOriginalRootInstance() {
        return originalRootInstance;
    }

    public Set<ResourceId> getPersistedInstanceToRemoveByLocator() {
        return persistedInstanceToRemoveByLocator;
    }

    public StateProvider getStateProvider() {
        return stateProvider;
    }
}
