/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.activityinfo.json;

import java.io.Serializable;

/**
 * Base interface for all Json values.
 */
public interface JsonValue extends Serializable {

    /**
     * Coerces underlying value to boolean according to the rules of Javascript coercion.
     */
    boolean asBoolean();

    /**
     * Coerces the underlying value to a number according to the rules of Javascript coercion.
     * If the value is a JsonNull or a JsonString that cannot be parsed as a number, then
     * Double.NaN will be returned.
     */
    double asNumber();

    int asInt();

    long asLong();


    /**
     * Coerces the underlying value to a String, or {@code null} for Json null values.son
     */
    String asString();

    /**
     * Returns an enumeration representing the fundamental JSON type.
     */
    JsonType getType();


    JsonObject getAsJsonObject();

    JsonArray getAsJsonArray();

    boolean isJsonNull();

    boolean isJsonArray();

    boolean isJsonString();

    boolean isJsonPrimitive();

    boolean isJsonObject();

    boolean isNumber();

    boolean isString();

    boolean isBoolean();


    /**
     * Returns a serialized JSON string representing this value.
     *
     */
    String toJson();

    /**
     * Equivalent of Javascript '==' operator comparison between two values.
     */
    boolean jsEquals(JsonValue value);


    /**
     * If used in a GWT context (dev or prod mode), converts the object to a native JavaScriptObject
     * suitable for passing to JSNI methods. Otherwise, returns the current object in other contexts,
     * such as server-side use.
     */
    Object toNative();


    String toString();



}
