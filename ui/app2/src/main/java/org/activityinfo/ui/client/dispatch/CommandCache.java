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
package org.activityinfo.ui.client.dispatch;

import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.ui.client.dispatch.remote.cache.CacheResult;

/**
 * Interface to a class which is capable of handling
 * {@link org.activityinfo.legacy.shared.command.Command}s locally, before they are
 * sent to the server.
 *
 * @param <T>
 */
public interface CommandCache<T extends Command> {

    /**
     * Tries to the execute the command locally.
     *
     * @param command the command to execute
     * @return a ProxyResult instance indicating whether local execution was
     * possible, and the CommandResult if it was.
     */
    CacheResult maybeExecute(T command);

    /**
     * Clears the cache, discarding any cached data
     */
    void clear();

}
