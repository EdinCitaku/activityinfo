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
package org.activityinfo.ui.client.component.importDialog;

import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromisesExecutionMonitor;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImportStrategies;
import org.activityinfo.ui.client.component.importDialog.model.strategy.FieldImportStrategy;
import org.activityinfo.ui.client.component.importDialog.model.strategy.ImportTarget;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Logger;


public class Importer {

    private static final Logger LOGGER = Logger.getLogger(Importer.class.getName());

    private ResourceLocator resourceLocator;

    static class TargetField {
        FormTree.Node node;
        FieldImportStrategy strategy;

        private TargetField(FormTree.Node node, FieldImportStrategy strategy) {
            this.node = node;
            this.strategy = strategy;
        }

        @Override
        public String toString() {
            return node.toString();
        }
    }

    private List<TargetField> fields = Lists.newArrayList();

    public Importer(ResourceLocator resourceLocator, FormTree formTree, FieldImportStrategies fieldImportStrategies) {
        this.resourceLocator = resourceLocator;
        for (FormTree.Node rootField : formTree.getRootFields()) {
            if (rootField.isCalculated()) {
                continue;
            }
            fields.add(new TargetField(rootField, fieldImportStrategies.forField(rootField)));
        }
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }

    public List<ImportTarget> getImportTargets() {
        List<ImportTarget> targets = Lists.newArrayList();
        for (TargetField binding : fields) {
            targets.addAll(binding.strategy.getImportSites(binding.node));
        }
        return targets;
    }

    public Promise<ValidatedRowTable> validateRows(final ImportModel model) {
        try {
            final ImportCommandExecutor modeller = new ImportCommandExecutor(model, fields, resourceLocator);
            return modeller.execute(new ValidateRowsImportCommand());
        } catch (Exception e) {
            LOGGER.severe("Failed to validate rows, exception: " + e.getMessage());
            return Promise.rejected(e);
        }
    }

    public Promise<List<ValidationResult>> validateClass(final ImportModel model) {
        try {
            final ImportCommandExecutor modeller = new ImportCommandExecutor(model, fields, resourceLocator);
            return modeller.execute(new ValidateClassImportCommand());
        } catch (Exception e) {
            LOGGER.severe("Failed to validate rows, exception: " + e.getMessage());
            return Promise.rejected(e);
        }
    }

    public Promise<Void> persist(final ImportModel model) {
        return persist(model, null);
    }

    public Promise<Void> persist(final ImportModel model, @Nullable PromisesExecutionMonitor monitor) {
        final ImportCommandExecutor modeller = new ImportCommandExecutor(model, fields, resourceLocator);
        return modeller.execute(new PersistImportCommand(monitor));
    }
}
