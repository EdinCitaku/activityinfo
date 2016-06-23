package org.activityinfo.store.query.impl;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;

import java.util.Collection;
import java.util.Map;


public class MockCollectionCatalog implements CollectionCatalog {
    
    private static final ResourceId COLLECTION_ID = ResourceId.valueOf("XYZ123");
    
    private MockResourceCollection collection = new MockResourceCollection();
    
    
    @Override
    public Optional<ResourceCollection> getCollection(ResourceId resourceId) {
        return Optional.<ResourceCollection>of(collection);
    }

    @Override
    public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
        return Optional.absent();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> collectionIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        
        FormClass formClass = new FormClass(COLLECTION_ID);
        
        return formClass;
    }
    
    private class MockResourceCollection implements ResourceCollection {

        @Override
        public CollectionPermissions getPermissions(int userId) {
            return CollectionPermissions.full();
        }

        @Override
        public Optional<FormRecord> get(ResourceId resourceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FormClass getFormClass() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateFormClass(FormClass formClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(ResourceUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(ResourceUpdate update) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ColumnQueryBuilder newColumnQuery() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long cacheVersion() {
            return 0;
        }

    }
}
