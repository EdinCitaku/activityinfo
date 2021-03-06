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
package org.activityinfo.server.digest.geo;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.GetSites;
import org.activityinfo.legacy.shared.command.result.SiteResult;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.legacy.shared.reports.content.BubbleMapMarker;
import org.activityinfo.legacy.shared.reports.content.MapMarker;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.SiteHistory;
import org.activityinfo.server.digest.DigestModel;
import org.activityinfo.server.digest.DigestRenderer;
import org.activityinfo.server.digest.geo.GeoDigestModel.DatabaseModel;
import org.activityinfo.server.util.date.DateCalc;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class GeoDigestRenderer implements DigestRenderer {
    private static final String BUBBLE_COLOR = "67a639";

    private static final Logger LOGGER = Logger.getLogger(GeoDigestRenderer.class.getName());

    private final Provider<EntityManager> entityManager;
    private final DispatcherSync dispatcher;

    @Inject
    public GeoDigestRenderer(Provider<EntityManager> entityManager, DispatcherSync dispatcher) {
        this.entityManager = entityManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public String renderHtml(DigestModel model) throws IOException {
        assert (model instanceof GeoDigestModel);

        StringBuilder html = new StringBuilder();
        html.append("<div id='geo-digest' style='margin-top:20px'>");

        renderHeader(html, (GeoDigestModel) model);

        renderDatabases(html, (GeoDigestModel) model);

        html.append("</div>");
        return html.toString();
    }

    private void renderHeader(StringBuilder html, GeoDigestModel model) {
        html.append("<div class='geo-header'>");
        html.append(I18N.MESSAGES.geoDigestIntro(model.getUserDigest().getDays() * 24));
        html.append("<br/>");
        html.append(I18N.CONSTANTS.digestUnsubscribeConstant());
        // uncomment when AI-709 is ready
//        html.append(I18N.MESSAGES.digestUnsubscribe(model.getUserDigest().getUnsubscribeLink()));
        html.append("</div>");
    }

    private void renderDatabases(StringBuilder html, GeoDigestModel model) throws IOException {
        html.append("<div class='geo-data' style='margin-top:20px'>");

        Collection<DatabaseModel> databases = model.getDatabases();
        for (DatabaseModel database : databases) {
            if (database.isRenderable()) {
                renderDatabase(html, database);
            }
        }

        html.append("</div>");
    }

    private void renderDatabase(StringBuilder html, DatabaseModel databaseModel) throws IOException {
        html.append("<div class='geo-database' style='margin-top:20px'>");
        html.append("<span class='geo-header' style='font-weight:bold; color: #" + BUBBLE_COLOR + ";'>");
        html.append(databaseModel.getName());
        html.append("</span><br>");

        html.append("<img class='geo-graph' width=\"450px\" src=\"");
        html.append(databaseModel.getUrl());
        html.append("\" /><br><br>");

        for (MapMarker marker : databaseModel.getContent().getMarkers()) {
            String label = ((BubbleMapMarker) marker).getLabel();
            html.append("<span class='geo-marker-header' style='color: #" + BUBBLE_COLOR + "; font-weight:bold;'>");
            html.append(label);
            html.append(":</span><br>");

            LOGGER.finest(marker.getSiteIds().size() + " sites for marker " + label + ": " + marker.getSiteIds());
            renderSites(html, databaseModel, marker.getSiteIds());
        }

        if (!databaseModel.getContent().getUnmappedSites().isEmpty()) {
            html.append("<br><span class='geo-unmapped-header' style='color:black; font-weight:bold;'>");
            html.append(I18N.MESSAGES.geoDigestUnmappedSites());
            html.append(":</span><br>");

            LOGGER.finest(databaseModel.getContent().getUnmappedSites().size() + " unmapped sites");
            renderSites(html, databaseModel, databaseModel.getContent().getUnmappedSites());
        }
        html.append("</div>");
    }

    private void renderSites(StringBuilder html, DatabaseModel databaseModel, Collection<Integer> siteIds) {
        if (!siteIds.isEmpty()) {
            for (Integer siteId : siteIds) {
                SiteResult siteResult = dispatcher.execute(GetSites.byId(siteId));
                SiteDTO siteDTO = siteResult.getData().get(0);
                ActivityDTO activityDTO = databaseModel.getModel()
                                                       .getSchemaDTO()
                                                       .getActivityById(siteDTO.getActivityId());

               // Check to see if this activity is visible to the user...
                if(activityDTO != null) {

                    List<SiteHistory> histories = findSiteHistory(siteId, databaseModel.getModel().getUserDigest().getFrom());
                    for (SiteHistory history : histories) {
                        html.append("<span class='geo-site' style='margin-left:10px;'>&bull; ");
                        html.append(I18N.MESSAGES.geoDigestSiteMsg(history.getUser().getEmail(),
                                history.getUser().getName(),
                                activityDTO.getName(),
                                siteDTO.getLocationName()));

                        Date targetDate = databaseModel.getModel().getUserDigest().getDate();
                        Date creationDate = new Date(history.getTimeCreated());
                        if (DateCalc.isOnToday(targetDate, creationDate)) {
                            html.append(I18N.MESSAGES.geoDigestSiteMsgDateToday(creationDate));
                        } else if (DateCalc.isOnYesterday(targetDate, creationDate)) {
                            html.append(I18N.MESSAGES.geoDigestSiteMsgDateYesterday(creationDate));
                        } else {
                            html.append(I18N.MESSAGES.geoDigestSiteMsgDateOther(creationDate));
                        }

                        html.append("</span><br>");
                    }
                }
            }
        }
    }

    /**
     * @param siteId
     * @param from
     * @param from
     * @return the sitehistory edited since the specified timestamp (milliseconds) and linked to the specified database
     * and user. The resulting list is grouped by user, keeping the last created sitehistory entry per user.
     */
    @VisibleForTesting @SuppressWarnings("unchecked") List<SiteHistory> findSiteHistory(Integer siteId, long from) {

        Query query = entityManager.get().createQuery("select distinct h from SiteHistory h " +
                                                      "where h.site.id = :siteId and h.timeCreated >= :from " +
                                                      "order by h.timeCreated");
        query.setParameter("siteId", siteId);
        query.setParameter("from", from);

        List<SiteHistory> list = query.getResultList();

        if (list.isEmpty()) {
            return list;
        }

        Map<Integer, SiteHistory> map = new HashMap<Integer, SiteHistory>();
        for (SiteHistory siteHistory : list) {
            SiteHistory old = map.get(siteHistory.getUser().getId());
            if (old == null || old.getTimeCreated() <= siteHistory.getTimeCreated()) {
                map.put(siteHistory.getUser().getId(), siteHistory);
            }
        }
        return new ArrayList<SiteHistory>(map.values());
    }
}
