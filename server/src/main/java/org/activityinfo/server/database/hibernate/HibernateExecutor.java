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
package org.activityinfo.server.database.hibernate;

import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.server.jdbc.JdbcExecutor;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.jdbc.Work;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HibernateExecutor extends JdbcExecutor {

    private final HibernateEntityManager entityManager;

    private final static Logger LOGGER = Logger.getLogger(HibernateExecutor.class.getName());

    @Inject
    public HibernateExecutor(EntityManager em) {
        this.entityManager = (HibernateEntityManager) em;
    }

    public HibernateExecutor(HibernateEntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    @Override
    public SqlResultSet execute(final String statement, final Object[] params) throws Exception {
        final List<SqlResultSet> result = Lists.newArrayList();
        entityManager.getSession().doWork(new Work() {

            @Override
            public void execute(Connection connection) throws SQLException {
                try {
                    result.add(doExecute(connection, statement, params));
                } catch (Throwable e) {
                    LOGGER.log(Level.SEVERE, "Exception thrown while executing query: " + statement, e);
                    throw new SQLException(e);
                }
            }
        });
        if (result.size() != 1) {
            throw new AssertionError();
        }
        return result.get(0);
    }

    @Override
    public boolean begin() throws Exception {
        return true;
    }

    @Override
    public void commit() throws Exception {
    }

    @Override
    public void rollback() throws Exception {

    }

    private String format(final String statement) {
        return FormatStyle.BASIC.getFormatter().format(statement);
    }
}
