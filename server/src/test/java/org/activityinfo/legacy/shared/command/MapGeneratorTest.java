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
package org.activityinfo.legacy.shared.command;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.legacy.shared.reports.content.BubbleMapMarker;
import org.activityinfo.legacy.shared.reports.content.IconMapMarker;
import org.activityinfo.legacy.shared.reports.content.MapContent;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.legacy.shared.reports.model.clustering.AdministrativeLevelClustering;
import org.activityinfo.legacy.shared.reports.model.layers.BubbleMapLayer;
import org.activityinfo.legacy.shared.reports.model.layers.IconMapLayer;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.report.ReportModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@Modules(ReportModule.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class MapGeneratorTest extends CommandTestCase2 {

    @Test
    public void adminMapIcon() {

        AdministrativeLevelClustering clustering = new AdministrativeLevelClustering();
        clustering.getAdminLevels().add(1);

        IconMapLayer layer = new IconMapLayer();
        layer.setClustering(clustering);
        layer.getIndicatorIds().add(1);

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Site, 3);

        MapReportElement map = new MapReportElement();
        map.addLayer(layer);
        map.setFilter(filter);

        MapContent result = (MapContent) execute(new GenerateElement(map));

        System.out.println(result.getMarkers());

        IconMapMarker marker = (IconMapMarker) result.getMarkers().get(0);
        assertThat(marker.getSiteIds().size(), equalTo(1));
        assertThat(marker.getSiteIds().get(0), equalTo(3));

        System.out.println(marker.getTitle());

        // assertThat(marker., equalTo(10000d));
        assertThat(result.getUnmappedSites().size(), equalTo(0));
    }

    @Test
    public void adminMapBubbles() {

        AdministrativeLevelClustering clustering = new AdministrativeLevelClustering();
        clustering.getAdminLevels().add(1);

        BubbleMapLayer layer = new BubbleMapLayer();
        layer.setClustering(clustering);
        layer.addIndicator(1);

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Site, 3);

        MapReportElement map = new MapReportElement();
        map.addLayer(layer);
        map.setFilter(filter);

        MapContent result = (MapContent) execute(new GenerateElement(map));

        System.out.println(result.getMarkers());

        BubbleMapMarker marker = (BubbleMapMarker) result.getMarkers().get(0);
        assertThat(marker.getSiteIds().size(), equalTo(1));
        assertThat(marker.getSiteIds().get(0), equalTo(3));

        assertThat(marker.getValue(), equalTo(10000d));

        assertThat(result.getUnmappedSites().size(), equalTo(0));

        System.out.println(marker.getTitle());

    }
}
