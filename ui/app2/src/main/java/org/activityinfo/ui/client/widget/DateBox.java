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
package org.activityinfo.ui.client.widget;

import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.Date;

/**
 * @author yuriyz on 11/20/2014.
 */
public class DateBox extends com.google.gwt.user.datepicker.client.DateBox {

    private boolean readOnly = false;

    public DateBox() {
        init();
    }

    public DateBox(Format format) {
        super(new DatePicker(), null, format);
        init();
    }

    public DateBox(DatePicker picker, Date date, Format format) {
        super(picker, date, format);
        init();
    }

    private void init() {
        DateBoxBundle.INSTANCE.css().ensureInjected();
        setStyleName("form-control");
        getTextBox().setStyleName("form-control");
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        getTextBox().setReadOnly(readOnly);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void showDatePicker() {
        if (!readOnly) {
            super.showDatePicker();
        }
    }
}
