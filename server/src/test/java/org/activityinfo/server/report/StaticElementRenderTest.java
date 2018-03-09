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
package org.activityinfo.server.report;

import org.activityinfo.legacy.shared.reports.content.FilterDescription;
import org.activityinfo.legacy.shared.reports.content.ReportContent;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;

public class StaticElementRenderTest {

    public static final String MAP_ICON_PATH = "";
    private Report report;

    @Before
    public void setup() throws JAXBException {
        report = Reports.parseXml(getClass(), "static.xml");
        report.setContent(new ReportContent());
        report.getContent().setFilterDescriptions(new ArrayList<FilterDescription>());
    }

    @Test
    public void testPdfRender() throws JAXBException, IOException {
        Reports.toPdf(getClass(), report);
    }

    @Test
    public void testRtfRender() throws JAXBException, IOException {
        Reports.toRtf(getClass(), report);
    }

    @Test
    public void testHtmlRender() throws JAXBException, IOException {
        Reports.toHtml(getClass(), report);
    }

}
