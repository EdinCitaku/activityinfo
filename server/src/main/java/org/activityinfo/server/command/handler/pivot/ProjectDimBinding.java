package org.activityinfo.server.command.handler.pivot;

import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

import java.util.Arrays;
import java.util.List;

public class ProjectDimBinding extends DimBinding {


    private static final String PROJECT_ID_COLUMN = "ProjectId";
    private static final String PROJECT_LABEL_COLUMN = "ProjectLabel";

    private final Dimension model = new Dimension(DimensionType.Project);

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {

        int activityId = activityIdOf(formTree);

        ColumnModel projectId = new ColumnModel();
        projectId.setExpression(CuidAdapter.projectField(activityId));
        projectId.setId(PROJECT_ID_COLUMN);

        ColumnModel projectLabel = new ColumnModel();
        projectLabel.setExpression(CuidAdapter.projectField(activityId).asString() + ".Label");
        projectLabel.setId(PROJECT_LABEL_COLUMN);

        return Arrays.asList(projectId, projectLabel);
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public DimensionCategory[] extractCategories(FormTree formTree, ColumnSet columnSet) {

        ColumnView id = columnSet.getColumnView(PROJECT_ID_COLUMN);
        ColumnView label = columnSet.getColumnView(PROJECT_LABEL_COLUMN);

        int numRows = columnSet.getNumRows();

        DimensionCategory categories[] = new DimensionCategory[numRows];

        for (int i = 0; i < numRows; i++) {
            String partnerId = id.getString(i);
            categories[i] = new EntityCategory(CuidAdapter.getLegacyIdFromCuid(partnerId), label.getString(i));
        }

        return categories;
    }
}
