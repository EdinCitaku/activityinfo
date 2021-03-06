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
package org.activityinfo.ui.client.analysis.view;

import org.activityinfo.analysis.pivot.viewModel.AnalysisViewModel;
import org.activityinfo.model.analysis.pivot.ImmutableMeasureModel;
import org.activityinfo.model.analysis.pivot.MeasureModel;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MeasureListItemStoreTest {

    private TestingFormStore formStore;

    @Before
    public void setup() {
        formStore = new TestingFormStore();
    }


    @Test
    public void test() {

        formStore.delayLoading();

        AnalysisViewModel viewModel = new AnalysisViewModel(formStore);
        MeasureListItemStore store = new MeasureListItemStore(viewModel);

        viewModel.addMeasure(surveyCount());

        assertThat(store.size(), equalTo(0));

        formStore.loadAll();


        assertThat(store.size(), equalTo(1));
    }

    private MeasureModel surveyCount() {
        return ImmutableMeasureModel.builder()
            .label("Count")
            .formId(formStore.getCatalog().getSurvey().getFormId())
            .formula("1")
            .build();
    }

}