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
package org.activityinfo.ui.client.component.form;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.event.BeforeSaveEvent;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import java.util.List;

/**
 * @author yuriyz on 02/18/2015.
 */
public class FormActions {

    private final ResourceLocator locator;
    private final FormModel model;

    public FormActions(ResourceLocator locator, FormModel model) {
        this.locator = locator;
        this.model = model;
    }

    public Promise<List<TypedFormRecord>> save() {

        model.getEventBus().fireEvent(new BeforeSaveEvent());

        final List<TypedFormRecord> instancesToPersist = getInstancesToPersist();

        Promise<Void> persist = locator.persist(instancesToPersist);
        Promise<Void> remove = remove();

        final Promise<List<TypedFormRecord>> result = new Promise<>();

        Promise.waitAll(persist, remove).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                result.onFailure(caught);
            }

            @Override
            public void onSuccess(Void input) {
                model.getChangedInstances().clear();
                result.onSuccess(instancesToPersist);
            }
        });

        return result;
    }

    private Promise<Void> remove() {
        Promise<Void> remove = Promise.done();

        if (!model.getPersistedInstanceToRemoveByLocator().isEmpty()) {
            List<Promise<Void>> removePromises = Lists.newArrayList();
            for (TypedFormRecord instance : model.getPersistedInstanceToRemoveByLocator()) {
                removePromises.add(locator.remove(instance.getFormId(), instance.getId()));
            }
            remove = Promise.waitAll(removePromises);
            remove.then(new Function<Void, Object>() {
                @Override
                public Object apply(Void input) {
                    model.getPersistedInstanceToRemoveByLocator().clear();
                    return null;
                }
            });
        }
        return remove;
    }

    private List<TypedFormRecord> getInstancesToPersist() {
        final List<TypedFormRecord> toPersist = Lists.newArrayList();

        for (TypedFormRecord instance : model.getChangedInstances()) {
            if (!instance.isEmpty("classId", "keyId", "sort")) {
                if (instance.getId().getDomain() == ResourceId.GENERATED_ID_DOMAIN) {
                    ResourceId.checkSubmissionId(instance.getId());
                }

                toPersist.add(instance);
            }
        }
        return toPersist;
    }

}
