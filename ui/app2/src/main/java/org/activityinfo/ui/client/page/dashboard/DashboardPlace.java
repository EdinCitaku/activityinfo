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
package org.activityinfo.ui.client.page.dashboard;

import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.app.Section;

import java.util.Arrays;
import java.util.List;

public class DashboardPlace implements PageState {

    @Override
    public PageId getPageId() {
        return DashboardPage.PAGE_ID;
    }

    @Override
    public String serializeAsHistoryToken() {
        return null;
    }

    @Override
    public List<PageId> getEnclosingFrames() {
        return Arrays.asList(DashboardPage.PAGE_ID);
    }

    @Override
    public Section getSection() {
        return Section.HOME;
    }

}