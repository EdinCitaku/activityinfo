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
package org.activityinfo.server.database.hibernate.entity;

import org.activityinfo.legacy.shared.model.LocationDTO;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex Bertram
 */
@Entity @JsonAutoDetect(JsonMethod.NONE)
public class Location implements java.io.Serializable {

    private int id;
    private LocationType locationType;
    private String locationGuid;
    private Double x;
    private Double y;
    private String name;
    private String axe;
    private Set<Site> sites = new HashSet<Site>(0);
    private Set<AdminEntity> adminEntities = new HashSet<AdminEntity>(0);
    private String workflowStatusId;
    private long timeEdited;
    private long version;

    public Location() {
        workflowStatusId = LocationDTO.VALIDATED;
    }

    @Id @JsonProperty @Column(name = "LocationID", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "LocationTypeID", nullable = false)
    public LocationType getLocationType() {
        return this.locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    @Column(name = "LocationGuid", length = 36)
    public String getLocationGuid() {
        return this.locationGuid;
    }

    public void setLocationGuid(String locationGuid) {
        this.locationGuid = locationGuid;
    }

    @JsonProperty("longitude") @Column(name = "X", precision = 7, scale = 0)
    public Double getX() {
        return this.x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public String getWorkflowStatusId() {
        return workflowStatusId;
    }

    public void setWorkflowStatusId(String workflowStatusId) {
        this.workflowStatusId = workflowStatusId;
    }

    @JsonProperty("latitude") @Column(name = "Y", precision = 7, scale = 0)
    public Double getY() {
        return this.y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    @JsonProperty @Column(name = "Name", nullable = false, length = 50)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "Axe", length = 50)
    public String getAxe() {
        return this.axe;
    }

    public void setAxe(String axe) {
        this.axe = axe;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "location")
    public Set<Site> getSites() {
        return this.sites;
    }

    public void setSites(Set<Site> sites) {
        this.sites = sites;
    }

    @JsonProperty @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinTable(name = "LocationAdminLink",
            joinColumns = {@JoinColumn(name = "LocationId", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "AdminEntityId", nullable = false, updatable = false)})
    public Set<AdminEntity> getAdminEntities() {
        return this.adminEntities;
    }

    public void setAdminEntities(Set<AdminEntity> adminEntities) {
        this.adminEntities = adminEntities;
    }

    public void setAdminEntity(int levelId, AdminEntity newEntity) {

        for (AdminEntity entity : getAdminEntities()) {
            if (entity.getLevel().getId() == levelId) {

                if (newEntity == null) {
                    getAdminEntities().remove(entity);
                } else if (newEntity.getId() != entity.getId()) {
                    getAdminEntities().remove(entity);
                    getAdminEntities().add(newEntity);
                }

                return;
            }
        }

        if (newEntity != null) {
            getAdminEntities().add(newEntity);
        }
    }

    /**
     * @deprecated Use the version field for synchronization purposes
     */
    @Deprecated
    public long getTimeEdited() {
        return timeEdited;
    }

    /**
     * @deprecated Use the version field for synchronization purposes
     */
    @Deprecated
    public void setTimeEdited(long timeEdited) {
        this.timeEdited = timeEdited;
    }

    /**
     * @deprecated Use the version field for synchronization purposes
     */
    @Deprecated
    public void setTimeEdited(Date date) {
        this.timeEdited = date.getTime();
    }


    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
