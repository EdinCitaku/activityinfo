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

public class ReportVisibilityDTO extends BaseModelData {

    private int databaseId;

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public String getDatabaseName() {
        return get("databaseName");
    }

    public void setDatabaseName(String databaseName) {
        set("databaseName", databaseName);
    }

    public boolean isVisible() {
        return get("visible", false);
    }

    public void setVisible(boolean visible) {
        set("visible", visible);
    }

    public boolean isDefaultDashboard() {
        return get("defaultDashboard", false);
    }

    public void setDefaultDashboard(boolean defaultDashboard) {
        set("defaultDashboard", defaultDashboard);
    }

}
