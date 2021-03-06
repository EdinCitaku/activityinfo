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
package org.activityinfo.legacy.shared;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.legacy.shared.type.IndicatorValueFormatter;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.attachment.AttachmentValue;

import java.util.List;
import java.util.Map.Entry;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.htmlEscape;

public class SiteRenderer {

    private final IndicatorValueFormatter indicatorValueFormatter;

    public SiteRenderer(IndicatorValueFormatter indicatorValueFormatter) {
        super();
        this.indicatorValueFormatter = indicatorValueFormatter;
    }

    public String renderLocation(SiteDTO site, ActivityFormDTO activity) {
        StringBuilder html = new StringBuilder();

        html.append("<table cellspacing='0'>");
        if (!activity.getLocationType().isAdminLevel()) {
            html.append("<tr><td>");
            html.append(SafeHtmlUtils.htmlEscape(activity.getLocationType().getName())).append(": ");
            html.append("</td><td>");
            html.append(SafeHtmlUtils.htmlEscape(site.getLocationName()));
            if (!Strings.isNullOrEmpty(site.getLocationAxe())) {
                html.append("<br>").append(SafeHtmlUtils.htmlEscape(site.getLocationAxe()));
            }
            html.append("</td></tr>");
        }
        for (AdminLevelDTO level : activity.getAdminLevels()) {
            AdminEntityDTO entity = site.getAdminEntity(level.getId());
            if (entity != null) {
                html.append("<tr><td>");
                html.append(SafeHtmlUtils.htmlEscape(level.getName())).append(":</td><td>");
                html.append(SafeHtmlUtils.htmlEscape(entity.getName()));
                html.append("</td></tr>");
            }
        }

        html.append("</table>");
        return html.toString();
    }

    public String renderSite(SiteDTO site, ActivityFormDTO activity, boolean renderComments) {
        StringBuilder html = new StringBuilder();

        if (site.getPartnerName() != null) {
            html.append(renderField("partner", site.getPartnerName(), I18N.CONSTANTS.partner()));
        }
        if (site.getProjectName() != null) {
            html.append(renderField("project", site.getProjectName(), I18N.CONSTANTS.project()));
        }
        if (renderComments && site.getComments() != null) {
            html.append(renderField("comments", site.getComments(), I18N.CONSTANTS.comments()));
        }

        renderAttributes(html, site, activity);

        if (activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            html.append(renderIndicators(site, activity));
        }

        return html.toString();
    }

    private String renderField(String fieldName, String fieldValue, String localizedName) {
        StringBuilder html = new StringBuilder();
        String valueHtml = SafeHtmlUtils.htmlEscape(fieldValue);
        valueHtml = valueHtml.replace("\n", "<br/>");

        html.append("<p class='" + fieldName + "'><span class='groupName'>");
        html.append(localizedName);
        html.append(":</span> ");
        html.append(valueHtml);
        html.append("</p>");

        return html.toString();
    }

    private String renderIndicators(SiteDTO site, ActivityFormDTO activity) {
        StringBuilder html = new StringBuilder();
        html.append("<br/><p><span class='groupName'>");
        html.append(I18N.CONSTANTS.indicators());
        html.append(":</p>");
        html.append("<table class='indicatorTable' cellspacing='0'>");
        boolean hasContent = false;
        for (IndicatorGroup group : activity.groupIndicators(true)) {
            boolean groupHasContent = renderIndicatorGroup(html, group, site);
            hasContent = hasContent || groupHasContent;
        }
        html.append("</table>");

        return hasContent ? html.toString() : "";
    }

    private boolean hasValue(Object value, FieldTypeClass type) {
        if (value != null) {
            if (type == AttachmentType.TYPE_CLASS) {
                AttachmentValue attachment = AttachmentValue.fromJsonSilently((String) value);
                return attachment != null && attachment.hasValues();
            }
            return true;
        }
        return false;
    }

    private boolean renderIndicatorGroup(StringBuilder html,
                                         IndicatorGroup group,
                                         SiteDTO site) {
        StringBuilder groupHtml = new StringBuilder();
        boolean empty = true;

        if (group.getName() != null) {
            groupHtml.append("<tr><td class='indicatorGroupHeading'>")
                     .append(htmlEscape(group.getName()))
                     .append("</td><td>&nbsp;</td></tr>");
        }
        for (IndicatorDTO indicator : group.getIndicators()) {

            Object value = getIndicatorValue(site, indicator);

            if (hasValue(value, indicator.getType())) {

                groupHtml.append("<tr><td class='indicatorHeading");
                if (group.getName() != null) {
                    groupHtml.append(" indicatorGroupChild");
                }

                groupHtml.append("'>")
                         .append(htmlEscape(indicator.getName()))
                         .append("</td>");

                if(indicator.getType() == FieldTypeClass.QUANTITY) {
                    groupHtml
                         .append("<td class='indicatorQuantity'>")
                         .append(formatValue(indicator, value))
                         .append("</td><td class='indicatorUnits'>")
                         .append(SafeHtmlUtils.htmlEscape(Strings.nullToEmpty(indicator.getUnits())))
                         .append("</td>");
                } else {
                    groupHtml
                         .append("<td colspan='2' class='indicatorText'>")
                         .append(formatValue(indicator, value))
                         .append("</td>");
                }
                groupHtml.append("</tr>");
                empty = false;
            }
        }
        if (!empty) {
            html.append(groupHtml.toString());
            return true;
        } else {
            return false;
        }
    }

    private Object getIndicatorValue(SiteDTO site, IndicatorDTO indicator) {
        if (indicator.getType() == FieldTypeClass.QUANTITY) {
            if (indicator.getAggregation() == IndicatorDTO.AGGREGATE_SITE_COUNT) {
                return 1.0;
            } else if(indicator.getAggregation() == IndicatorDTO.AGGREGATE_SUM) {
                Double value = site.getIndicatorDoubleValue(indicator);
                if(value == null) {
                    return null;
                } else {
                    return value;
                }
            } else {
                return site.getIndicatorValue(indicator);
            }
        } else {
            return site.getIndicatorValue(indicator);
        }
    }

    protected String formatValue(IndicatorDTO indicator, Object value) {
        if(indicator.getType() == FieldTypeClass.QUANTITY) {
            if (value instanceof Double) {
                return indicatorValueFormatter.format((Double) value);
            }
        } else if(indicator.getType() == FieldTypeClass.FREE_TEXT) {
            if (value instanceof String) {
                return htmlEscape((String) value);
            }
        } else if(indicator.getType() == FieldTypeClass.NARRATIVE) {
            if (value instanceof String) {
                SafeHtmlBuilder html = new SafeHtmlBuilder();
                html.appendEscapedLines((String)value);
                return html.toSafeHtml().asString();
            }
        }
        return "-";
    }

    protected void renderAttributes(StringBuilder html, SiteDTO site, ActivityFormDTO activity) {
        if (site.hasAttributeDisplayMap()) {
            for (Entry<String, List<String>> entry : site.getAttributeDisplayMap().entrySet()) {
                renderAttribute(html, entry.getKey(), entry.getValue());
            }
        } else {
            for (AttributeGroupDTO group : activity.getAttributeGroups()) {
                renderAttribute(html, group, site);
            }
        }
    }

    protected void renderAttribute(StringBuilder html, String groupName, List<String> attributeNames) {
        int count = 0;
        for (String attributeName : attributeNames) {
            if (count == 0) {
                html.append("<p class='attribute'><span class='groupName'>");
                html.append(SafeHtmlUtils.htmlEscape(groupName));
                html.append(": </span><span class='attValues'>");
            } else {
                html.append(", ");
            }
            html.append(SafeHtmlUtils.htmlEscape(attributeName));
            count++;
        }
        html.append("</span></p>");
    }

    protected void renderAttribute(StringBuilder html, AttributeGroupDTO group, SiteDTO site) {
        int count = 0;
        if (group != null) {
            for (AttributeDTO attribute : group.getAttributes()) {
                boolean value = site.getAttributeValue(attribute.getId());
                if (value) {
                    if (count == 0) {
                        html.append("<p class='attribute'><span class='groupName'>");
                        html.append(SafeHtmlUtils.htmlEscape(group.getName()));
                        html.append(": </span><span class='attValues'>");
                    } else {
                        html.append(", ");
                    }
                    html.append(SafeHtmlUtils.htmlEscape(attribute.getName()));
                    count++;
                }
            }
            if (count != 0) {
                html.append("</span></p>");
            }
        }
    }
}
