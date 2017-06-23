package org.activityinfo.model.query;

import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.resource.ResourceId;

import static org.activityinfo.json.Json.createObject;

public class RowSource {

    private ResourceId rootFormId;

    public RowSource() {
    }

    public RowSource(ResourceId rootFormId) {
        this.rootFormId = rootFormId;
    }

    public ResourceId getRootFormId() {
        return rootFormId;
    }

    public RowSource setRootFormId(ResourceId rootFormId) {
        this.rootFormId = rootFormId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RowSource rowSource = (RowSource) o;

        if (rootFormId != null ? !rootFormId.equals(rowSource.rootFormId) : rowSource.rootFormId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return rootFormId != null ? rootFormId.hashCode() : 0;
    }


    public JsonValue toJsonElement() {
        JsonObject object = createObject();
        object.put("rootFormId", rootFormId.asString());

        return object;
    }


    public static RowSource fromJson(org.activityinfo.json.JsonObject object) {
        RowSource source = new RowSource();
        source.setRootFormId(ResourceId.valueOf(object.get("rootFormId").asString()));
        return source;
    }

}
