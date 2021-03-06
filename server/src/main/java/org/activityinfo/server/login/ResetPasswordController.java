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
package org.activityinfo.server.login;

import com.google.inject.Inject;
import com.sun.jersey.api.view.Viewable;
import org.activityinfo.server.authentication.SecureTokenGenerator;
import org.activityinfo.server.database.hibernate.dao.Transactional;
import org.activityinfo.server.database.hibernate.dao.UserDAO;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.login.model.ResetPasswordPageModel;
import org.activityinfo.server.mail.MailSender;
import org.activityinfo.server.mail.ResetPasswordMessage;

import javax.inject.Provider;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path(ResetPasswordController.ENDPOINT)
public class ResetPasswordController {
    public static final String ENDPOINT = "/loginProblem";

    private static final Logger LOGGER = Logger.getLogger(ResetPasswordController.class.getName());

    @Inject
    private MailSender mailer;

    @Inject
    private Provider<UserDAO> userDAO;

    @GET 
    @Produces(MediaType.TEXT_HTML)
    public Viewable getPage(@Context HttpServletRequest req) throws ServletException, IOException {
        return new ResetPasswordPageModel().asViewable();
    }

    @POST 
    @Produces(MediaType.TEXT_HTML) 
    @Transactional
    public Viewable resetPassword(@FormParam("email") String email) {
        try {
            User user = userDAO.get().findUserByEmail(email);
            user.setChangePasswordKey(SecureTokenGenerator.generate());
            user.setDateChangePasswordKeyIssued(new Date());

            mailer.send(new ResetPasswordMessage(user));

            ResetPasswordPageModel model = new ResetPasswordPageModel();
            model.setEmailSent(true);

            return model.asViewable();

        } catch (NoResultException e) {
            ResetPasswordPageModel model = new ResetPasswordPageModel();
            model.setLoginError(true);

            return model.asViewable();

        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, "Failed to send password reset email", e);

            ResetPasswordPageModel model = new ResetPasswordPageModel();
            model.setEmailError(true);

            return model.asViewable();
        }
    }
}
