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
package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.RenderElement;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.UrlResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.server.report.generator.ReportGenerator;
import org.activityinfo.server.report.renderer.Renderer;
import org.activityinfo.server.report.renderer.RendererFactory;

import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 * @see org.activityinfo.legacy.shared.command.RenderElement
 */
public class RenderElementHandler implements CommandHandler<RenderElement> {

    private static final Logger LOGGER = Logger.getLogger(RenderElementHandler.class.getName());

    private final RendererFactory rendererFactory;
    private final ReportGenerator generator;
    private StorageProvider storageProvider;

    @Inject
    public RenderElementHandler(RendererFactory rendererFactory,
                                ReportGenerator generator,
                                StorageProvider storageProvider) {
        this.rendererFactory = rendererFactory;
        this.generator = generator;
        this.storageProvider = storageProvider;
    }

    @Override
    public CommandResult execute(RenderElement cmd, User user) throws CommandException {

        try {
            Renderer renderer = rendererFactory.get(cmd.getFormat());
            GeneratedResource storage = storageProvider.create(renderer.getMimeType(),
                    cmd.getFilename() + renderer.getFileSuffix());

            LOGGER.fine("Rendering element: " + cmd + "\nURL: " + storage.getDownloadUri());

            try(OutputStream out = storage.openOutputStream()) {
                generator.generateElement(user, cmd.getElement(), new Filter(), new DateRange());
                renderer.render(cmd.getElement(), out);
            }
            return new UrlResult(storage.getDownloadUri());
        } catch (Exception e) {
            throw new RuntimeException("Exception generating export", e);
        }
    }
}
