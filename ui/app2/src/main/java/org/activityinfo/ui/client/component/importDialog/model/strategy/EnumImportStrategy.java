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
package org.activityinfo.ui.client.component.importDialog.model.strategy;

import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 09/01/2015.
 */
public class EnumImportStrategy implements FieldImportStrategy {

    public static final TargetSiteId VALUE = new TargetSiteId("value");

    @Override
    public boolean accept(FormTree.Node fieldNode) {
        return fieldNode.isEnum();
    }

    @Override
    public List<ImportTarget> getImportSites(FormTree.Node node) {
        EnumType type = (EnumType) node.getType();
        List<ImportTarget> result = Lists.newArrayList();
        if (type.getCardinality() == Cardinality.SINGLE) {
            result.add(new ImportTarget(node.getField(), VALUE, node.getField().getLabel(), node.getDefiningFormClass().getId()));
        } else {
            for (EnumItem item : type.getValues()) {
                result.add(new ImportTarget(node.getField(), new TargetSiteId(item.getId().asString()), label(item.getLabel(), node.getField().getLabel()), node.getDefiningFormClass().getId()));
            }
        }
        return result;
    }

    public static String label(String itemLabel, String fieldLabel) {
        return itemLabel + " - " + fieldLabel;
    }

    @Override
    public FieldImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> mappings, ImportModel model) {

        EnumType type = (EnumType) node.getType();
        List<ColumnAccessor> sourceColumns = Lists.newArrayList();

        if (type.getCardinality() == Cardinality.SINGLE) {
            sourceColumns.add(mappings.get(VALUE));
        } else {
            for (EnumItem item : type.getValues()) {
                ColumnAccessor accessor = mappings.get(new TargetSiteId(item.getId().asString()));
                if (accessor == null) {
                    accessor = new EmptyColumn(item.getLabel());
                }
                sourceColumns.add(accessor);
            }
        }

        return new EnumFieldImporter(sourceColumns, getImportSites(node), type);
    }

}
