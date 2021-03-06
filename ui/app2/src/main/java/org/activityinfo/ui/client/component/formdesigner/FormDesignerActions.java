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
package org.activityinfo.ui.client.component.formdesigner;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.promise.Promise;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuriyz on 7/14/14.
 */
public class FormDesignerActions {

    private final FormDesigner formDesigner;
    private final FormDesignerPanel formDesignerPanel;

    private FormDesignerActions(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        this.formDesignerPanel = formDesigner.getFormDesignerPanel();
    }

    public static FormDesignerActions create(FormDesigner formDesigner) {
        return new FormDesignerActions(formDesigner).bind();
    }

    private FormDesignerActions bind() {
        formDesignerPanel.getSaveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                save();
            }
        });
        return this;
    }

    private List<FormClass> getFormClassesForPersistence() {
        List<FormClass> classesToPersist = Lists.newArrayList();
        for (FormClass subForm : formDesigner.getModel().getSubforms()) {
            classesToPersist.add(subForm);
        }
        classesToPersist.add(formDesigner.getRootFormClass()); // order is important, subform classes must be saved first
        return classesToPersist;
    }

    public Promise<Void> save() {

        formDesignerPanel.getStatusMessage().setHTML(I18N.CONSTANTS.saving());
        formDesignerPanel.getSaveButton().setEnabled(false);

        Promise<Void> promise = persist(getFormClassesForPersistence());
        promise.then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                showFailureDelayed(caught);
                formDesigner.getSavedGuard().setSaved(false);
            }

            @Override
            public void onSuccess(Void result) {
                // delay a bit to show user progress
                Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        formDesignerPanel.getSaveButton().setText(I18N.CONSTANTS.save());
                        formDesignerPanel.getSaveButton().setEnabled(true);
                        formDesignerPanel.getStatusMessage().setHTML(I18N.CONSTANTS.saved());
                        formDesigner.getSavedGuard().setSaved(true);
                        return false;
                    }
                }, 500);
            }
        });
        return promise;
    }

    private Promise<Void> persist(List<FormClass> formClasses) {
        List<Promise<Void>> promises = new ArrayList<>();
        for (FormClass formClass : formClasses) {
            promises.add(formDesigner.getResourceLocator().persist(formClass));
        }
        return Promise.waitAll(promises);
    }

    private void showFailureDelayed(final Throwable caught) {
        Log.error(caught.getMessage(), caught);

        // Show failure message only after a short fixed delay to ensure that
        // the progress stage is displayed. Otherwise if we have a synchronous error, clicking
        // the retry button will look like it's not working.
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                formDesignerPanel.getSaveButton().setEnabled(true);
                formDesignerPanel.getStatusMessage().setHTML(I18N.CONSTANTS.failedToSaveClass());
                formDesignerPanel.getSaveButton().setText(I18N.CONSTANTS.retry());
                return false;
            }
        }, 500);
    }
}
