package similarity

import output.TestInterface


class TaskInterfaceSimilarityAnalyser {

    private static calculateJaccardIndex(Set set1, Set set2){
        if(!set1 || !set2 || set1.isEmpty() || set2.isEmpty()) 0
        else (set1.intersect(set2)).size()/(set1+set2).size()
    }

    /* How to interpret different similarity measures? */
    static calculateSimilarity(TestInterface scen1, TestInterface scen2){
        def relClassIndex = calculateJaccardIndex(scen1.getRelevantClasses(), scen2.getRelevantClasses())
        def prodCalledMethodIndex = calculateJaccardIndex(scen1.getProductionCalledMethods(), scen2.getProductionCalledMethods())
        def files = calculateJaccardIndex(scen1.referencedPages, scen2.referencedPages)
        [classIndex:relClassIndex, methodIndex:prodCalledMethodIndex, filesIndex:files]
    }

    static calculateRelevantClassSimilarity(TestInterface scen1, TestInterface scen2){
        calculateJaccardIndex(scen1.getRelevantClasses(), scen2.getRelevantClasses())
    }

    static calculateReferencedClassSimilarity(TestInterface scen1, TestInterface scen2){
        calculateJaccardIndex(scen1.classes, scen2.classes)
    }

    static calculateCalledMethodSimilarity(TestInterface scen1, TestInterface scen2){
        calculateJaccardIndex(scen1.getProductionCalledMethods(), scen2.getProductionCalledMethods())
    }

    static calculateDeclaredFieldSimilarity(TestInterface scen1, TestInterface scen2){
        calculateJaccardIndex(scen1.getDeclaredFields(), scen2.getDeclaredFields())
    }

    static calculateAccessedPropSimilarity(TestInterface scen1, TestInterface scen2){
        calculateJaccardIndex(scen1.accessedProperties, scen2.accessedProperties)
    }

    static calculateReferencedPages(TestInterface scen1, TestInterface scen2){
        calculateJaccardIndex(scen1.referencedPages, scen2.referencedPages)
    }

}
