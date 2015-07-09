package output

import utils.Utils

class ConsoleManager implements TestInterfaceManager {

    public ConsoleManager(String path){
        println "Code to analyse: ${Utils.getShortClassPath(path)}\n"
    }

    public ConsoleManager(String featurePath, String scenarioName){
        println "Code to analyse: ${Utils.getShortClassPath(featurePath)}, scenario: $scenarioName\n"
    }

    private static listReferencedClasses(TestInterface scenarioInterface){
        println "<Referenced classes: ${scenarioInterface?.classes?.size()}>"
        scenarioInterface?.classes?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listCalledMethods(TestInterface scenarioInterface){
        println "<Called methods: ${scenarioInterface?.methods?.size()}>"
        scenarioInterface?.methods?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listFields(TestInterface scenarioInterface){
        println "<Fields: ${scenarioInterface?.fields?.size()}>"
        scenarioInterface?.fields?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type: $obj.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listProperties(TestInterface scenarioInterface){
        println "<Properties: ${scenarioInterface?.accessedProperties?.size()}>"
        scenarioInterface?.accessedProperties?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listStaticFields(TestInterface scenarioInterface){
        println "<Static fields: ${scenarioInterface?.staticFields?.size()}>"
        scenarioInterface?.staticFields?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type: $obj.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listCalledPageMethods(TestInterface scenarioInterface){
        println "<Called page methods: ${scenarioInterface?.calledPageMethods?.size()}>"
        scenarioInterface?.calledPageMethods?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.arg"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listCalledProductionMethods(TestInterface scenarioInterface){
        def methods = scenarioInterface?.getProductionCalledMethods()
        println "<Called production Methods: ${methods?.size()}>"
        methods?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listReferencedProductionClasses(TestInterface scenarioInterface){
        def classes =  scenarioInterface?.classes?.findAll{ !Utils.isTestCode(it.name) }
        println "<Referenced production classes: ${classes?.size()}>"
        classes?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listRelevantProductionClasses(TestInterface scenarioInterface){
        println "<Relevant production classes: ${scenarioInterface.relevantClasses?.size()}>"
        scenarioInterface.relevantClasses?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listGSP(TestInterface scenarioInterface){
        println "<Referenced production GSP files: ${ scenarioInterface?.referencedPages?.size()}>"
        scenarioInterface?.referencedPages?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj"
        }
        println "---------------------------------------------------------------------------------"
    }

    @Override
    void detailedAnalysis(TestInterface scenarioInterface){
        listReferencedClasses(scenarioInterface)
        listCalledMethods(scenarioInterface)
        listFields(scenarioInterface)
        listProperties(scenarioInterface)
        listStaticFields(scenarioInterface)
        listCalledPageMethods(scenarioInterface)
    }

    @Override
    void updateScenarioInterfaceOutput(TestInterface scenarioInterface){
        listCalledProductionMethods(scenarioInterface)
        listReferencedProductionClasses(scenarioInterface)
        listRelevantProductionClasses(scenarioInterface)
        listGSP(scenarioInterface)
    }

}
