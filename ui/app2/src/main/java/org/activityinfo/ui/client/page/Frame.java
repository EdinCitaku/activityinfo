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
package org.activityinfo.ui.client.page;

import org.activityinfo.ui.client.dispatch.AsyncMonitor;

/**
 * Page which encloses or decorates another Page
 *
 * @author Alex Bertram
 */
public interface Frame extends Page {

    /**
     * Changes the enclosed page
     *
     * @param page the new active page
     */
    public void setActivePage(Page page);

    /**
     * @return the current active Page
     */
    public Page getActivePage();

    /**
     * Instructs the frame to show a loading placeholder while the new active
     * page is being loaded asynchronously.
     *
     * @param pageId       the pageId of the page that is being loaded
     * @param loadingPlace
     * @return
     */
    public AsyncMonitor showLoadingPlaceHolder(PageId pageId,
                                               PageState loadingPlace);
}
