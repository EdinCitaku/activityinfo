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
package org.activityinfo.test.webdriver;

import javax.annotation.Nonnull;

public final class OperatingSystem {

    
    public static final OperatingSystem WINDOWS_8 = OperatingSystemType.WINDOWS.version("8");
    public static final OperatingSystem WINDOWS_7 = OperatingSystemType.WINDOWS.version("7");
    public static final OperatingSystem WINDOWS_XP = OperatingSystemType.WINDOWS.version("XP");
    
    public static final OperatingSystem SNOW_LEOPARD = OperatingSystemType.OSX.version("10.6");
    public static final OperatingSystem MOUNTAIN_LION = OperatingSystemType.OSX.version("10.8");
    public static final OperatingSystem MAVERICK = OperatingSystemType.OSX.version("10.9");
    public static final OperatingSystem YOSEMITE = OperatingSystemType.OSX.version("10.10");


    @Nonnull
    private final OperatingSystemType type;
    
    @Nonnull
    private final Version version;

    public OperatingSystem(OperatingSystemType type, Version version) {
        this.type = type;
        this.version = version;
    }

    public OperatingSystem(OperatingSystemType type, String version) {
        this(type, new Version(version));
    }

    public OperatingSystemType getType() {
        return type;
    }

    public Version getVersion() {
        return version;
    }

    public static OperatingSystem host() {
        return new OperatingSystem(OperatingSystemType.host(), System.getProperty("os.version", ""));
    }
    
    @Override
    public String toString() {
        if(version.isEmpty()) {
            return type.name();
        } else {
            return type.name() + " " + version;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperatingSystem that = (OperatingSystem) o;

        if (type != that.type) return false;
        if (!version.equals(that.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
