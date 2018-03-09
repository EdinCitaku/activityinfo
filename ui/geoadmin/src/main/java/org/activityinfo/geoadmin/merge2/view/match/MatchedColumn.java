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

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.activityinfo.geoadmin.match.MatchLevel;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;

import java.awt.*;


/**
 * Displays a column from the source or target collection that is matched to the opposing collection
 */
public class MatchedColumn extends MatchTableColumn {

    public static final Color WARNING_COLOR = Color.decode("#FF6600");
    private final MatchTable matching;
    private final FieldProfile sourceField;
    private final FieldProfile targetField;
    private final LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
    private final MatchSide side;

    public MatchedColumn(MatchTable matching, FieldProfile targetField, FieldProfile sourceField, MatchSide side) {
        this.matching = matching;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.side = side;
    }

    @Override
    public String getHeader() {
        if(side == MatchSide.SOURCE) {
            return sourceField.getLabel();
        } else {
            return targetField.getLabel();
        }
    }

    @Override
    public String getValue(int rowIndex) {
        if(matching.isLoading()) {
            return null;
        }
        if(side == MatchSide.SOURCE) {
            return formatValue(getSourceValue(rowIndex));
        } else {
            return formatValue(getTargetValue(rowIndex));
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private String getTargetValue(int rowIndex) {
        int targetRow = matching.get(rowIndex).getTargetRow();
        if(targetRow == -1) {
            return null;
        }
        return targetField.getString(targetRow);
    }

    private String getSourceValue(int rowIndex) {
        int sourceRow = matching.get(rowIndex).getSourceRow();
        if(sourceRow == -1) {
            return null;
        }
        return sourceField.getString(sourceRow);
    }

    public Optional<MatchLevel> getMatchConfidence(int rowIndex) {
        String sourceValue = getSourceValue(rowIndex);
        String targetValue = getTargetValue(rowIndex);

        if(Strings.isNullOrEmpty(sourceValue) && Strings.isNullOrEmpty(targetValue)) {
            return Optional.absent();

        } else if(Strings.isNullOrEmpty(sourceValue) || Strings.isNullOrEmpty(targetValue)) {
            return Optional.of(MatchLevel.POOR);
        
        } else {
            double score = scorer.score(sourceValue, targetValue);
            return Optional.of(MatchLevel.of(score));            
        }
    }

    @Override
    public Optional<MatchSide> getSide() {
        return Optional.of(side);
    }
}
