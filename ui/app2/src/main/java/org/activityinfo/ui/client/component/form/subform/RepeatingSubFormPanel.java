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
package org.activityinfo.ui.client.component.form.subform;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.FormPanelStyles;
import org.activityinfo.ui.client.component.form.SimpleFormPanel;
import org.activityinfo.ui.client.component.form.event.BeforeSaveEvent;
import org.activityinfo.ui.client.style.ElementStyle;
import org.activityinfo.ui.client.widget.Button;
import org.activityinfo.ui.client.widget.LoadingPanel;

import java.util.*;

/**
 * @author yuriyz on 01/18/2016.
 */
public class RepeatingSubFormPanel implements SubFormPanel {

    private final FlowPanel panel;

    private final FormClass subForm;
    private final FormModel formModel;
    private final Button addButton;
    private final SubFormInstanceLoader loader;
    private final LoadingPanel<Void> loadingPanel;

    private final Map<TypedFormRecord, SimpleFormPanel> forms = Maps.newHashMap();

    public RepeatingSubFormPanel(FormClass subForm, FormModel formModel) {
        this.subForm = subForm;
        this.formModel = formModel;
        this.panel = new FlowPanel();
        this.loader = new SubFormInstanceLoader(formModel);

        this.loadingPanel = new LoadingPanel<>();
        this.loadingPanel.setDisplayWidget(this);
        this.loadingPanel.showWithoutLoad();

        addButton = new Button(ElementStyle.LINK);
        addButton.setLabel(I18N.CONSTANTS.addAnother());
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addForm(newValueInstance(), RepeatingSubFormPanel.this.panel.getWidgetIndex(addButton));
                setDeleteButtonsState();
            }
        });

        bindEvents();
    }

    private void bindEvents() {
        formModel.getEventBus().addHandler(BeforeSaveEvent.TYPE, new BeforeSaveEvent.Handler() {
            @Override
            public void handle(BeforeSaveEvent event) {
                for (SimpleFormPanel panel : forms.values()) {
                    formModel.getChangedInstances().addAll(panel.getModel().getChangedInstances());
                }
            }
        });
    }

    private Set<TypedFormRecord> getInstances() {
        Set<TypedFormRecord> typedFormRecords = formModel.getSubFormInstances().get(key());
        if (typedFormRecords == null) {
            typedFormRecords = Sets.newHashSet(newValueInstance());
            formModel.getSubFormInstances().put(key(), typedFormRecords);
        }
        if (typedFormRecords.isEmpty()) {
            typedFormRecords.add(newValueInstance());
        }
        return typedFormRecords;
    }

    private void render() {
        List<TypedFormRecord> instances = Lists.newArrayList(getInstances());

        Collections.sort(instances, new Comparator<TypedFormRecord>() {
            @Override
            public int compare(TypedFormRecord o1, TypedFormRecord o2) {
                return getCreationTime(o1).compareTo(getCreationTime(o2));
            }
        });

        for (TypedFormRecord instance : instances) {
            addForm(instance);
        }

        panel.add(addButton);
    }

    private static Long getCreationTime(TypedFormRecord instance) {
        return 0L; // todo
    }

    private void addForm(final TypedFormRecord typedFormRecord) {
        addForm(typedFormRecord, -1);
    }

    private TypedFormRecord newValueInstance() {
        TypedFormRecord newInstance = new TypedFormRecord(ResourceId.generateSubmissionId(subForm.getId()), subForm.getId());
        newInstance.setParentRecordId(formModel.getWorkingRootInstance().getId());

        FormModel.SubformValueKey key = key();

        Set<TypedFormRecord> instances = formModel.getSubFormInstances().get(key);
        if (instances == null) {
            instances = Sets.newHashSet();
            formModel.getSubFormInstances().put(key, instances);
        }

        instances.add(newInstance);
        formModel.getChangedInstances().add(newInstance);

        return newInstance;
    }

    private FormModel.SubformValueKey key() {
        return new FormModel.SubformValueKey(subForm, formModel.getWorkingRootInstance());
    }

    private void addForm(final TypedFormRecord instance, int panelIndex) {

        final SimpleFormPanel formPanel = new SimpleFormPanel(formModel.getLocator(), formModel.getStateProvider());
        formPanel.asWidget().addStyleName(FormPanelStyles.INSTANCE.subformPanel());

        formPanel.addDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.remove(formPanel);
                forms.remove(instance);

                if (loader.isPersisted(instance)) { // schedule deletion only if instance is persisted
                    formModel.getPersistedInstanceToRemoveByLocator().add(instance);
                }

                getInstances().remove(instance);
                formModel.getChangedInstances().remove(instance);

                setDeleteButtonsState();
            }
        });

        formPanel.show(instance);

        forms.put(instance, formPanel);

        if (panelIndex == -1) {
            panel.add(formPanel);
        } else {
            panel.insert(formPanel, panelIndex);
        }

        setDeleteButtonsState();
    }

    private void setDeleteButtonsState() {
        boolean moreThanOne = forms.size() > 1;
        for (SimpleFormPanel form : forms.values()) {
            form.getDeleteButton().get().setEnabled(moreThanOne);
        }
    }

    public Map<TypedFormRecord, SimpleFormPanel> getForms() {
        return forms;
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    public Promise<Void> show(Void value) {
        return loader.load(subForm).then(new Function<List<TypedFormRecord>, Void>() {
            @Override
            public Void apply(List<TypedFormRecord> input) {
                render();
                return null;
            }
        });
    }

    @Override
    public LoadingPanel<Void> getLoadingPanel() {
        return loadingPanel;
    }
}
