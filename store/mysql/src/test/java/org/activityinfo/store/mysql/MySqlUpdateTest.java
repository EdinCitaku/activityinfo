package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.SerialNumber;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.hrd.HrdSerialNumberProvider;
import org.activityinfo.store.query.server.Updater;
import org.activityinfo.store.spi.BlobAuthorizerStub;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.RecordUpdate;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.activityinfo.json.Json.createObject;
import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasValues;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class MySqlUpdateTest extends AbstractMySqlTest {

    public static final int CATASTROPHE_NATURELLE_ID = 1;

    private int userId = 1;

    @Before
    public void setupDatabase() throws Throwable {
        resetDatabase("catalog-test.db.xml");
    }
    
    @Test
    public void createSite() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", "s0000000013");
        changeObject.put("@class", activityFormClass(1).asString());
        changeObject.put("partner", partnerRecordId(1).asString());
        changeObject.put("date1", "2015-01-01");
        changeObject.put("date2", "2015-01-01");
        changeObject.put("BENE", 45000);
        changeObject.put("location", locationInstanceId(3).asString());

        Updater updater = updater();
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id", "partner.label", "BENE");
        
        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003", "s0000000013"));
        assertThat(column("partner.label"), hasValues("NRC", "NRC", "Solidarites", "NRC"));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000, 45000));
    }
    
    @Test
    public void updateSite() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", "s0000000001");
        changeObject.put("@class", activityFormClass(1).asString());
        changeObject.put("partner", partnerRecordId(2).asString());

        Updater updater = updater();
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id", "partner.label", "BENE");

        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003"));
        assertThat(column("partner.label"), hasValues("Solidarites", "NRC", "Solidarites"));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000));
    }

    @Test
    public void updateSiteSetValueToBlank() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", "s0000000001");
        changeObject.put("@class", activityFormClass(1).asString());
        changeObject.put("BENE", Json.createNull());
        changeObject.put("comments", Json.createNull());

        Updater updater = updater();
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id", "BENE", "comments");

        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003"));
        assertThat(column("BENE"), hasValues(null, 3600, 10000));
        assertThat(column("comments"), hasValues((String)null, null, null));

    }


    @Test
    public void updateAdminEntity() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", entity(21).asString());
        changeObject.put("@class", adminLevelFormClass(2).asString());
        changeObject.put("name", "Nouveau Irumu");

        Updater updater = updater();
        updater.setEnforcePermissions(false);
        updater.executeChange(changeObject);
        
        query(adminLevelFormClass(2), "_id", "name");
        
        assertThat(column("_id"), hasValues("z0000000010", "z0000000011", "z0000000012", "z0000000013", "z0000000021"));
        assertThat(column("name"), hasValues("Bukavu", "Walungu", "Shabunda", "Kalehe", "Nouveau Irumu"));
    }
    
    @Test
    public void deleteAdminEntity() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", entity(21).asString());
        changeObject.put("@class", adminLevelFormClass(2).asString());
        changeObject.put("@deleted", true);

        Updater updater = updater();
        updater.setEnforcePermissions(false);
        updater.executeChange(changeObject);

        query(adminLevelFormClass(2), "_id", "name");

        assertThat(column("name"), hasValues("Bukavu", "Walungu", "Shabunda", "Kalehe"));
    }


    @Test
    public void deleteSite() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", "s0000000001");
        changeObject.put("@class", activityFormClass(1).asString());
        changeObject.put("@deleted", true);

        Updater updater = updater();
        updater.executeChange(changeObject);

        newRequest();
        
        query(activityFormClass(1), "_id");

        assertThat(column("_id"), hasValues("s0000000002", "s0000000003"));
    }

    @Test
    public void deleteSiteWithMonthlyReports() {

        query(CuidAdapter.reportingPeriodFormClass(3), "_id", "site", CuidAdapter.indicatorField(5).asString());

        assertThat(column("site"), hasValues("s0000000009", "s0000000009", "s0000000009"));

        JsonObject changeObject = createObject();
        changeObject.put("@id", "s0000000009");
        changeObject.put("@class", activityFormClass(3).asString());
        changeObject.put("@deleted", true);

        newRequest();

        Updater updater = updater();
        updater.executeChange(changeObject);

        newRequest();

        query(CuidAdapter.reportingPeriodFormClass(3), "_id", "site", CuidAdapter.indicatorField(5).asString());
        assertThat(column("site"), hasValues(new String[0]));

    }

    @Test
    public void updateSiteWithMultipleProperties() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", "s0000000001");
        changeObject.put("@class", activityFormClass(1).asString());
        changeObject.put("partner", partnerRecordId(2).asString());

        changeObject.put("BENE", 2100);
        changeObject.put(attributeGroupField(1).asString(), "Deplacement");


        Updater updater = updater();
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id", "partner.label", "BENE", "cause");

        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003"));
        assertThat(column("partner.label"), hasValues("Solidarites", "NRC", "Solidarites"));
        assertThat(column("BENE"), hasValues(2100, 3600, 10000));
        assertThat(column("cause"), hasValues("Deplacement", "Deplacement", "Catastrophe Naturelle"));
    }

    @Test
    public void updateSiteWithMultiAttributes() {
        JsonObject changeObject = createObject();
        changeObject.put("@id", "s0000000001");
        changeObject.put("@class", activityFormClass(1).asString());
        changeObject.put(attributeGroupField(1).asString(), "Deplacement");
        changeObject.put(attributeGroupField(2).asString(), "Casserole");

        Updater updater = updater();
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id",  "cause", "[contenu du kit]");

        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003"));
        assertThat(column("cause"), hasValues("Deplacement", "Deplacement", "Catastrophe Naturelle"));
    }
    
    @Test
    public void creatingActivitiesWithNullaryLocations() {

        int newId = new KeyGenerator().generateInt();

        JsonObject change = createObject();
        change.put("@id", cuid(SITE_DOMAIN, newId).asString());
        change.put("@class", activityFormClass(ADVOCACY).asString());
        change.put("partner", partnerRecordId(1).asString());
        change.put("date1", "2015-01-01");
        change.put("date2", "2015-01-31");

        Updater updater = updater();
        updater.executeChange(change);

        query(activityFormClass(ADVOCACY), "_id", "partner");
    }

    @Test
    public void createForm() {
        KeyGenerator generator = new KeyGenerator();
        int activityId = generator.generateInt();

        FormClass formClass = new FormClass(CuidAdapter.activityFormClass(activityId));
        formClass.setDatabaseId(1);
        formClass.setLabel("New Form");
        formClass.addElement(new FormField(CuidAdapter.generateIndicatorId())
                .setType(TextType.SIMPLE)
                .setLabel("Name")
                .setRequired(true));


        catalog.createOrUpdateFormSchema(formClass);

        System.out.println("Created activity " + activityId);

//        FormClass reform = catalog.getFormClass(formClass.getId());
//
//        // Ensure that partner field is automatically added
//        FormField partnerField = reform.getField(CuidAdapter.partnerField(activityId));
//
//        assertThat(partnerField.getType(), instanceOf(ReferenceType.class));
    }

    @Test
    public void createFormWithSerialNumber() {

        userId = 1;

        KeyGenerator generator = new KeyGenerator();
        int activityId = generator.generateInt();

        FormClass formClass = new FormClass(CuidAdapter.activityFormClass(activityId));
        formClass.setDatabaseId(1);
        formClass.setLabel("New Form");
        FormField serialNumField = new FormField(CuidAdapter.generateIndicatorId())
                .setType(new SerialNumberType())
                .setLabel("NUM")
                .setRequired(true);
        formClass.addElement(serialNumField);

        FormField nameField = new FormField(CuidAdapter.generateIndicatorId())
                .setType(TextType.SIMPLE)
                .setLabel("Name")
                .setRequired(true);
        formClass.addElement(nameField);

        catalog.createOrUpdateFormSchema(formClass);

        newRequest();

        ResourceId siteId = CuidAdapter.generateSiteCuid();

        // Create the record
        FormInstance creation = new FormInstance(siteId, formClass.getId());
        creation.set(nameField.getId(), TextValue.valueOf("Bob"));
        creation.set(partnerField(activityId), CuidAdapter.partnerRef(1, 1));

        executeUpdate(creation);

        newRequest();

        // Verify that the record has been created
        FormInstance created = FormInstance.toFormInstance(formClass,
                catalog.getForm(formClass.getId()).get().get(siteId).get());

        assertThat(created.get(nameField.getId()), equalTo((FieldValue)TextValue.valueOf("Bob")));
        assertThat(created.get(serialNumField.getId()), equalTo((FieldValue)new SerialNumber(1)));

        newRequest();

        // Now update the record's name
        JsonObject fieldValues = createObject();
        fieldValues.put(nameField.getName(), TextValue.valueOf("Sue").toJsonElement());

        // the UI may send null values
        fieldValues.put(serialNumField.getName(), Json.createNull());

        JsonObject update = createObject();
        update.put("fieldValues", fieldValues);

        updater().execute(formClass.getId(), siteId, update);

        newRequest();

        // Finally verify that the serial number is unchanged
        FormInstance updated = FormInstance.toFormInstance(formClass,
                catalog.getForm(formClass.getId()).get().get(siteId).get());

        assertThat(updated.get(nameField.getId()), equalTo((FieldValue)TextValue.valueOf("Sue")));
        assertThat(updated.get(serialNumField.getId()), equalTo((FieldValue)new SerialNumber(1)));

    }


    private void executeUpdate(FormInstance creation) {
        Updater updater = updater();
        updater.execute(creation);
    }

    private Updater updater() {
        return new Updater(catalog, userId, new BlobAuthorizerStub(), new HrdSerialNumberProvider());
    }


    @Test
    public void testSingleSiteResource() throws IOException {

        int databaseId = 1;
        ResourceId formId = CuidAdapter.activityFormClass(1);
        RecordUpdate update = new RecordUpdate();
        update.setUserId(userId);
        update.setFormId(formId);
        update.setRecordId(cuid(SITE_DOMAIN, 1));
        update.set(field(formId, PARTNER_FIELD), CuidAdapter.partnerRef(databaseId, 2));
        update.set(indicatorField(1), new Quantity(900, "units"));
        update.set(attributeGroupField(1), new EnumValue(attributeId(CATASTROPHE_NATURELLE_ID)));

        Updater updater = updater();
        updater.execute(update);

        query(CuidAdapter.activityFormClass(1), "_id", "partner", "BENE", "cause");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2), cuid(SITE_DOMAIN, 3)));
        assertThat(column("partner"), hasValues(partnerRecordId(2), partnerRecordId(1), partnerRecordId(2)));
        assertThat(column("BENE"), hasValues(900, 3600, 10000));
        assertThat(column("cause"), hasValues("Catastrophe Naturelle", "Deplacement", "Catastrophe Naturelle"));
    }

    @Test
    public void updateGeometry() throws SQLException {

        userId = 3;

        ResourceId formId = CuidAdapter.adminLevelFormClass(1);
        ResourceId recordId = entity(1);
        ResourceId fieldId = CuidAdapter.field(formId, CuidAdapter.GEOMETRY_FIELD);

        Optional<FormStorage> storage = catalog.getForm(formId);

        GeometryFactory factory = new GeometryFactory();
        Polygon polygon = new Polygon(new LinearRing(new CoordinateArraySequence(
                new Coordinate[]{
                        new Coordinate(100, 0),
                        new Coordinate(101, 0),
                        new Coordinate(101, 1),
                        new Coordinate(100, 1),
                        new Coordinate(100, 0)
                }), factory), new LinearRing[0], factory);



        storage.get().updateGeometry(recordId, fieldId, polygon);

        query(formId, "_id", "ST_XMIN(boundary)", "ST_XMAX(boundary)");
    }

    @Test
    public void addNewAttributes() {
        KeyGenerator generator = new KeyGenerator();
        int activityId = generator.generateInt();

        EnumType enumType = new EnumType(Cardinality.SINGLE,
                new EnumItem(EnumItem.generateId(), "A"),
                new EnumItem(EnumItem.generateId(), "B"));

        FormField selectField = new FormField(ResourceId.generateFieldId(EnumType.TYPE_CLASS))
                .setType(enumType)
                .setLabel("Select");

        FormClass formClass = new FormClass(CuidAdapter.activityFormClass(activityId));
        formClass.setDatabaseId(1);
        formClass.setLabel("New Form");

        formClass.addElement(selectField);

        catalog.createOrUpdateFormSchema(formClass);

        System.out.println("Created activity " + activityId);

        // Now change the enum items
        EnumType updatedType = new EnumType(Cardinality.SINGLE,
                new EnumItem(EnumItem.generateId(), "C"),
                new EnumItem(EnumItem.generateId(), "D"));

        selectField.setType(updatedType);

        newRequest();

        catalog.createOrUpdateFormSchema(formClass);

        newRequest();


        // Now try to save a new instance with the value

        FieldValue valueC = new EnumValue(updatedType.getValues().get(0).getId());

        FormInstance newRecord = new FormInstance(CuidAdapter.generateSiteCuid(), formClass.getId());
        newRecord.set(selectField.getId(), new EnumValue(updatedType.getValues().get(0).getId()));
        newRecord.set(CuidAdapter.partnerField(activityId), CuidAdapter.partnerRef(1, 1));

        executeUpdate(newRecord);

        // Ensure that the select field has been saved
        FormRecord saved = catalog.getForm(formClass.getId()).get().get(newRecord.getId()).get();
        FormInstance savedInstance = FormInstance.toFormInstance(formClass, saved);

        assertThat(savedInstance.get(selectField.getId()), equalTo(valueC));
    }


}
