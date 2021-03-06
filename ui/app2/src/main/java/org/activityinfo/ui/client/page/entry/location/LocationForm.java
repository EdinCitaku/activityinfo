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
package org.activityinfo.ui.client.page.entry.location;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.user.client.Timer;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.entry.admin.AdminComboBox;
import org.activityinfo.ui.client.page.entry.admin.AdminFieldSetPresenter;
import org.activityinfo.ui.client.page.entry.admin.AdminSelectionChangedEvent;
import org.activityinfo.ui.client.page.entry.admin.BoundsChangedEvent;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;
import org.activityinfo.ui.client.widget.coord.CoordinateFields;

public class LocationForm extends LayoutContainer {

    private static final int LABEL_WIDTH = 100;
    private static final int FIELD_WIDTH = 150;
    private static final int BUTTON_SPACE = 5;

    private TextField<String> nameField;
    private TextField<String> axeField;

    private Timer nameTypeAheadTimer;

    private LocationSearchPresenter searchPresenter;
    private NewLocationPresenter newLocationPresenter;
    private AdminFieldSetPresenter adminPresenter;

    private CoordinateFields coordinateFields;

    private LayoutContainer newFormButtonContainer;
    private SearchAdminComboBoxSet comboBoxes;

    private LocationTypeDTO locationType;

    public LocationForm(Dispatcher dispatcher,
                        LocationTypeDTO locationType,
                        final LocationSearchPresenter searchPresenter,
                        final NewLocationPresenter newLocationPresenter) {
        this.searchPresenter = searchPresenter;
        this.newLocationPresenter = newLocationPresenter;
        this.locationType = locationType;

        FormLayout layout = new FormLayout();
        layout.setLabelWidth(LABEL_WIDTH);
        layout.setDefaultWidth(FIELD_WIDTH);
        setLayout(layout);
        setStyleAttribute("marginLeft", "8px");

        addAdminCombos(dispatcher, searchPresenter);
        addNameField();
        addAxeField();
        addCoordFields();
        addNewLocationButtons();

        adminPresenter.addListener(AdminSelectionChangedEvent.TYPE, new Listener<AdminSelectionChangedEvent>() {
            @Override
            public void handleEvent(AdminSelectionChangedEvent be) {
                search();
                coordinateFields.validate();
                forceBoundsUpdate();
            }
        });

        newLocationPresenter.addListener(NewLocationPresenter.ACTIVE_STATE_CHANGED, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                setNewFormActive(newLocationPresenter.isActive());
            }
        });

        adminPresenter.addListener(BoundsChangedEvent.TYPE, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                coordinateFields.setBounds(adminPresenter.getBoundsName(), adminPresenter.getBounds());
                newLocationPresenter.setBounds(adminPresenter.getBounds());
            }
        });

        nameTypeAheadTimer = new Timer() {

            @Override
            public void run() {
                search();
            }
        };
    }

    private void forceBoundsUpdate() {
        if (!newLocationPresenter.isActive()) {
            newLocationPresenter.setActive(true, false);
            newLocationPresenter.setBounds(adminPresenter.getBounds());
            newLocationPresenter.setActive(false, false);
        } else {
            newLocationPresenter.setBounds(adminPresenter.getBounds());
        }
    }

    private void addNameField() {
        nameField = new TextField<String>();
        nameField.setFieldLabel(locationType.getName());
        nameField.setMaxLength(50);
        add(nameField);

        nameField.addKeyListener(new KeyListener() {

            @Override
            public void componentKeyDown(ComponentEvent event) {
                nameTypeAheadTimer.schedule(200);
            }
        });
    }

    private void addAxeField() {
        axeField = new TextField<String>();
        axeField.setFieldLabel(I18N.CONSTANTS.axe());
        axeField.setMaxLength(50);
        add(axeField);
    }

    private void addAdminCombos(Dispatcher dispatcher, final LocationSearchPresenter searchPresenter) {
        adminPresenter = new AdminFieldSetPresenter(dispatcher,
                searchPresenter.getCountryBounds(),
                searchPresenter.getLocationType().getAdminLevels());

        comboBoxes = new SearchAdminComboBoxSet(this, adminPresenter);

        for (AdminComboBox comboBox : comboBoxes) {
            add(comboBox.asWidget());
        }
    }

    private void addCoordFields() {
        coordinateFields = new CoordinateFields();
        coordinateFields.setToolTip(I18N.CONSTANTS.coordinateToolTip());

        add(coordinateFields.getLatitudeField());
        add(coordinateFields.getLongitudeField());

        newLocationPresenter.addListener(NewLocationPresenter.POSITION_CHANGED, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                if (!newLocationPresenter.isProvisional()) {
                    coordinateFields.setValue(newLocationPresenter.getLatLng());
                }
            }
        });
        coordinateFields.addChangeListener(new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                AiLatLng value = coordinateFields.getValue();
                if (value != null) {
                    newLocationPresenter.setLatLng(value);
                }
            }
        });
    }

    private void addNewLocationButtons() {

        int buttonWidth = (FIELD_WIDTH - BUTTON_SPACE) / 2;

        Button saveButton = new Button(I18N.CONSTANTS.useNewLocation(),
                IconImageBundle.ICONS.save(),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        saveNewLocation();
                    }
                });
        saveButton.setWidth(buttonWidth);

        Button cancelButton = new Button(I18N.CONSTANTS.cancel(), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                newLocationPresenter.setActive(false, true);
            }
        });
        cancelButton.setWidth(buttonWidth);

        newFormButtonContainer = new LayoutContainer();
        newFormButtonContainer.setWidth(FIELD_WIDTH);
        newFormButtonContainer.setLayout(new HBoxLayout());
        newFormButtonContainer.add(saveButton);
        newFormButtonContainer.add(cancelButton, new HBoxLayoutData(0, 0, 0, BUTTON_SPACE));

        add(newFormButtonContainer, buttonLayout());

        setNewFormActive(false);
    }

    private FormData buttonLayout() {
        FormData containerLayout = new FormData();
        containerLayout.setMargins(new Margins(0, 0, 0, LABEL_WIDTH + BUTTON_SPACE));
        return containerLayout;
    }

    private void saveNewLocation() {
        if (coordinateFields.validate() && nameField.validate() && axeField.validate()) {
            LocationDTO newLocation = new LocationDTO();
            newLocation.setId(new KeyGenerator().generateInt());
            newLocation.setLocationTypeId(locationType.getId());
            newLocation.setName(nameField.getValue());
            newLocation.setAxe(axeField.getValue());
            newLocation.setLatitude(coordinateFields.getLatitudeField().getValue());
            newLocation.setLongitude(coordinateFields.getLongitudeField().getValue());
            for (AdminLevelDTO level : adminPresenter.getAdminLevels()) {
                newLocation.setAdminEntity(level.getId(), adminPresenter.getAdminEntity(level));
            }
            newLocationPresenter.accept(newLocation);
        }
    }

    private void setNewFormActive(boolean active) {
        axeField.setVisible(active);
        coordinateFields.setVisible(active);
        newFormButtonContainer.setVisible(active);
        newFormButtonContainer.layout(true);
        comboBoxes.setMode(active ? EditMode.NEW_LOCATION : EditMode.SEARCH);

        nameField.setAllowBlank(!active);
        nameField.setEmptyText(active ? null : I18N.CONSTANTS.typeToFilter());

        layout(true);
    }

    private void search() {
        searchPresenter.search(nameField.getRawValue(), adminPresenter.getAdminEntityIds(), adminPresenter.getBounds());
    }
}
