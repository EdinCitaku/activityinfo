/**
 * Sencha GXT 4.0.0 - Sencha for GWT
 * Copyright (c) 2006-2015, Sencha Inc.
 *
 * licensing@sencha.com
 * http://www.sencha.com/products/gxt/license/
 *
 * ================================================================================
 * Evaluation/Trial License
 * ================================================================================
 * This version of Sencha GXT is licensed commercially for a limited period for
 * evaluation purposes only. Production use or use beyond the applicable evaluation
 * period is prohibited under this license.
 *
 * Please see the Sencha GXT Licensing page at:
 * http://www.sencha.com/products/gxt/license/
 *
 * For clarification or additional options, please contact:
 * licensing@sencha.com
 * ================================================================================
 *
 *
 *
 *
 *
 *
 *
 * ================================================================================
 * Disclaimer
 * ================================================================================
 * THIS SOFTWARE IS DISTRIBUTED "AS-IS" WITHOUT ANY WARRANTIES, CONDITIONS AND
 * REPRESENTATIONS WHETHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES AND CONDITIONS OF MERCHANTABILITY, MERCHANTABLE QUALITY,
 * FITNESS FOR A PARTICULAR PURPOSE, DURABILITY, NON-INFRINGEMENT, PERFORMANCE AND
 * THOSE ARISING BY STATUTE OR FROM CUSTOM OR USAGE OF TRADE OR COURSE OF DEALING.
 * ================================================================================
 */
package com.sencha.gxt.edash.shared.model;


import com.google.gwt.core.client.JavaScriptObject;

import java.io.Serializable;

public class News extends JavaScriptObject implements Serializable {

  public final native int getId() /*-{ return this.news_id; }-*/;
  public final native String getTitle() /*-{ return this.title; }-*/;
  public final native String getParagraph() /*-{ return this.paragraph; }-*/;
  public final native String getDate() /*-{ return this.date; }-*/;
  public final native String getTime() /*-{ return this.time; }-*/;
  public final native String getAuthor() /*-{ return this.author; }-*/;
  public final native String getGroup() /*-{ return this.group; }-*/;
  public final native String getPhoto() /*-{ return this.image; }-*/;
  public final native String getType() /*-{ return this.type; }-*/;


  protected News() {
  }
}
