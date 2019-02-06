package org.activityinfo.server.database.hibernate;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.json.Json;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.database.RecordLock;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.server.database.hibernate.entity.Activity;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Folder;
import org.activityinfo.server.database.hibernate.entity.LockedPeriod;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.store.spi.DatabaseMetaCache;
import org.activityinfo.store.spi.DatabaseMetaProvider;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.FormStorageProvider;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HibernateDatabaseMetaProvider implements DatabaseMetaProvider {

    private static final Logger LOGGER = Logger.getLogger(HibernateDatabaseGrantProvider.class.getName());

    private static final String CACHE_PREFIX = "dbMeta";
    private static final String CACHE_VERSION = "5";

    private final Provider<EntityManager> entityManager;
    private final FormStorageProvider formStorageProvider;
    private final DatabaseMetaCache sessionCache;
    private final MemcacheService memcacheService;
    private final BillingAccountOracle billingAccountOracle;

    @Inject
    public HibernateDatabaseMetaProvider(Provider<EntityManager> entityManager,
                                         FormStorageProvider formStorageProvider,
                                         DatabaseMetaCache sessionCache,
                                         MemcacheService memcacheService,
                                         BillingAccountOracle billingAccountOracle) {
        this.entityManager = entityManager;
        this.formStorageProvider = formStorageProvider;
        this.sessionCache = sessionCache;
        this.memcacheService = memcacheService;
        this.billingAccountOracle = billingAccountOracle;
    }

    @Override
    public Optional<DatabaseMeta> getDatabaseMeta(@NotNull ResourceId databaseId) {
        Long databaseVersion = queryDatabaseVersion(databaseId);
        if (databaseVersion == null) {
            return Optional.empty();
        }

        // Session Cache
        Map<ResourceId,DatabaseMeta> loaded = loadFromSessionCache(Collections.singletonMap(databaseId,databaseVersion));
        if (!loaded.isEmpty()) {
            return Optional.of(loaded.get(databaseId));
        }

        // Memcache
        loaded = loadFromMemcache(Collections.singletonMap(databaseId,databaseVersion));
        if (!loaded.isEmpty()) {
            cacheToSessionCache(loaded.values());
            return Optional.of(loaded.get(databaseId));
        }

        // Database
        Map<ResourceId,DatabaseMeta> loadedFromDb = loadFromDb(Collections.singleton(databaseId));
        if (loadedFromDb.isEmpty()) {
            return Optional.empty();
        }
        cacheToSessionCache(loadedFromDb.values());
        cacheToMemcache(loadedFromDb.values());
        return Optional.of(loadedFromDb.get(databaseId));
    }

    @Override
    public Map<ResourceId,DatabaseMeta> getDatabaseMeta(@NotNull Set<ResourceId> databases) {
        if (databases.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ResourceId,Long> toFetch = queryDatabaseVersions(databases);
        if (toFetch.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ResourceId,DatabaseMeta> loaded = new HashMap<>(databases.size());

        // Session Cache
        loaded.putAll(loadFromSessionCache(toFetch));
        loaded.forEach((dbId,cachedDbMeta) -> toFetch.remove(dbId));
        if (toFetch.isEmpty()) {
            return loaded;
        }

        // Memcache
        Map<ResourceId, DatabaseMeta> loadedFromMemcache = loadFromMemcache(toFetch);
        if (!loadedFromMemcache.isEmpty()) {
            cacheToSessionCache(loadedFromMemcache.values());
            loaded.putAll(loadedFromMemcache);
            loadedFromMemcache.forEach((dbId,cachedDbMeta) -> toFetch.remove(dbId));
        }
        if (toFetch.isEmpty()) {
            return loaded;
        }

        // Database
        Map<ResourceId,DatabaseMeta> loadedFromDb = loadFromDb(toFetch.keySet());
        if (!loadedFromDb.isEmpty()) {
            cacheToSessionCache(loadedFromDb.values());
            cacheToMemcache(loadedFromDb.values());
            loaded.putAll(loadedFromDb);
        }
        return loaded;
    }



    @Override
    public Map<ResourceId, DatabaseMeta> getOwnedDatabaseMeta(int ownerId) {
        Set<ResourceId> ownedDatabases = queryOwnedDatabaseIds(ownerId);
        if (ownedDatabases.isEmpty()) {
            return Collections.emptyMap();
        }
        return getDatabaseMeta(ownedDatabases);
    }

    @Override
    public Optional<DatabaseMeta> getDatabaseMetaForResource(@NotNull ResourceId resourceId) {
        switch(resourceId.getDomain()) {
            case CuidAdapter.DATABASE_DOMAIN:
                return getDatabaseMeta(resourceId);
            case CuidAdapter.ACTIVITY_DOMAIN:
                Optional<ResourceId> activityDatabaseId = Optional.ofNullable(queryDatabaseIdForForm(resourceId));
                return activityDatabaseId.isPresent() ? getDatabaseMeta(activityDatabaseId.get()) : Optional.empty();
            case CuidAdapter.MONTHLY_REPORT_FORM_CLASS:
                ResourceId activityFormId = CuidAdapter.activityFormClass(CuidAdapter.getLegacyIdFromCuid(resourceId));
                Optional<ResourceId> monthlyActivityDatabaseId = Optional.ofNullable(queryDatabaseIdForForm(activityFormId));
                return monthlyActivityDatabaseId.isPresent() ? getDatabaseMeta(monthlyActivityDatabaseId.get()) : Optional.empty();
            case CuidAdapter.FOLDER_DOMAIN:
                Optional<ResourceId> folderDatabaseId = Optional.ofNullable(queryDatabaseIdForFolder(resourceId));
                return folderDatabaseId.isPresent() ? getDatabaseMeta(folderDatabaseId.get()) : Optional.empty();
            case ResourceId.GENERATED_ID_DOMAIN:
                // Check for a Sub-Form Resource
                com.google.common.base.Optional<FormStorage> subForm = formStorageProvider.getForm(resourceId);
                return subForm.isPresent() ? getDatabaseMeta(subForm.get().getFormClass().getDatabaseId()) : Optional.empty();
            default:
                throw new IllegalArgumentException("Cannot fetch UserDatabaseMeta for Resource: " + resourceId.toString());
        }
    }

    private Map<ResourceId,Long> queryDatabaseVersions(@NotNull Set<ResourceId> databaseIds) {
        Map<ResourceId,Long> versions = databaseIds.stream()
                .collect(Collectors.toMap(
                        dbId -> dbId,
                        this::queryDatabaseVersion));
        return versions.entrySet().stream()
                .filter(dbVersion -> dbVersion.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private Set<ResourceId> queryOwnedDatabaseIds(int ownerId) {
        return entityManager.get().createQuery("SELECT db.id " +
                "FROM Database db " +
                "WHERE db.owner.id=:ownerId " +
                "AND db.dateDeleted IS NULL", Integer.class)
                .setParameter("ownerId", ownerId)
                .getResultList().stream()
                .map(CuidAdapter::databaseId)
                .collect(Collectors.toSet());
    }

    private Map<ResourceId,DatabaseMeta> loadFromSessionCache(Map<ResourceId,Long> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseMeta from SessionCache", toFetch.size()));
        Map<ResourceId,DatabaseMeta> loaded = new HashMap<>(toFetch.size());
        Map<ResourceId,String> fetchKeys = toFetch.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, db -> memcacheKey(db.getKey(),db.getValue())));
        try {
            loaded.putAll(sessionCache.loadAll(fetchKeys));
        } catch (Exception ignorable) {
            // Session cache load failed, but we can still retrieve from memcache or database
            LOGGER.severe(String.format("Fetching failed for %d DatabaseMeta from SessionCache", toFetch.size()));
        }
        LOGGER.info(() -> String.format("Fetched %d/%d DatabaseMeta from SessionCache", loaded.size(), toFetch.size()));
        return loaded;
    }

    private Map<ResourceId,DatabaseMeta> loadFromMemcache(Map<ResourceId,Long> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseMeta from Memcache", toFetch.size()));
        Map<ResourceId,DatabaseMeta> loaded = new HashMap<>(toFetch.size());
        Map<String,ResourceId> fetchKeys = toFetch.entrySet().stream()
                .collect(Collectors.toMap(
                        db -> memcacheKey(db.getKey(), db.getValue()),
                        Map.Entry::getKey));
        try {
            Map<String,Object> cached = memcacheService.getAll(fetchKeys.keySet());
            loaded.putAll(cached.entrySet().stream()
                    .collect(Collectors.toMap(
                            cachedDb -> fetchKeys.get(cachedDb.getKey()),
                            cachedDb -> deserialize((String) cachedDb.getValue()))));
        } catch (Exception ignorable) {
            // Memcache load failed, but we can still retrieve from database
            LOGGER.severe(String.format("Fetching failed for %d DatabaseMeta from Memcache", toFetch.size()));
        }
        LOGGER.info(() -> String.format("Fetched %d/%d DatabaseMeta from Memcache", loaded.size(), toFetch.size()));
        return loaded;
    }

    private DatabaseMeta deserialize(String serializedDatabaseMeta) {
        return DatabaseMeta.fromJson(Json.parse(serializedDatabaseMeta));
    }

    private Map<ResourceId,DatabaseMeta> loadFromDb(Set<ResourceId> toFetch) {
        LOGGER.info(() -> String.format("Fetching %d DatabaseMeta from MySqlDatabase", toFetch.size()));
        if (toFetch.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Integer> legacysIds = toFetch.stream()
                .map(CuidAdapter::getLegacyIdFromCuid)
                .collect(Collectors.toSet());
        Set<Integer> suspendedDatabases = billingAccountOracle.getSuspendedDatabases(legacysIds);
        return entityManager.get().createQuery("SELECT db " +
                "FROM Database db " +
                "WHERE db.id IN :databaseIds", Database.class)
                .setParameter("databaseIds", legacysIds)
                .getResultList().stream()
                .filter(Objects::nonNull)
                .map(db -> buildMeta(db, suspendedDatabases.contains(db.getId())))
                .collect(Collectors.toMap(
                        DatabaseMeta::getDatabaseId,
                        dbMeta -> dbMeta));
    }

    private void cacheToSessionCache(Collection<DatabaseMeta> databases) {
        LOGGER.info(() -> String.format("Caching %d DatabaseMeta to SessionCache", databases.size()));
        Map<String,DatabaseMeta> toCache = databases.stream()
                .collect(Collectors.toMap(
                        db -> memcacheKey(db.getDatabaseId(),db.getVersion()),
                        db -> db));
        sessionCache.putAll(toCache);
    }

    private void cacheToMemcache(Collection<DatabaseMeta> databases) {
        LOGGER.info(() -> String.format("Caching %d DatabaseMeta to Memcache", databases.size()));
        Map<String,String> toCache = databases.stream()
                .collect(Collectors.toMap(
                        db -> memcacheKey(db.getDatabaseId(),db.getVersion()),
                        db -> db.toJson().toJson()));
        try {
            memcacheService.putAll(toCache);
        } catch (Exception ignorable) {
            // Caching failed, but is not terminal
            LOGGER.severe(String.format("Caching failed for %d DatabaseMeta to Memcache", databases.size()));
        }
    }

    static String memcacheKey(ResourceId databaseId, long databaseVersion) {
        return String.format("%s:%s_%s:%d", CACHE_PREFIX, CACHE_VERSION,  databaseId.asString(), databaseVersion);
    }

    private @Nullable Long queryDatabaseVersion(ResourceId databaseId) {
        try {
            return entityManager.get().createQuery("SELECT db.metaVersion " +
                    "FROM Database db " +
                    "WHERE db.id=:dbId", Long.class)
                    .setParameter("dbId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                    .getSingleResult();
        } catch (NoResultException noDatabase) {
            return null;
        }
    }

    private @Nullable ResourceId queryDatabaseIdForForm(@NotNull ResourceId formId) {
        try {
            int dbId = entityManager.get().createQuery("select form.database.id " +
                    "from Activity form " +
                    "where form.id = :formId " +
                    "and form.dateDeleted is null", Integer.class)
                    .setParameter("formId", CuidAdapter.getLegacyIdFromCuid(formId))
                    .getSingleResult();
            return CuidAdapter.databaseId(dbId);
        } catch (NoResultException noResult) {
            return null;
        }
    }

    private @Nullable ResourceId queryDatabaseIdForFolder(@NotNull ResourceId folderId) {
        try {
            int dbId = entityManager.get().createQuery("select folder.database.id " +
                    "from Folder folder " +
                    "where folder.id = :folderId", Integer.class)
                    .setParameter("folderId", CuidAdapter.getLegacyIdFromCuid(folderId))
                    .getSingleResult();
            return CuidAdapter.databaseId(dbId);
        } catch (NoResultException noResult) {
            return null;
        }
    }

    private @Nullable DatabaseMeta buildMeta(@Nullable Database database, boolean suspended) {
        if (database == null) {
            return null;
        }
        if (database.isDeleted()) {
            return buildDeletedMeta(database);
        }
        return new DatabaseMeta.Builder()
                .setDatabaseId(CuidAdapter.databaseId(database.getId()))
                .setOwnerId(database.getOwner().getId())
                .setVersion(database.getMetaVersion())
                .setLabel(database.getName())
                .setDescription(database.getFullName())
                .setPublished(false)
                .setPendingTransfer(database.hasPendingTransfer())
                .addResources(fetchResources(database))
                .addLocks(fetchLocks(database))
                .setSuspended(suspended)
                .build();
    }

    private @Nullable DatabaseMeta buildDeletedMeta(@NotNull Database database) {
        return new DatabaseMeta.Builder()
                .setDatabaseId(CuidAdapter.databaseId(database.getId()))
                .setOwnerId(database.getOwner().getId())
                .setVersion(database.getMetaVersion())
                .setDeleted(true)
                .build();
    }

    private List<Resource> fetchResources(@NotNull Database database) {
        List<Resource> resources = new ArrayList<>();

        List<Resource> formResources = fetchForms(database);
        List<Resource> monthlyReportingResources = fetchMonthlyReportingSubForms(database);
        List<Resource> subFormResources = fetchSubForms(formResources);
        List<Resource> folderResources = fetchFolders(database);

        Resource partnerResource = partnerFormResource(database);

        resources.addAll(formResources);
        resources.addAll(monthlyReportingResources);
        resources.addAll(subFormResources);
        resources.addAll(folderResources);
        resources.add(partnerResource);

        return resources;
    }

    private Resource partnerFormResource(Database database) {
        return new Resource.Builder()
                .setId(CuidAdapter.partnerFormId(database.getId()))
                .setParentId(database.getResourceId())
                .setLabel(I18N.CONSTANTS.partners())
                .setVisibleAsReference()
                .setType(ResourceType.FORM)
                .build();
    }

    private List<Resource> fetchForms(@NotNull Database database) {
        return database.getActivities().stream()
                .filter(a -> !a.isDeleted())
                .map(Activity::asResource)
                .collect(Collectors.toList());
    }

    private List<Resource> fetchMonthlyReportingSubForms(Database database) {
        return database.getActivities().stream()
                .filter(a -> !a.isDeleted())
                .filter(Activity::isClassicView)
                .filter(a -> a.getReportingFrequency() == ActivityFormDTO.REPORT_MONTHLY)
                .map(HibernateDatabaseMetaProvider::buildMonthlyReportResource)
                .collect(Collectors.toList());
    }

    private static Resource buildMonthlyReportResource(Activity monthlyActivity) {
        return new Resource.Builder()
                .setId(CuidAdapter.reportingPeriodFormClass(monthlyActivity.getId()))
                .setParentId(monthlyActivity.getFormId())
                .setLabel(monthlyActivity.getName() + " Monthly Reports")
                .setVisibility(monthlyActivity.resourceVisibility())
                .setType(ResourceType.SUB_FORM)
                .build();
    }

    private List<Resource> fetchSubForms(List<Resource> formResources) {
        return formResources.stream()
                .flatMap(this::extractSubFormResources)
                .collect(Collectors.toList());
    }

    private Stream<Resource> extractSubFormResources(@NotNull Resource formResource) {
        return formStorageProvider.getForm(formResource.getId())
                .transform(form -> extractSubFormReferenceFields(form.getFormClass())
                        .map(sf -> buildSubFormResource(formResource, sf)))
                .or(Stream::empty);
    }

    private static Stream<FormField> extractSubFormReferenceFields(FormClass formClass) {
        return formClass.getFields().stream()
                .filter(field -> field.getType() instanceof SubFormReferenceType);
    }

    private static Resource buildSubFormResource(Resource parentFormResource, FormField subFormReferenceField) {
        return new Resource.Builder()
                .setId(subFormId(subFormReferenceField))
                .setParentId(parentFormResource.getId())
                .setLabel(subFormReferenceField.getLabel())
                .setVisibility(parentFormResource.getVisibility())
                .setType(ResourceType.SUB_FORM)
                .build();
    }

    private static ResourceId subFormId(@NotNull FormField subFormField) {
        return ((SubFormReferenceType) subFormField.getType()).getClassId();
    }

    private List<Resource> fetchFolders(@NotNull Database database) {
        return entityManager.get().createQuery("SELECT f " +
                "FROM Folder f " +
                "WHERE f.database=:database", Folder.class)
                .setParameter("database", database)
                .getResultList().stream()
                .map(Folder::asResource)
                .collect(Collectors.toList());
    }

    private List<RecordLock> fetchLocks(@NotNull Database database) {
        return database.getLockedPeriods().stream()
                .filter(LockedPeriod::isEnabled)
                .map(LockedPeriod::asDatabaseLock)
                .collect(Collectors.toList());
    }

}
