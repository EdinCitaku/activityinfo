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
package org.activityinfo.server.mail;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import freemarker.template.Configuration;
import org.activityinfo.server.DeploymentConfiguration;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Sends mail messages by SMTP using the javax.mail API.
 */
public class SmtpMailSender extends MailSender {
    private static final Logger LOGGER = Logger.getLogger(SmtpMailSender.class.getName());

    private final DeploymentConfiguration configuration;

    @Inject
    public SmtpMailSender(DeploymentConfiguration configuration, Configuration templateCfg) {
        super(templateCfg);
        this.configuration = configuration;
    }

    @Override
    public void send(Message message) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setSubject(message.getSubject(), Charsets.UTF_8.name());
            mimeMessage.addRecipients(RecipientType.TO, toArray(message.getTo()));
            mimeMessage.addRecipients(RecipientType.BCC, toArray(message.getBcc()));
            mimeMessage.setFrom(new InternetAddress(configuration.getProperty("smtp.from",
                    "activityinfo@configure-me.com"), configuration.getProperty("smtp.from.name", "ActivityInfo")));

            if (message.getReplyTo() != null) {
                mimeMessage.setReplyTo(new Address[]{message.getReplyTo()});
            }

            String body;
            if (message.hasHtmlBody()) {
                body = message.getHtmlBody();
                mimeMessage.setDataHandler(new DataHandler(new HTMLDataSource(body)));
            } else {
                body = message.getTextBody();
                mimeMessage.setText(body, Charsets.UTF_8.name());
            }
            LOGGER.finest("message to " + message.getTo() + ":\n" + body);

            if (!message.getAttachments().isEmpty()) {
                Multipart multipart = new MimeMultipart();
                for (MessageAttachment attachment : message.getAttachments()) {
                    MimeBodyPart part = new MimeBodyPart();
                    part.setFileName(attachment.getFilename());

                    DataSource src = new ByteArrayDataSource(attachment.getContent(), attachment.getContentType());
                    part.setDataHandler(new DataHandler(src));
                    multipart.addBodyPart(part);
                }
                mimeMessage.setContent(multipart);
            }
            mimeMessage.saveChanges();

            Transport.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Address[] toArray(List<InternetAddress> to) {
        return to.toArray(new Address[to.size()]);
    }

    static class HTMLDataSource implements DataSource {
        private String html;

        public HTMLDataSource(String htmlString) {
            // transform non-ascii characters into entities to avoid
            // encoding issues

        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (html == null) {
                throw new IOException("Null HTML");
            }
            return new ByteArrayInputStream(html.getBytes());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        @Override
        public String getContentType() {
            return "text/html; charset=UTF-8";
        }

        @Override
        public String getName() {
            return "text/html dataSource";
        }
    }
}
