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

import com.extjs.gxt.ui.client.Style;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.activityinfo.json.Json;
import org.activityinfo.legacy.shared.command.GetUsers;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.UserResult;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.FolderDTO;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.database.UserPermissionModel;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.FunctionCallNode;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.Folder;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 * @see org.activityinfo.legacy.shared.command.GetUsers
 */
public class GetUsersHandler implements CommandHandler<GetUsers> {

    private static final Logger LOGGER = Logger.getLogger(GetUsersHandler.class.getName());

    private final EntityManager em;
    private final DatabaseProvider provider;

    @Inject
    public GetUsersHandler(EntityManager em,
                           DatabaseProvider databaseProvider,
                           BillingAccountOracle billingAccounts) {
        this.em = em;
        this.provider = databaseProvider;
    }

    @Override
    public CommandResult execute(GetUsers cmd, User currentUser) {
        Optional<UserDatabaseMeta> dbMeta = provider.getDatabaseMetadata(CuidAdapter.databaseId(cmd.getDatabaseId()), currentUser.getId());
        if (!dbMeta.isPresent()) {
            throw new IllegalArgumentException("DatabaseMeta must exist");
        }
        if (!PermissionOracle.canManageUsersForOneOrMoreResources(dbMeta.get())) {
            throw new IllegalAccessCommandException(String.format(
                    "User %d does not have permission to view user permissions in database %d",
                    currentUser.getId(), cmd.getDatabaseId()));
        }

        String whereClause = "up.database.id = :dbId and " +
                             "up.user.id <> :currentUserId and " +
                             "up.allowView = true";

        Optional<String> manageUsersFilter = PermissionOracle.legacyManageUserFilter(dbMeta.get());
        if (manageUsersFilter.isPresent()) {
            whereClause += " and up.partner.id = " + partnerFromFilter(manageUsersFilter.get());
        }

        TypedQuery<UserPermission> query = em.createQuery("select up " +
                "from UserPermission up " +
                 "where " + whereClause + " " + composeOrderByClause(cmd), UserPermission.class)
                .setParameter("dbId", cmd.getDatabaseId())
                .setParameter("currentUserId", currentUser.getId());

        List<Folder> folders = em.createQuery("select f " +
                "from Folder f " +
                "where f.database.id = :dbId", Folder.class)
                .setParameter("dbId", cmd.getDatabaseId())
                .getResultList();

        Map<ResourceId, Folder> folderMap = new HashMap<>();
        for (Folder folder : folders) {
            folderMap.put(CuidAdapter.folderId(folder.getId()), folder);
        }

        if (cmd.getOffset() > 0) {
            query.setFirstResult(cmd.getOffset());
        }
        if (cmd.getLimit() > 0) {
            query.setMaxResults(cmd.getLimit());
        }

        List<UserPermissionDTO> models = new ArrayList<>();
        for (UserPermission perm : query.getResultList()) {
            UserPermissionDTO dto = new UserPermissionDTO();
            dto.setEmail(perm.getUser().getEmail());
            dto.setName(perm.getUser().getName());
            dto.setOrganization(perm.getUser().getOrganization());
            dto.setJobtitle(perm.getUser().getJobtitle());
            dto.setAllowDesign(perm.isAllowDesign());
            dto.setAllowView(perm.isAllowView());
            dto.setAllowViewAll(perm.isAllowViewAll());
            dto.setAllowCreate(perm.isAllowCreate());
            dto.setAllowCreateAll(perm.isAllowCreateAll());
            dto.setAllowEdit(perm.isAllowEdit());
            dto.setAllowEditAll(perm.isAllowEditAll());
            dto.setAllowDelete(perm.isAllowDelete());
            dto.setAllowDeleteAll(perm.isAllowDeleteAll());
            dto.setAllowManageUsers(perm.isAllowManageUsers());
            dto.setAllowManageAllUsers(perm.isAllowManageAllUsers());
            dto.setAllowExport(perm.isAllowExport());
            dto.setPartner(new PartnerDTO(perm.getPartner().getId(), perm.getPartner().getName()));
            dto.setFolderLimitation(!Strings.isNullOrEmpty(perm.getModel()));
            dto.setFolders(folderList(folderMap, perm));
            models.add(dto);
        }

        return new UserResult(models, cmd.getOffset(), queryTotalCount(cmd, currentUser, whereClause));
    }

    private int partnerFromFilter(String filter) {
        FormulaNode filterFormula = FormulaParser.parse(filter);
        FunctionCallNode equalFunctionCall = (FunctionCallNode) filterFormula;
        ConstantNode partnerFieldNode = (ConstantNode) equalFunctionCall.getArgument(1);
        return CuidAdapter.getLegacyIdFromCuid(partnerFieldNode.getValue().toString());
    }

    private List<FolderDTO> folderList(Map<ResourceId, Folder> folderMap, UserPermission perm) {

        if(Strings.isNullOrEmpty(perm.getModel())) {
            // Include all folders, as user has access to all
            List<FolderDTO> folderList = new ArrayList<>(folderMap.size());
            folderMap.values().forEach(folder -> folderList.add(createFolderDTO(folder)));
            return folderList;
        }

        try {
            UserPermissionModel model = UserPermissionModel.fromJson(Json.parse(perm.getModel()));
            List<FolderDTO> folderList = new ArrayList<>();
            for (GrantModel grantModel : model.getGrants()) {
                if (grantModel.getResourceId().getDomain() != CuidAdapter.FOLDER_DOMAIN) {
                    continue;
                }
                Folder folder = folderMap.get(grantModel.getResourceId());
                if(folder != null) {
                    folderList.add(createFolderDTO(folder));
                }
            }
            return folderList;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Permissions model: " + perm.getModel());
            LOGGER.log(Level.SEVERE, "Failed to parse permissions model", e);
            return null;
        }
    }

    private FolderDTO createFolderDTO(Folder folder) {
        FolderDTO dto = new FolderDTO();
        dto.setId(folder.getId());
        dto.setDatabaseId(folder.getDatabase().getId());
        dto.setName(folder.getName());
        return dto;
    }

    private int queryTotalCount(GetUsers cmd, User currentUser, String whereClause) {
        return ((Number) em.createQuery("select count(up) from UserPermission up where " + whereClause)
                           .setParameter("dbId", cmd.getDatabaseId())
                           .setParameter("currentUserId", currentUser.getId())
                           .getSingleResult()).intValue();
    }

    private String composeOrderByClause(GetUsers cmd) {
        String orderByClause = " ";

        if (cmd.getSortInfo().getSortDir() != Style.SortDir.NONE) {
            String dir = cmd.getSortInfo().getSortDir() == Style.SortDir.ASC ? "asc" : "desc";
            String property = null;
            String field = cmd.getSortInfo().getSortField();

            if ("name".equals(field)) {
                property = "up.user.name";
            } else if ("email".equals(field)) {
                property = "up.user.email";
            } else if ("partner".equals(field) || "partner.name".equals(field)) {
                property = "up.partner.name";
            } else if (field != null && field.startsWith("allow")) {
                property = "up." + field;
            }

            if (property != null) {
                orderByClause = " order by " + property + " " + dir;
            }
        }
        return orderByClause;
    }
}
