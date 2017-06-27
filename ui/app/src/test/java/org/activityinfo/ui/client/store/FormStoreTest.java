package org.activityinfo.ui.client.store;

import com.google.gwt.core.client.testing.StubScheduler;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.observable.Connection;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.store.http.HttpStore;
import org.activityinfo.indexedb.IDBFactoryStub;
import org.activityinfo.ui.client.store.offline.OfflineStore;
import org.activityinfo.ui.client.store.offline.PendingStatus;
import org.activityinfo.ui.client.store.offline.RecordSynchronizer;
import org.activityinfo.ui.client.store.offline.SnapshotStatus;
import org.junit.Before;
import org.junit.Test;

import static org.activityinfo.observable.ObservableTesting.connect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class FormStoreTest {

    @Before
    public void setup() {
        LocaleProxy.initialize();
    }

    private final StubScheduler scheduler = new StubScheduler();

    /**
     * If at first the remote fetch does not succeed because
     * of network problems, we should keep retrying.
     */
    @Test
    public void formSchemaFetchesAreRetried() {

        AsyncClientStub client = new AsyncClientStub();
        HttpStore httpStore = new HttpStore(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(httpStore, new IDBFactoryStub());

        Survey survey = client.getCatalog().getSurvey();

        // We start offline
        client.setConnected(false);



        // Now the view connects and should remain in loading state...
        FormStoreImpl formStore = new FormStoreImpl(httpStore, offlineStore, scheduler);
        Connection<FormTree> view = connect(formStore.getFormTree(survey.getFormId()));
        view.assertLoading();

        // Start retries, but we're still offline
        scheduler.executeCommands();

        view.assertLoading();

        // Now connect and retry
        client.setConnected(true);

        scheduler.executeCommands();

        // View should be loaded
        view.assertLoaded();
    }

    @Test
    public void httpBus() {
        AsyncClientStub client = new AsyncClientStub();
        Survey survey = client.getCatalog().getSurvey();

        HttpStore httpStore = new HttpStore(client, scheduler);

        Connection<FormTree> view = connect(httpStore.getFormTree(survey.getFormId()));

        runScheduled();

        view.assertLoaded();
    }


    @Test
    public void offlineRecordFetching() {
        AsyncClientStub client = new AsyncClientStub();
        HttpStore httpStore = new HttpStore(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(httpStore, new IDBFactoryStub());
        FormStoreImpl formStore = new FormStoreImpl(httpStore, offlineStore, scheduler);

        Survey survey = client.getCatalog().getSurvey();

        // Start online
        Connection<OfflineStatus> offlineStatusView = connect(formStore.getOfflineStatus(survey.getFormId()));

        // Initially form should not be loaded
        assertFalse(offlineStatusView.assertLoaded().isEnabled());

        // and mark the survey form for offline usage
        offlineStore.enableOffline(survey.getFormId(), true);

        assertTrue(offlineStatusView.assertLoaded().isEnabled());
        assertFalse(offlineStatusView.assertLoaded().isCached());

        // Now synchronize...
        RecordSynchronizer synchronizer = new RecordSynchronizer(httpStore, offlineStore);

        runScheduled();

        // We go offline...
        client.setConnected(false);

        // Should be able to view the form class and a record
        Connection<FormTree> schemaView = connect(formStore.getFormTree(survey.getFormId()));
        Connection<Maybe<FormRecord>> recordView = connect(formStore.getRecord(survey.getRecordRef(0)));

        runScheduled();

        schemaView.assertLoaded();
        recordView.assertLoaded();

        assertTrue(offlineStatusView.assertLoaded().isEnabled());
        assertTrue(offlineStatusView.assertLoaded().isCached());
    }

    @Test
    public void offlineColumnQuery() {

        TestSetup setup = new TestSetup();
        Survey survey = setup.getSurveyForm();

        setup.getFormStore().setFormOffline(survey.getFormId(), true);
        setup.runScheduled();
        setup.setConnected(false);


        Connection<SnapshotStatus> snapshot = setup.connect(setup.getOfflineStore().getCurrentSnapshot());
        assertTrue(snapshot.assertLoaded().isFormCached(survey.getFormId()));

        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectResourceId().as("id");
        queryModel.selectField(survey.getNameFieldId()).as("name");
        queryModel.selectField(survey.getAgeFieldId()).as("age");

        ColumnSet columnSet = setup.connect(setup.getFormStore().query(queryModel)).assertLoaded();

        assertThat(columnSet.getNumRows(), equalTo(survey.getRowCount()));
        assertThat(columnSet.getColumnView("name").get(0), equalTo("Melanie"));
        assertThat(columnSet.getColumnView("name").get(1), equalTo("Joe"));
        assertThat(columnSet.getColumnView("name").get(2), equalTo("Matilda"));
    }

    @Test
    public void relatedFormsAreAlsoCached() {
        AsyncClientStub client = new AsyncClientStub();
        IntakeForm intakeForm = client.getCatalog().getIntakeForm();

        HttpStore httpStore = new HttpStore(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(httpStore, new IDBFactoryStub());
        FormStoreImpl formStore = new FormStoreImpl(httpStore, offlineStore, scheduler);

        // Start online, and enable offline mode for incidents
        formStore.setFormOffline(IncidentForm.FORM_ID, true);

        // Now synchronize...
        RecordSynchronizer synchronizer = new RecordSynchronizer(httpStore, offlineStore);

        runScheduled();

        // Ensure that related forms and subforms are also synchronized
        SnapshotStatus snapshot = connect(offlineStore.getCurrentSnapshot()).assertLoaded();

        assertTrue("incident form is cached", snapshot.isFormCached(IncidentForm.FORM_ID));
        assertTrue("sub form is cached", snapshot.isFormCached(ReferralSubForm.FORM_ID));
        assertTrue("related form is cached", snapshot.isFormCached(intakeForm.getFormId()));
    }

    @Test
    public void newRecordHitsQuery() {
        TestingCatalog catalog = new TestingCatalog();
        Survey survey = catalog.getSurvey();

        AsyncClientStub client = new AsyncClientStub(catalog);
        HttpStore httpStore = new HttpStore(client, scheduler);
        OfflineStore offlineStore = new OfflineStore(httpStore, new IDBFactoryStub());
        FormStoreImpl formStore = new FormStoreImpl(httpStore, offlineStore, scheduler);

        // Open a query on a set of records

        QueryModel queryModel = new QueryModel(survey.getFormId());
        queryModel.selectResourceId().as("id");

        Connection<ColumnSet> tableView = connect(formStore.query(queryModel));
        tableView.assertLoaded();

        // Add an new record to Survey
        tableView.resetChangeCounter();
        formStore.updateRecords(new RecordTransactionBuilder().add(catalog.addNew(survey.getFormId())).build());


        // Verify that the table view has been updated
        tableView.assertLoaded();
        tableView.assertChanged();

        assertThat(tableView.assertLoaded().getNumRows(), equalTo(survey.getRowCount() + 1));
    }

    @Test
    public void newRecordOffline() {
        TestSetup setup = new TestSetup();
        Survey survey = setup.getSurveyForm();

        // Synchronize the survey form

        setup.setConnected(true);
        setup.getFormStore().setFormOffline(survey.getFormId(), true);
        setup.runScheduled();

        // Go offline...
        setup.setConnected(false);

        // Monitor the pending queue status
        Connection<PendingStatus> pendingStatus = setup.connect(setup.getOfflineStore().getPendingStatus());

        assertThat(pendingStatus.assertLoaded().isEmpty(), equalTo(true));

        // Create a new survey record
        FormInstance newRecordTyped = survey.getGenerator().get();
        RecordTransaction tx = RecordTransaction.builder()
            .create(newRecordTyped)
            .build();

        // Update a record...
        Promise<Void> updateResult = setup.getFormStore().updateRecords(tx);
        assertThat(updateResult.getState(), equalTo(Promise.State.FULFILLED));

        // Now query offline...
        Connection<Maybe<FormRecord>> recordView = setup.connect(setup.getFormStore().getRecord(newRecordTyped.getRef()));

        // It should be listed as a pending change...
        assertThat(pendingStatus.assertLoaded().getCount(), equalTo(1));

        Maybe<FormRecord> record = recordView.assertLoaded();
        assertThat(record.getState(), equalTo(Maybe.State.VISIBLE));
        assertThat(record.get().getRecordId(), equalTo(newRecordTyped.getId().asString()));

        // Finally go online and ensure that results are sent to the server
        setup.setConnected(true);
        setup.getOfflineStore().syncChanges();

        // Our queue should be empty again
        assertThat(pendingStatus.assertLoaded().isEmpty(), equalTo(true));

    }

    private void runScheduled() {
        while(scheduler.executeScheduledCommands()) {
            System.err.println("Still executing...");
        }
    }

}