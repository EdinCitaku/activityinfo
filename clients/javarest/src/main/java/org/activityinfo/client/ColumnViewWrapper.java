package org.activityinfo.client;

import com.google.common.base.Preconditions;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.FilteredColumnView;
import org.activityinfo.model.query.SortModel;


public class ColumnViewWrapper implements ColumnView {

    @SuppressWarnings("GwtInconsistentSerializableClass")
    private JsonValue array;
    private int numRows;

    public ColumnViewWrapper() {
    }

    public ColumnViewWrapper(int numRows, JsonValue array) {
        this.array = array;
        this.numRows = numRows;
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int numRows() {
        return numRows;
    }

    @Override
    public Object get(int row) {
        return getString(row);
    }

    @Override
    public double getDouble(int row) {
        JsonValue jsonElement = array.get(row);
        if(jsonElement.isJsonNull()) {
            return Double.NaN;
        } else {
            return jsonElement.asNumber();
        }
    }

    @Override
    public String getString(int row) {
        Preconditions.checkPositionIndex(row, numRows);

        JsonValue jsonElement = array.get(row);
        if(jsonElement.isJsonNull()) {
            return null;
        } else {
            return jsonElement.asString();
        }
    }

    @Override
    public int getBoolean(int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMissing(int row) {
        return array.get(row).isJsonNull();
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        return new FilteredColumnView(this, selectedRows);
    }


    @Override
    public String toString() {
        return array.toJson();
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        // TODO: ColumnViewWrapper Sorting
        // Do not sort on column
        return sortVector;
    }
}
