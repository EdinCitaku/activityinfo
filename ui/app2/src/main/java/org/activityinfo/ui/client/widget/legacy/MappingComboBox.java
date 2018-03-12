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
package org.activityinfo.ui.client.widget.legacy;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.activityinfo.i18n.shared.I18N;

/**
 * ComboBox that wraps primitive values (integers, Strings, etc) with labels.
 * <p/>
 * This is a common case where we want to present a drop down list to the user
 * so they can choose from a list of codes. We could use
 * {@link com.extjs.gxt.ui.client.widget.form.SimpleComboBox}, but then the user
 * sees the full
 *
 * @param <T> the underlying (boxed) primitive type
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MappingComboBox<T> extends ComboBox<MappingComboBox.Wrapper<T>> {

    public static final String VALUE_PROPERTY = "value";
    public static final String LABEL_PROPERTY = "label";

    public static class Wrapper<T> extends BaseModelData {
        public Wrapper(T value, String label) {
            set(VALUE_PROPERTY, value);
            set(LABEL_PROPERTY, label);
        }

        public T getWrappedValue() {
            return get(VALUE_PROPERTY);
        }

        @Override
        public int hashCode() {
            Object value = get(VALUE_PROPERTY);
            return value == null ? 0 : value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof Wrapper) {
                return false;
            }
            Wrapper otherWrapper = (Wrapper) obj;
            Object otherValue = otherWrapper.get(VALUE_PROPERTY);
            Object value = get(VALUE_PROPERTY);

            if (value == null) {
                return otherValue != null;
            }
            return !value.equals(otherValue);
        }

        public String getLabel() {
            return get(LABEL_PROPERTY);
        }
    }

    private ListStore<Wrapper<T>> myStore;
    private boolean isNoneAdded = false;

    public MappingComboBox() {
        super();
        myStore = new ListStore<>();
        setStore(myStore);
        setValueField(VALUE_PROPERTY);
        setDisplayField(LABEL_PROPERTY);
        setEditable(true);
        setForceSelection(true);
        setTypeAhead(true);
        this.setMinChars(0);
        setTriggerAction(TriggerAction.ALL);
    }

    public void add(T value, String label) {
        myStore.add(new Wrapper(value, label));
    }

    public Wrapper wrap(T value) {
        if (value == null) {
            if (isNoneAdded) {
                return new Wrapper(null, I18N.CONSTANTS.none());
            } else {
                return null;
            }
        } else {
            return myStore.findModel(VALUE_PROPERTY, value);
        }
    }

    public void addNone() {
        add(null, I18N.CONSTANTS.none());
        isNoneAdded = true;
    }

    public boolean isNoneAdded() {
        return isNoneAdded;
    }

    public void setOriginalMappedValue(T value) {
        setOriginalValue(wrap(value));
    }

    public void setMappedValue(T value) {
        setValue(wrap(value));
    }

    public String getValueLabel() {
        if (getValue() == null) {
            return null;
        } else {
            return getValue().getLabel();
        }
    }

    public String getValueLabel(T value) {
        Wrapper wrapper = store.findModel(VALUE_PROPERTY, value);
        return wrapper == null ? null : wrapper.getLabel();
    }

    /**
     * @return the underlying primitive type
     */
    public T getMappedValue() {
        if (getValue() == null) {
            return null;
        }

        return getValue().getWrappedValue();
    }
}
