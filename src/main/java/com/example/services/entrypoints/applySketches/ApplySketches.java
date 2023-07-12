package com.example.services.entrypoints.applySketches;

import com.example.dao.*;
import com.example.service.PivotPairsForXpSketchesService;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadataService;
import com.example.service.distance.ProteinChainService;
import com.example.services.configuration.AppConfig;
import com.example.services.distance.AbstractMetricSpaceDBImpl;
import com.example.services.distance.CachedDistanceFunctionInterfaceImpl;
import com.example.services.entrypoints.DatasetImpl;
import com.example.services.storage.GHPSketchesPivotPairsStorageDBImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import vm.objTransforms.perform.TransformDataToGHPSketches;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.io.IOException;

import static com.example.App.getSessionFactory;

public class ApplySketches {

    public static void run(int sketchesLength) throws IOException {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotService = new PivotService(new PivotDao(session), pivotSetService);
            var pivotPairsForXpSketchesService = new PivotPairsForXpSketchesService(pivotSetService, new PivotPairsForXpSketchesDao(session));
            GHPSketchingPivotPairsStoreInterface sketchingTechStorage = new GHPSketchesPivotPairsStorageDBImpl(session, pivotPairsForXpSketchesService, pivotSetService);
            int[] sketchesLengths = new int[]{sketchesLength};

            //we want full cache. Todo cleaner variant without hardcoded constants
            var metricSpace = new AbstractMetricSpaceDBImpl(new CachedDistanceFunctionInterfaceImpl<String>(session, pivotService, 720000, 512));
            //for learning sketches will return proteins with distance
            //todo corrent the "for" naming later
            var proteinChainDao = new ProteinChainForLearningSketchesDao(session);
            var proteinChainService = new ProteinChainService(pivotSetService, proteinChainDao);
            var proteinChainMetadaService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session, sessionFactory), pivotSetService);
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(pivotService, proteinChainService, proteinChainMetadaService);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);

            TransformDataToGHPSketches evaluator = new TransformDataToGHPSketches(dataset, sketchingTechStorage, metricSpaceStorage, AppConfig.SKETCH_LEARNING_BALANCE, AppConfig.SKETCH_LEARNING_PIVOTS_COUNT);
            evaluator.createSketchesForDatasetPivotsAndQueries(sketchesLengths);
        }
    }
}