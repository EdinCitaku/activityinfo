package org.activityinfo.store.mysql;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.CollectionPermissions;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.collections.*;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;


public class MySqlCatalogProvider {

    private static final Logger LOGGER = Logger.getLogger(MySqlCatalogProvider.class.getName());

    private List<CollectionProvider> mappings = Lists.newArrayList();

    public MySqlCatalogProvider() {
        mappings.add(new SimpleTableCollectionProvider(new DatabaseTable(), CollectionPermissions.readonly()));
        mappings.add(new SimpleTableCollectionProvider(new UserTable(), CollectionPermissions.readonly()));
        mappings.add(new SimpleTableCollectionProvider(new CountryTable(), CollectionPermissions.readonly()));
        mappings.add(new SimpleTableCollectionProvider(new AdminEntityTable(), CollectionPermissions.readonly()));
        mappings.add(new SimpleTableCollectionProvider(new PartnerTable(), CollectionPermissions.readonly()));
        mappings.add(new SiteCollectionProvider());
        mappings.add(new LocationCollectionProvider());
        mappings.add(new ReportingPeriodCollectionProvider());
    }

    public CollectionCatalog openCatalog(final QueryExecutor executor) {
        return new CollectionCatalog() {
            @Override
            public Optional<ResourceCollection> getCollection(ResourceId resourceId) {
                for(CollectionProvider mapping : mappings) {
                    if(mapping.accept(resourceId)) {
                        try {
                            return Optional.of(mapping.openCollection(executor, resourceId));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return Optional.absent();
            }

            @Override
            public Optional<ResourceCollection> lookupCollection(ResourceId resourceId) {
                for (CollectionProvider mapping : mappings) {
                    try {
                        Optional<ResourceId> collectionId = mapping.lookupCollection(executor, resourceId);
                        if(collectionId.isPresent()) {
                            return getCollection(collectionId.get());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                return Optional.absent();  
            }

            @Override
            public FormClass getFormClass(ResourceId formClassId) {
                LOGGER.info("Requesting formClass " + formClassId);
                return getCollection(formClassId).get().getFormClass();
            }
        };
    }
}
