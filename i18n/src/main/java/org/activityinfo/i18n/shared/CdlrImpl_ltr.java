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
package org.activityinfo.i18n.shared;

import com.google.gwt.i18n.client.impl.CldrImpl;

/**
 * Forces Left-to-Right text.
 * 
 * <p>Exceptionally, we want to maintain an overall <em>document</em> orientation of Left-to-Right as this
 * is the (stated) preference of users.</p>
 */
public class CdlrImpl_ltr extends CldrImpl {

    @Override
    public boolean isRTL() {
        return false;
    }
}
