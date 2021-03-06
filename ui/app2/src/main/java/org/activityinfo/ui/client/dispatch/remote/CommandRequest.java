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
package org.activityinfo.ui.client.dispatch.remote;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.MutatingCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates a pending command request to the server.
 *
 * @author Alex Bertram
 */
class CommandRequest implements AsyncCallback {
    /**
     * The pending command
     */
    private final Command command;
    private final List<AsyncCallback> callbacks = new ArrayList<AsyncCallback>();

    public CommandRequest(Command command, AsyncCallback callback) {
        this.command = command;
        this.callbacks.add(callback);
    }

    public Command getCommand() {
        return command;
    }

    public Collection<AsyncCallback> getCallbacks() {
        return Collections.unmodifiableCollection(this.callbacks);
    }

    public boolean mergeSuccessfulInto(List<CommandRequest> list) {
        for (CommandRequest request : list) {
            if (command.equals(request.getCommand())) {
                request.merge(this);
                return true;
            }
        }
        return false;
    }

    private void merge(CommandRequest request) {
        Log.debug("Dispatcher: merging " + request.getCommand().toString() + " with pending/executing command " +
                  getCommand().toString());

        callbacks.addAll(request.callbacks);
    }

    /**
     * True if this CommandRequest is expected to mutate (change) the state of
     * the remote server.
     */
    public boolean isMutating() {
        return command instanceof MutatingCommand;
    }

    @Override
    public void onFailure(Throwable caught) {
        for (AsyncCallback c : callbacks) {
            try {
                c.onFailure(caught);
            } catch (Exception e) {
                Log.error("Uncaught exception during onFailure()", e);
            }
        }
    }

    @Override
    public void onSuccess(Object result) {
        List<AsyncCallback> toCallback = new ArrayList<AsyncCallback>(callbacks);
        for (AsyncCallback c : toCallback) {
            try {
                c.onSuccess(result);
            } catch (Exception e) {
                Log.error("Exception thrown during callback on AsyncCallback.onSuccess() for " + command.toString(), e);
            }
        }
    }
}
    