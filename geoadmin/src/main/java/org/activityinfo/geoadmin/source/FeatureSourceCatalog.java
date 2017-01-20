package org.activityinfo.geoadmin.source;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.query.RowSource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.FormCatalog;
import org.activityinfo.service.store.FormStorage;
import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FeatureSourceCatalog implements FormCatalog {

    public static final String FILE_PREFIX = "file://";
    private Map<ResourceId, FeatureSourceStorage> sources = new HashMap<>();
    
    public boolean isLocalResource(ResourceId resourceId) {
        return resourceId.asString().startsWith(FILE_PREFIX);
    }

    public boolean isLocalQuery(QueryModel queryModel) {
        for (RowSource rowSource : queryModel.getRowSources()) {
            if(!isLocalResource(rowSource.getRootFormId())) {
                return false;
            }
        }
        return true;
    }
    
    public void add(ResourceId id, String path) throws IOException {
        File shapeFile = new File(path);
        ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
        sources.put(id, new FeatureSourceStorage(id, dataStore.getFeatureSource()));
    }
    
    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {

        FeatureSourceStorage accessor = sources.get(formId);
        if(accessor == null) {

            Preconditions.checkArgument(formId.asString().startsWith(FILE_PREFIX),
                    "FeatureSourceCatalog supports only resourceIds starting with file://");

            try {
                File shapeFile = new File(formId.asString().substring(FILE_PREFIX.length()));
                ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
                accessor = new FeatureSourceStorage(formId, dataStore.getFeatureSource());
                sources.put(formId, accessor);

            } catch (Exception e) {
                throw new IllegalArgumentException("Could not load " + formId, e);
            }
        }
        return Optional.<FormStorage>of(accessor);
    }

    @Override
    public Optional<FormStorage> lookupForm(ResourceId recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        Map<ResourceId, FormClass> map = new HashMap<>();
        for (ResourceId collectionId : formIds) {
            Optional<FormStorage> collection = getForm(collectionId);
            if(collection.isPresent()) {
                map.put(collectionId, collection.get().getFormClass());
            }
        }
        return map;
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        return null;
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return getForm(resourceId).get().getFormClass();
    }


}
