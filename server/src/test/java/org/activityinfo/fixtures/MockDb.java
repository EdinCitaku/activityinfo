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
package org.activityinfo.fixtures;

import org.activityinfo.server.database.hibernate.dao.DAO;

import javax.persistence.Id;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class MockDb {

    private List entities = new ArrayList(0);

    public MockDb() {

    }

    public Object findById(Class entityClass, Object primaryKey) {
        if (primaryKey == null) {
            return null;
        }

        for (Object entity : entities) {
            if (entity.getClass().equals(entityClass)
                    && primaryKey.equals(getId(entity))) {
                return entity;
            }
        }
        return null;
    }

    public void persist(Object entity) {
        entities.add(entity);
    }

    private Object getId(Object t) {
        for (Method method : t.getClass().getMethods()) {
            if (method.getAnnotation(Id.class) != null) {
                try {
                    return method.invoke(t);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("No @Id field!");
    }

    public <S extends DAO> S getDAO(Class<S> daoSubClass) {
        ClassLoader cl = daoSubClass.getClassLoader();
        Type daoClass = daoSubClass.getGenericInterfaces()[0];
        Class persistentClass = (Class) ((ParameterizedType) daoClass)
                .getActualTypeArguments()[0];
        return (S) Proxy.newProxyInstance(cl, new Class[]{daoSubClass},
                new Handler(persistentClass));
    }

    public class Handler implements InvocationHandler {

        private final Class persistentClass;

        public Handler(Class persistentClass) {
            this.persistentClass = persistentClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

            if (method.getName().equals("findById")) {
                return findById(persistentClass, args[0]);
            } else if (method.getName().equals("persist")) {
                persist(args[0]);
            } else if (method.getReturnType().equals(Boolean.TYPE)) {
                return false;
            }
            return null;
        }
    }

}
