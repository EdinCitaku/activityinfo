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
package org.activityinfo.server.report.renderer.itext;

import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import org.activityinfo.legacy.shared.reports.model.TextReportElement;

/**
 * Render {@link TextReportElement} for iText documents
 */
public class ItextTextRenderer implements ItextRenderer<TextReportElement> {

    @Override
    public void render(DocWriter writer, Document doc, TextReportElement element) throws DocumentException {

        doc.add(ThemeHelper.elementTitle(element.getTitle()));
        if (element.getText() != null) {
            doc.add(new Paragraph(element.getText()));
        }
    }
}
