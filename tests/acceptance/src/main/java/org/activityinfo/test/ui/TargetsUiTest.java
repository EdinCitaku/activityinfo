package org.activityinfo.test.ui;
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

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.test.driver.FieldValue;
import org.activityinfo.test.driver.UiApplicationDriver;
import org.activityinfo.test.pageobject.web.ApplicationPage;
import org.activityinfo.test.pageobject.web.design.TargetsPage;
import org.junit.Test;

import javax.inject.Inject;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author yuriyz on 05/18/2015.
 */
public class TargetsUiTest {

    private static final String DATABASE = "My db";
    private static final String FORM_NAME = "NFI Distribution";

    @Inject
    private UiApplicationDriver driver;

    private void background() throws Exception {
        driver.login();
        driver.setup().createDatabase(property("name", DATABASE));

        String[] partners = new String[] { "ARC", "NRC", "RI" };
        for(String partner : partners) {
            driver.setup().addPartner(partner, DATABASE);
        }

        for (String project : new String[] {"FY2014", "FY2015"}) {
            driver.setup().createProject(
                    property("name", project),
                    property("database", DATABASE));
        }

        driver.setup().createForm(name(FORM_NAME), property("database", DATABASE));
        driver.setup().createField(
                property("form", FORM_NAME),
                property("name", "nb. kits"),
                property("type", "quantity"),
                property("code", "1")
        );
        driver.setup().createField(
                property("form", FORM_NAME),
                property("name", "Satisfaction score"),
                property("type", "quantity"),
                property("code", "1")
        );


        driver.createTarget(
                property("database", DATABASE),
                property("name", "Target1"),
                property("project", "FY2014")
        );
        driver.createTarget(
                property("database", DATABASE),
                property("name", "Target2"),
                property("project", "FY2015")
        );

        driver.setTargetValues("Target1", Lists.newArrayList(
                new FieldValue("nb. kits", "1000")
        ));
        driver.setTargetValues("Target2", Lists.newArrayList(
                new FieldValue("nb. kits", "2000")
        ));
    }

    @Test
    public void treeEditorStateOnSelectionChange() throws Exception {
        background();

        driver.setTargetValues("Target1", Lists.newArrayList(
                new FieldValue("nb. kits", "1000")
        ));

        TargetsPage targetPage = driver.targetsPage();
        targetPage.select(driver.alias("Target2"));
        targetPage.select(driver.alias("Target1")); // switch back and check whether value is in tree

        assertNotNull(targetPage.valueGrid().findCell("1000")); // value must be present in tree
    }

    @Test
    public void statefulTree() throws Exception {

        background();


        ApplicationPage app = driver.getApplicationPage();

        TargetsPage targets = app.navigateToDesignTab().selectDatabase(driver.alias(DATABASE)).targets();
        targets.select("Target1");
        targets.expandTree("nb. kits"); // expand tree only one time for Target1, then it should be stateful and keep it expanded
        targets.valueGrid().findCell("1000");
        targets.select("Target2");
        targets.valueGrid().findCell("2000");

        // back to Target1 without expand, tree must be already expanded
        targets.select("Target1");
        targets.valueGrid().findCell("1000");
    }

    @Test
    public void targetEdit() throws Exception {
        background();

        TargetsPage targets = driver.getApplicationPage().navigateToDesignTab().
                selectDatabase(driver.alias(DATABASE)).targets();
        targets.editTarget("Target1", driver.alias("ARC"), driver.alias("FY2014"));

        targets.targetGrid().findCell(driver.alias("ARC"));
        targets.targetGrid().findCell(driver.alias("FY2014"));

        targets.editTarget("Target1", I18N.CONSTANTS.none(), I18N.CONSTANTS.none());
        assertFalse(targets.targetGrid().findCellOptional(driver.alias("ARC")).isPresent());

    }

}
