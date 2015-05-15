package org.activityinfo.ui.client.page.config;

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

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.callback.SuccessCallback;
import org.activityinfo.legacy.client.monitor.MaskingAsyncMonitor;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.ui.client.AppEvents;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.grid.AbstractEditorGridPresenter;
import org.activityinfo.ui.client.page.common.grid.TreeGridView;
import org.activityinfo.ui.client.page.common.nav.Link;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TargetIndicatorPresenter extends AbstractEditorGridPresenter<ModelData> {

    @ImplementedBy(TargetIndicatorView.class)
    public interface View extends TreeGridView<TargetIndicatorPresenter, ModelData> {
        void init(TargetIndicatorPresenter presenter, UserDatabaseDTO db, TreeStore store);

        void expandAll();
    }

    private final EventBus eventBus;
    private final Dispatcher service;
    private final View view;
    private final Map<ActivityDTO, ActivityFormDTO> activities = Maps.newHashMap();
    private TargetDTO targetDTO;

    private UserDatabaseDTO db;
    private TreeStore<ModelData> treeStore;

    @Inject
    public TargetIndicatorPresenter(EventBus eventBus,
                                    Dispatcher service,
                                    StateProvider stateMgr,
                                    View view,
                                    UiConstants messages) {
        super(eventBus, service, stateMgr, view);
        this.eventBus = eventBus;
        this.service = service;
        this.view = view;
    }

    public void go(UserDatabaseDTO db) {
        this.db = db;

        this.treeStore = new TreeStore<ModelData>(new Loader());

        initListeners(treeStore, null);

        this.view.init(this, db, treeStore);
        this.view.setActionEnabled(UIActions.DELETE, false);
    }

    public void load(TargetDTO targetDTO) {
        this.targetDTO = targetDTO;
        this.treeStore.getLoader().load();
    }

    public ActivityFormDTO getActivityFormDTO(ActivityDTO activity) {
        return activities.get(activity);
    }

    private TargetValueDTO createTargetValueModel(IndicatorDTO indicator) {
        TargetValueDTO targetValueDTO = new TargetValueDTO();
        targetValueDTO.setTargetId(targetDTO.getId());
        targetValueDTO.setIndicatorId(indicator.getId());
        targetValueDTO.setName(indicator.getName());

        return targetValueDTO;
    }

    private TargetValueDTO getTargetValueByIndicatorId(int indicatorId) {
        List<TargetValueDTO> values = targetDTO.getTargetValues();

        if (values == null) {
            return null;
        }

        for (TargetValueDTO dto : values) {
            if (dto.getIndicatorId() == indicatorId) {
                return dto;
            }
        }

        return null;
    }

    private Link createIndicatorCategoryLink(IndicatorDTO indicatorNode, Map<String, Link> categories) {
        return Link.folderLabelled(indicatorNode.getCategory())
                .usingKey(categoryKey(indicatorNode, categories))
                .withIcon(IconImageBundle.ICONS.folder())
                .build();
    }

    private Link createCategoryLink(ActivityDTO activity, Map<String, Link> categories) {

        return Link.folderLabelled(activity.getCategory())
                .usingKey(categoryKey(activity, categories))
                .withIcon(IconImageBundle.ICONS.folder())
                .build();
    }

    private String categoryKey(ActivityDTO activity, Map<String, Link> categories) {
        return "category" + activity.getDatabaseId() + activity.getCategory() + categories.size();
    }

    private String categoryKey(IndicatorDTO indicatorNode, Map<String, Link> categories) {
        return "category-indicator" + indicatorNode.getCategory() + categories.size();
    }

    @Override
    public Store<ModelData> getStore() {
        return treeStore;
    }

    public TreeStore<ModelData> getTreeStore() {
        return treeStore;
    }

    protected IsActivityDTO findActivityFolder(ModelData selected) {

        while (!(selected instanceof IsActivityDTO)) {
            selected = treeStore.getParent(selected);
        }

        return (IsActivityDTO) selected;
    }

    public void updateTargetValue() {
        onSave();
    }

    public void rejectChanges() {
        treeStore.rejectChanges();
    }

    @Override
    protected void onDeleteConfirmed(final ModelData model) {
        service.execute(new Delete((EntityDTO) model), view.getDeletingMonitor(), new SuccessCallback<VoidResult>() {
            @Override
            public void onSuccess(VoidResult result) {
                treeStore.remove(model);
                eventBus.fireEvent(AppEvents.SCHEMA_CHANGED);
            }
        });
    }

    @Override
    protected String getStateId() {
        return "target" + db.getId();
    }

    @Override
    protected Command createSaveCommand() {
        BatchCommand batch = new BatchCommand();

        for (ModelData model : treeStore.getRootItems()) {
            prepareBatch(batch, model);
        }
        return batch;
    }

    protected void prepareBatch(BatchCommand batch, ModelData model) {
        if (model instanceof EntityDTO) {
            Record record = treeStore.getRecord(model);
            if (record.isDirty()) {
                UpdateTargetValue cmd = new UpdateTargetValue((Integer) model.get("targetId"),
                        (Integer) model.get("indicatorId"),
                        changes(record));

                batch.add(cmd);
            }
        }

        for (ModelData child : treeStore.getChildren(model)) {
            prepareBatch(batch, child);
        }
    }

    private Map<String, Double> changes(Record record) {
        Map<String, Object> changedProperties = this.getChangedProperties(record);
        Map<String, Double> changes = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : changedProperties.entrySet()) {
            if (entry.getValue() instanceof Double) {
                changes.put(entry.getKey(), (Double) entry.getValue());
            } else if (entry.getValue() instanceof String) {
                changes.put(entry.getKey(), Double.valueOf((String) entry.getValue()));
            }
        }
        return changes;
    }

    @Override
    public void onSelectionChanged(ModelData selectedItem) {
        view.setActionEnabled(UIActions.DELETE, this.db.isDesignAllowed() && selectedItem instanceof EntityDTO);
    }

    @Override
    public Object getWidget() {
        return view;
    }

    @Override
    protected void onSaved() {
        treeStore.commitChanges();
    }

    @Override
    public PageId getPageId() {
        return null;
    }

    @Override
    public boolean navigate(PageState place) {
        return false;
    }

    @Override
    public void shutdown() {
    }

    public IndicatorDTO getIndicatorById(int indicatorId) {
        for (ActivityFormDTO activityFormDTO : activities.values()) {
            IndicatorDTO indicator = activityFormDTO.getIndicatorById(indicatorId);
            if (indicator != null) {
                return indicator;
            }
        }
        return null;
    }

    private class Loader extends BaseTreeLoader<ModelData> {
        public Loader() {
            super(new Proxy());
        }

        @Override
        public boolean hasChildren(ModelData parent) {
            return !(parent instanceof TargetValueDTO);
        }
    }

    private class Proxy implements DataProxy<List<ModelData>> {

        private final BiMap<String, Link> categories = HashBiMap.create();
        private final BiMap<String, Link> indicatorCategories = HashBiMap.create();
        private final BiMap<Link, List<TargetValueDTO>> indicatorLinkChilds = HashBiMap.create();

        @Override
        public void load(DataReader<List<ModelData>> listDataReader,
                         Object parent,
                         final AsyncCallback<List<ModelData>> callback) {

            if (targetDTO == null) {
                callback.onSuccess(new ArrayList<ModelData>()); // targetDTO is not selected yet, ignore initial loading
                return;
            }

            if (parent == null) { // root : activities and activity categories

                List<ModelData> childs = Lists.newArrayList();

                for (ActivityDTO activity : db.getActivities()) {
                    if (activity.getCategory() != null) {
                        Link actCategoryLink = categories.get(activity.getCategory());

                        if (actCategoryLink == null) {

                            actCategoryLink = createCategoryLink(activity, categories);
                            categories.put(activity.getCategory(), actCategoryLink);
                            childs.add(actCategoryLink);
                        }
                    } else {
                        childs.add(activity);
                    }
                }
                callback.onSuccess(childs);
            } else if (parent instanceof Link) { // links : here we handle both activity and indicator links
                List<ModelData> childs = Lists.newArrayList();

                for (ActivityDTO activity : db.getActivities()) { // activity links
                    if (activity.getCategory() != null && activity.getCategory().equals(categories.inverse().get(parent))) {
                        childs.add(activity);
                    }
                }

                if (childs.isEmpty()) { // try maybe it's indicator links
                    List<TargetValueDTO> targetValueDTOs = indicatorLinkChilds.get(parent);
                    if (targetValueDTOs != null && !targetValueDTOs.isEmpty()) {
                        childs.addAll(targetValueDTOs);
                    }
                }

                callback.onSuccess(childs);
            } else if (parent instanceof ActivityDTO) {
                final ActivityDTO activity = (ActivityDTO) parent;
                ActivityFormDTO activityFormDTO = activities.get(activity);
                if (activityFormDTO != null) {
                    activityChilds(activityFormDTO, activity, callback);
                } else {
                    service.execute(new GetActivityForm(activity.getId()),
                            new MaskingAsyncMonitor((TargetIndicatorView) view, I18N.CONSTANTS.loading()),
                            new SuccessCallback<ActivityFormDTO>() {

                                @Override
                                public void onSuccess(ActivityFormDTO result) {
                                    activities.put(activity, result);
                                    activityChilds(result, activity, callback);
                                }
                            });
                }
            }
        }

        private void activityChilds(ActivityFormDTO result, ActivityDTO activity, AsyncCallback<List<ModelData>> callback) {
            List<ModelData> childs = Lists.newArrayList();


            for (IndicatorDTO indicator : result.getIndicators()) {

                // yuriy : right now we support only quantity indicators in targets, skip other types
                if (indicator.getType() != FieldTypeClass.QUANTITY) {
                    continue;
                }

                if (indicator.getCategory() != null) {
                    String key = indicator.getCategory() + result.getId();
                    Link indCategoryLink = indicatorCategories.get(key);

                    if (indCategoryLink == null) {
                        indCategoryLink = createIndicatorCategoryLink(indicator, indicatorCategories);
                        indicatorCategories.put(key, indCategoryLink);
                    }
                    childs.add(indCategoryLink);

                    TargetValueDTO targetValueDTO = getTargetValueByIndicatorId(indicator.getId());
                    if (null != targetValueDTO) {
                        addIndicatorLinkChild(indCategoryLink, targetValueDTO);
                    } else {
                        addIndicatorLinkChild(indCategoryLink, createTargetValueModel(indicator));
                    }

                } else {
                    TargetValueDTO targetValueDTO = getTargetValueByIndicatorId(indicator.getId());
                    if (null != targetValueDTO) {
                        childs.add(targetValueDTO);
                    } else {
                        childs.add(createTargetValueModel(indicator));
                    }
                }
            }
            callback.onSuccess(childs);
        }

        public void addIndicatorLinkChild(Link indCategoryLink, TargetValueDTO targetValueDTO) {
            List<TargetValueDTO> list = indicatorLinkChilds.get(indCategoryLink);
            if (list == null) {
                list = Lists.newArrayList();
                indicatorLinkChilds.put(indCategoryLink, list);
            }
            list.add(targetValueDTO);
        }

    }
}
