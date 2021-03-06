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
package org.activityinfo.ui.client.page.config;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.*;
import org.activityinfo.ui.client.page.config.design.DbEditor;
import org.activityinfo.ui.client.page.config.link.IndicatorLinkPage;
import org.activityinfo.ui.client.page.config.link.IndicatorLinkPlace;

import java.util.Map;

public class ConfigLoader implements PageLoader {

    private final Dispatcher dispatch;
    private Map<PageId, Provider<? extends Page>> pageProviders = Maps.newHashMap();
    private NavigationHandler navigationHandler;

    @Inject
    public ConfigLoader(Dispatcher dispatcher,
                        Provider<ConfigFrameSet> frameSet,
                        Provider<DbConfigPresenter> databaseConfigPage,
                        Provider<DbListPage> databaseListPage,
                        Provider<DbUserEditor> userPage,
                        Provider<DbPartnerEditor> partnerPage,
                        Provider<DbProjectEditor> projectPage,
                        Provider<LockedPeriodsPresenter> lockPage,
                        Provider<DbEditor> designPage,
                        Provider<DbTargetEditor> targetPage,
                        Provider<IndicatorLinkPage> linkPage,
                        NavigationHandler navigationHandler,
                        PageStateSerializer placeSerializer) {

        this.dispatch = dispatcher;
        this.navigationHandler = navigationHandler;

        register(ConfigFrameSet.PAGE_ID, frameSet);
        register(DbConfigPresenter.PAGE_ID, databaseConfigPage);
        register(DbListPresenter.PAGE_ID, databaseListPage);
        register(DbUserEditor.PAGE_ID, userPage);
        register(DbPartnerEditor.PAGE_ID, partnerPage);
        register(DbProjectEditor.PAGE_ID, projectPage);
        register(LockedPeriodsPresenter.PAGE_ID, lockPage);
        register(DbEditor.PAGE_ID, designPage);
        register(DbTargetEditor.PAGE_ID, targetPage);
        register(IndicatorLinkPage.PAGE_ID, linkPage);

        placeSerializer.registerStatelessPlace(DbListPresenter.PAGE_ID, new DbListPageState());
        placeSerializer.registerParser(DbConfigPresenter.PAGE_ID, new DbPageState.Parser(DbConfigPresenter.PAGE_ID));
        placeSerializer.registerParser(DbUserEditor.PAGE_ID, new DbPageState.Parser(DbUserEditor.PAGE_ID));
        placeSerializer.registerParser(DbPartnerEditor.PAGE_ID, new DbPageState.Parser(DbPartnerEditor.PAGE_ID));
        placeSerializer.registerParser(DbProjectEditor.PAGE_ID, new DbPageState.Parser(DbProjectEditor.PAGE_ID));
        placeSerializer.registerParser(LockedPeriodsPresenter.PAGE_ID,
                new DbPageState.Parser(LockedPeriodsPresenter.PAGE_ID));
        placeSerializer.registerParser(DbEditor.PAGE_ID, new DbPageState.Parser(DbEditor.PAGE_ID));
        placeSerializer.registerParser(DbTargetEditor.PAGE_ID, new DbPageState.Parser(DbTargetEditor.PAGE_ID));
        placeSerializer.registerStatelessPlace(IndicatorLinkPage.PAGE_ID, new IndicatorLinkPlace());
    }

    private void register(PageId pageId, Provider<? extends Page> provider) {

        navigationHandler.registerPageLoader(pageId, this);
        pageProviders.put(pageId, provider);
    }

    @Override
    public void load(final PageId pageId, final PageState place, final AsyncCallback<Page> callback) {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess() {

                final Page page = pageProviders.get(pageId).get();

                if (page == null) {
                    callback.onFailure(new Exception("ConfigLoader didn't know how to handle " + place.toString()));
                } else if (page instanceof DbPage) {
                    dispatch.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure(caught);
                        }

                        @Override
                        public void onSuccess(SchemaDTO result) {
                            DbPageState dbPlace = (DbPageState) place;
                            UserDatabaseDTO database = result.getDatabaseById(dbPlace.getDatabaseId());
                            if(database.isSuspended()) {
                                callback.onSuccess(new DbErrorPage(database));
                            } else {
                                ((DbPage) page).go(database);
                                callback.onSuccess(page);
                            }
                        }

                    });
                } else {
                    page.navigate(place);
                    callback.onSuccess(page);
                }
            }
        });

    }
}
