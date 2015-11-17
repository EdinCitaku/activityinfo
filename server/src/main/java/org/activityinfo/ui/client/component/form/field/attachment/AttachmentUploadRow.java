package org.activityinfo.ui.client.component.form.field.attachment;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.type.attachment.Attachment;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.util.GwtUtil;

import javax.annotation.Nullable;

/**
 * @author yuriyz on 8/12/14.
 */
public class AttachmentUploadRow extends Composite {

    private static final int THUMBNAIL_SIZE = 24;

    public interface ValueChangedCallback {
        void fireValueChanged();
    }

    interface OurUiBinder extends UiBinder<FormPanel, AttachmentUploadRow> {
    }

    private static OurUiBinder ourUiBinder = GWT.create(OurUiBinder.class);

    private final Attachment value;
    private final AttachmentUploadRow.ValueChangedCallback valueChangedCallback;
    private final Uploader uploader;

    private boolean readOnly;
    private HandlerRegistration oldHandler;
    private String servingUrl = null;

    @UiField
    FileUpload fileUpload;
    @UiField
    HTMLPanel loadingContainer;
    @UiField
    ImageElement loadingImage;
    @UiField
    Button downloadButton;
    @UiField
    Button removeButton;
    @UiField
    VerticalPanel hiddenFieldsContainer;
    @UiField
    Button addButton;
    @UiField
    FormPanel formPanel;
    @UiField
    HTMLPanel uploadFailed;
    @UiField
    HTMLPanel thumbnailContainer;

    public AttachmentUploadRow(Attachment value, String fieldId, String resourceId,
                               final FieldWidgetMode fieldWidgetMode, AttachmentUploadRow.ValueChangedCallback valueChangedCallback) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.value = value;
        this.valueChangedCallback = valueChangedCallback;

        this.uploader = new Uploader(formPanel, fileUpload, value, hiddenFieldsContainer, new Uploader.UploadCallback() {
            @Override
            public void onFailure(@Nullable Throwable exception) {
                uploadFailed.setVisible(true);
            }

            @Override
            public void upload() {
                AttachmentUploadRow.this.upload();
            }
        });

        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (fieldWidgetMode == FieldWidgetMode.NORMAL) {
                    uploader.requestUploadUrl();
                } else {
                    Window.alert(I18N.CONSTANTS.uploadIsNotAllowedInDuringDesing());
                }
            }
        });
        downloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.open(servingUrl, "_blank", null);
            }
        });

        if (value.getBlobId() != null) {
            loadingContainer.setVisible(false);
            downloadButton.setVisible(true);
            setThumbnail();
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        fileUpload.setEnabled(!readOnly);
        downloadButton.setEnabled(!readOnly);
        removeButton.setEnabled(!readOnly);
        addButton.setEnabled(!readOnly);
    }

    private void upload() {
        loadingContainer.setVisible(true);
        downloadButton.setVisible(false);
        uploadFailed.setVisible(false);

        if (oldHandler != null) {
            oldHandler.removeHandler();
        }

        oldHandler = formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                // event.getResults is always null because of cross-domain upload
                // we are forced to make additional call to check whether upload is successful

                fetchImageServingUrl();
            }
        });
        formPanel.submit();
    }

    @Deprecated
    private void fetchImageServingUrl() { // todo attachment serving url
        try {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uploader.getBaseUrl() + "/image_url");
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    servingUrl = response.getText();
                    setUploadState();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Log.error("Failed to fetch image serving url. ", exception);
                    setUploadState();
                }
            });
        } catch (RequestException e) {
            Log.error("Failed to send request for fetching serving url. ", e);
            setUploadState();
        }
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(servingUrl);
    }

    private void setUploadState() {
        if (isValid()) {
            loadingContainer.setVisible(false);
            downloadButton.setVisible(true);
            setThumbnail();

            valueChangedCallback.fireValueChanged();
        } else {
            Log.error("Failed to fetch image serving url.");
            uploadFailed.setVisible(true);
        }
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    public Attachment getValue() {
        return value;
    }

    private void setThumbnail() {
        thumbnailContainer.setVisible(false);
        GwtUtil.removeChildren(thumbnailContainer);
        if (Strings.isNullOrEmpty(value.getMimeType())) {
            return;
        }

        if (value.getMimeType().contains("pdf")) {
            appendTumbnailImage("icons.filePdf");
        } else if (value.getMimeType().startsWith("text")) {
            appendTumbnailImage("icons.file");
        }
    }

    private void appendTumbnailImage(String iconClassName) {
        thumbnailContainer.setVisible(true);
        thumbnailContainer.add(new HTML("<span class=\"{" + iconClassName + "}\"/>"));
    }
}
