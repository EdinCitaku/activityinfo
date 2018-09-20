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
package org.activityinfo.model.database;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Describes a single user's view of database, including the folders, forms,
 * and locks visible to the user, as well as their own permissions within this database.
 */
public class UserDatabaseMeta implements JsonSerializable {

    public static final String VERSION_SEP = "#";

    private ResourceId databaseId;
    private int userId;
    private String label;
    private boolean visible;
    private boolean owner;
    private boolean pendingTransfer;
    private String version;

    private final Map<ResourceId, Resource> resources = new HashMap<>();
    private final Map<ResourceId, GrantModel> grants = new HashMap<>();
    private final Multimap<ResourceId, RecordLock> locks = HashMultimap.create();

    public ResourceId getDatabaseId() {
        return databaseId;
    }

    public int getUserId() {
        return userId;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isOwner() {
        return owner;
    }

    public Collection<Resource> getResources() {
        return resources.values();
    }

    public boolean hasResource(ResourceId resourceId) {
        return resources.containsKey(resourceId);
    }

    public Resource getResource(ResourceId resourceId) {
        assert hasResource(resourceId) : "No resource found for given resourceId";
        return resources.get(resourceId);
    }

    public boolean hasGrant(ResourceId resourceId) {
        return grants.containsKey(resourceId);
    }

    public GrantModel getGrant(ResourceId resourceId) {
        assert hasGrant(resourceId) : "No grant found for given resourceId";
        return grants.get(resourceId);
    }

    public Collection<RecordLock> getLocks() {
        return locks.values();
    }

    public RecordLockSet getEffectiveLocks(ResourceId resourceId) {
        List<RecordLock> effective = new ArrayList<>();
        do {
            effective.addAll(this.locks.get(resourceId));
            Resource resource = resources.get(resourceId);
            if(resource == null) {
                break;
            }
            resourceId = resource.getParentId();
        } while(true);

        return new RecordLockSet(effective);
    }

    public String getVersion() {
        return version;
    }

    public long getDatabaseVersion() {
        return isOwner()
                ? Long.valueOf(version)
                : Long.valueOf(version.substring(0,version.indexOf(VERSION_SEP)));
    }

    public long getUserVersion() {
        return isOwner()
                ? Long.valueOf(version)
                : Long.valueOf(version.substring(version.indexOf(VERSION_SEP)+1, version.length()));
    }

    public boolean isPendingTransfer() {
        assert isOwner() : "User is not the owner of the database.";
        return pendingTransfer;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("id", databaseId.asString());
        object.put("version", version);
        object.put("label", label);
        object.put("visible", visible);
        object.put("owner", owner);
        if (owner) {
            object.put("pendingTransfer", pendingTransfer);
        }
        object.put("userId", userId);
        object.put("resources", Json.toJsonArray(resources.values()));
        object.put("locks", Json.toJsonArray(locks.values()));
        object.put("grants", Json.toJsonArray(grants.values()));
        return object;
    }

    public static UserDatabaseMeta fromJson(JsonValue object) {
        UserDatabaseMeta meta = new UserDatabaseMeta();
        meta.databaseId = ResourceId.valueOf(object.getString("id"));
        meta.userId = (int) object.getNumber("userId");
        meta.version = object.getString("version");
        meta.label = object.getString("label");
        meta.visible = object.getBoolean("visible");
        meta.owner = object.getBoolean("owner");
        if (meta.owner) {
            meta.pendingTransfer = object.getBoolean("pendingTransfer");
        }

        JsonValue resourceArray = object.get("resources");
        for (int i = 0; i < resourceArray.length(); i++) {
            Resource resource = Resource.fromJson(resourceArray.get(i));
            meta.resources.put(resource.getId(), resource);
        }

        JsonValue lockArray = object.get("locks");
        for (int i = 0; i < lockArray.length(); i++) {
            RecordLock lock = RecordLock.fromJson(lockArray.get(i));
            meta.locks.put(lock.getResourceId(), lock);
        }

        JsonValue grantsArray = object.get("grants");
        for (int i = 0; i < grantsArray.length(); i++) {
            GrantModel grant = GrantModel.fromJson(grantsArray.get(i));
            assert !meta.grants.containsKey(grant.getResourceId()) : "Cannot define more than 1 Grant for a given Resource.";
            meta.grants.put(grant.getResourceId(), grant);
        }
        return meta;
    }


    public static class Builder {
        private final UserDatabaseMeta meta = new UserDatabaseMeta();

        public Builder() {
            meta.version = "0";
        }

        public Builder setVersion(String version) {
            meta.version = version;
            return this;
        }

        public Builder setDatabaseId(ResourceId id) {
            meta.databaseId = id;
            return this;
        }

        public Builder setDatabaseId(int id) {
            return setDatabaseId(CuidAdapter.databaseId(id));
        }

        public Builder setUserId(int userId) {
            meta.userId = userId;
            return this;
        }

        public Builder setLabel(String label) {
            meta.label = label;
            return this;
        }

        public Builder setOwner(boolean owner) {
            meta.owner = owner;
            meta.visible = true;
            return this;
        }

        public Builder setPendingTransfer(boolean pendingTransfer) {
            meta.pendingTransfer = pendingTransfer;
            return this;
        }

        public Builder addGrants(List<GrantModel> grants) {
            for (GrantModel grant : grants) {
                assert !meta.grants.containsKey(grant.getResourceId()) : "Cannot define more than 1 Grant for a given Resource.";
                meta.grants.put(grant.getResourceId(), grant);
            }
            return this;
        }

        public Builder addLock(RecordLock lock) {
            meta.locks.put(lock.getResourceId(), lock);
            return this;
        }

        public Builder addLocks(List<RecordLock> locks) {
            for (RecordLock lock : locks) {
                addLock(lock);
            }
            return this;
        }

        public Builder addResources(List<Resource> resources) {
            for (Resource resource : resources) {
                addResource(resource);
            }
            return this;
        }

        public Builder addResource(Resource resource) {
            meta.resources.put(resource.getId(), resource);
            return this;
        }

        public boolean isVisible() {
            return meta.owner || !meta.grants.isEmpty();
        }

        public Set<ResourceId> folderGrants() {
            assert isVisible();
            return meta.grants.keySet().stream()
                    .filter(grantResource -> grantResource.getDomain() == CuidAdapter.FOLDER_DOMAIN)
                    .collect(Collectors.toSet());
        }

        public Set<ResourceId> formGrants() {
            assert isVisible();
            return meta.grants.keySet().stream()
                    .filter(grantResource -> grantResource.getDomain() == CuidAdapter.ACTIVITY_DOMAIN)
                    .collect(Collectors.toSet());
        }

        public UserDatabaseMeta build() {
            meta.visible = isVisible();
            return meta;
        }
    }
}
