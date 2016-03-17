package org.activityinfo.ui.client.component.formdesigner.palette;
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

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;

/**
 * @author yuriyz on 01/21/2015.
 */
public class SubformTemplate implements Template<FormField> {

    @Override
    public String getLabel() {
        return "Sub Form";
    }

    @Override
    public FormField create() {
        FormField field = new FormField(ResourceId.generateFieldId(SubFormReferenceType.TYPE_CLASS));
        field.setLabel("Sub Form");
        field.setType(new SubFormReferenceType());

        return field;
    }
}
