package output

import utils.Utils

class ConsoleManager implements ScenarioInterfaceManager {

    public ConsoleManager(String path){
        println "Code to analyse: ${Utils.getShortClassPath(path)}\n"
    }

    public ConsoleManager(String featurePath, String scenarioName){
        println "Code to analyse: ${Utils.getShortClassPath(featurePath)}, scenario: $scenarioName\n"
    }

    private static listReferencedClasses(ScenarioInterface scenarioInterface){
        println "<Referenced classes: ${scenarioInterface?.classes?.size()}>"
        scenarioInterface?.classes?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listCalledMethods(ScenarioInterface scenarioInterface){
        println "<Called methods: ${scenarioInterface?.methods?.size()}>"
        scenarioInterface?.methods?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listFields(ScenarioInterface scenarioInterface){
        println "<Fields: ${scenarioInterface?.fields?.size()}>"
        scenarioInterface?.fields?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type: $obj.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listProperties(ScenarioInterface scenarioInterface){
        println "<Properties: ${scenarioInterface?.accessedProperties?.size()}>"
        scenarioInterface?.accessedProperties?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listStaticFields(ScenarioInterface scenarioInterface){
        println "<Static fields: ${scenarioInterface?.staticFields?.size()}>"
        scenarioInterface?.staticFields?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type: $obj.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listCalledPageMethods(ScenarioInterface scenarioInterface){
        println "<Called page methods: ${scenarioInterface?.calledPageMethods?.size()}>"
        scenarioInterface?.calledPageMethods?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.arg"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listCalledProductionMethods(ScenarioInterface scenarioInterface){
        def methods = scenarioInterface?.getProductionCalledMethods()
        println "<Called production Methods: ${methods?.size()}>"
        methods?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listReferencedProductionClasses(ScenarioInterface scenarioInterface){
        def classes =  scenarioInterface?.classes?.findAll{ !Utils.isTestCode(it) }
        println "<Referenced production classes: ${classes?.size()}>"
        classes?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listRelevantProductionClasses(ScenarioInterface scenarioInterface){
        println "<Relevant production classes: ${scenarioInterface.relevantClasses?.size()}>"
        scenarioInterface.relevantClasses?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj"
        }
        println "---------------------------------------------------------------------------------"
    }

    private static listGSP(ScenarioInterface scenarioInterface){
        println "<Referenced production GSP files: ${ scenarioInterface?.referencedPages?.size()}>"
        scenarioInterface?.referencedPages?.eachWithIndex{ obj, i ->
            println "(${i+1}) $obj"
        }
        println "---------------------------------------------------------------------------------"
    }

    @Override
    void generateAnalysisDetailedView(ScenarioInterface scenarioInterface){
        listReferencedClasses(scenarioInterface)
        listCalledMethods(scenarioInterface)
        listFields(scenarioInterface)
        listProperties(scenarioInterface)
        listStaticFields(scenarioInterface)
        listCalledPageMethods(scenarioInterface)
    }

    @Override
    void updateScenarioInterfaceOutput(ScenarioInterface scenarioInterface){
        listCalledProductionMethods(scenarioInterface)
        listReferencedProductionClasses(scenarioInterface)
        listRelevantProductionClasses(scenarioInterface)
        listGSP(scenarioInterface)
    }

}
