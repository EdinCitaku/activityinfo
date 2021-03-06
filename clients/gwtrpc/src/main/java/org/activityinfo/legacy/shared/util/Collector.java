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
package org.activityinfo.legacy.shared.util;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class Collector<T> implements AsyncCallback<T> {

    private T result = null;
    private boolean callbackCalled = false;

    @Override
    public void onFailure(Throwable caught) {
        if (caught instanceof RuntimeException) {
            throw (RuntimeException) caught;
        } else {
            throw new RuntimeException(caught);
        }
    }

    @Override
    public void onSuccess(T result) {
        if (callbackCalled) {
            throw new IllegalStateException("Callback called a second time");
        }
        this.callbackCalled = true;
        this.result = result;
    }

    public T getResult() {
        if (!callbackCalled) {
            throw new IllegalStateException("Callback was not called");
        }
        return result;
    }

    public static <T> Collector<T> newCollector() {
        return new Collector<T>();
    }
}
