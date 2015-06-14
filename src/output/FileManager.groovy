package output

import utils.Utils


class FileManager implements ScenarioInterfaceManager {

    File file

    public FileManager(String path){
        this.file = new File(Utils.getInterfaceFileName(path))
        writeText("Code to analyse: ${Utils.getShortClassPath(path)}\n")
    }

    public FileManager(String featurePath, String scenarioName){
        this.file = new File(Utils.getInterfaceFileName(featurePath, scenarioName))
        writeText("Code to analyse: ${Utils.getShortClassPath(featurePath)}, scenario: $scenarioName\n")
    }

    private writeText(String text){
        file.withWriterAppend { out ->
            out.write(text+"\n")
        }
    }

    private listReferencedClasses(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Referenced classes: ${scenarioInterface?.classes?.size()}>\n")
            scenarioInterface?.classes?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listCalledMethods(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Called methods: ${scenarioInterface?.methods?.size()}>\n")
            scenarioInterface?.methods?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listFields(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Fields: ${scenarioInterface?.fields?.size()}>\n")
            scenarioInterface?.fields?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type: $obj.value\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listProperties(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Properties: ${scenarioInterface?.accessedProperties?.size()}>\n")
            scenarioInterface?.accessedProperties?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listStaticFields(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Static fields: ${scenarioInterface?.staticFields?.size()}>\n")
            scenarioInterface?.staticFields?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type: $obj.value\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listCalledPageMethods(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Called page methods: ${scenarioInterface?.calledPageMethods?.size()}>\n")
            scenarioInterface?.calledPageMethods?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.arg\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listCalledProductionMethods(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            def methods = scenarioInterface?.getProductionCalledMethods()
            out.write("<Called production Methods: ${methods?.size()}>\n")
            methods?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listReferencedProductionClasses(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            def classes =  scenarioInterface?.classes?.findAll{ !Utils.isTestCode(it) }
            out.write("<Referenced production classes: ${classes?.size()}>\n")
            classes?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listRelevantProductionClasses(ScenarioInterface scenarioInterface){
       file.withWriterAppend{ out ->
            out.write("<Relevant production classes: ${scenarioInterface.relevantClasses?.size()}>\n")
            scenarioInterface.relevantClasses?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listGSP(ScenarioInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Referenced production GSP files: ${ scenarioInterface?.referencedPages?.size()}>\n")
            scenarioInterface?.referencedPages?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
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
