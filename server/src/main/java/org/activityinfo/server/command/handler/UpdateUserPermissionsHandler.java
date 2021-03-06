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
package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.UpdateUserPermissions;
import org.activityinfo.legacy.shared.command.result.BillingException;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.UserExistsException;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.FolderDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.database.UserPermissionModel;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.database.hibernate.dao.*;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.server.mail.InvitationMessage;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.server.mail.Message;
import org.activityinfo.store.query.UsageTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 * @see org.activityinfo.legacy.shared.command.UpdateUserPermissions
 */
public class UpdateUserPermissionsHandler implements CommandHandler<UpdateUserPermissions> {

    private final UserDAO userDAO;
    private final UserDatabaseDAO databaseDAO;
    private final PartnerDAO partnerDAO;
    private final UserPermissionDAO permDAO;
    private final BillingAccountOracle billingAccountOracle;

    private final MailSender mailSender;


    private static final Logger LOGGER = Logger.getLogger(UpdateUserPermissionsHandler.class.getName());

    @Inject
    public UpdateUserPermissionsHandler(UserDatabaseDAO databaseDAO,
                                        PartnerDAO partnerDAO,
                                        UserDAO userDAO,
                                        UserPermissionDAO permDAO,
                                        BillingAccountOracle billingAccountOracle, MailSender mailSender) {
        this.userDAO = userDAO;
        this.partnerDAO = partnerDAO;
        this.permDAO = permDAO;
        this.billingAccountOracle = billingAccountOracle;
        this.mailSender = mailSender;
        this.databaseDAO = databaseDAO;
    }

    @Override
    public CommandResult execute(UpdateUserPermissions cmd, User executingUser) {

        LOGGER.info("UpdateUserPermissions: " + cmd);

        Database database = databaseDAO.findById(cmd.getDatabaseId());
        UserPermissionDTO dto = cmd.getModel();
        /*
         * First check that the current user has permission to add users to to
         * the queries
         */
        boolean isOwner = executingUser.getId() == database.getOwner().getId();
        UserPermission executingUserPermission = queryUserPermission(executingUser, database);

        LOGGER.info("executingUserPermission: isOwner: " + isOwner + ", executingUserPermissions: " + cmd);


        if (!isOwner) {
            verifyAuthority(cmd, executingUserPermission);
        }

        /* Database owner cannot be added */
        if(database.getOwner().getEmail().equalsIgnoreCase(cmd.getModel().getEmail())) {
            throw new UserExistsException();
        }

        User user = null;
        if (userDAO.doesUserExist(dto.getEmail())) {
            user = userDAO.findUserByEmail(dto.getEmail());
        }

        if(!billingAccountOracle.isAllowAddUser(user, database)) {
            throw new BillingException(billingAccountOracle.getStatusForDatabase(database.getId()));
        }

        if (user == null) {
            user = createNewUser(executingUser, dto);
        }

        /*
         * Does the permission record exist ?
         */
        UserPermission perm = queryUserPermission(user, database);
        if (perm == null) {
            perm = new UserPermission(database, user);
            doUpdate(perm, dto, isOwner, executingUserPermission);
            permDAO.persist(perm);

            UsageTracker.track(executingUser.getId(), "invite_user", database.getResourceId());

        } else {
            // If the user is intending to add a new user, verify that this user doesn't already exist
            if(cmd.isNewUser() && perm.isAllowView()) {
                throw new UserExistsException();
            }
            doUpdate(perm, dto, isOwner, executingUserPermission);
            UsageTracker.track(executingUser.getId(), "update_permissions", database.getResourceId());
        }


        return null;
    }

    private UserPermission queryUserPermission(User user, Database database) {
        return permDAO.findUserPermissionByUserIdAndDatabaseId(user.getId(), database.getId());
    }

    private User createNewUser(User executingUser, UserPermissionDTO dto) {
        if (executingUser.getId() == 0) {
            throw new AssertionError("executingUser.id == 0!");
        }
        if (executingUser.getName() == null) {
            throw new AssertionError("executingUser.name == null!");
        }

        User user = UserDAOImpl.createNewUser(dto.getEmail(), dto.getName(), executingUser.getLocale());
        user.setInvitedBy(executingUser);
        userDAO.persist(user);

        try {
            Message message = mailSender.createMessage(new InvitationMessage(user, executingUser));
            message.replyTo(executingUser.getEmail(), executingUser.getName());
            mailSender.send(message);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not send invitation mail", e);
            throw new CommandException("Failed to send invitation email");
        }
        return user;
    }

    /**
     * Verifies that the user executing the command has the permission to do
     * assign these permissions.
     * <p/>
     * Static and visible for testing
     *
     * @param cmd
     * @param executingUserPermissions
     * @throws IllegalAccessCommandException
     */
    public static void verifyAuthority(UpdateUserPermissions cmd,
                                       UserPermission executingUserPermissions) {
        if (!executingUserPermissions.isAllowManageUsers()) {
            throw new IllegalAccessCommandException("Current user does not have the right to manage other users");
        }

        if (!executingUserPermissions.isAllowManageAllUsers() &&
            executingUserPermissions.getPartner().getId() != cmd.getModel().getPartner().getId()) {
            throw new IllegalAccessCommandException(
                    "Current user does not have the right to manage users from other partners");
        }

        if (!executingUserPermissions.isAllowDesign() && cmd.getModel().getAllowDesign()) {
            throw new IllegalAccessCommandException("Current user does not have the right to grant design privileges");
        }

        if (!executingUserPermissions.isAllowManageAllUsers() &&
            (cmd.getModel().getAllowViewAll() || cmd.getModel().getAllowEditAll() ||
             cmd.getModel().getAllowManageAllUsers())) {
            throw new IllegalAccessCommandException(
                    "Current user does not have the right to grant viewAll, editAll, or manageAllUsers privileges");
        }
    }

    protected void doUpdate(UserPermission perm,
                            UserPermissionDTO dto,
                            boolean isOwner,
                            UserPermission executingUserPermissions) {

        perm.setPartner(partnerDAO.findById(dto.getPartner().getId()));
        if(perm.getPartner() == null) {
            throw new CommandException("Partner with id " + dto.getPartner().getId() + " does not exist");
        }
        perm.setAllowView(dto.getAllowView());
        perm.setAllowCreate(dto.getAllowCreate());
        perm.setAllowEdit(dto.getAllowEdit());
        perm.setAllowDelete(dto.getAllowDelete());
        perm.setAllowManageUsers(dto.getAllowManageUsers());
        perm.setAllowExport(dto.getAllowExport());

        if(dto.hasFolderLimitation()) {
            perm.setModel(constructModel(perm, dto).toJson().toJson());
        } else {
            // If there are no folders specified, then revert back to the
            // the simple model
            perm.setModel(null);
        }

        // If currentUser does not have the manageAllUsers permission, then
        // careful not to overwrite permissions that may have been granted by
        // other users with greater permissions

        // The exception is when a user with partner-level user management
        // rights
        // (manageUsers but not manageAllUsers) removes view or edit permissions
        // from
        // an existing user who had been previously granted viewAll or editAll
        // rights
        // by a user with greater permissions.
        //
        // In this case, the only logical outcome (I think) is that

        if (isOwner || executingUserPermissions.isAllowManageAllUsers() || !dto.getAllowView()) {
            perm.setAllowViewAll(dto.getAllowViewAll());
        }

        if (isOwner || executingUserPermissions.isAllowManageAllUsers() || !dto.getAllowCreate()) {
            perm.setAllowCreateAll(dto.getAllowCreateAll());
        }

        if (isOwner || executingUserPermissions.isAllowManageAllUsers() || !dto.getAllowEdit()) {
            perm.setAllowEditAll(dto.getAllowEditAll());
        }

        if (isOwner || executingUserPermissions.isAllowManageAllUsers() || !dto.getAllowDelete()) {
            perm.setAllowDeleteAll(dto.getAllowDeleteAll());
        }

        if (isOwner || executingUserPermissions.isAllowManageAllUsers()) {
            perm.setAllowManageAllUsers(dto.getAllowManageAllUsers());
        }

        if (isOwner || executingUserPermissions.isAllowDesign()) {
            perm.setAllowDesign(dto.getAllowDesign());
        }

        perm.setLastSchemaUpdate(new Date());
    }

    private UserPermissionModel constructModel(UserPermission perm, UserPermissionDTO dto) {
        List<GrantModel> grants = new ArrayList<>();
        for (FolderDTO folderDTO : dto.getFolders()) {
            GrantModel grant = new GrantModel.Builder()
                .setResourceId(CuidAdapter.folderId(folderDTO.getId()))
                .build();
            grants.add(grant);
        }
        return new UserPermissionModel(perm.getUser().getId(), perm.getDatabase().getId(), grants);
    }
}
