package org.activityinfo.model.type.subform;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceIdPrefixType;
import org.activityinfo.model.type.period.PredefinedPeriods;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 01/27/2015.
 */
public class SubFormKindRegistry {

    private static final SubFormKindRegistry INSTANCE = new SubFormKindRegistry();

    private static ResourceId USER_DEFINED_ID = ResourceIdPrefixType.SUBFORM.id("_user_defined");

    private final Map<ResourceId, SubFormKind> kinds = Maps.newLinkedHashMap();

    private SubFormKindRegistry() {
        USER_DEFINED_ID = ResourceIdPrefixType.SUBFORM.id("_user_defined");
        register(new PeriodSubFormKind(PredefinedPeriods.YEARLY));
        register(new PeriodSubFormKind(PredefinedPeriods.MONTHLY));
//        register(new PeriodSubFormKind(PredefinedPeriods.WEEKLY));
        register(new PeriodSubFormKind(PredefinedPeriods.DAILY));
        register(userDefinedKind());
    }

    public static ResourceId getUserDefinedId() {
        return USER_DEFINED_ID;
    }

    private SubFormKind userDefinedKind() {
        return new SubFormKind() {
            @Override
            public FormClass getDefinition() {
                FormClass formClass = new FormClass(USER_DEFINED_ID);
                return formClass.setLabel("Select type");
            }
        };
    }

    private void register(SubFormKind kind) {
        kinds.put(kind.getDefinition().getId(), kind);
    }

    public static SubFormKindRegistry get() {
        return INSTANCE;
    }

    public SubFormKind getKind(String id) {
        return getKind(ResourceId.valueOf(id));
    }

    public SubFormKind getKind(ResourceId id) {
        return kinds.get(id);
    }

    public List<SubFormKind> getKinds() {
        return Lists.newArrayList(kinds.values());
    }
}
