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
package org.activityinfo.io.xlsform;

import com.google.common.base.Preconditions;
import org.activityinfo.io.xform.xpath.XSymbolException;
import org.activityinfo.io.xform.xpath.XSymbolHandler;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XlsSymbolHandler implements XSymbolHandler {

    private Map<String,String> symbolMap;

    public XlsSymbolHandler(List<FormField> fields) {
        symbolMap = new HashMap<>();
        for (FormField field : fields) {
            if (field.getType() instanceof EnumType) {
                addEnumItems((EnumType) field.getType());
            }
            if (field.getCode() != null) {
                symbolMap.put(field.getId().asString(), fieldRef(field.getCode()));
            } else {
                symbolMap.put(field.getId().asString(), fieldRef(field.getId().asString()));
            }
        }
    }

    private void addEnumItems(EnumType enumType) {
        for(EnumItem item : enumType.getValues()) {
            symbolMap.put(item.getId().asString(), quote(item.getId().asString()));
        }
    }

    private String fieldRef(String field) {
        return "${" + field + "}";
    }

    private String quote(String value) {
        return "'" + value + "'";
    }

    @Override
    public String resolveSymbol(String symbol) throws XSymbolException {
        Preconditions.checkArgument(symbol != null, "Symbol cannot be null.");
        String resolved = symbolMap.get(symbol);
        if (resolved == null) {
            throw new XSymbolException(symbol);
        }
        return resolved;
    }

}
