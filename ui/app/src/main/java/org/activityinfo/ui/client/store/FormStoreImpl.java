package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.Scheduler;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.job.JobDescriptor;
import org.activityinfo.model.job.JobResult;
import org.activityinfo.model.job.JobStatus;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.http.CatalogRequest;
import org.activityinfo.ui.client.store.http.HttpStore;
import org.activityinfo.ui.client.store.offline.FormOfflineStatus;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.SnapshotStatus;

import java.util.List;
import java.util.logging.Logger;


public class FormStoreImpl implements FormStore {

    private static final Logger LOGGER = Logger.getLogger(FormStoreImpl.class.getName());

    private final HttpStore httpStore;
    private final OfflineStore offlineStore;
    private final Scheduler scheduler;

    public FormStoreImpl(HttpStore httpStore, OfflineStore offlineStore, Scheduler scheduler) {
        this.httpStore = httpStore;
        this.offlineStore = offlineStore;
        this.scheduler = scheduler;
    }

    @Override
    public Promise<Void> deleteForm(ResourceId formId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId rootFormId) {
        return new ObservableTree<>(new FormTreeLoader(rootFormId, this::getFormMetadata), scheduler);
    }


    @Override
    public Observable<List<CatalogEntry>> getCatalogRoots() {
        return httpStore.get(new CatalogRequest());
    }

    @Override
    public Observable<List<CatalogEntry>> getCatalogChildren(ResourceId parentId) {
        return httpStore.get(new CatalogRequest(parentId));
    }

    @Override
    public Observable<FormMetadata> getFormMetadata(ResourceId formId) {
        return offlineStore.getCurrentSnapshot().join(snapshot -> {
            if(snapshot.isFormCached(formId)) {
                return offlineStore.getCachedMetadata(formId);
            } else {
                return httpStore.getFormMetadata(formId);
            }
        });
    }


    @Override
    public Observable<Maybe<FormRecord>> getRecord(RecordRef recordRef) {
        return offlineStore.getCurrentSnapshot().join(snapshot -> {
           if(snapshot.isFormCached(recordRef.getFormId())) {
               return offlineStore.getCachedRecord(recordRef);
           } else {
               return httpStore.getRecord(recordRef);
           }
        });
    }

    @Override
    public Observable<ColumnSet> query(QueryModel queryModel) {
        // No support for multiple row sources -- should this still
        // be part of the model??

        if(queryModel.getRowSources().size() != 1) {
            return Observable.loading();
        }

        ResourceId rootFormId = queryModel.getRowSources().get(0).getRootFormId();

        return offlineStore.getCurrentSnapshot().join(snapshot -> {

            if(snapshot.isFormCached(rootFormId)) {
                // Snapshots by definition must include all related forms, so
                // if the root form is included in the offline set, we can safely
                // serve from the cache.

                // First grab the FormTree, needed for the query planning...

                return getFormTree(rootFormId).join(formTree -> offlineStore.query(formTree, queryModel));

            } else {

                // Hit the server for the query.

                return httpStore.query(queryModel);
            }
        });
    }


    @Override
    public void setFormOffline(ResourceId formId, boolean offline) {
        offlineStore.enableOffline(formId, offline);
    }

    @Override
    public Observable<FormOfflineStatus> getOfflineStatus(ResourceId formId) {
        Observable<Boolean> enabled = offlineStore.getOfflineForms().transform(set -> set.contains(formId));
        Observable<SnapshotStatus> snapshot = offlineStore.getCurrentSnapshot();

        return Observable.transform(enabled, snapshot, (e, s) -> new FormOfflineStatus(e, s.isFormCached(formId)));
    }

    @Override
    public Promise<Void> updateRecords(RecordTransaction tx) {
        Promise<SnapshotStatus> status = offlineStore.getCurrentSnapshot().once();

        return status.join(snapshot -> {
            if(snapshot.areAllCached(tx.getAffectedFormIds())) {
                return offlineStore.execute(tx);
            } else {
                return httpStore.updateRecords(tx);
            }
        });
    }

    @Override
    public <T extends JobDescriptor<R>, R extends JobResult> Observable<JobStatus<T, R>> startJob(T job) {
        return httpStore.startJob(job);
    }

}
