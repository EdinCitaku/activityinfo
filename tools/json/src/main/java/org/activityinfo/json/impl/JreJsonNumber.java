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
package org.activityinfo.json.impl;

import org.activityinfo.json.JsonType;
import org.activityinfo.json.JsonValue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Server-side implementation of JsonNumber.
 */
public class JreJsonNumber extends JreJsonValue  {

    private static final long serialVersionUID = 1L;

    private transient double number;

    public JreJsonNumber(double number) {
        this.number = number;
    }

    @Override
    public boolean asBoolean() {
        return Double.isNaN(getNumber()) || Math.abs(getNumber()) == 0.0 ? false : true;
    }

    @Override
    public double asNumber() {
        return getNumber();
    }


    @Override
    public String asString() {
        return toJson();
    }

    @Override
    public boolean isJsonPrimitive() {
        return true;
    }

    public double getNumber() {
        return number;
    }


    public Object getObject() {
        return getNumber();
    }

    public JsonType getType() {
        return JsonType.NUMBER;
    }

    @Override
    public boolean jsEquals(JsonValue value) {
        return getObject().equals(((JreJsonValue) value).getObject());
    }

    @Override
    public void traverse(JsonVisitor visitor, JsonContext ctx) {
        visitor.visit(getNumber(), ctx);
    }

    public String toJson() {
        if (Double.isInfinite(number) || Double.isNaN(number)) {
            return "null";
        }
        String toReturn = String.valueOf(number);
        if (toReturn.endsWith(".0")) {
            toReturn = toReturn.substring(0, toReturn.length() - 2);
        }
        return toReturn;
    }

    @Override
    public long asLong() {
        return Long.parseLong(asString());
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        JreJsonNumber instance = parseJson(stream);
        this.number = instance.number;
    }

    @com.google.gwt.core.shared.GwtIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(toJson());
    }
}