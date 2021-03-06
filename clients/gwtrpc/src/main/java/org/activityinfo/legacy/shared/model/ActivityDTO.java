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
package org.activityinfo.legacy.shared.model;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.Extents;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonAutoDetect(JsonMethod.NONE)
public final class ActivityDTO extends BaseModelData implements EntityDTO, ProvidesKey,
        LockedPeriodDTO.HasLockedPeriod, IsActivityDTO {

    public static final String ENTITY_NAME = "Activity";
    public static final String CATEGORY_PROPERTY = "category";
    public static final String FOLDER_ID_PROPERTY = "folderId";
    public static final String LOCATION_TYPE_ID_PROPERTY = "locationTypeId";
    public static final String LOCATION_TYPE = "locationType";
    public static final String REPORTING_FREQUENCY_PROPERTY = "reportingFrequency";
    public static final String PUBLISHED_PROPERTY = "published";
    public static final String CLASSIC_VIEW_PROPERTY = "classicView";

    private UserDatabaseDTO database;
    private FolderDTO folder;

    private Set<LockedPeriodDTO> lockedPeriods = new HashSet<>(0);
    private List<PartnerDTO> partnerRange = Lists.newArrayList();

    private LocationTypeDTO locationType;

    public ActivityDTO() {
        setReportingFrequency(ActivityFormDTO.REPORT_ONCE);
    }

    /**
     * Constructs a DTO with the given properties
     */
    public ActivityDTO(Map<String, Object> properties) {
        super(properties);
    }

    /**
     * Creates a shallow clone
     *
     */
    public ActivityDTO(ActivityDTO model) {
        super(model.getProperties());
        this.database = model.database;
        this.setLocationType(model.getLocationType());
    }

    public ActivityDTO(UserDatabaseDTO db, ActivityFormDTO form) {
        setId(form.getId());
        setDatabase(db);
        setName(form.getName());
        setLocationType(form.getLocationType());
        setReportingFrequency(form.getReportingFrequency());
        setCategory(form.getCategory());
        setClassicView(form.getClassicView());
        setPublished(form.getPublished());
    }

    /**
     * @param id   the Activity's id
     * @param name the Activity's name
     */
    public ActivityDTO(int id, String name) {
        this();
        setId(id);
        setName(name);
    }

    /**
     * @param db the UserDatabaseDTO to which this Activity belongs
     */
    public ActivityDTO(UserDatabaseDTO db) {
        setDatabase(db);
    }

    @Override
    public ResourceId getResourceId() {
        return CuidAdapter.activityFormClass(getId());
    }

    /**
     * @return this Activity's id
     */
    @Override
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public int getId() {
        return (Integer) get("id");
    }

    /**
     * Sets this Activity's id
     */
    public void setId(int id) {
        set("id", id);
    }

    /**
     * Sets this Activity's name
     */
    public void setName(String value) {
        set(NAME_PROPERTY, value);
    }

    /**
     * @return this Activity's name
     */
    @Override
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getName() {
        return get(NAME_PROPERTY);
    }

    /**
     * @return the database to which this Activity belongs
     */
    @JsonIgnore
    public UserDatabaseDTO getDatabase() {
        return database;
    }

    public int getDatabaseId() {
        return database.getId();
    }

    /**
     * Sets the database to which this Activity belongs
     */
    public void setDatabase(UserDatabaseDTO database) {
        this.database = database;
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public int getPublished() {
        return (Integer) get(PUBLISHED_PROPERTY);
    }

    public void setPublished(int published) {
        set(PUBLISHED_PROPERTY, published);
    }

    public void setClassicView(boolean value) {
        set(CLASSIC_VIEW_PROPERTY, value);
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public boolean getClassicView() {
        return get(CLASSIC_VIEW_PROPERTY);
    }

    public FolderDTO getFolder() {
        return folder;
    }

    public void setFolder(FolderDTO folder) {
        this.folder = folder;
        set(FOLDER_ID_PROPERTY, folder.getId());
    }

    /**
     * Sets the ReportingFrequency of this Activity, either
     * <code>REPORT_ONCE</code> or <code>REPORT_MONTHLY</code>
     */
    public void setReportingFrequency(int frequency) {
        set(REPORTING_FREQUENCY_PROPERTY, frequency);
    }

    /**
     * @return the ReportingFrequency of this Activity, either
     * <code>REPORT_ONCE</code> or <code>REPORT_MONTHLY</code>
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public int getReportingFrequency() {
        return (Integer) get(REPORTING_FREQUENCY_PROPERTY);
    }

    /**
     * Sets the id of the LocationType of the Location to which this Site
     * belongs.
     */
    public void setLocationTypeId(int locationId) {
        set(LOCATION_TYPE_ID_PROPERTY, locationId);

    }

    /**
     * @return the id of the LocationType of the Location to which this Site
     * belongs
     */

    public int getLocationTypeId() {
        return locationType.getId();
    }

    public void setLocationType(LocationTypeDTO locationType) {
        this.locationType = locationType;

        // for form binding. uck.
        if(locationType != null) {
            set(LOCATION_TYPE_ID_PROPERTY, locationType.getId());
        }
    }

    public List<PartnerDTO> getPartnerRange() {
        return partnerRange;
    }

    public void setPartnerRange(List<PartnerDTO> partnerRange) {
        this.partnerRange = partnerRange;
    }

    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public LocationTypeDTO getLocationType() {
        return locationType;
    }


    /**
     * @return this Activity's category
     */
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public String getCategory() {
        return get(ActivityDTO.CATEGORY_PROPERTY);
    }

    /**
     * Sets this Activity's category
     */
    public void setCategory(String category) {
        if(category != null && category.trim().length() == 0) {
            category = null;
        }
        set(ActivityDTO.CATEGORY_PROPERTY, category);
    }

    public boolean hasCategory() {
        return !Strings.isNullOrEmpty(getCategory());
    }


    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    /**
     * @return The list of admin levels that can be set for this Activity's
     * LocationType.
     */
    public List<AdminLevelDTO> getAdminLevels() {
        return locationType.getAdminLevels();
    }



    @Override
    public String getKey() {
        return "act" + getId();
    }


    @Override
    @JsonProperty
    @JsonView(DTOViews.Schema.class)
    public Set<LockedPeriodDTO> getLockedPeriods() {
        return lockedPeriods;
    }

    public String getDatabaseName() {
        return database.getName();
    }

    public boolean isEditAllowed() {
        return database.isEditAllowed();
    }

    public boolean isDesignAllowed() {
        return database.isEditAllowed();
    }

    public List<ProjectDTO> getProjects() {
        return database.getProjects();
    }

    public CountryDTO getCountry() {
        return database.getCountry();
    }

    public Extents getBounds() {
        return database.getCountry().getBounds();
    }

    public ResourceId getFormId() {
        return CuidAdapter.activityFormClass(getId());
    }
}
