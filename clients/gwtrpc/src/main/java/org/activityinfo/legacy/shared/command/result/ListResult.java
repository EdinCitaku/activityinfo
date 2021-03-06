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
package org.activityinfo.legacy.shared.command.result;

import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import org.activityinfo.legacy.shared.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class of <code>CommandResult</code>s that are compatible with
 * the GXT loading interface.
 *
 * @param <D> The type of item that the list will contain.
 * @see com.extjs.gxt.ui.client.data.ListLoadResult
 * @see com.extjs.gxt.ui.client.data.ModelData
 */
public abstract class ListResult<D extends ModelData> implements CommandResult, ListLoadResult<D> {

    private List<D> data;

    protected ListResult() {
        data = new ArrayList<D>();
    }

    public ListResult(List<D> data) {
        this.data = data;
    }

    public ListResult(ListResult<D> result) {
        this.data = new ArrayList<D>();
        if (result != null) {
            this.data.addAll(result.getData());
        }
    }

    @Override
    public List<D> getData() {
        return data;
    }

    public void setData(List<D> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof ListResult<?>)) {
            return false;
        }
        return CollectionUtil.containsEqual(this.getData(), ((ListResult<D>) obj).getData());
    }

    @Override
    public int hashCode() {
        if(data == null) {
            return 0;
        } else {
            return data.hashCode();
        }
    }
}
