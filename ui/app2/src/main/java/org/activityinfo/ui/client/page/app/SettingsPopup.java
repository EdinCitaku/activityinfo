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
package org.activityinfo.ui.client.page.app;

import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.ui.client.ClientContext;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.SessionUtil;
import org.activityinfo.ui.client.inject.ClientSideAuthProvider;
import org.activityinfo.ui.client.local.LocalController;
import org.activityinfo.ui.client.local.LocalStateChangeEvent;
import org.activityinfo.ui.client.local.LocalStateChangeEvent.State;
import org.activityinfo.ui.client.local.UnsupportedDialog;
import org.activityinfo.ui.client.local.capability.LocalCapabilityProfile;
import org.activityinfo.ui.client.local.sync.SyncCompleteEvent;
import org.activityinfo.ui.client.local.sync.SyncStatusEvent;
import org.activityinfo.ui.client.page.NavigationEvent;
import org.activityinfo.ui.client.page.NavigationHandler;

import java.util.Date;

public class SettingsPopup extends PopupPanel {

    public static final int WIDTH = 250;

    private static SettingsPopupUiBinder uiBinder = GWT.create(SettingsPopupUiBinder.class);

    interface SettingsPopupUiBinder extends UiBinder<Widget, SettingsPopup> {
    }

    @UiField SpanElement versionLabel;

    @UiField
    DivElement emailLabel;

    @UiField SpanElement versionStatus;

    @UiField SpanElement appCacheStatus;

    @UiField Label refreshLink;

    @UiField Label logoutLink;

    @UiField Label userProfileLink;

    @UiField Label offlineInstallLabel;

    @UiField Label offlineStatusLabel;

    @UiField Label lastSyncedLabel;

    Date lastSyncTime = null;

    @UiField DivElement syncRow;

    @UiField Label syncNowLabel;

    private boolean syncing = false;

    private EventBus eventBus;

    private LocalStateChangeEvent.State state = State.CHECKING;

    private LocalController offlineController;

    private LocalCapabilityProfile offlineCapabilityProfile = GWT.create(LocalCapabilityProfile.class);

    public SettingsPopup(EventBus eventBus, LocalController offlineController) {
        this.eventBus = eventBus;
        this.offlineController = offlineController;

        setWidget(uiBinder.createAndBindUi(this));
        setAutoHideEnabled(true);
        setAutoHideOnHistoryEventsEnabled(true);
        setWidth(WIDTH + "px");

        versionLabel.setInnerText(ClientContext.getVersion());
        emailLabel.setInnerText(I18N.MESSAGES.loggedInAs(new ClientSideAuthProvider().get().getEmail()));

        syncRow.getStyle().setDisplay(Display.NONE);

        eventBus.addListener(SyncStatusEvent.TYPE, new Listener<SyncStatusEvent>() {

            @Override
            public void handleEvent(SyncStatusEvent be) {
                offlineStatusLabel.setText(be.getTask() + " " + ((int) (be.getPercentComplete())) + "%");
                syncing = true;
            }
        });
        eventBus.addListener(SyncCompleteEvent.TYPE, new Listener<SyncCompleteEvent>() {

            @Override
            public void handleEvent(SyncCompleteEvent event) {
                syncing = false;
                syncRow.getStyle().setDisplay(Display.BLOCK);
                lastSyncTime = event.getTime();
                updateLastSyncLabel();
            }

        });
        eventBus.addListener(LocalStateChangeEvent.TYPE, new Listener<LocalStateChangeEvent>() {

            @Override
            public void handleEvent(LocalStateChangeEvent be) {
                onOfflineStatusChange(be.getState());
            }
        });
        onOfflineStatusChange(offlineController.getState());
    }

    @Override
    public void show() {
        sendUpdateRequest();
        updateLastSyncLabel();
        if (!syncing) {
            offlineStatusLabel.setText("");
        }
        super.show();
    }

    /**
     * Queries the server for the latest deployed version.
     */
    private void sendUpdateRequest() {
        versionStatus.setInnerText(I18N.CONSTANTS.versionChecking());
        appCacheStatus.setInnerText("");
        refreshLink.setVisible(false);
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, "/commit.id");
        request.setCallback(new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() != 200) {
                    versionStatus.setInnerText(I18N.CONSTANTS.versionConnectionProblem());

                } else if (response.getText().startsWith(ClientContext.getCommitId())) {
                    versionStatus.setInnerText(I18N.CONSTANTS.versionLatest());

                } else {
                    versionStatus.setInnerText(I18N.CONSTANTS.versionUpdateAvailable());
                    refreshLink.setVisible(true);
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                versionStatus.setInnerText(I18N.CONSTANTS.versionConnectionProblem());
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            versionStatus.setInnerText(I18N.CONSTANTS.versionConnectionProblem());
            Log.debug("Problem fetching latest version", e);
        }
    }

    @UiHandler("refreshLink")
    public void onRefreshLink(ClickEvent e) {

    }

    @UiHandler("logoutLink")
    public void onLogoutClicked(ClickEvent e) {
        SessionUtil.logout();
    }

    @UiHandler("userProfileLink")
    public void onUserProfileLink(ClickEvent e) {
        eventBus.fireEvent(new NavigationEvent(NavigationHandler.NAVIGATION_REQUESTED, new UserProfilePage.State()));
        super.hide();
    }

    @UiHandler("offlineInstallLabel")
    public void onOfflineInstallClicked(ClickEvent e) {
        switch (state) {
            case UNINSTALLED:
                if (offlineCapabilityProfile.isOfflineModeSupported()) {
                    offlineController.install();
                } else {
                    UnsupportedDialog.show();
                }
                break;
        }
    }

    @UiHandler("syncNowLabel")
    public void onSyncNowClicked(ClickEvent e) {
        offlineController.synchronize();
    }

    private void onOfflineStatusChange(State state) {
        this.state = state;
        offlineStatusLabel.setText("");
        switch (state) {
            case UNINSTALLED:
                offlineInstallLabel.setVisible(true);
                offlineInstallLabel.setText(I18N.CONSTANTS.installOffline());
                syncRow.getStyle().setDisplay(Display.NONE);
                break;
            case INSTALLING:
                offlineInstallLabel.setVisible(true);
                offlineInstallLabel.setText("");
                syncRow.getStyle().setDisplay(Display.NONE);
                break;
            case INSTALLED:
                offlineInstallLabel.setVisible(false);
                syncRow.getStyle().setDisplay(Display.BLOCK);
                lastSyncTime = offlineController.getLastSyncTime();
                updateLastSyncLabel();
        }
    }

    private void updateLastSyncLabel() {
        if (lastSyncTime != null) {
            lastSyncedLabel.setText(I18N.MESSAGES.lastSynced(formatLastSyncTime(lastSyncTime)));
        }
    }

    private String formatLastSyncTime(Date time) {
        long now = new Date().getTime();
        long last = time.getTime();

        long secondsAgo = (now - last) / 1000;
        if (secondsAgo <= 60) {
            return I18N.CONSTANTS.relativeTimeMinAgo();
        }
        long minutesAgo = secondsAgo / 60;
        if (minutesAgo <= 60) {
            return I18N.MESSAGES.minutesAgo((int) minutesAgo);
        }
        long hoursAgo = minutesAgo / 60;
        if (hoursAgo <= 48) {
            return I18N.MESSAGES.hoursAgo((int) hoursAgo);
        }
        long daysAgo = hoursAgo / 24;
        return I18N.MESSAGES.daysAgo((int) daysAgo);
    }
}
