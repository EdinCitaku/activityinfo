package org.activityinfo.server.command;

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

import com.bedatadriven.rebar.sql.server.jdbc.JdbcScheduler;
import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.api.shared.adapter.CuidAdapter;
import org.activityinfo.api.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.api.shared.command.CreateEntity;
import org.activityinfo.api.shared.command.GetSchema;
import org.activityinfo.api.shared.exception.CommandException;
import org.activityinfo.api.shared.model.*;
import org.activityinfo.api2.client.Promise;
import org.activityinfo.api2.client.PromiseMatchers;
import org.activityinfo.api2.client.ResourceLocator;
import org.activityinfo.api2.shared.Cuids;
import org.activityinfo.api2.shared.form.UserForm;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.endpoint.rest.SchemaCsvWriter;
import org.activityinfo.ui.full.client.importer.data.PastedImportSource;
import org.activityinfo.ui.full.client.page.config.design.importer.SchemaImporter;
import org.activityinfo.ui.full.client.page.config.design.importer.SchemaImporter.ProgressListener;
import org.activityinfo.ui.full.client.page.config.design.importer.SchemaImporter.Warning;
import org.activityinfo.ui.full.client.page.entry.LockedPeriodSet;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.activityinfo.api2.client.PromiseMatchers.resolution;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class GetSchemaTest extends CommandTestCase2 {

    @Before
    public void cleanUpScheduler() {
        JdbcScheduler.get().forceCleanup();
    }

    @Test
    public void testDatabaseVisibilityForOwners() throws CommandException {

        // owners should be able to see their databases

        setUser(1); // Alex

        SchemaDTO schema = execute(new GetSchema());

        assertThat("database count", schema.getDatabases().size(), equalTo(3));
        assertThat("database list is sorted", schema.getDatabases().get(0)
                .getName(), equalTo("Alpha"));

        assertTrue("ALEX(owner) in PEAR", schema.getDatabaseById(1) != null); // PEAR
        assertTrue("ALEX can design", schema.getDatabaseById(1)
                .isDesignAllowed());
        assertTrue("Alex can edit all", schema.getDatabaseById(1)
                .isEditAllowed());
        assertTrue("object graph is preserved", schema.getDatabaseById(1)
                .getCountry() == schema.getDatabaseById(2).getCountry());
        assertTrue("object graph is preserved (database-activity)",
                schema.getDatabaseById(1) ==
                        schema.getDatabaseById(1).getActivities().get(0).getDatabase());
        AdminLevelDTO adminLevel = schema.getCountries().get(0)
                .getAdminLevels().get(0);
        assertThat("CountryId is not null", adminLevel.getCountryId(),
                not(equalTo(0)));
        assertThat("CountryId is not null", adminLevel.getId(), not(equalTo(0)));

        assertTrue("CountryId is not null", schema.getCountries().get(0)
                .getAdminLevels().get(0).getCountryId() != 0);

        assertThat("deleted attribute is not present", schema
                .getActivityById(1).getAttributeGroups().size(), equalTo(3));
    }

    @Test
    @OnDataSet("/dbunit/sites-public.db.xml")
    public void testDatabasePublished() throws CommandException {

        // Anonymouse user should fetch schema database with pulished
        // activities.
        setUser(0);

        SchemaDTO schema = execute(new GetSchema());

        assertThat(schema.getDatabases().size(), equalTo(1));
    }

    @Test
    public void testLockedProjects() {
        setUser(1);
        SchemaDTO schema = execute(new GetSchema());

        assertThat(schema.getProjectById(1).getLockedPeriods().size(),
                equalTo(1));

        LockedPeriodSet locks = new LockedPeriodSet(schema);
        assertTrue(locks.isProjectLocked(1, new LocalDate(2009, 1, 1)));
        assertTrue(locks.isProjectLocked(1, new LocalDate(2009, 1, 6)));
        assertTrue(locks.isProjectLocked(1, new LocalDate(2009, 1, 12)));
        assertFalse(locks.isProjectLocked(1, new LocalDate(2008, 1, 12)));
        assertFalse(locks.isProjectLocked(1, new LocalDate(2010, 1, 12)));

    }

    @Test
    public void testDatabaseVisibilityForView() throws CommandException {

        setUser(2); // Bavon

        SchemaDTO schema = execute(new GetSchema());

        assertThat(schema.getDatabases().size(), equalTo(1));
        assertThat("BAVON in PEAR", schema.getDatabaseById(1),
                is(not(nullValue())));
        assertThat(schema.getDatabaseById(1).getMyPartnerId(), equalTo(1));
        assertThat(schema.getDatabaseById(1).isEditAllowed(), equalTo(true));
        assertThat(schema.getDatabaseById(1).isEditAllAllowed(), equalTo(false));
    }

    @Test
    public void testDatabaseVisibilityNone() throws CommandException {
        setUser(3); // Stefan

        SchemaDTO schema = execute(new GetSchema());

        assertTrue("STEFAN does not have access to RRM",
                schema.getDatabaseById(2) == null);
    }

    @Test
    public void testIndicators() throws CommandException {

        setUser(1); // Alex

        SchemaDTO schema = execute(new GetSchema());

        assertTrue("no indicators case",
                schema.getActivityById(2).getIndicators().size() == 0);

        ActivityDTO nfi = schema.getActivityById(1);

        assertThat("indicators are present", nfi.getIndicators().size(),
                equalTo(4));

        IndicatorDTO test = nfi.getIndicatorById(2);
        assertThat("property:name", test.getName(), equalTo("baches"));
        assertThat("property:units", test.getUnits(), equalTo("menages"));
        assertThat("property:aggregation", test.getAggregation(),
                equalTo(IndicatorDTO.AGGREGATE_SUM));
        assertThat("property:category", test.getCategory(), equalTo("outputs"));
        assertThat("property:listHeader", test.getListHeader(),
                equalTo("header"));
        assertThat("property:description", test.getDescription(),
                equalTo("desc"));
    }

    @Test
    public void testAttributes() throws CommandException {

        setUser(1); // Alex

        SchemaDTO schema = execute(new GetSchema());

        assertTrue("no attributes case", schema.getActivityById(3)
                .getAttributeGroups().size() == 0);

        ActivityDTO nfi = schema.getActivityById(1);
        AttributeDTO[] attributes = nfi.getAttributeGroupById(1)
                .getAttributes().toArray(new AttributeDTO[0]);

        assertTrue("attributes are present", attributes.length == 2);

        AttributeDTO test = nfi.getAttributeById(1);

        assertEquals("property:name", "Catastrophe Naturelle", test.getName());
    }

    @Test
    public void toCSV() {
        SchemaDTO schema = execute(new GetSchema());

        SchemaCsvWriter writer = new SchemaCsvWriter();
        writer.write(schema.getDatabaseById(1));

        System.out.println(writer.toString());
    }

    @Test
    public void importCsv() throws IOException {

        String csv = Resources.toString(Resources.getResource("schema_1064.csv"), Charsets.UTF_8);
        PastedImportSource source = new PastedImportSource(csv);

        Map<String, Object> dbProps = Maps.newHashMap();
        dbProps.put("name", "Syria");
        dbProps.put("countryId", 1);

        execute(new CreateEntity("UserDatabase", dbProps));

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO syria = null;
        for (UserDatabaseDTO db : schema.getDatabases()) {
            if (db.getName().equals("Syria")) {
                syria = db;
                break;
            }
        }
        if (syria == null) {
            throw new AssertionError("database not created");
        }

        SchemaImporter importer = new SchemaImporter(getDispatcher(), syria);
        importer.setProgressListener(new ProgressListener() {

            @Override
            public void submittingBatch(int batchNumber, int batchCount) {
                System.out.println("Submitting batch " + batchNumber + " of " + batchCount);
            }
        });
        boolean success = importer.parseColumns(source);
        if (success) {
            importer.processRows();
        }

        for (Warning warning : importer.getWarnings()) {
            System.err.println(warning);
        }

        if (!success) {
            throw new AssertionError("there were fatal errors");
        }

        importer.persist(new AsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                System.out.println("Success");
            }

            @Override
            public void onFailure(Throwable caught) {
                throw new AssertionError(caught);
            }
        });

        syria = execute(new GetSchema()).getDatabaseById(syria.getId());

        assertThat(syria.getActivities().get(0).getAttributeGroups().size(), equalTo(3));

        SchemaCsvWriter writer = new SchemaCsvWriter();
        writer.write(syria);

        Files.write(writer.toString(), new File("target/syria.csv"), Charsets.UTF_8);
    }

    @Test
    public void newApiTest() {

        ResourceLocator locator = new ResourceLocatorAdaptor(getDispatcher());

        Promise<UserForm> userForm = locator.getUserForm(Cuids.toIri(CuidAdapter.ACTIVITY_DOMAIN, 1)).fetch();

        assertThat(userForm, resolution(CoreMatchers.<UserForm>notNullValue()));


    }
}
