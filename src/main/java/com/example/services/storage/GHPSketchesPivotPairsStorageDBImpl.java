package com.example.services.storage;

import com.example.model.Pivot512;
import com.example.model.PivotPairsFor64pSketches;
import com.example.model.ProteinChain;
import com.example.model.SimpleProtein;
import com.example.service.PivotPairsForXpSketchesService;
import com.example.service.PivotSetService;
import com.example.services.configuration.AppConfig;
import org.hibernate.Session;
import vm.metricSpace.AbstractMetricSpace;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.util.List;

public class GHPSketchesPivotPairsStorageDBImpl implements GHPSketchingPivotPairsStoreInterface {

    private final Session session; //todo dont use session directly use the Service
    private final PivotPairsForXpSketchesService pivotPairsForXpSketchesService;
    private final PivotSetService pivotSetService;


    public GHPSketchesPivotPairsStorageDBImpl(Session session, PivotPairsForXpSketchesService pivotPairsForXpSketchesService, PivotSetService pivotSetService) {
        this.session = session;
        this.pivotPairsForXpSketchesService = pivotPairsForXpSketchesService;
        this.pivotSetService = pivotSetService;
    }

    @Override
    public void storeSketching(String resultName, AbstractMetricSpace<Object> metricSpace, List<Object> pivots, Object... additionalInfoToStoreWithLearningSketching) {
        // sketchbitorder1-pivot1, sketchbitorder1-pivot2, sketchbitorder2-pivot1, sketchbitorder2pivot2
        System.out.println("Saving " + pivots.size() / 2 + " sketches");
        var currentPivotSet = pivotSetService.GetCurrentPivotSet();
        //todo as the simpleproteins already got all the info needed we can skip this step
        var pivotList = pivots.stream().map(o -> {
            var sp = (SimpleProtein) o;
            // the objects coming here are pivots, stripped of the pivotset information.
            // we are assuming the pivotset was not changed during runtime, it's the best we can do
            ProteinChain proteinChain = this.session.get(ProteinChain.class, sp.getIntId());
            var pivotId = new Pivot512.Pivot512Id();
            pivotId.setPivotSet(currentPivotSet);
            pivotId.setProteinChain(proteinChain);
            var pivot = session.get(Pivot512.class, pivotId);
            return pivot;
        }).toList();
        if (!AppConfig.DRY_RUN) {
            pivotPairsForXpSketchesService.storePairs(pivotList);
        }
        System.out.println("Saved");
    }

    // returns list of pivots that are used for sketching
    //todo add test that tests that this returns the same as above stores
    @Override
    public List<String[]> loadPivotPairsIDs(String sketchesName) {
        return pivotPairsForXpSketchesService.loadPivotPairsIDs();
    }
}
