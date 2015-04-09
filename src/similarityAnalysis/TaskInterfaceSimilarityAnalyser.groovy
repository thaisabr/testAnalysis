package similarityAnalysis

import output.ScenarioInterface


class TaskInterfaceSimilarityAnalyser {

    /* How to interpret different similarity measures? */
    static calculateSimilarity(ScenarioInterface scen1, ScenarioInterface scen2){
        def refClassIndex = calculateJaccardIndex(scen1.referencedClasses, scen2.referencedClasses)
        def prodCalledMethodIndex = calculateJaccardIndex(scen1.getProductionCalledMethods(), scen2.getProductionCalledMethods())
        def declaredFieldIndex = calculateJaccardIndex(scen1.getDeclaredFields(), scen2.getDeclaredFields())
        def accessedPropIndex = calculateJaccardIndex(scen1.accessedProperties, scen2.accessedProperties)

        [classIndex:refClassIndex, methodIndex:prodCalledMethodIndex, decFieldIndex:declaredFieldIndex, fieldIndex:accessedPropIndex]
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

    private static calculateJaccardIndex(Set set1, Set set2){
        if(!set1 || !set2 || set1.isEmpty() || set2.isEmpty()) 0
        else (set1.intersect(set2)).size()/(set1+set2).size()
    }

}
