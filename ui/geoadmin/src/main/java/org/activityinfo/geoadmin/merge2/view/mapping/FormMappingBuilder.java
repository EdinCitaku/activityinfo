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
package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.activityinfo.geoadmin.merge2.model.ReferenceMatch;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulSet;
import org.activityinfo.observable.SynchronousScheduler;
import org.activityinfo.store.ResourceStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Constructs a set of {@link org.activityinfo.geoadmin.merge2.view.mapping.FieldMapping}s
 * 
 */
public class FormMappingBuilder {
    
    private final ResourceStore resourceStore;
    private StatefulSet<ReferenceMatch> referenceMatches;
    private final FormProfile source;
    private final FormProfile target;
    
    private final KeyFieldPairSet fieldMatching;

    private List<Observable<FieldMapping>> mappings = new ArrayList<>();

    public FormMappingBuilder(ResourceStore resourceStore,
                              KeyFieldPairSet keyFields, 
                              StatefulSet<ReferenceMatch> referenceMatches) {
        this.resourceStore = resourceStore;
        this.referenceMatches = referenceMatches;
        this.source = keyFields.getSource();
        this.target = keyFields.getTarget();
        this.fieldMatching = keyFields;
    }
    
    public Observable<List<FieldMapping>> build() {
        // Add simple field mappings, where we just copy the text/quantity/etc field from 
        // the source to the target field
        for (FieldProfile targetField : target.getFields()) {
            if (targetField.getNode().isRoot())
                if (SimpleFieldMapping.isSimple(targetField)) {
                    buildSimpleMapping(targetField);
                }
        }
        
        // Add mappings for ReferenceFields, for which we have to perform look ups
        for (FormTree.Node targetNode : target.getFormTree().getRootFields()) {
            if(targetNode.isReference()) {
                buildReferenceMapping(targetNode.getField());
            }
        }

        // Add mapping for geography, IIF source and target have exactly one GeoArea
        Set<FormField> targetGeoFields = findGeoFields(target.getFormTree());
        Set<FormField> sourceGeoFields = findGeoFields(source.getFormTree());
        if(targetGeoFields.size() == 1 && sourceGeoFields.size() == 1) {
            FormField sourceField = Iterables.getOnlyElement(sourceGeoFields);
            FormField targetField = Iterables.getOnlyElement(targetGeoFields);
            mappings.add(Observable.<FieldMapping>just(new GeoAreaFieldMapping(
                    source.getField(sourceField.getId()),
                    targetField)));
        }

        return Observable.flatten(SynchronousScheduler.INSTANCE, mappings);
    }

    private Set<FormField> findGeoFields(FormTree tree) {
        Set<FormField> set = new HashSet<>();
        for (FormTree.Node node : tree.getRootFields()) {
            if(node.getType() instanceof GeoAreaType) {
                set.add(node.getField());
            }
        }
        return set;
    }


    /**
     * Builds a reference mapping for a given target node.
     * 
     * <p>A reference field takes the value of a single {@code ResourceId}, but
     * most of the time we don't have the actual id of the field in the dataset to import:
     * we have to obtain the id by performing a look up against text fields in the 
     * </p>
     *
     * @param targetField the reference field to look up
     */
    private void buildReferenceMapping(final FormField targetField) {

        // In order to match against the ReferenceField, we need the actual data
        // from the form that is being referenced. 
        // 
        // Example: If we have a "Location" field that references the "Province" form class,
        // then we need then names and/or codes of the Province form class in order to lookup the
        // ids, assuming that our source dataset has a "province name" column with the names of the provinces.

        ReferenceType type = (ReferenceType) targetField.getType();

        // Currently this only supports reference fields that reference exactly one form class.
        if (type.getCardinality() == Cardinality.SINGLE && type.getRange().size() == 1) {

            ResourceId referenceFormId = Iterables.getOnlyElement(type.getRange());
            Observable<FormProfile> lookupForm = FormProfile.profile(resourceStore, referenceFormId);

            Observable<FieldMapping> mapping = lookupForm.transform(new Function<FormProfile, FieldMapping>() {
                @Override
                public FieldMapping apply(FormProfile lookupForm) {
                    return new ReferenceFieldMapping(
                            targetField, 
                            KeyFieldPairSet.matchKeys(source, lookupForm), 
                            referenceMatches);
                }
            });
            mappings.add(mapping);
        }
    }


    private void buildSimpleMapping(FieldProfile targetField) {
        Optional<FieldProfile> sourceField = fieldMatching.targetToSource(targetField);
        if(sourceField.isPresent()) {
            mappings.add(Observable.<FieldMapping>just(new SimpleFieldMapping(sourceField.get(), targetField)));
        }
    }


}
