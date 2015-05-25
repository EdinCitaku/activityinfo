package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.view.model.RowMatching;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.activityinfo.observable.Observable;

import java.awt.*;

/**
 * Source Column
 */
public class SourceColumn implements MergeTableColumn {

    private Observable<RowMatching> matching;
    private SourceFieldMapping mapping;

    public SourceColumn(Observable<RowMatching> matching, SourceFieldMapping mapping) {
        this.matching = matching;
        this.mapping = mapping;
    }

    @Override
    public String getHeader() {
        return mapping.getSourceField().getLabel();
    }

    @Override
    public String getValue(int rowIndex) {
        if(matching.isLoading()) {
            return null;
        }
        int sourceRow = matching.get().get(rowIndex).getSourceRow();
        if(sourceRow == -1) {
            return null;
        }
        Object value = mapping.getSourceField().getView().get(sourceRow);
        if(value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public Color getColor(int rowIndex) {
        return Color.WHITE;
    }

    @Override
    public int getTextAlignment() {
        return 0;
    }

    @Override
    public int getWidth() {
        return -1;
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
