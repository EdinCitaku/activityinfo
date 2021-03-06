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

import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.model.type.geo.GeoPoint;

import java.util.List;

public class LocationDTO extends BaseModelData implements EntityDTO, HasAdminEntityValues {

    public static final String NAME_PROPERTY = "name";
    public static final String LATITUDE_PROPERTY = "latitude";
    public static final String LONGITUDE_PROPERTY = "longitude";

    public static final String VALIDATED = "validated";
    public static final String REJECTED = "rejected";
    public static final int MAX_NAME_LENGTH = 50;

    public LocationDTO() {
        super();
    }

    public LocationDTO(LocationDTO location) {
        super(location.getProperties());
    }

    public static LocationDTO fromSqlRow(SqlResultSetRow row) {
        String name = row.getString("Name");
        String axe = row.isNull("Axe") ? null : row.getString("Axe");
        Double longitude = row.isNull("X") ? null : row.getDouble("X");
        Double latitude = row.isNull("Y") ? null : row.getDouble("Y");
        Integer locationType = row.isNull("LocationTypeId") ? null : row.getInt("LocationTypeId");
        int id = row.getInt("LocationId");
        String workflowStatusId = row.getString("workflowStatusId");

        return new LocationDTO().setId(id)
                                .setName(name)
                                .setAxe(axe)
                                .setLongitude(longitude)
                                .setLatitude(latitude)
                                .setLocationTypeId(locationType)
                                .setWorkflowStatusId(workflowStatusId);
    }

    public static boolean isValidWorkflowId(String workflowId) {
        return LocationTypeDTO.OPEN_WORKFLOW_ID.equals(workflowId) || LocationTypeDTO.CLOSED_WORKFLOW_ID.equals(workflowId);
    }

    @Override
    public String getName() {
        return (String) get("name");
    }

    public LocationDTO setName(String name) {
        set("name", name);
        return this;
    }

    public String getAxe() {
        return (String) get("axe");
    }

    public LocationDTO setAxe(String axe) {
        set("axe", axe);
        return this;
    }

    public Double getLatitude() {
        return (Double) get("latitude");
    }

    public LocationDTO setLatitude(Double latitude) {
        set("latitude", latitude);
        return this;
    }

    public boolean isNew() {
        return get("new", false);
    }

    public void setNew(boolean isNew) {
        set("new", isNew);
    }

    public Double getLongitude() {
        return (Double) get("longitude");
    }

    public LocationDTO setLongitude(Double longitude) {
        set("longitude", longitude);
        return this;
    }

    public LocationDTO setId(int id) {
        set("id", id);
        return this;
    }

    @Override
    public int getId() {
        return (Integer) get("id");
    }

    public int getLocationTypeId() {
        Object value = get("locationTypeId");
        if(!(value instanceof Number)) {
            throw new CommandException("Expecting a numeric value for locationTypeId");
        }
        return ((Number) value).intValue();
    }

    public LocationDTO setLocationTypeId(int locationTypeId) {
        set("locationTypeId", locationTypeId);
        return this;
    }

    public LocationDTO setAdminEntity(int levelId, AdminEntityDTO value) {
        set(AdminLevelDTO.getPropertyName(levelId), value);
        return this;
    }

    @Override
    public AdminEntityDTO getAdminEntity(int levelId) {
        return (AdminEntityDTO) get(AdminLevelDTO.getPropertyName(levelId));
    }

    public List<AdminEntityDTO> getAdminEntities() {
        List<AdminEntityDTO> list = Lists.newArrayList();
        for (String property : getPropertyNames()) {
            if (property.startsWith(AdminLevelDTO.PROPERTY_PREFIX)) {
                AdminEntityDTO entity = (AdminEntityDTO) get(property);
                if (entity != null) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    public String getMarker() {
        return get("marker");
    }

    public void setMarker(String marker) {
        set("marker", marker);
    }

    @Override
    public String getEntityName() {
        return "Location";
    }

    /**
     * True when latitude() and longitude() are non-null
     */
    public boolean hasCoordinates() {
        return getLatitude() != null && getLongitude() != null;
    }

    /**
     * True when this Location has a non-empty Axe
     */
    public boolean hasAxe() {
        return !Strings.isNullOrEmpty(getAxe());
    }

    public LocationDTO setWorkflowStatusId(String workflowStatusId) {
        set("workflowStatusId", workflowStatusId);
        return this;
    }

    public String getWorkflowStatusId() {
        return get("workflowStatusId");
    }

    public GeoPoint getPoint() {
        if (hasCoordinates()) {
            return new GeoPoint(getLatitude(), getLongitude());
        }
        return null;
    }
}
