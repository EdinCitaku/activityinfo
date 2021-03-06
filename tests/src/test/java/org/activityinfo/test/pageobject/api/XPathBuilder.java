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
package org.activityinfo.test.pageobject.api;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import javax.annotation.Nullable;


public class XPathBuilder {

    public enum Axis {
        CHILD,
        DESCENDANT,
        ANCESTORS,
        PARENT,
        FOLLOWING_SIBLING,
        PRECEDING_SIBLING,
        PRECEDING
    }
    
    private FluentElement context;
    private XPathBuilder parent;
    private String step;
    private Axis axis = Axis.DESCENDANT;

    
    public XPathBuilder(FluentElement context, XPathBuilder parent) {
        this.context = context;
        this.parent = parent;
    }


    public XPathBuilder(FluentElement context, Axis axis) {
        this.context = context;
        this.axis = axis;

    }

    public XPathBuilder(FluentElement context) {
        this.context = context;
    }


    public XPathBuilder ancestor() {
        this.axis = Axis.ANCESTORS;
        return this;
    }
    
    public XPathBuilder followingSibling() {
        this.axis = Axis.FOLLOWING_SIBLING;
        return this;
    }

    public XPathBuilder preceding() {
        this.axis = Axis.PRECEDING;
        return this;
    }

    public XPathBuilder precedingSibling() {
        this.axis = Axis.PRECEDING_SIBLING;
        return this;
    }


    public XPathBuilder parent() {
        this.axis = Axis.PARENT;
        return this;
    }
    
    public XPathBuilder li(String... conditions) {
        return tagName("li", conditions);
    }
    
    public XPathBuilder div(String... conditions) {
        return tagName("div", conditions);
    }

    public XPathBuilder h1(String... conditions) {
        return tagName("h1", conditions);
    }

    public XPathBuilder h2(String... conditions) {
        return tagName("h2", conditions);
    }

    public XPathBuilder h3(String... conditions) {
        return tagName("h3", conditions);
    }

    public XPathBuilder h5(String... conditions) {
        return tagName("h5", conditions);
    }


    public XPathBuilder select(String... conditions) {
        return tagName("select", conditions);
    }

    public XPathBuilder option(String... conditions) {
        return tagName("option", conditions);
    }

    public XPathBuilder table(String... conditions) {
        return tagName("table", conditions);
    }

    public XPathBuilder p(String... conditions) {
        return tagName("p", conditions);
    }

    public XPathBuilder ul(String... conditions) {
        return tagName("ul", conditions);
    }

    public XPathBuilder span(String... conditions) {
        return tagName("span", conditions);
    }
    
    public XPathBuilder button(String... conditions) {
        return tagName("button", conditions);
    }


    public XPathBuilder label(String... conditions) {
        return tagName("label", conditions);
    }

    public XPathBuilder input(String... conditions) {
        return tagName("input", conditions);
    }

    public XPathBuilder form(String... conditions) {
        return tagName("form", conditions);
    }

    public XPathBuilder textArea(String... conditions) {
        return tagName("textArea", conditions);
    }

    public XPathBuilder b(String... conditions) {
        return tagName("b", conditions);
    }

    public XPathBuilder a(String... conditions) {
        return tagName("a", conditions);
    }

    public XPathBuilder anyElement(String... conditions) {
        return tagName("*", conditions);
    }

    public XPathBuilder td(String... conditions) {
        return tagName("td", conditions);
    }

    public XPathBuilder tr(String... conditions) {
        return tagName("tr", conditions);
    }

    public XPathBuilder th(String... conditions) {
        return tagName("th", conditions);
    }

    public XPathBuilder thead(String... conditions) {
        return tagName("thead", conditions);
    }

    public XPathBuilder h4(String... conditions) {
        return tagName("h4", conditions);
    }

    public XPathBuilder img(String... conditions) {
        return tagName("img", conditions);
    }

    public static String withClass(String className) {
        return String.format("contains(concat(' ', @class, ' '), ' %s ')", className);
    }
    public static String withoutClass(String className) {
        return String.format("not(%s)", withClass(className));
    }
    
    public static String containingText(String text) {
        return String.format("contains(normalize-space(text()), '%s')", text);
    }
    

    public static String withText(String text) {
        return String.format("normalize-space(text()) = '%s'", text);
    }

    public static String withRole(String roleName ) {
        return String.format("@role = '%s'", roleName);
    }

    public XPathBuilder descendants() {
        this.axis = Axis.DESCENDANT;
        return this;
    }

    public XPathBuilder child() {
        this.axis = Axis.CHILD;
        return this;
    }

    private XPathBuilder tagName(String tagName, String... conditions) {
        return tagName(tagName, true, conditions);
    }
    
    public XPathBuilder tagName(String tagName, boolean and, String... conditions) {
        StringBuilder xpath = new StringBuilder(tagName);
        if(conditions.length > 0) {
            String condition = and ? " and " : " or ";
            xpath.append("[");
            Joiner.on(condition).appendTo(xpath, conditions);
            xpath.append("]");
        }
        this.step = xpath.toString();
        return new XPathBuilder(context, this);
    }
    
    public String toString() {
        StringBuilder xpath = new StringBuilder();
        appendTo(xpath);
        return xpath.toString();
    }

    private void appendTo(StringBuilder xpath) {
        if(parent != null) {
            parent.appendTo(xpath);
        }
        if(step != null) {
            if(xpath.length() > 0) {
                xpath.append("/");
            }
            if(axis == Axis.DESCENDANT) {
                xpath.append("descendant::");
            } else if(axis == Axis.ANCESTORS) {
                xpath.append("ancestor::");
            } else if(axis == Axis.PARENT) {
                xpath.append("parent::");
            } else if(axis == Axis.FOLLOWING_SIBLING) {
                xpath.append("following-sibling::");
            } else if (axis == Axis.PRECEDING) {
                xpath.append("preceding::");
            } else if (axis == Axis.PRECEDING_SIBLING) {
                xpath.append("preceding-sibling::");
            }
            xpath.append(step);
        }
    }
    
    private By firstLocator() {
        StringBuilder xpath = new StringBuilder();
        appendTo(xpath);
        xpath.append("[1]");
        return By.xpath(xpath.toString());
    }
    
    public FluentElement first() {
        try {
            return context.findElement(firstLocator());
        } catch (Exception e) {
            throw new NoSuchElementException("Could not locate element by XPath '" + toString() + "'", e);
        }
    }

    public Optional<FluentElement> firstIfPresent() {
        return context.findElements(firstLocator()).first();
    }

    public FluentElements asList() {
        return context.findElements(By.xpath(toString()));
    }

    public boolean exists() {
        return !all().isEmpty();
    }
    
    public FluentElement waitForFirst() {
        return context.waitFor(firstLocator());
    }
    
    public FluentElements waitForList() {
        return context.waitFor(new Function<WebDriver, FluentElements>() {
            @Nullable
            @Override
            public FluentElements apply(WebDriver input) {
                FluentElements elements = asList();
                if(elements.size() > 0) {
                    return elements;
                } else {
                    return null;
                }
            }
        });
    }
    

    public <T> T find(Class<T> clazz) {
        return all().as(clazz).first().get();
    }

    private FluentElements all() {
        return context.findElements(By.xpath(toString()));
    }


    public void clickWhenReady() {
        context.waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                try {
                    first().click();
                    return true;

                } catch (WebDriverException e) {
                    return false;
                }
            }
        });
    }
}
