package output

import analyser.TaskDescription
import utils.Utils


class FileManager implements TestInterfaceManager {

    File file

    public FileManager(TaskDescription... descriptions){
        if(descriptions.size()==1){
            this.file = new File(Utils.getInterfaceFileName(descriptions.getAt(0).path, descriptions.getAt(0).lines.toString()))
            writeText("Code to analyse: ${Utils.getShortClassPath(descriptions.getAt(0).path)}, scenario: ${descriptions.getAt(0).lines.toString()}\n")
        }
        else{
            this.file = new File(Utils.getCompoundInterfaceFileName())
            def path = descriptions.collect{ Utils.getShortClassPath(it.path) }.toString()
            writeText("Codes to analyse: $path\n")
        }
    }

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

    private listReferencedClasses(TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Referenced classes: ${scenarioInterface?.classes?.size()}>\n")
            scenarioInterface?.classes?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listCalledMethods(TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Called methods: ${scenarioInterface?.methods?.size()}>\n")
            scenarioInterface?.methods?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listFields(String title, TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write(title)
            scenarioInterface?.fields?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type: $obj.value\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listFields(TestInterface scenarioInterface){
        listFields("<Fields: ${scenarioInterface?.fields?.size()}>\n", scenarioInterface)
    }

    private listStaticFields(TestInterface scenarioInterface){
        listFields("<Static fields: ${scenarioInterface?.staticFields?.size()}>\n", scenarioInterface)
    }

    private listProperties(TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Properties: ${scenarioInterface?.accessedProperties?.size()}>\n")
            scenarioInterface?.accessedProperties?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listCalledPageMethods(TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Called page methods: ${scenarioInterface?.calledPageMethods?.size()}>\n")
            scenarioInterface?.calledPageMethods?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.arg\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listCalledProductionMethods(TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            def methods = scenarioInterface?.getProductionCalledMethods()
            out.write("<Called production Methods: ${methods?.size()}>\n")
            methods?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name: $obj.type\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listReferencedProductionClasses(TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            def classes =  scenarioInterface?.classes?.findAll{ !Utils.isTestCode(it.name) }
            out.write("<Referenced production classes: ${classes?.size()}>\n")
            classes?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj.name\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listRelevantProductionClasses(TestInterface scenarioInterface){
       file.withWriterAppend{ out ->
            out.write("<Relevant production classes: ${scenarioInterface.relevantClasses?.size()}>\n")
            scenarioInterface.relevantClasses?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
    }

    private listGSP(TestInterface scenarioInterface){
        file.withWriterAppend{ out ->
            out.write("<Referenced production GSP files: ${ scenarioInterface?.referencedPages?.size()}>\n")
            scenarioInterface?.referencedPages?.eachWithIndex{ obj, i ->
                out.write("(${i+1}) $obj\n")
            }
            out.write("---------------------------------------------------------------------------------\n")
        }
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

