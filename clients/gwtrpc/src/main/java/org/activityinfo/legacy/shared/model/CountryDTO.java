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
import org.activityinfo.model.type.geo.Extents;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.List;

/**
 * One-to-one DTO for
 * {@link org.activityinfo.server.database.hibernate.entity.Country} domain
 * objects.
 */
@JsonAutoDetect(JsonMethod.NONE)
public final class CountryDTO extends BaseModelData implements DTO {

    private static final long serialVersionUID = 3189552164304073119L;

    private List<AdminLevelDTO> adminLevels = new ArrayList<AdminLevelDTO>(0);
    private List<LocationTypeDTO> locationTypes = new ArrayList<LocationTypeDTO>(0);
    private Extents bounds;

    public CountryDTO() {
    }

    public CountryDTO(int id, String name) {
        setId(id);
        setName(name);
    }

    public void setId(int id) {
        set("id", id);
    }

    @JsonProperty @JsonView({DTOViews.List.class, DTOViews.Schema.class})
    public int getId() {
        return (Integer) get("id");
    }

    @JsonProperty @JsonView({DTOViews.List.class, DTOViews.Schema.class})
    public String getName() {
        return get("name");
    }

    public void setName(String value) {
        set("name", value);
    }

    public List<AdminLevelDTO> getAdminLevels() {
        return this.adminLevels;
    }

    public void setAdminLevels(List<AdminLevelDTO> levels) {
        this.adminLevels = levels;
    }

    public List<LocationTypeDTO> getLocationTypes() {
        return this.locationTypes;
    }

    public void setLocationTypes(List<LocationTypeDTO> types) {
        this.locationTypes = types;
    }

    @JsonProperty @JsonView(DTOViews.Detail.class)
    public Extents getBounds() {
        return bounds;
    }

    public void setBounds(Extents bounds) {
        this.bounds = bounds;
    }

    @JsonProperty("code") @JsonView({DTOViews.List.class, DTOViews.Schema.class})
    public String getCodeISO() {
        return get("codeISO");
    }

    public void setCodeISO(String codeISO) {
        set("codeISO", codeISO);
    }

    /**
     * Finds an AdminEntity by id
     *
     * @param levelId the id of the AdminEntity to return
     * @return the AdminEntity with corresponding id or null if no such
     * AdminEntity is found in the list
     */
    public AdminLevelDTO getAdminLevelById(int levelId) {
        for (AdminLevelDTO level : this.adminLevels) {
            if (level.getId() == levelId) {
                return level;
            }
        }
        return null;
    }

    /**
     * Returns a list of <code>AdminLevelDTO</code>s that are ancestors of the
     * the AdminLevel with an id of <code>levelId</code> in order descending
     * from the root.
     *
     * @param levelId the id of AdminLevel
     * @return a list of AdminLevelDTOs in <code>adminLevels</code> which are
     * ancestors of the AdminLevel with the id of <code>levelId</code>,
     * or null if no AdminLevelDTO with the given id or exists or if the
     * indicated AdminLevel is a root level.
     */
    public List<AdminLevelDTO> getAdminLevelAncestors(int levelId) {
        List<AdminLevelDTO> ancestors = new ArrayList<AdminLevelDTO>();

        AdminLevelDTO level = getAdminLevelById(levelId);

        if (level == null) {
            return null;
        }

        while (true) {
            ancestors.add(0, level);

            if (level.isRoot()) {
                return ancestors;
            } else {
                level = getAdminLevelById(level.getParentLevelId());
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CountryDTO");
        sb.append("\nname:");
        sb.append(this.getName());
        sb.append("\niso2:");
        sb.append(this.getCodeISO());
        sb.append("\nbounds:");
        sb.append(this.getBounds());
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CountryDTO other = (CountryDTO) obj;
        return getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    public LocationTypeDTO getNullLocationTypeSilently() {
        for(LocationTypeDTO type : locationTypes) {
            if(type.isNationwide()) {
                return type;
            }
        }
        return null;
    }

    public LocationTypeDTO getNullLocationType() {
        LocationTypeDTO locationTypeDTO = getNullLocationTypeSilently();
        if (locationTypeDTO != null) {
            return locationTypeDTO;
        }
        throw new IllegalStateException("No null LocationType has been defined for " + getName());
    }

    public LocationTypeDTO getLocationTypeForAdminLevel(int adminLevelId) {
        for(LocationTypeDTO type : locationTypes) {
            if(type.isAdminLevel() && type.getBoundAdminLevelId() == adminLevelId) {
                return type;
            }
        }
        throw new IllegalStateException("No bound LocationType has been defined for admin level " +
                adminLevelId + " in country " + getName());
    }

    public LocationTypeDTO getLocationTypeById(int id) {
        for (LocationTypeDTO locationType : locationTypes) {
            if(locationType.getId() == id) {
                return locationType;
            }
        }
        return null;
    }
}
