package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.input.view.FormDialog;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.ColumnDialog;

import java.util.logging.Logger;

/**
 * Displays a Form as a Table
 */
public class TableView implements IsWidget {

    public static final int MARGINS = 8;

    private static final Logger LOGGER = Logger.getLogger(TableView.class.getName());


    private TableViewModel viewModel;
    private ContentPanel panel;
    private final BorderLayoutContainer container;

    private ToolBar toolBar;
    private TableGrid grid;

    private IsWidget errorWidget;

    private VerticalLayoutContainer center;

    private Subscription subscription;

    private FormStore formStore;
    private final SidePanel sidePanel;


    public TableView(FormStore formStore, final TableViewModel viewModel) {
        this.formStore = formStore;

        TableBundle.INSTANCE.style().ensureInjected();

        this.viewModel = viewModel;

        TextButton newButton = new TextButton(I18N.CONSTANTS.newText());
        newButton.addSelectHandler(this::onNewRecordClicked);

        TextButton removeButton = new TextButton(I18N.CONSTANTS.remove());
        removeButton.addSelectHandler(this::onDeleteRecordClicked);

        TextButton editButton = new TextButton(I18N.CONSTANTS.edit());
        editButton.addSelectHandler(this::onEditRecordClicked);

        TextButton printButton = new TextButton(I18N.CONSTANTS.printForm());
        printButton.addSelectHandler(this::onPrintRecordClicked);

        TextButton importButton = new TextButton(I18N.CONSTANTS.importText());
        importButton.addSelectHandler(this::onImportClicked);

        TextButton exportButton = new TextButton(I18N.CONSTANTS.export());
        exportButton.addSelectHandler(this::onExportClicked);

        TextButton columnsButton = new TextButton(I18N.CONSTANTS.chooseColumns());
        columnsButton.addSelectHandler(this::onChooseColumnsSelected);

        OfflineStatusButton offlineButton = new OfflineStatusButton(formStore, viewModel.getFormId());

        this.toolBar = new ToolBar();
        toolBar.add(newButton);
        toolBar.add(editButton);
        toolBar.add(removeButton);
        toolBar.add(importButton);
        toolBar.add(exportButton);
        toolBar.add(columnsButton);
        toolBar.add(offlineButton);

        center = new VerticalLayoutContainer();
        center.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));

        this.container = new BorderLayoutContainer();


        sidePanel = new SidePanel(viewModel);
        BorderLayoutContainer.BorderLayoutData sidePaneLayout = new BorderLayoutContainer.BorderLayoutData(150);
        sidePaneLayout.setSplit(true);
        sidePaneLayout.setMargins(new Margins(0, 0, 0, MARGINS));

//        SubFormPane subFormPane = new SubFormPane(viewModel);
//        BorderLayoutContainer.BorderLayoutData subFormPaneLayout = new BorderLayoutContainer.BorderLayoutData(150);
//        subFormPaneLayout.setSplit(true);
//        subFormPaneLayout.setMargins(new Margins(0, 0, 0, MARGINS));

        this.container.setEastWidget(sidePanel, sidePaneLayout);
//        this.container.setSouthWidget(subFormPane, subFormPaneLayout);
        this.container.setCenterWidget(center);

        this.panel = new ContentPanel() {

            @Override
            protected void onAttach() {
                super.onAttach();
                LOGGER.info("TableView attaching...");
                subscription = viewModel.getEffectiveTable().subscribe(observable -> effectiveModelChanged());
            }

            @Override
            protected void onDetach() {
                super.onDetach();
                LOGGER.info("TableView detaching...");
                subscription.unsubscribe();
            }
        };
        this.panel.setHeading(I18N.CONSTANTS.loading());
        this.panel.add(container);
    }



    private void onNewRecordClicked(SelectEvent event) {
        FormDialog dialog = new FormDialog(formStore, viewModel.getFormId());
        dialog.show();
    }

    private void onEditRecordClicked(SelectEvent event) {

    }

    private void onPrintRecordClicked(SelectEvent event) {

    }

    private void onDeleteRecordClicked(SelectEvent event) {


    }

    private void onImportClicked(SelectEvent event) {

    }


    private void onExportClicked(SelectEvent event) {

    }


    private void onChooseColumnsSelected(SelectEvent event) {
        if(viewModel.getEffectiveTable().isLoaded()) {
            ColumnDialog dialog = new ColumnDialog(viewModel.getEffectiveTable().get());
            dialog.show();
        }
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    private void effectiveModelChanged() {
        if(viewModel.getEffectiveTable().isLoading()) {
            this.panel.mask();
        } else {
            this.panel.unmask();

            switch (viewModel.getEffectiveTable().get().getRootFormState()) {
                case FORBIDDEN:
                case DELETED:
                    showErrorState(viewModel.getEffectiveTable().get().getRootFormState());
                    break;
                case VALID:
                    updateGrid(viewModel.getEffectiveTable().get());
                    break;
            }
        }
    }

    private void showErrorState(FormTree.State rootFormState) {


        errorWidget = new ForbiddenWidget();

        panel.setWidget(errorWidget);
        panel.setHeaderVisible(false);
        panel.forceLayout();
    }

    private void updateGrid(EffectiveTableModel effectiveTableModel) {

        panel.setHeading(effectiveTableModel.getFormLabel());
        panel.setHeaderVisible(true);

        if(grid == null) {
            grid = new TableGrid(effectiveTableModel);
            grid.addSelectionChangedHandler(event -> {
                if(!event.getSelection().isEmpty()) {
                    RecordRef ref = event.getSelection().get(0);
                    viewModel.select(ref);
                }
            });
            center.add(grid, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
            center.forceLayout();

        } else {
            grid.update(viewModel.getEffectiveTable());
        }

        // If we are transitioning from an error state, make the container with the
        // grid and sidebars is set
        if(!container.isAttached()) {
            panel.setWidget(container);
        }
        panel.forceLayout();
    }

    public void stop() {


    }
}
