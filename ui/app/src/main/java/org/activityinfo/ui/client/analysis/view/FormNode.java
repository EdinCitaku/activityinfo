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
package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.analysis.pivot.DimensionMapping;
import org.activityinfo.model.analysis.pivot.DimensionModel;
import org.activityinfo.model.analysis.pivot.ImmutableDimensionModel;
import org.activityinfo.ui.client.icons.IconBundle;


public class FormNode extends DimensionNode {
    @Override
    public String getKey() {
        return "_form";
    }

    @Override
    public String getLabel() {
        return I18N.CONSTANTS.form();
    }

    @Override
    public DimensionModel dimensionModel() {
        return ImmutableDimensionModel.builder()
            .id(ResourceId.generateCuid())
            .label(I18N.CONSTANTS.form())
            .addMappings(DimensionMapping.formMapping())
            .build();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.form();
    }
}
