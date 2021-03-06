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
package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.activityinfo.geoadmin.match.ScoreMatrix;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Matrix for testing
 */
public class TestScoreMatrix extends ScoreMatrix {
    
    private int numFields;
    private List<String[]> sourceItems = new ArrayList<>();
    private List<String[]> targetItems = new ArrayList<>();
    
    public TestScoreMatrix(int numFields, String sourceResource, String targetResource) throws IOException {
        this.numFields = numFields;
        this.sourceItems = loadTsv(sourceResource);
        this.targetItems = loadTsv(targetResource);
    }

    public TestScoreMatrix(int numFields) {
        this.numFields = numFields;
    }

    private List<String[]> loadTsv(String sourceResource) throws IOException {
        URL resource = Resources.getResource(sourceResource);
        List<String> lines = Resources.readLines(resource, Charsets.UTF_8);
        List<String[]> items = new ArrayList<>();
        for (String line : lines) {
            if(!line.isEmpty()) {
                items.add(line.split("\t"));
            }
        }
        return items;
    }
    
    public String getSourceValue(int rowIndex, int fieldIndex) {
        return sourceItems.get(rowIndex)[fieldIndex];
    }
    
    public String getTargetValue(int rowIndex, int fieldIndex) {
        return targetItems.get(rowIndex)[fieldIndex];
    }

    public int addSource(String... fields) {
        assert fields.length == numFields;
        sourceItems.add(fields);
        return sourceItems.size() - 1;
    }
    
    public int addTarget(String... fields) {
        assert fields.length == numFields;
        targetItems.add(fields);
        return targetItems.size() - 1;
    }
    
    @Override
    public int getDimensionCount() {
        return numFields;
    }

    @Override
    public int getRowCount() {
        return sourceItems.size();
    }

    @Override
    public int getColumnCount() {
        return targetItems.size();
    }

    @Override
    public double score(int i, int j, int d) {
        String source = sourceItems.get(i)[d];
        String target = targetItems.get(j)[d];
        
        if(Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(target)) {
            return Double.NaN;
        } else {
            LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
            return scorer.score(source, target);
        }
    }
}
