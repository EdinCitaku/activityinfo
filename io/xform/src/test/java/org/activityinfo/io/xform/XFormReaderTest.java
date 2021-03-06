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
package org.activityinfo.io.xform;

import com.google.common.io.Resources;
import org.activityinfo.io.xform.XFormReader;
import org.activityinfo.io.xform.form.XForm;
import org.activityinfo.model.form.*;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class XFormReaderTest {

    @Test
    public void test() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(XForm.class);

        URL formURL = Resources.getResource(XFormReader.class, "survey.xml");
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        XForm xform = (XForm) jaxbUnmarshaller.unmarshal(formURL);

        XFormReader reader = new XFormReader(xform);
        reader.setDatabaseId(101);
        reader.setActivityId(201);
        FormClass formClass = reader.build();

        dump("", formClass);

        FormField veg408 = findField(formClass, "veg408");

        assertThat(veg408.getLabel(),
                equalTo("E.37 In the last seven days did anyone in your household consume any cabbage ?"));

        FormField consumption_veg401 = findField(formClass, "v_401/consumption_veg401");
        assertThat(consumption_veg401.isVisible(), equalTo(true));
        assertThat(consumption_veg401.getLabel(),
                equalTo("E.30.1.1 why was the previous question left blank?"));

    }

    private void dump(String indent, FormElementContainer container) {
        for(FormElement element : container.getElements()) {
            if(element instanceof FormSection) {
                System.out.println(indent + element.getLabel());
                dump(indent + "   ", ((FormSection) element));
            } else {
                FormField field = ((FormField) element);
                System.out.println(String.format("%s[%s] %s : %s", indent, field.getCode(), field.getLabel(),
                        field.getType().getTypeClass().getId()));
            }
        }

    }

    private FormField findField(FormClass formClass, String code) {
        for(FormField field : formClass.getFields()) {
            if(code.equals(field.getCode())) {
                return field;
            }
        }
        throw new AssertionError("No field with code '" + code + "'");
    }
}
