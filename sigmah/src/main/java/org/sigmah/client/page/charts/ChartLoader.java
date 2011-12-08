/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.charts;

import org.sigmah.client.page.NavigationHandler;
import org.sigmah.client.page.Page;
import org.sigmah.client.page.PageId;
import org.sigmah.client.page.PageLoader;
import org.sigmah.client.page.PageState;
import org.sigmah.client.page.PageStateSerializer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 * PageLoader for the Chart page
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class ChartLoader implements PageLoader {
    private final Provider<ChartPage> chartPageProvider;

    @Inject
    public ChartLoader(NavigationHandler pageManager, PageStateSerializer placeSerializer,
                       Provider<ChartPage> chartPageProvider) {
        this.chartPageProvider = chartPageProvider;

        pageManager.registerPageLoader(ChartPage.PAGE_ID, this);
        placeSerializer.registerStatelessPlace(ChartPage.PAGE_ID, new ChartPageState());
    }

    @Override
    public void load(final PageId pageId, final PageState pageState, final AsyncCallback<Page> callback) {

        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess() {
                if(pageState instanceof ChartPageState) {
                    callback.onSuccess(chartPageProvider.get());
                }
            }
        });
    }
}
