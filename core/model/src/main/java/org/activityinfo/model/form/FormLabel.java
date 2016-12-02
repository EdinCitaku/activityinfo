package org.activityinfo.model.form;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;

/**
 * Created by yuriyz on 4/15/2016.
 */
public class FormLabel extends FormElement {

    private final ResourceId id;
    private String label;
    private boolean visible = true;

    public FormLabel(ResourceId id) {
        this(id, null);
    }

    public FormLabel(ResourceId id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public ResourceId getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public FormLabel setLabel(String label) {
        this.label = label;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public FormLabel setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public JsonElement toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id.asString());
        object.addProperty("label", label);
        object.addProperty("type", "label");
        object.addProperty("visible", visible);
        return object;
    }

    public static FormElement fromJson(JsonObject jsonObject) {
        FormLabel label = new FormLabel(ResourceId.valueOf(jsonObject.get("id").getAsString()));
        label.setLabel(jsonObject.get("label").getAsString());
        if(jsonObject.has("visible")) {
            label.setVisible(jsonObject.get("visible").getAsBoolean());
        }
        return label;
    }

    public enum TextStyle {
        PLAIN("plain", I18N.CONSTANTS.plain(), "", ""),
        BOLD("bold", I18N.CONSTANTS.bold(), "<b>", "</b>");

        private String value;
        private String label;
        private String htmlStartTag;
        private String htmlEndTag;

        TextStyle(String value, String label, String htmlStartTag, String htmlEndTag) {
            this.value = value;
            this.label = label;
            this.htmlStartTag = htmlStartTag;
            this.htmlEndTag = htmlEndTag;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public ResourceId getResourceId() {
            return ResourceId.valueOf(value);
        }

        public static TextStyle fromValue(String textStyle) {
            for (TextStyle style : TextStyle.values()) {
                if (style.getValue().equalsIgnoreCase(textStyle)) {
                    return style;
                }
            }

            return null;
        }

        public String applyStyle(String html) {
            return getHtmlStartTag() + html + getHtmlEndTag();
        }

        public String getHtmlStartTag() {
            return htmlStartTag;
        }

        public String getHtmlEndTag() {
            return htmlEndTag;
        }
    }
}
