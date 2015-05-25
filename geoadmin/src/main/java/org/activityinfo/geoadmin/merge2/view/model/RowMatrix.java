package org.activityinfo.geoadmin.merge2.view.model;

import com.google.common.collect.BiMap;
import org.activityinfo.geoadmin.match.DistanceMatrix;
import org.activityinfo.geoadmin.merge.model.MergeColumn;
import org.activityinfo.geoadmin.merge.model.MergeForm;
import org.activityinfo.geoadmin.merge2.view.swing.merge.MergeSide;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;
import org.activityinfo.model.query.ColumnView;

import java.util.Map;

/**
 * Matrix representing the distance between two sets of rows
 */
public class RowMatrix implements DistanceMatrix {

    private ColumnView target[];
    private ColumnView source[];

    private int targetRowCount;
    private int sourceRowCount;

    private int dimensionCount;

    public RowMatrix(FormProfile targetForm, FormProfile sourceForm, BiMap<FieldProfile, FieldProfile> columnMapping) {
        dimensionCount = columnMapping.size();
        this.target = new ColumnView[dimensionCount];
        this.source = new ColumnView[dimensionCount];
        this.targetRowCount = targetForm.getRowCount();
        this.sourceRowCount = sourceForm.getRowCount();

        int columnIndex = 0;
        for (Map.Entry<FieldProfile, FieldProfile> mapping : columnMapping.entrySet()) {
            target[columnIndex] = mapping.getKey().getView();
            source[columnIndex] = mapping.getValue().getView();
            columnIndex++;
        }
    }

    public RowMatrix(FormMapping formMapping) {
        this(formMapping.getTarget(), formMapping.getSource(), formMapping.asMap(MergeSide.TARGET, MergeSide.SOURCE));
    }

    @Override
    public int getDimensionCount() {
        return dimensionCount;
    }

    @Override
    public int getRowCount() {
        return targetRowCount;
    }

    @Override
    public int getColumnCount() {
        return sourceRowCount;
    }

    @Override
    public boolean matches(int targetRowIndex, int sourceRowIndex) {
        LatinPlaceNameScorer placeNameScorer = new LatinPlaceNameScorer();
        for(int d=0;d<dimensionCount;++d) {
            String targetValue = target[d].getString(targetRowIndex);
            String sourceValue = source[d].getString(sourceRowIndex);
            if(targetValue != null && sourceValue != null) {
                double score = placeNameScorer.score(targetValue, sourceValue);
                if(score > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public double distance(int targetRowIndex, int sourceRowIndex) {
        double distance = 0;

        LatinPlaceNameScorer placeNameScorer = new LatinPlaceNameScorer();
        for(int d=0;d<dimensionCount;++d) {
            String targetValue = target[d].getString(targetRowIndex);
            String sourceValue = source[d].getString(sourceRowIndex);
            if(targetValue != null && sourceValue != null) {
                double score = placeNameScorer.score(targetValue, sourceValue);
                distance += (1.0 - score);
            }
        }
        return distance;
    }
}
