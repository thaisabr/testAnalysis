package similarity

import output.ScenarioInterface


class TaskInterfaceSimilarityAnalyser {

    /* How to interpret different similarity measures? */
    static calculateSimilarity(ScenarioInterface scen1, ScenarioInterface scen2){
        def relClassIndex = calculateJaccardIndex(scen1.getRelevantClasses(), scen2.getRelevantClasses())
        def prodCalledMethodIndex = calculateJaccardIndex(scen1.getProductionCalledMethods(), scen2.getProductionCalledMethods())
        def files = calculateJaccardIndex(scen1.referencedPages, scen2.referencedPages)
        [classIndex:relClassIndex, methodIndex:prodCalledMethodIndex, filesIndex:files]
    }

    static calculateRelevantClassSimilarity(ScenarioInterface scen1, ScenarioInterface scen2){
        calculateJaccardIndex(scen1.getRelevantClasses(), scen2.getRelevantClasses())
    }

    static calculateReferencedClassSimilarity(ScenarioInterface scen1, ScenarioInterface scen2){
        calculateJaccardIndex(scen1.referencedClasses, scen2.referencedClasses)
    }

    static calculateCalledMethodSimilarity(ScenarioInterface scen1, ScenarioInterface scen2){
        calculateJaccardIndex(scen1.getProductionCalledMethods(), scen2.getProductionCalledMethods())
    }

    static calculateDeclaredFieldSimilarity(ScenarioInterface scen1, ScenarioInterface scen2){
        calculateJaccardIndex(scen1.getDeclaredFields(), scen2.getDeclaredFields())
    }

    static calculateAccessedPropSimilarity(ScenarioInterface scen1, ScenarioInterface scen2){
        calculateJaccardIndex(scen1.accessedProperties, scen2.accessedProperties)
    }

    static calculateReferencedPages(ScenarioInterface scen1, ScenarioInterface scen2){
        calculateJaccardIndex(scen1.referencedPages, scen2.referencedPages)
    }

    private static calculateJaccardIndex(Set set1, Set set2){
        if(!set1 || !set2 || set1.isEmpty() || set2.isEmpty()) 0
        else (set1.intersect(set2)).size()/(set1+set2).size()
    }

}
