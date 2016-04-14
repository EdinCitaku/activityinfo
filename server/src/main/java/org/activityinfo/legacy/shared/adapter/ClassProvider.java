package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.client.NotFoundException;
import org.activityinfo.core.shared.application.ApplicationClassProvider;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.GetFormClass;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.FormClassResult;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.model.type.subform.SubFormTypeRegistry;
import org.activityinfo.promise.Promise;

import static org.activityinfo.model.legacy.CuidAdapter.*;

public class ClassProvider implements Function<ResourceId, Promise<FormClass>> {

    private final Dispatcher dispatcher;
    private final ApplicationClassProvider systemClassProvider = new ApplicationClassProvider();

    public ClassProvider(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Promise<FormClass> apply(final ResourceId classId) {
        switch (classId.getDomain()) {
            case ACTIVITY_DOMAIN:
                return dispatcher.execute(new GetFormClass(classId)).then(new Function<FormClassResult, FormClass>() {
                    @Override
                    public FormClass apply(FormClassResult input) {
                        return input.getFormClass();
                    }
                });
            case DATABASE_DOMAIN:
                return dispatcher.execute(new GetSchema()).then(new DatabaseFormClassAdapter(getLegacyIdFromCuid(classId)));

            case PARTNER_FORM_CLASS_DOMAIN:
                return Promise.resolved(PartnerClassAdapter.create(getLegacyIdFromCuid(classId)));

            case PROJECT_CLASS_DOMAIN:
                return Promise.resolved(BuiltinFormClasses.projectFormClass(getLegacyIdFromCuid(classId)));

            case ADMIN_LEVEL_DOMAIN:
                return dispatcher.execute(new GetSchema())
                                 .then(new AdminLevelClassAdapter(getLegacyIdFromCuid(classId)));

            case LOCATION_TYPE_DOMAIN:
                return dispatcher.execute(new GetSchema()).then(new LocationClassAdapter(getLegacyIdFromCuid(classId)));

            // this is just spike: not exactly sure how to deal with application/system-level classes properties
            // etc. The 'domains' that we're using for the legacy objects aren't really the same thing -- the whole
            // point
            // is that a location_type form class isn't treated specially by the application, while we are going
            // to have a small number of *different* form classes that ARE treated specially...
            case '_':
                SubFormType subFormKind = SubFormTypeRegistry.get().getType(classId);
                if (subFormKind != null) {
                    return Promise.resolved(subFormKind.getDefinition());
                }
                return Promise.resolved(systemClassProvider.get(classId));


            default:
                final Promise<FormClass> promise = new Promise<>();
                dispatcher.execute(new GetFormClass(classId)).then(new AsyncCallback<FormClassResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        promise.onFailure(new NotFoundException(classId));
                    }

                    @Override
                    public void onSuccess(FormClassResult result) {
                        promise.onSuccess(result.getFormClass());
                    }
                });
                return promise;
        }
    }

}
