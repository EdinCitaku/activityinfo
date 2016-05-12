package org.activityinfo.ui.client.component.table;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.RangeChangeEvent;
import org.activityinfo.core.client.ProjectionKeyProvider;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.component.table.action.*;
import org.activityinfo.ui.client.component.table.filter.FilterCellAction;
import org.activityinfo.ui.client.component.table.filter.FilterHeader;
import org.activityinfo.ui.client.style.table.CellTableResources;
import org.activityinfo.ui.client.widget.CellTable;
import org.activityinfo.ui.client.widget.loading.TableLoadingIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable component to display Instances in a table
 */
public class InstanceTable implements IsWidget {

//    private static final Logger LOGGER = Logger.getLogger(InstanceTable.class.getName());

    /**
     * The default column width, in {@code em}
     */
    public static final int COLUMN_WIDTH = 10;

    private final ResourceLocator resourceLocator;
    private final StateProvider stateProvider;

    private final CellTable<Projection> table;
    private final MultiSelectionModel<Projection> selectionModel = new MultiSelectionModel<>(new ProjectionKeyProvider());
    private final List<TableHeaderAction> headerActions;
    private final InstanceTableView tableView;
    private final InstanceTableDataLoader dataLoader;
    private final TableLoadingIndicator loadingIndicator = new TableLoadingIndicator()
            .setHideOnSuccess(true);

    private FormClass rootFormClass;
    private List<FieldColumn> columns = Lists.newArrayList();

    public InstanceTable(InstanceTableView tableView) {
        this.tableView = tableView;
        this.resourceLocator = tableView.getResourceLocator();
        this.stateProvider = tableView.getStateProvider();

        CellTableResources.INSTANCE.cellTableStyle().ensureInjected();

        final TableHeaderActionBrowserEventHandler headerActionEventHandler = new TableHeaderActionBrowserEventHandler(this);
        table = new CellTable<Projection>(Integer.MAX_VALUE, CellTableResources.INSTANCE) {
            @Override
            protected void onBrowserEvent2(Event event) {
                super.onBrowserEvent2(event);
                headerActionEventHandler.onBrowserEvent(event);
            }
        };
        table.setSkipRowHoverCheck(true);
        table.setSkipRowHoverFloatElementCheck(true);
        table.setSkipRowHoverStyleUpdate(true);
        table.setHeaderBuilder(new InstanceTableHeaderBuilder(this));

        // Set the table to fixed width: we will provide explicit
        // column widths
        table.setWidth("100%", true);
        table.setSelectionModel(selectionModel);
        table.addRangeChangeHandler(new RangeChangeEvent.Handler() {
            @Override
            public void onRangeChange(RangeChangeEvent event) {
                table.redrawHeaders();
            }
        });

        dataLoader = new InstanceTableDataLoader(this);

        loadingIndicator.getRetryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dataLoader.loadMore();
            }
        });

        headerActions = createHeaderActions();

    }

    private List<TableHeaderAction> createHeaderActions() {
        final List<TableHeaderAction> actions = new ArrayList<>();
        actions.add(new NewHeaderAction(this));
        actions.add(new DeleteHeaderAction(this));
        actions.add(new EditHeaderAction(this));
        actions.add(new PrintFormAction(this));
        actions.add(new ImportHeaderAction(this));
        actions.add(new ChooseColumnsHeaderAction(this));
        return actions;
    }

    public void setColumns(List<FieldColumn> columns) {
        removeAllColumns();
        this.dataLoader.reset();
        this.columns = Lists.newArrayList(columns);

        for (FieldColumn column : columns) {
            final FilterCellAction filterAction = new FilterCellAction(this, column);
            table.addColumn(column, new FilterHeader(column, filterAction));
            dataLoader.getFields().addAll(column.getFieldPaths());
        }

        reload();
        table.saveColumnWidthInformation();
    }

    private void removeAllColumns() {
        while (table.getColumnCount() > 0) {
            table.removeColumn(0);
        }
    }

    public InstanceTableDataLoader getDataLoader() {
        return dataLoader;
    }

    public void reload() {
        dataLoader.reload();
    }

    public MultiSelectionModel<Projection> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public Widget asWidget() {
        return table;
    }

    public List<FieldColumn> getColumns() {
        return columns;
    }

    public CellTable<Projection> getTable() {
        return table;
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }

    public void setRootFormClass(FormClass rootFormClass) {
        this.rootFormClass = rootFormClass;
    }

    public FormClass getRootFormClass() {
        return rootFormClass;
    }

    public List<TableHeaderAction> getHeaderActions() {
        return headerActions;
    }

    public InstanceTableView getTableView() {
        return tableView;
    }

    public TableLoadingIndicator getLoadingIndicator() {
        return loadingIndicator;
    }

    public StateProvider getStateProvider() {
        return stateProvider;
    }
}
