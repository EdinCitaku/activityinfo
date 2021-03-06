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
package org.activityinfo.server.command;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.UpdateEntity;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/schema1.db.xml")
public class AttributeGroupTest extends CommandTestCase {

    @Before
    public void setUp() {
        setUser(1);
    }

    @Test
    public void testCreate() throws Exception {

        // execute the command

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "Type de Conflit");
        properties.put("multipleAllowed", true);
        properties.put("activityId", 1);

        CreateEntity cmd = new CreateEntity("AttributeGroup", properties);

        CreateResult result = execute(cmd);

        // check if it has been added

        ActivityFormDTO activity = execute(new GetActivityForm(1));
        AttributeGroupDTO group = activity.getAttributeGroupById(result.getNewId());

        Assert.assertNotNull("attribute group is created", group);
        Assert.assertEquals("name is correct", group.getName(), "Type de Conflit");
        Assert.assertTrue("multiple allowed is set to true", group.isMultipleAllowed());
    }

    @Test
    public void testUpdate() throws Exception {


        // change the name of an entity group
        ActivityFormDTO activity = execute(new GetActivityForm(1));
        AttributeGroupDTO group = activity.getAttributeGroups().get(0);
        group.setName("Foobar");

        Map<String, Object> changes = new HashMap<String, Object>();
        changes.put("name", group.getName());

        execute(new UpdateEntity(group, changes));

        // reload data
        activity = execute(new GetActivityForm(1));
        // verify the property has been duly changed
        assertThat(activity.getAttributeGroups().get(0).getName(), equalTo(group.getName()));
    }
}
