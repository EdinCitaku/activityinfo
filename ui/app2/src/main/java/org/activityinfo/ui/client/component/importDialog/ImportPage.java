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
package org.activityinfo.ui.client.component.importDialog;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface to import wizard pages
 */
public interface ImportPage extends IsWidget {

    boolean isValid();

    /**
     *
     * @return true if this page has a next step
     */
    boolean hasNextStep();

    /**
     *
     * @return true if this page has an (internal) previous step
     */
    boolean hasPreviousStep();

    void start();

    void fireStateChanged();

    void nextStep();

    void previousStep();


}
