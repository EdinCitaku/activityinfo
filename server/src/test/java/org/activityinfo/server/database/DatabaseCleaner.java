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
package org.activityinfo.server.database;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Cleans the MySQL test database
 */
public class DatabaseCleaner {

    private static final Logger LOGGER = Logger.getLogger(DatabaseCleaner.class.getName());

    private final Provider<Connection> connectionProvider;

    private static final String LIQUIBASE_TABLE_PREFIX = "databasechangelog";

    @Inject
    public DatabaseCleaner(Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void clean() {
        Connection connection = connectionProvider.get();

        try {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            statement.execute("SET foreign_key_checks = 0");

            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("DatabaseCleaner - url : " + metaData.getURL());
            ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
            try {
                while (tables.next()) {
                    String tableName = tables.getString(3);
                    if (!tableName.toLowerCase().startsWith(LIQUIBASE_TABLE_PREFIX)) {
                        statement.execute("DELETE FROM " + tableName);
                        LOGGER.fine("Dropped all from " + tableName);
                    }
                }
            } finally {
                tables.close();
            }
            statement.execute("SET foreign_key_checks = 1");
            statement.close();
            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
                ignored.printStackTrace();
            }
        }
    }
}
