package org.activityinfo.store.mysql;

import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.store.mysql.collections.CountryCollection;
import org.activityinfo.store.mysql.collections.DatabaseCollection;
import org.activityinfo.store.query.impl.Updater;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasAllNullValuesWithLengthOf;
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasValues;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;


public class MySqlCatalogTest extends AbstractMySqlTest {

    public static final int CATASTROPHE_NATURELLE_ID = 1;
 
    @BeforeClass
    public static void initDatabase() throws Throwable {
        resetDatabase();
    }
    
    @Test
    public void testCountry() {
        query(CountryCollection.FORM_CLASS_ID, "label", "code");
        assertThat(column("label"), hasValues("Rdc"));
        assertThat(column("code"), hasValues("CD"));
    }

    @Test
    public void testDatabase() {
        query(DatabaseCollection.FORM_CLASS_ID, "label", "description", "country", "country.label", "country.code");
        assertThat(column("label"), hasValues("PEAR", "PEAR Plus", "Alpha", "Public"));
        assertThat(column("description"), hasAllNullValuesWithLengthOf(4));
        assertThat(column("country"), hasValues("c0000000001", "c0000000001", "c0000000001", "c0000000001"));
        assertThat(column("country.label"), hasValues("Rdc", "Rdc", "Rdc", "Rdc"));
        assertThat(column("country.code"), hasValues("CD", "CD", "CD", "CD"));
    }

    @Test
    public void testLocation() {
        query(CuidAdapter.locationFormClass(1), "label", "axe");
        assertThat(column("label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga", "Boga"));

        assertThat(column("axe"), hasValues(null, "Bunia-Wakombe", null, null));
    }
    
    
    @Test
    public void testAdmin() {
        query(CuidAdapter.adminLevelFormClass(2), "name", "province.name", "code", "boundary");
        assertThat(column("province.name"), hasValues("Ituri", "Sud Kivu", "Sud Kivu", "Sud Kivu", "Ituri"));
        assertThat(column("name"), hasValues("Bukavu", "Walungu", "Shabunda", "Kalehe", "Irumu"));
        assertThat(column("code"), hasValues( "203", "201", "202", "203", "203"));
    }
    
    @Test
    public void testAdminTree() {
        FormTreeBuilder builder = new FormTreeBuilder(catalogProvider);
        FormTree formTree = builder.queryTree(adminLevelFormClass(2));
        JsonObject formTreeObject = JsonFormTreeBuilder.toJson(formTree);
        formTree = JsonFormTreeBuilder.fromJson(formTreeObject);

        FormTreePrettyPrinter.print(formTree);
    }
    
    @Test
    public void testNoColumns() {
        QueryModel queryModel = new QueryModel(CuidAdapter.activityFormClass(1));
        columnSet = executor.build(queryModel);

        assertThat(columnSet.getNumRows(), equalTo(3));
    }
    

    @Test
    public void testSiteSimple() {
        query(CuidAdapter.activityFormClass(1), "_id", "date1", "date2", "partner", 
                "partner.label", "location.label", "BENE", "cause");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2), cuid(SITE_DOMAIN, 3)));
        assertThat(column("partner"), hasValues(partnerInstanceId(1), partnerInstanceId(1), partnerInstanceId(2)));
        assertThat(column("partner.label"), hasValues("NRC", "NRC", "Solidarites"));
        assertThat(column("location.label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga"));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000));
        assertThat(column("cause"), hasValues(null, "Deplacement", "Catastrophe Naturelle"));
    }

    
    @Test
    public void testSingleSiteResource() throws IOException {
        ResourceId formClass = CuidAdapter.activityFormClass(1);
        ResourceUpdate update = new ResourceUpdate();
        update.setResourceId(cuid(SITE_DOMAIN, 1));
        update.set(field(formClass, PARTNER_FIELD), new ReferenceValue(CuidAdapter.partnerInstanceId(2)));
        update.set(indicatorField(1), new Quantity(900, "units"));
        update.set(attributeGroupField(1), new EnumValue(attributeId(CATASTROPHE_NATURELLE_ID)));

        Updater updater = new Updater(catalogProvider);
        updater.execute(update);

        query(CuidAdapter.activityFormClass(1), "_id", "partner", "BENE", "cause");

        assertThat(column("_id"), hasValues(cuid(SITE_DOMAIN, 1), cuid(SITE_DOMAIN, 2), cuid(SITE_DOMAIN, 3)));
        assertThat(column("partner"), hasValues(partnerInstanceId(2), partnerInstanceId(1), partnerInstanceId(2)));
        assertThat(column("BENE"), hasValues(900, 3600, 10000));
        assertThat(column("cause"), hasValues("Catastrophe Naturelle", "Deplacement", "Catastrophe Naturelle"));
    }
    
    @Test
    public void siteFormClassWithNullaryLocations() {

        FormClass formClass = catalogProvider.getCollection(activityFormClass(ADVOCACY)).get().getFormClass();
       
        // Make a list of field codes
        Set<String> codes = new HashSet<>();
        for (FormField formField : formClass.getFields()) {
            codes.add(formField.getCode());
        }
        assertThat(codes, not(hasItem("location")));
    }

    @Test
    public void testReportingPeriod() {

        FormTreeBuilder treeBuilder = new FormTreeBuilder(catalogProvider);
        FormTree formTree = treeBuilder.queryTree(CuidAdapter.reportingPeriodFormClass(3));

        FormTreePrettyPrinter.print(formTree);
        
        query(CuidAdapter.reportingPeriodFormClass(3), "rate", "date1", "date2", "site.partner", "site.partner.label",
                "site.location.label");
    }


    @Test
    public void testSiteIndicator() {
        query(CuidAdapter.activityFormClass(1), "_id",
                CuidAdapter.indicatorField(1).asString(), "contact")    ;
        
    }


    @Test
    public void emptyIndicator() {
        query(CuidAdapter.activityFormClass(1), "_id", "q");
    }


}
