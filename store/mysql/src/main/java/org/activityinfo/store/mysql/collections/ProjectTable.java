package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.DeleteMethod;
import org.activityinfo.store.mysql.mapping.SimpleTable;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.mapping.TableMappingBuilder;
import org.activityinfo.store.mysql.metadata.DatabaseCache;

import java.sql.SQLException;

import static org.activityinfo.model.legacy.CuidAdapter.*;

public class ProjectTable implements SimpleTable {
    
    private final DatabaseCache databaseVersionMap;

    public ProjectTable(DatabaseCache databaseVersionMap) {
        this.databaseVersionMap = databaseVersionMap;
    }

    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.PROJECT_CLASS_DOMAIN;
    }

    @Override
    public TableMapping getMapping(QueryExecutor executor, ResourceId formId) throws SQLException {
        int databaseId = CuidAdapter.getLegacyIdFromCuid(formId);

        TableMappingBuilder mapping = TableMappingBuilder.newMapping(formId, "partner");
        mapping.setFormLabel("Project");
        mapping.setDatabaseId(CuidAdapter.databaseId(databaseId));
        mapping.setPrimaryKeyMapping(CuidAdapter.PROJECT_DOMAIN, "projectId");
        mapping.setFromClause("project base");
        mapping.setBaseFilter("dateDeleted IS NULL AND databaseId=" + databaseId);
        mapping.setDeleteMethod(DeleteMethod.SOFT_BY_DATE);
        mapping.setVersion(databaseVersionMap.getSchemaVersion(databaseId));
        mapping.setSchemaVersion(1L); // Schema is static
        
        FormField nameField = new FormField(field(formId, NAME_FIELD))
                .setRequired(true)
                .setLabel("Name")
                .setCode("label")
                .setKey(true)
                .setType(TextType.INSTANCE);

        mapping.addTextField(nameField, "name");


        FormField fullNameField = new FormField(field(formId, FULL_NAME_FIELD))
                .setLabel("Description")
                .setRequired(false)
                .setType(TextType.INSTANCE);

        mapping.addTextField(fullNameField, "description");

        return mapping.build();
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor queryExecutor, ResourceId id) throws SQLException {
        return Optional.absent();
    }
}
