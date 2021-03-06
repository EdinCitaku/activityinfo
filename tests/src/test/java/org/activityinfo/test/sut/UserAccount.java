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
package org.activityinfo.test.sut;

/**
 * A user account in ActivityInfo
 */
public class UserAccount {
    private String email;
    private String password;

    public UserAccount(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    /**
     *
     * @return the user portion of the email address. 
     */
    public String nameFromEmail() {
        int at = email.indexOf('@');
        if(at == -1) {
            throw new IllegalStateException("Invalid email address: " + email);
        }
        return email.substring(0, at);
    }
    
    public String domainFromEmail() {
        int at = email.indexOf('@');
        if(at == -1) {
            throw new IllegalStateException("Invalid email address: " + email);
        }
        return email.substring(at+1);
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return email;
    }
}
