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
package org.activityinfo.model.permission;

/**
 * Describes the different operations that can be granted to a user.
 */
public enum Operation {

    /**
     * View the resource, whether a form, folder, or database.
     */
    VIEW,

    /**
     * Create a record within a form contained by this
     * folder or form.
     */
    CREATE_RECORD,

    /**
     * Edit a record's values within a form contained by this folder or form.
     */
    EDIT_RECORD,

    /**
     * Delete a record within this form.
     */
    DELETE_RECORD,

    /**
     * Grant permissions to a user to this database, folder, or form.
     */
    MANAGE_USERS,

    /**
     * Create a new Resource (Form or Folder)
     */
    CREATE_RESOURCE,

    /**
     * Delete a Resource (Form or Folder)
     */
    DELETE_RESOURCE,

    /**
     * Edit a Resource's schema, structure, attributes or data.
     */
    EDIT_RESOURCE,

    /**
     * Add, modify, or remove locks on records.
     */
    LOCK_RECORDS,

    /**
     * Import Records into a form.
     */
    IMPORT_RECORDS,

    /**
     * Export Records from a form, folder or database.
     */
    EXPORT_RECORDS,

    /**
     * Add, modify, or remove targets on fields.
     */
    MANAGE_TARGETS

}
