package org.activityinfo.ui.client.page.entry;

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

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.PivotSites.PivotResult;
import org.activityinfo.legacy.shared.command.PivotSites.ValueType;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.MonthCategory;
import org.activityinfo.legacy.shared.reports.content.YearCategory;
import org.activityinfo.legacy.shared.reports.model.DateDimension;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.date.DateUnit;
import org.activityinfo.ui.client.dispatch.Dispatcher;

import java.util.ArrayList;
import java.util.List;

public class SiteTimeTreeLoader extends BaseTreeLoader<ModelData> implements SiteTreeLoader {

    private TreeProxy treeProxy;

    public SiteTimeTreeLoader(Dispatcher dispatcher) {
        super(new TreeProxy(dispatcher));
        treeProxy = (TreeProxy) proxy;
    }

    @Override
    public void setFilter(Filter filter) {
        treeProxy.setFilter(filter);
    }

    @Override
    public String getKey(ModelData model) {
        if (model instanceof YearModel) {
            return ((YearModel) model).getKey();
        } else if (model instanceof MonthModel) {
            return ((MonthModel) model).getKey();
        } else if (model instanceof SiteDTO) {
            return "S" + ((SiteDTO) model).getId();
        } else {
            return "X" + model.hashCode();
        }
    }

    @Override
    public boolean hasChildren(ModelData parent) {
        return parent instanceof YearModel || parent instanceof MonthModel;
    }

    private static class TreeProxy extends SafeRpcProxy<List<ModelData>> {

        private static final DateDimension YEAR_DIMENSION = new DateDimension(DateUnit.YEAR);
        private static final DateDimension MONTH_DIMENSION = new DateDimension(DateUnit.MONTH);

        private final Dispatcher dispatcher;
        private Filter filter;

        public TreeProxy(Dispatcher dispatcher) {
            super();

            this.dispatcher = dispatcher;
        }

        public void setFilter(Filter filter) {
            this.filter = filter;
        }

        @Override
        protected void load(Object parentNode, AsyncCallback<List<ModelData>> callback) {

            if (parentNode == null) {
                loadYears(callback);

            } else if (parentNode instanceof YearModel) {
                loadMonths((YearModel) parentNode, callback);

            } else if (parentNode instanceof MonthModel) {
                loadSites((MonthModel) parentNode, callback);
            }
        }

        private void loadYears(final AsyncCallback<List<ModelData>> callback) {
            PivotSites pivot = new PivotSites();
            pivot.setDimensions(Sets.<Dimension>newHashSet(YEAR_DIMENSION));
            pivot.setFilter(filter);
            pivot.setValueType(ValueType.TOTAL_SITES);

            if (pivot.isTooBroad()) {
                callback.onSuccess(new ArrayList<ModelData>());
                return;
            }

            dispatcher.execute(pivot, new AsyncCallback<PivotResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(PivotResult result) {
                    List<ModelData> years = Lists.newArrayList();
                    for (Bucket bucket : result.getBuckets()) {
                        years.add(new YearModel((YearCategory) bucket.getCategory(YEAR_DIMENSION)));
                    }
                    callback.onSuccess(years);
                }
            });
        }

        private void loadMonths(YearModel parentNode, final AsyncCallback<List<ModelData>> callback) {
            PivotSites pivot = new PivotSites();
            pivot.setDimensions(Sets.<Dimension>newHashSet(MONTH_DIMENSION));
            pivot.setFilter(narrowFilter(parentNode.getDateRange()));
            pivot.setValueType(ValueType.TOTAL_SITES);

            if (pivot.isTooBroad()) {
                callback.onSuccess(new ArrayList<ModelData>());
                return;
            }

            dispatcher.execute(pivot, new AsyncCallback<PivotResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(PivotResult result) {
                    List<ModelData> months = Lists.newArrayList();
                    for (Bucket bucket : result.getBuckets()) {
                        months.add(new MonthModel((MonthCategory) bucket.getCategory(MONTH_DIMENSION)));
                    }
                    callback.onSuccess(months);
                }
            });
        }

        private void loadSites(MonthModel parentEntity, final AsyncCallback<List<ModelData>> callback) {

            GetSites siteQuery = new GetSites();
            siteQuery.setFilter(narrowFilter(parentEntity.getDateRange()));
            siteQuery.setSortInfo(new SortInfo("date2", SortDir.ASC));

            dispatcher.execute(siteQuery, new AsyncCallback<SiteResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(SiteResult result) {
                    callback.onSuccess((List) result.getData());
                }
            });
        }

        private Filter narrowFilter(DateRange range) {
            Filter narrowed = new Filter(filter);
            narrowed.setEndDateRange(DateRange.intersection(filter.getEndDateRange(), range));
            return narrowed;
        }
    }

    /**
     * Clone for RPC Proxy that does not swallow exceptions.
     *
     * @param <D>
     */
    public abstract static class SafeRpcProxy<D> implements DataProxy<D> {

        @Override
        public void load(final DataReader<D> reader, final Object loadConfig, final AsyncCallback<D> callback) {
            load(loadConfig, new AsyncCallback<D>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override @SuppressWarnings("unchecked")
                public void onSuccess(Object result) {
                    try {
                        D data = null;
                        if (reader != null) {
                            data = reader.read(loadConfig, result);
                        } else {
                            data = (D) result;
                        }
                        callback.onSuccess(data);
                    } catch (Exception e) {
                        Log.error("Rpc load failed: " + e.getMessage(), e);
                        callback.onFailure(e);
                    }
                }

            });
        }

        /**
         * Subclasses should make RPC call using the load configuration.
         *
         * @param callback the callback to be used when making the rpc call.
         */
        protected abstract void load(Object loadConfig, AsyncCallback<D> callback);
    }
}
