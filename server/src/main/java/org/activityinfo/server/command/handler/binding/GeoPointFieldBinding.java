package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public class GeoPointFieldBinding implements FieldBinding {

    public static final String LAT_SYMBOL = "latitude";
    public static final String LONG_SYMBOL = "longitude";

    public static final String GEO_LATITUDE_COLUMN = "y";
    public static final String GEO_LONGITUDE_COLUMN = "x";

    private FormField geoField;

    public GeoPointFieldBinding(FormField geoField) {
        this.geoField = geoField;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView latitude = columnSet.getColumnView(GEO_LATITUDE_COLUMN);
        ColumnView longitude = columnSet.getColumnView(GEO_LONGITUDE_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            Double latDouble = latitude.getDouble(i);
            Double longDouble = longitude.getDouble(i);
            if (!latDouble.isNaN()) {
                dataArray[i].set(GEO_LATITUDE_COLUMN, latDouble);
            }
            if(!longDouble.isNaN()) {
                dataArray[i].set(GEO_LONGITUDE_COLUMN, longDouble);
            }
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Arrays.asList(
                new ColumnModel().setExpression(new CompoundExpr(geoField.getId(),LAT_SYMBOL)).as(GEO_LATITUDE_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(geoField.getId(),LONG_SYMBOL)).as(GEO_LONGITUDE_COLUMN)
        );
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Arrays.asList(
                new ColumnModel().setExpression(new CompoundExpr(geoField.getId(),LAT_SYMBOL)).as(GEO_LATITUDE_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(geoField.getId(),LONG_SYMBOL)).as(GEO_LONGITUDE_COLUMN)
        );
    }
}