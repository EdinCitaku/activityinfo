package org.activityinfo.ui.full.client.report.editor.pivotTable;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.inject.Inject;
import org.activityinfo.reports.shared.content.PivotContent;
import org.activityinfo.reports.shared.model.PivotTableReportElement;
import org.activityinfo.api.shared.command.Filter;
import org.activityinfo.api.shared.command.RenderElement.Format;
import org.activityinfo.ui.full.client.EventBus;
import org.activityinfo.api.client.AsyncMonitor;
import org.activityinfo.api.client.Dispatcher;
import org.activityinfo.ui.full.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.full.client.i18n.I18N;
import org.activityinfo.ui.full.client.page.report.editor.ReportElementEditor;
import org.activityinfo.ui.full.client.report.editor.chart.PivotFilterPanel;
import org.activityinfo.ui.full.client.report.view.PivotGridPanel;
import org.activityinfo.ui.full.client.report.view.ReportViewBinder;
import org.activityinfo.ui.full.client.util.state.StateProvider;

import java.util.Arrays;
import java.util.List;

public class PivotTableEditor extends LayoutContainer implements
        ReportElementEditor<PivotTableReportElement> {

    private final EventBus eventBus;
    private final Dispatcher service;
    private final StateProvider stateMgr;

    private PivotTrayPanel pivotPanel;
    private PivotFilterPanel filterPane;
    private ReportViewBinder<PivotContent, PivotTableReportElement> viewBinder;

    private DimensionPruner pruner;

    private LayoutContainer center;
    private PivotGridPanel gridPanel;

    private PivotTableReportElement model;

    @Inject
    public PivotTableEditor(EventBus eventBus, Dispatcher service,
                            StateProvider stateMgr) {
        this.eventBus = eventBus;
        this.service = service;
        this.stateMgr = stateMgr;

        initializeComponent();

        createPane();
        createFilterPane();
        createGridContainer();

        this.pruner = new DimensionPruner(eventBus, service);

        // initialDrillDownListener = new Listener<PivotCellEvent>() {
        // @Override
        // public void handleEvent(PivotCellEvent be) {
        // createDrilldownPanel(be);
        // }
        // };
        // eventBus.addListener(AppEvents.DRILL_DOWN, initialDrillDownListener);

    }

    private void initializeComponent() {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setEnableState(true);
        setStateId("pivotPage");
        setLayout(borderLayout);
    }

    private void createPane() {

        pivotPanel = new PivotTrayPanel(eventBus, service);

        BorderLayoutData east = new BorderLayoutData(Style.LayoutRegion.EAST);
        east.setCollapsible(true);
        east.setSplit(true);
        east.setMargins(new Margins(0, 5, 0, 0));

        add(pivotPanel, east);
    }

    private void createFilterPane() {
        filterPane = new PivotFilterPanel(eventBus, service);
        filterPane.applyBaseFilter(new Filter());

        BorderLayoutData west = new BorderLayoutData(Style.LayoutRegion.WEST);
        west.setMinSize(250);
        west.setSize(250);
        west.setCollapsible(true);
        west.setSplit(true);
        west.setMargins(new Margins(0, 0, 0, 0));
        add(filterPane, west);
    }

    private void createGridContainer() {
        center = new LayoutContainer();
        center.setLayout(new BorderLayout());
        add(center, new BorderLayoutData(Style.LayoutRegion.CENTER));

        gridPanel = new PivotGridPanel();
        gridPanel.setHeaderVisible(true);
        gridPanel.setHeadingText(I18N.CONSTANTS.preview());

        center.add(gridPanel, new BorderLayoutData(
                Style.LayoutRegion.CENTER));

        viewBinder = ReportViewBinder.create(eventBus, service, gridPanel);
    }

    public AsyncMonitor getMonitor() {
        return new MaskingAsyncMonitor(this, I18N.CONSTANTS.loading());
    }

    @Override
    public PivotTableReportElement getModel() {
        return model;
    }

    @Override
    public void bind(PivotTableReportElement model) {
        this.model = model;
        pivotPanel.bind(model);
        filterPane.bind(model);
        viewBinder.bind(model);
        pruner.bind(model);
    }

    @Override
    public void disconnect() {
        pivotPanel.disconnect();
        filterPane.disconnect();
        viewBinder.disconnect();
        pruner.disconnect();
    }

    @Override
    public Component getWidget() {
        return this;
    }

    @Override
    public List<Format> getExportFormats() {
        return Arrays.asList(Format.Excel, Format.Word, Format.PDF);
    }

}
