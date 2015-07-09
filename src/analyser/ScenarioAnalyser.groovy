package analyser

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import output.Task
import utils.Utils
import output.TestInterface
import output.FileManager
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
        configureClassLoader()
        parser = new TestCodeParser()
    }

    private configureClassLoader(){
        classLoader = new GroovyClassLoader()

        Utils.fillPluginsPath(pluginsPath)
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

    private static List getFilesToAnalyse(Scenario scenario){
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

    private static updateVisitedFiles(List allVisitedFiles, List filesToVisit){
        def allFiles = allVisitedFiles + filesToVisit
        def paths = (allFiles*.path)?.unique()
        def result = []
        paths?.each{ path ->
            def methods = (allFiles?.findAll{ it.path == path}*.methods)?.flatten()?.unique()
            if(methods!=null && !methods.isEmpty()) result += [path:path, methods:methods]
        }
        return result
    }

    private static listTestFilesToVisit(Collection lastCalledMethods){
        def testFiles = []
        def externalTestMethods = lastCalledMethods?.findAll{ it.type!=null && Utils.isTestCode(it.type) }?.unique()
        externalTestMethods*.file.unique().each{ path ->
            def methods = externalTestMethods.findAll{ it.file == path }*.name
            testFiles += [path:path, methods:methods]
        }
        return testFiles
    }

    //keywords of lastCalledMethods: [name, type, file]
    //keywords of allVisitedFiles: [path:[], methods:[]]
    private static listFilesToVisit(Collection lastCalledMethods, List allVisitedFiles){
        def testFiles = listTestFilesToVisit(lastCalledMethods)
        def filesToVisit = []
        testFiles.each{ file ->
            def match = allVisitedFiles?.find{ it.path == file.path }
            if(match!=null) {
                filesToVisit += [path:file.path, methods:file.methods-match.methods]
            }
            else {
                filesToVisit += [path:file.path, methods:file.methods]
            }
        }
        return filesToVisit
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

    private fillGspPath(Visitor visitor){
        def pageCodeVisitor = new PageCodeVisitor(projectFiles)
        def filesToVisit = visitor?.scenarioInterface?.calledPageMethods*.file as Set
        filesToVisit?.each{ f ->
            if(f != null){ //f could be null if the test code references a class or file that does not exist
                generateAst(f).classes.get(0).visitContents(pageCodeVisitor)
            }
        }
        visitor?.scenarioInterface?.referencedPages = pageCodeVisitor.pages
    }

    private visitFile(def file, Visitor visitor){
        def ast = generateAst(file.path)
        def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, file.methods, visitor)
        ast.classes.get(0).visitContents(auxVisitor)
    }

    private TestInterface search(List firstStepFiles){
        def scenarioInterface = new TestInterface()
        firstStepFiles.each { stepFile ->
            def visitor = doFirstLevelAnalysis(stepFile)
            def visitedFiles = [] //format:[path:[], methods:[]]
            def files = listFilesToVisit(visitor.scenarioInterface.methods, visitedFiles)

            while (!files.isEmpty()) {
                def backupCalledMethods = visitor.scenarioInterface.methods //keys:[name, type, file]
                files.each { f ->
                    visitFile(f, visitor)
                }
                visitedFiles = updateVisitedFiles(visitedFiles, files)
                def lastCalledMethods = visitor.scenarioInterface.methods - backupCalledMethods
                files = listFilesToVisit(lastCalledMethods, visitedFiles)
            }

            fillGspPath(visitor)
            scenarioInterface.update(visitor.scenarioInterface)
        }
        return scenarioInterface
    }

    /* Computing task interface for each scenario of a feature, considering each scenario as a task. */
    List<Task> computeTaskInterfacesForFeature(String featurePath){
        def tasks = []
        def scenarios = parser.getFeatureCode(featurePath)
        scenarios.each { scenario ->
            tasks += computeTaskInterface(scenario)
        }
        return tasks
    }

    /* Computing task interface for a scenario */
    Task computeTaskInterface(Scenario scenario){
        if(scenario == null) return null
        def firstStepFiles = getFilesToAnalyse(scenario)
        def scenarioInterface = search(firstStepFiles)
        def interfaceManager = new FileManager(scenario.file, scenario.line.toString())
        interfaceManager.updateScenarioInterfaceOutput(scenarioInterface)
        return new Task(scenarios:[scenario], testInterface:scenarioInterface)
    }

    /* Computing task interface for a group of scenarios, considering all of them as an unique task. */
    Task computeTaskInterface(TaskDescription... descriptions) {
        if(descriptions == null || descriptions.length==0) return null
        Task task = new Task()
        descriptions.each{ description ->
            def scenarios = parser.getScenariosCode(description.path, description.lines)
            scenarios?.each{ scenario ->
                def firstStepFiles = getFilesToAnalyse(scenario)
                def scenarioInterface = search(firstStepFiles)
                task.scenarios += scenario
                task.testInterface.update(scenarioInterface)
            }
        }
        def interfaceManager = new FileManager(descriptions)
        interfaceManager.updateScenarioInterfaceOutput(task.testInterface)
        return task
    }

    Task computeTaskInterface(String featurePath, int... lines){
        if(lines == null || lines.length==0) return null
        Task task = new Task()
        lines?.each{ line ->
            def scenario = parser.getScenarioCode(featurePath, line)
            def firstStepFiles = getFilesToAnalyse(scenario)
            def scenarioInterface = search(firstStepFiles)
            task.scenarios += scenario
            task.testInterface.update(scenarioInterface)
        }
        def interfaceManager = new FileManager(featurePath, lines.toString())
        interfaceManager.updateScenarioInterfaceOutput(task.testInterface)
        return task
    }

    /* Computing task interface for a scenario that is specified at the configuration file. */
    Task computeTaskInterface(){
        def scenario = parser.getScenarioCode(Utils.config.scenario.path, Utils.config.scenario.line)
        if(scenario == null) return null
        def firstStepFiles = getFilesToAnalyse(scenario)
        def scenarioInterface = search(firstStepFiles)
        def interfaceManager = new FileManager(Utils.config.scenario.path, scenario.line.toString())
        interfaceManager.updateScenarioInterfaceOutput(scenarioInterface)
        return new Task(scenarios:[scenario], testInterface:scenarioInterface)
    }

    /* Computing task interface for each scenario of a feature that is specified at the configuration file, considering
     each scenario as a task. */
    List<Task> computeTaskInterfaces(){
        def tasks = []
        Utils.config.scenario.lines.each{ scenarioLine ->
            def scenario = parser.getScenarioCode(Utils.config.scenario.path, scenarioLine)
            def firstStepFiles = getFilesToAnalyse(scenario)
            def scenarioInterface = search(firstStepFiles)
            def interfaceManager = new FileManager(Utils.config.scenario.path, scenario.line.toString())
            interfaceManager.updateScenarioInterfaceOutput(scenarioInterface)
            tasks += new Task(scenarios:[scenario], testInterface:scenarioInterface)
        }
        return tasks
    }

}
