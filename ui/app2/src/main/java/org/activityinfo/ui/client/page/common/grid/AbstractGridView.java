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
package org.activityinfo.ui.client.page.common.grid;

import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.ui.client.ClientContext;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.page.common.toolbar.ActionToolBar;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.fromString;

public abstract class AbstractGridView<M extends ModelData, P extends GridPresenter<M>> extends ContentPanel
        implements GridView<P, M> {

    protected ActionToolBar toolBar;
    protected P presenter;
    protected PagingToolBar pagingBar;
    private Grid<M> grid;

    protected abstract <D extends ModelData> Grid<D> createGridAndAddToContainer(Store store);

    protected abstract void initToolBar();


    public void init(final P presenter, Store store) {
        this.presenter = presenter;

        createToolBar();

        grid = createGridAndAddToContainer(store);

        initGridListeners(grid);

        if (store instanceof ListStore) {
            Loader loader = ((ListStore) store).getLoader();
            if (loader instanceof PagingLoader) {
                pagingBar = new PagingToolBar(presenter.getPageSize());
                setBottomComponent(pagingBar);
                pagingBar.bind((PagingLoader<?>) loader);
            }
        }

        /**
         * In some cases, there is async call before the user inerface can be
         * loaded. So we have to make sure our new components are rendered
         */
        if (this.isRendered()) {
            this.layout();
        }
    }

    protected void initGridListeners(Grid<M> grid) {
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<M>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<M> se) {
                presenter.onSelectionChanged(se.getSelectedItem());
            }
        });
    }

    protected void createToolBar() {
        toolBar = new ActionToolBar(presenter);
        setTopComponent(toolBar);

        initToolBar();

        toolBar.setDirty(false);
    }

    @Override
    public void setActionEnabled(String actionId, boolean enabled) {
        toolBar.setActionEnabled(actionId, enabled);
    }

    @Override
    public void confirmDeleteSelected(final ConfirmCallback callback) {
        M selection = getSelection();
        if (selection instanceof ActivityDTO) {
            ActivityDTO activity = (ActivityDTO) selection;
            MessageBox.confirm(
                    fromString(ClientContext.getAppTitle()),
                    I18N.MESSAGES.confirmDeleteForm(activity.getName()),
                    new Listener<MessageBoxEvent>() {
                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                                callback.confirmed();
                            }
                        }
                    });
        } else {
            callback.confirmed();
        }
    }

    @Override
    public M getSelection() {
        GridSelectionModel<M> sm = grid.getSelectionModel();
        if (sm instanceof CellSelectionModel) {
            CellSelectionModel<M>.CellSelection cell = ((CellSelectionModel<M>) sm).getSelectCell();
            return cell == null ? null : cell.model;
        } else {
            return sm.getSelectedItem();
        }
    }

    @Override
    public AsyncMonitor getDeletingMonitor() {
        return new MaskingAsyncMonitor(this, I18N.CONSTANTS.deleting());
    }

    @Override
    public AsyncMonitor getSavingMonitor() {
        return new MaskingAsyncMonitor(this, I18N.CONSTANTS.saving());
    }

    @Override
    public void refresh() {
        grid.getView().refresh(false);
    }

    public ActionToolBar getToolBar() {
        return toolBar;
    }
}
