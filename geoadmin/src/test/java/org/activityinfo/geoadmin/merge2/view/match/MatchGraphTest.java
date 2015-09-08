package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.state.ResourceStoreStub;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class MatchGraphTest {

    public static final int COLUMN_WIDTH = 25;
    private ImportModel model;
    private MatchTable matchTable;
    private ImportView importView;
    private FormProfile sourceForm;
    private FormProfile targetForm;
    private MatchGraph graph;
    private KeyFieldPairSet keyFields;

    @Before
    public void setUp() throws IOException {
        ResourceStoreStub resourceStore = new ResourceStoreStub();

        ResourceId sourceId = ResourceStoreStub.COMMUNE_SOURCE_ID;
        ResourceId targetId = ResourceStoreStub.COMMUNE_TARGET_ID;

        sourceForm = FormProfile.profile(resourceStore, sourceId).get();
        targetForm = FormProfile.profile(resourceStore, targetId).get();

        keyFields = KeyFieldPairSet.matchKeys(sourceForm, targetForm);

        for (KeyFieldPair keyField : keyFields) {
            System.out.println(keyField.getSourceField().getLabel() + " <=> " + keyField.getTargetField().getLabel());
        }

        graph = new MatchGraph(keyFields);
    }
    
    
    @Test
    @Ignore
    public void pcaTest() {

        for (KeyFieldPair keyField : keyFields) {
            System.out.println(keyField.getTargetField().getLabel());
        }
        
        InstanceMatrix similarityMatrix = new InstanceMatrix(keyFields);
        
        ColumnView sourceRegion = sourceForm.getField("REGION").getView();
        ColumnView sourceRegionCode = sourceForm.getField("REG_PCODE").getView();
        
        ColumnView targetRegion = targetForm.getField("Region.Name").getView();
        ColumnView targetRegionCode = targetForm.getField("Region.Code").getView();
        
        int regionNameKey = findKeyIndex("Region.Name");
        int regionCodeKey = findKeyIndex("Region.Code");
        
        int rowIndex = 0;
        printLoop: for (int i = 0; i < similarityMatrix.getRowCount(); i++) {
            for (int j = 0; j < similarityMatrix.getColumnCount(); j++) {
                System.out.println(format("[%3.0f: %6s <> %6s][%3.0f: %25s <> %25s]",
                        similarityMatrix.score(i, j, regionCodeKey) * 100d,
                        sourceRegionCode.getString(i),
                        targetRegionCode.getString(j),
                        
                        similarityMatrix.score(i, j, regionNameKey) * 100d,
                        sourceRegion.getString(i),
                        targetRegion.getString(j)));
                rowIndex++;
                if(rowIndex > 1000) {
                    break printLoop;
                }
            }
        }
        
        // Now try our online version
        Stopwatch stopwatch = Stopwatch.createStarted();
        RealMatrix covMatrix2 = similarityMatrix.computeCovarianceMatrix();
        
        stopwatch.stop();

        System.out.println(format("Covariance matrix computed in %d seconds: %8.7f",
                stopwatch.elapsed(TimeUnit.SECONDS),
                covMatrix2.getEntry(regionCodeKey, regionNameKey)));

        System.out.println(covMatrix2);


//      
//        EigenDecomposition eigenDecomposition = new EigenDecomposition(covMatrix2);
//        eigenDecomposition.
//        RealMatrix eigenVectors = eigenDecomposition.getV();
        
//        System.out.println(eigenVectors);
//
//        keyFields.
        
    }

    @Test
    public void missingValues() throws IOException {

        int sourceIndex = findSourceIndex("COMMUNE", "Vinany");

        graph.buildParetoFrontierForSource(sourceIndex);
        
        dumpCandidatesForSource(sourceIndex);
        
        
    }
    
    @Test
    public void optimalTest() throws IOException {
        int sourceIndex = findSourceIndex("COMMUNE", "Komajia");
        graph.buildParetoFrontierForSource(sourceIndex);

        dumpCandidatesForSource(sourceIndex);

        Collection<MatchGraph.Candidate> frontier = graph.getParetoFrontierForSource(sourceIndex);
        
        assertThat(frontier.size(), equalTo(1));
 
        MatchGraph.Candidate optimal = Iterables.getOnlyElement(frontier);

        assertThat(targetForm.getField("Name").getView().getString(optimal.getTargetIndex()), equalTo("Komajia"));
    }


    /**
     * The simplest case is when the pareto frontier consists of a single match.
     */
    @Test
    public void singleParetoOptimal() {

        assertThat(sourceForm.getField("the_geom").getView().getExtents(0), notNullValue());

        graph.build();

        int sourceIndex = findTargetIndex("Name", "Komajia");
        int targetIndex = graph.getBestMatchForTarget(sourceIndex);

        checkMatch("Komajia", "Komajia");
        assertThat(targetIndex, equalTo(findSourceIndex("COMMUNE", "Komajia")));
        


    }

    @Test
    public void aboalimena() {
        graph.build();

        int targetIndex = findTargetIndex("Name", "Amboalimena");
        int sourceIndex = graph.getBestMatchForTarget(targetIndex);

    }
    
    @Test
    public void elonty() {
        graph.rankScoreMatrix();
        int sourceIndex = findSourceIndex("COMMUNE", "Niherenana");
        int targetIndex = findTargetIndex("Name", "Nierenana");
    
        graph.buildParetoFrontierForSource(sourceIndex);

        int matchedTargetIndex = graph.getBestMatchForSource(sourceIndex);

        dumpCandidatesForSource(sourceIndex);

        targetForm.dump(matchedTargetIndex);
        
        
       // assertThat(matchedSourceIndex, equalTo(sourceIndex));

    }
    

    /**
     * If there are multiple pareto optimums, we choose the best using
     */
    @Test
    public void multipleParetoOptimums() {
        
        graph.build();

       //checkMatch("Mandritsara", "Ambohijato Mandritsara");
        checkMatch( "Niherenana", "Nierenana");
    }

    private void checkMatch(String source, String target) {
        int sourceIndex = findSourceIndex("COMMUNE", source);

        System.out.println("SOURCE");
        sourceForm.dump(sourceIndex);

        System.out.println("TARGET");
        dumpCandidatesForSource(sourceIndex);

        int matchedSourceIndex = graph.getBestMatchForTarget(findTargetIndex("Name", target));
        assertThat(matchedSourceIndex, equalTo(sourceIndex));
    }


    private void dumpCandidatesForSource(int sourceIndex) {
        
        Collection<MatchGraph.Candidate> candidates = graph.getParetoFrontierForSource(sourceIndex);

        for (MatchGraph.Candidate candidate : candidates) {
            System.out.println(format("%s[%3.2f]%s",
                    candidate.toString(),
                    graph.rank(candidate), 
                    targetForm.toString(candidate.getTargetIndex())));
        }
        
    }

    private int findSourceIndex(String fieldName, String fieldValue) {
        ColumnView view = sourceForm.getField(fieldName).getView();
        for(int i=0;i<view.numRows();++i) {
            if(fieldValue.equals(view.getString(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException(fieldValue);
    }

    private int findTargetIndex(String fieldName, String fieldValue) {
        ColumnView view = targetForm.getField(fieldName).getView();
        for(int i=0;i<view.numRows();++i) {
            if(fieldValue.equals(view.getString(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException(fieldValue);
    }


    private int findKeyIndex(String fieldName) {
        int keyIndex = 0;
        for (int i = 0; i < keyFields.size(); i++) {
            if (keyFields.get(i).getTargetField().getLabel().equals(fieldName)) {
                return i;
            }
        }
        throw new IllegalArgumentException(fieldName);
    }

}