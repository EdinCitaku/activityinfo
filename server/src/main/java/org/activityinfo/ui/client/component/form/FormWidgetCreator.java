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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.ValueUpdater;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.event.FieldMessageEvent;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 02/17/2015.
 */
public class FormWidgetCreator {

    public interface FieldUpdated {
        void onFieldUpdated(FormField field, FieldValue newValue);
    }

    private final FieldContainerFactory containerFactory;
    private final FormFieldWidgetFactory widgetFactory;

    private final Map<ResourceId, FieldContainer> containers = Maps.newHashMap();

    private final FormModel model;

    public FormWidgetCreator(FormModel model, FieldContainerFactory containerFactory, FormFieldWidgetFactory widgetFactory) {
        this.model = model;
        this.containerFactory = containerFactory;
        this.widgetFactory = widgetFactory;

        bindEvents();
    }

    private void bindEvents() {
        model.getEventBus().addHandler(FieldMessageEvent.TYPE, new FieldMessageEvent.Handler() {
            @Override
            public void handle(FieldMessageEvent event) {
                showFieldMessage(event);
            }
        });
    }

    public Promise<Void> createWidgets(final FormClass formClass, final FieldUpdated fieldUpdated) {
        List<Promise<Void>> promises = Lists.newArrayList();
        for (final FormField field : formClass.getFields()) {
            if (field.getType() instanceof SubFormReferenceType) {
                FormClass subForm = model.getSubFormByOwnerFieldId(field.getId());
                if (subForm.getSubFormKind() != SubFormKind.REPEATING) { // for repeating we create it internally in sub SimpleFormPanel
                    Promise<Void> subFormWidgetsPromise = createWidgets(subForm, fieldUpdated);
                    promises.add(subFormWidgetsPromise);
                }
            } else {
                Promise<Void> promise = widgetFactory.createWidget(formClass, field, new ValueUpdater<FieldValue>() {
                    @Override
                    public void update(FieldValue value) {
                        fieldUpdated.onFieldUpdated(field, value);
                    }
                }, model.getValidationFormClass(), model.getEventBus()).then(new Function<FormFieldWidget, Void>() {
                    @Override
                    public Void apply(FormFieldWidget widget) {
                        FieldContainer fieldContainer = containerFactory.createContainer(field, widget, 4);
                        containers.put(field.getId(), fieldContainer);

                        model.addContainerOfClass(formClass.getId(), fieldContainer);

                        return null;
                    }
                });
                promises.add(promise);
            }
        }
        return Promise.waitAll(promises);
    }

    public FieldContainer get(ResourceId fieldId) {
        return containers.get(fieldId);
    }

    public Map<ResourceId, FieldContainer> getContainers() {
        return containers;
    }

    private void showFieldMessage(FieldMessageEvent event) {
        FieldContainer container = containers.get(event.getFieldId());
        if (event.isClearMessage()) {
            container.setValid();
        } else {
            container.setInvalid(event.getMessage());
        }
    }
}
