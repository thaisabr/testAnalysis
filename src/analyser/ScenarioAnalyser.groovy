package analyser

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import utils.Utils
import output.ScenarioInterface
import output.ScenarioInterfaceFileManager
import scenarioParser.Scenario
import scenarioParser.TestCodeParser


class ScenarioAnalyser {

    Collection projectFiles
    List pluginsPath
    GroovyClassLoader classLoader
    TestCodeParser parser

    public ScenarioAnalyser(){
        projectFiles = Utils.getFilesFromDirectory(Utils.config.project.path)
        pluginsPath = []
        Utils.config.grails.plugin.path?.each{ k, v ->
            pluginsPath += v
        }
        configureClassLoader()
        parser = new TestCodeParser()
    }

    private configureClassLoader(){
        classLoader = new GroovyClassLoader()

        configurePlugins()

        //compiled code files
        classLoader.addClasspath(Utils.config.project.production.path)
        println "Compiled code path: ${Utils.config.project.production.path}"

        //compiled test code
        classLoader.addClasspath(Utils.config.project.test.path)
        println "Compiled test code path: ${Utils.config.project.test.path}"
    }

    private configurePlugins(){
        if(pluginsPath.isEmpty()){
            def jars = Utils.getJarFilesFromDirectory(Utils.config.grails.dependencyCache)
            jars?.each{
                classLoader.addClasspath(it)
                println "jar: $it"
            }
        }
        else{
            pluginsPath?.each{ path ->
                classLoader.addClasspath(path)
                println "Plugin path: $path"
            }
        }
    }

    private static getFilesToAnalyse(Scenario scenario){
        def organizedFiles = []
        def stepsList = scenario.testcode.stepDefinition
        def stepFiles = stepsList*.file as Set
        stepFiles.each{ s ->
            def steps = stepsList.findAll{ it.file.equals(s) }
            def lines = steps*.line
            organizedFiles += [path:s, lines:lines]
        }
        organizedFiles
    }

    private generateAst(String path){
        def file = new File(path)
        SourceUnit unit = SourceUnit.create(file.name, file.text) //or file.absolutePath
        CompilationUnit compUnit = new CompilationUnit(classLoader)
        compUnit.addSource(unit)
        compUnit.compile(Phases.SEMANTIC_ANALYSIS)
        unit.getAST()
    }

    private Visitor doFirstLevelAnalysis(Map file){
        def ast = generateAst(file.path)
        def visitor = new Visitor(ast.scriptClassDummy.name, projectFiles)
        def testCodeVisitor = new TestCodeVisitor(file.lines, visitor)
        ast.classes.get(0).visitContents(testCodeVisitor)
        visitor
    }

    private listFilesToVisit(Collection lastCalledMethods, Collection allVisitedFiles){
        def files = []
        def externalValidClasses = lastCalledMethods*.type as Set
        externalValidClasses = externalValidClasses?.findAll { it != null && Utils.isTestCode(it) }

        externalValidClasses.each{ className ->
            def file = Utils.getClassPath(className, projectFiles)
            if(file){
                def methods = getMethodsToVisit(className, file, lastCalledMethods, allVisitedFiles)
                if(!methods.isEmpty()) files += [path:file, methods:methods] //file to analyse and its methods
            }
        }

        files
    }

    private static getMethodsToVisit(String className, String file, Collection lastCalledMethods, Collection visitedFiles){
        def methods = lastCalledMethods?.findAll{it.type == className}?.sort{it.name}
        methods = methods*.name
        def index = visitedFiles?.findLastIndexOf{ it.path == file }
        if(index != -1) {
            def alreadyVisitedFile = visitedFiles[index]
            if (alreadyVisitedFile.methods != methods) {
                methods = (alreadyVisitedFile.methods + methods as Set).sort()
            } else {
                methods = []
            }
        }

        methods
    }

    private extractGSPClassesName(Visitor visitor){
        def pageCodeVisitor = new PageCodeVisitor(projectFiles)
        def filesToVisit = visitor?.scenarioInterface?.calledPageMethods*.arg as Set
        filesToVisit?.each{ f ->
            def file = Utils.getClassPath(f, projectFiles)
            if(file){
                def ast = generateAst(file)
                ast.classes.get(0).visitContents(pageCodeVisitor)
            }
        }
        visitor?.scenarioInterface?.referencedPages = pageCodeVisitor.pages
    }

    List<ScenarioInterface> computeTaskInterfacesForFeature(String featurePath){
        def scenarioInterfaces = []
        def scenarios = parser.getFeatureCode(featurePath)
        scenarios.each { scenario ->
            def scenarioInterface = computeTaskInterface(scenario)
            scenarioInterfaces += [scenario:scenario, interface:scenarioInterface]
        }
        return scenarioInterfaces
    }

    ScenarioInterface computeTaskInterface(Scenario scenario){
        def firstStepFiles = getFilesToAnalyse(scenario)
        def interfaceManager = new ScenarioInterfaceFileManager(scenario.file, scenario.name)
        def scenarioInterface = new ScenarioInterface()

        firstStepFiles.each { stepFile ->
            def visitor = doFirstLevelAnalysis(stepFile)
            def visitedFiles = []
            def files = listFilesToVisit(visitor.scenarioInterface.methods, visitedFiles)

            while (!files.isEmpty()) {
                def backupCalledMethods = visitor.scenarioInterface.methods
                files.each { f ->
                    def ast = generateAst(f.path)
                    def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, f.methods, visitor)
                    ast.classes.get(0).visitContents(auxVisitor)
                }
                visitedFiles += files
                def lastCalledMethods = visitor.scenarioInterface.methods - backupCalledMethods
                files = listFilesToVisit(lastCalledMethods, visitedFiles)
            }

            extractGSPClassesName(visitor)
            scenarioInterface.update(visitor.scenarioInterface)
        }

        interfaceManager.updateScenarioInterfaceFile(scenarioInterface)
        return scenarioInterface
    }

    ScenarioInterface computeTaskInterface(String featurePath, int line){
        def scenario = parser.getScenarioCode(featurePath, line)
        def firstStepFiles = getFilesToAnalyse(scenario)
        def interfaceManager = new ScenarioInterfaceFileManager(featurePath, scenario.name)
        def scenarioInterface = new ScenarioInterface()

        firstStepFiles.each { stepFile ->
            def visitor = doFirstLevelAnalysis(stepFile)
            def visitedFiles = []
            def files = listFilesToVisit(visitor.scenarioInterface.methods, visitedFiles)

            while (!files.isEmpty()) {
                def backupCalledMethods = visitor.scenarioInterface.methods
                files.each { f ->
                    def ast = generateAst(f.path)
                    def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, f.methods, visitor)
                    ast.classes.get(0).visitContents(auxVisitor)
                }
                visitedFiles += files
                def lastCalledMethods = visitor.scenarioInterface.methods - backupCalledMethods
                files = listFilesToVisit(lastCalledMethods, visitedFiles)
            }

            extractGSPClassesName(visitor)
            scenarioInterface.update(visitor.scenarioInterface)
        }

        interfaceManager.updateScenarioInterfaceFile(scenarioInterface)
        return scenarioInterface
    }

    ScenarioInterface computeTaskInterface(){
        def scenario = parser.getScenarioCode(Utils.config.scenario.path, Utils.config.scenario.line)
        def firstStepFiles = getFilesToAnalyse(scenario)
        def interfaceManager = new ScenarioInterfaceFileManager(Utils.config.scenario.path, scenario.name)
        def scenarioInterface = new ScenarioInterface()

        firstStepFiles.each { stepFile ->
            def visitor = doFirstLevelAnalysis(stepFile)
            def visitedFiles = []
            def files = listFilesToVisit(visitor.scenarioInterface.methods, visitedFiles)

            while (!files.isEmpty()) {
                def backupCalledMethods = visitor.scenarioInterface.methods
                files.each { f ->
                    def ast = generateAst(f.path)
                    def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, f.methods, visitor)
                    ast.classes.get(0).visitContents(auxVisitor)
                }
                visitedFiles += files
                def lastCalledMethods = visitor.scenarioInterface.methods - backupCalledMethods
                files = listFilesToVisit(lastCalledMethods, visitedFiles)
            }

            extractGSPClassesName(visitor)
            scenarioInterface.update(visitor.scenarioInterface)
        }

        interfaceManager.updateScenarioInterfaceFile(scenarioInterface)
        return scenarioInterface
    }

    List<ScenarioInterface> computeTaskInterfaces(){
        def interfaces = []
        Utils.config.scenario.lines.each{ scenarioLine ->
            def scenario = parser.getScenarioCode(Utils.config.scenario.path, scenarioLine)
            def firstStepFiles = getFilesToAnalyse(scenario)
            def interfaceManager = new ScenarioInterfaceFileManager(Utils.config.scenario.path, scenario.name)
            def scenarioInterface = new ScenarioInterface()

            firstStepFiles.each { stepFile ->
                def visitor = doFirstLevelAnalysis(stepFile)
                def visitedFiles = []
                def files = listFilesToVisit(visitor.scenarioInterface.methods, visitedFiles)

                while (!files.isEmpty()) {
                    def backupCalledMethods = visitor.scenarioInterface.methods
                    files.each { f ->
                        def ast = generateAst(f.path)
                        def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, f.methods, visitor)
                        ast.classes.get(0).visitContents(auxVisitor)
                    }
                    visitedFiles += files
                    def lastCalledMethods = visitor.scenarioInterface.methods - backupCalledMethods
                    files = listFilesToVisit(lastCalledMethods, visitedFiles)
                }

                extractGSPClassesName(visitor)
                scenarioInterface.update(visitor.scenarioInterface)
            }

            interfaceManager.updateScenarioInterfaceFile(scenarioInterface)
            interfaces += scenarioInterface
        }
        return interfaces
    }

}
