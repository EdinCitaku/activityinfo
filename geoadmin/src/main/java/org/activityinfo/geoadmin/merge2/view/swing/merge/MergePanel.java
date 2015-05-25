package org.activityinfo.geoadmin.merge2.view.swing.merge;


import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.model.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.model.FormMapping;
import org.activityinfo.geoadmin.merge2.view.model.RowMatching;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.observable.Subscription;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MergePanel extends StepPanel {

    private final MergeModelStore store;

    private final MergeTableModel tableModel;
    private final JTable table;

    private final Subscription columnsSubscription;

    public MergePanel(MergeModelStore store) {
        this.store = store;
        setLayout(new BorderLayout());

        tableModel = new MergeTableModel(store);
        table = new JTable(tableModel);
        table.setAutoCreateColumnsFromModel(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        columnsSubscription = store.getFieldMapping().subscribe(new Observer<FormMapping>() {
            @Override
            public void onChange(Observable<FormMapping> observable) {
                if(!observable.isLoading()) {
                    onColumnsChanged(observable.get());
                }
            }
        });

        add(new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
    }

    private void onColumnsChanged(FormMapping formMapping) {
        java.util.List<MergeTableColumn> columns = new ArrayList<>();

        // On the left hand side, show the existing "Target" features
        for (FieldProfile targetField : formMapping.getTarget().getFields()) {
            if(targetField.getView() != null) {
                columns.add(new TargetColumn(store.getRowMatching(), targetField,
                        formMapping.getMappingForTarget(targetField)));
            }
        }

        columns.add(new SeparatorColumn());

        // On the right, show the imported features, in the same order
        // as the columns to which they're mapped
        for (FieldProfile targetField : formMapping.getTarget().getFields()) {
            java.util.List<SourceFieldMapping> mappings = formMapping.getMappingForTarget(targetField);
            for(SourceFieldMapping mapping : mappings) {
                columns.add(new SourceColumn(store.getRowMatching(), mapping));
            }
        }
        // Also include the unmatched source columns as reference
        for (SourceFieldMapping sourceMapping : formMapping.getMappings()) {
            if(!sourceMapping.getTargetField().isPresent() &&
                    sourceMapping.getSourceField().getView() != null) {
                columns.add(new SourceColumn(store.getRowMatching(), sourceMapping));
            }
        }

        TableColumnModel tableColumnModel = buildColumnModel(columns);
        table.setColumnModel(tableColumnModel);

        JTableHeader tableHeader = new JTableHeader(tableColumnModel);
        tableHeader.setResizingAllowed(true);
        tableHeader.setReorderingAllowed(false);
        table.setTableHeader(tableHeader);

        tableModel.updateColumns(columns);
    }

    private TableColumnModel buildColumnModel(List<MergeTableColumn> columns) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        for (int i = 0; i < columns.size(); i++) {
            MergeTableColumn column = columns.get(i);
            TableColumn tableColumn = new TableColumn(i);
            tableColumn.setHeaderValue(column.getHeader());
            tableColumn.setCellRenderer(new MergeColumnRenderer(column));

            tableColumn.setResizable(column.isResizable());
            if(column.getWidth() > 0) {
                tableColumn.setWidth(column.getWidth());
            }
            model.addColumn(tableColumn);
        }
        return model;
    }


    @Override
    public void stop() {
        columnsSubscription.unsubscribe();
        tableModel.stop();
    }
}
