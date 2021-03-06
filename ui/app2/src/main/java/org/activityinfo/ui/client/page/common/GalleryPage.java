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
package org.activityinfo.ui.client.page.common;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.inject.Inject;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.NavigationEvent;
import org.activityinfo.ui.client.page.NavigationHandler;
import org.activityinfo.ui.client.page.PageState;

/**
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class GalleryPage extends LayoutContainer implements GalleryView {

    private ListStore<GalleryModel> store;
    private Html heading;
    private Html introPara;
    private Html permissionsInfo;

    public static class GalleryModel extends BaseModelData {

        private PageState place;

        public GalleryModel(String name, String desc, String path, PageState place) {
            set("name", name);
            set("path", path);
            set("description", desc);

            this.place = place;
        }

        public PageState getPlace() {
            return place;
        }
    }

    @Inject
    public GalleryPage(final EventBus eventBus) {

        this.setStyleName(ApplicationBundle.INSTANCE.styles().gallery());
        this.setScrollMode(Style.Scroll.AUTOY);
        this.setStyleAttribute("background", "white");

        setLayout(new FlowLayout());

        heading = new Html();
        heading.setTagName("h3");
        heading.setStyleAttribute("margin-bottom", "2px");
        add(heading);

        introPara = new Html();
        introPara.setTagName("p");
        introPara.setStyleName("gallery-intro");
        introPara.setStyleAttribute("margin-left", "15px");
        introPara.setStyleAttribute("color", "grey");
        introPara.setStyleAttribute("font-size", "80%");
        add(introPara);

        Html space = new Html(" ");
        space.setTagName("p");
        add(space); // space

        permissionsInfo = new Html();
        permissionsInfo.setTagName("p");
        permissionsInfo.setStyleAttribute("margin-left", "15px");
        permissionsInfo.setStyleAttribute("margin-top", "15px");

        add(permissionsInfo);


        store = new ListStore<GalleryModel>();

        ListView<GalleryModel> view = new ListView<GalleryModel>();
        view.setRenderer(new GalleryRenderer<GalleryModel>("image/thumbs/"));
        view.setBorders(false);
        view.setStore(store);
        view.setItemSelector("dd");
        view.setOverStyle(ApplicationBundle.INSTANCE.styles().over());

        view.addListener(Events.Select, new Listener<ListViewEvent<GalleryModel>>() {

            @Override
            public void handleEvent(ListViewEvent<GalleryModel> event) {
                eventBus.fireEvent(new NavigationEvent(NavigationHandler.NAVIGATION_REQUESTED,
                        event.getModel().getPlace()));
            }
        });
        add(view);
    }

    @Override
    public void setHeading(String text) {
        heading.setText(text);
    }

    @Override
    public void setIntro(String text) {
        introPara.setText(text);
    }

    @Override
    public void setPermissionsInfo(String text) {
        permissionsInfo.setText(text);
    }

    @Override
    public void add(String name, String desc, String path, PageState place) {
        store.add(new GalleryModel(name, desc, path, place));
    }

    public ListStore<GalleryModel> getStore() {
        return store;
    }

}
