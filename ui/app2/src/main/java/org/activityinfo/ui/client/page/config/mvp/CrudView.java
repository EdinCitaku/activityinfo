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
package org.activityinfo.ui.client.page.config.mvp;

import org.activityinfo.legacy.shared.model.DTO;

import java.util.List;
import java.util.Map;

/*
 * A generalized view interface for views showing a list of items and allowing the user to
 * perform create, update and delete actions
 * 
 * The view is passive and only notifies the Presenter when a user wants to perform
 * a C/U/D action.
 * 
 * Each event and handler are defined explicitly to allow for more verbose implementation
 * on the presenter and the view. Technically, Update/CancelUpdate and RequestDelete/
 * ConfirmDelete can be merged.
 * 
 * The model will usually be an entity wrapping a list of items.
 * 
 * M: the model, a DTO object 
 * P: parent, holding a collection of DTO's
 */
@Deprecated
public interface CrudView<M extends DTO, P extends DTO> extends ListView<M, P>, CanCreate<M>, CanUpdate<M>,
        CanDelete<M>, CanFilter<M>, CanRefresh<M> {

    List<M> getUnsavedItems();

    boolean hasChangedItems();

    boolean hasSingleChangedItem();

    Map<String, Object> getChanges(M item);
}
