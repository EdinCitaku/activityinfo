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
import org.activityinfo.legacy.shared.validation.Required;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

import static org.activityinfo.legacy.shared.model.EntityDTO.ID_PROPERTY;

/**
 * One-to-one DTO of the
 * {@link org.activityinfo.server.database.hibernate.entity.Partner} domain
 * class.
 *
 * @author Alex Bertram
 */
@JsonAutoDetect(JsonMethod.NONE)
public final class PartnerDTO extends BaseModelData implements DTO, ProvidesKey {

    public static final int NAME_MAX_LENGTH = 255;

    public static final String DEFAULT_PARTNER_NAME = "Default";

    public PartnerDTO() {

    }

    public PartnerDTO(int id, String name) {
        setId(id);
        setName(name);
    }

    public PartnerDTO(String name) {
        setName(name);
    }

    public void setId(int id) {
        set(ID_PROPERTY, id);
    }


    public boolean hasId() {
        return get(ID_PROPERTY) != null;
    }

    @Required
    @JsonProperty 
    @JsonView(DTOViews.Schema.class)
    public int getId() {
        return (Integer) get(ID_PROPERTY);
    }

    @JsonProperty 
    @JsonView(DTOViews.Schema.class)
    public String getName() {
        return get("name");
    }

    public void setName(String value) {
        set("name", value);
    }

    public void setFullName(String value) {
        set("fullName", value);
    }

    @JsonProperty @JsonView(DTOViews.Schema.class)
    public String getFullName() {
        return get("fullName");
    }

    @Override
    public String toString() {
        return "PartnerDTO{id="+ get(ID_PROPERTY) + ", name=" + getName() + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof PartnerDTO)) {
            return false;
        }

        PartnerDTO that = (PartnerDTO) other;

        return that.getId() == this.getId();
    }

    @Override
    public int hashCode() {
        if (get("id") == null) {
            return 0;
        } else {
            return getId();
        }
    }

    @Override
    public String getKey() {
        return "partner" + getId();
    }

}
