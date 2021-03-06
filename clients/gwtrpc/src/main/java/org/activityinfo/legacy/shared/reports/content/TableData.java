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
package org.activityinfo.legacy.shared.reports.content;

import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.model.TableColumn;

import java.io.Serializable;
import java.util.List;

public class TableData implements Serializable {

    private final TableColumn rootColumn;
    private final List<TableColumn> leaves;
    private final List<SiteDTO> rows;

    public TableData(TableColumn rootColumn, List<SiteDTO> rows) {
        this.rootColumn = rootColumn;
        this.leaves = rootColumn.getLeaves();
        this.rows = rows;
    }

    public String getColumnIndex(String source) {
        for (TableColumn column : leaves) {
            if (column.getProperty().equals(source)) {
                return column.getSitePropertyName();
            }
        }
        return null;
    }

    public int getLeafColumnCount() {
        return leaves.size();
    }

    public TableColumn getRootColumn() {
        return rootColumn;
    }

    public List<SiteDTO> getRows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows == null || rows.size() == 0;
    }
}
