package stepsFileParser

import analyser.MethodVisitor
import analyser.PageCodeVisitor
import analyser.Visitor
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import output.ScenarioInterfaceManager
import utils.Utils


class ClassAnalyser {
    def analysedFile
    def config
    Collection projectFiles
    List pluginsPath
    GroovyClassLoader classLoader
    Visitor visitor
    ScenarioInterfaceManager interfaceManager

    public ClassAnalyser(){
        config = new ConfigSlurper().parse(new File(Utils.CONFIG_FILE_NAME).toURI().toURL())
        analysedFile = config.project.test.file
        projectFiles = Utils.getFilesFromDirectory(config.project.path)
        pluginsPath = []
        config.grails.plugin.path?.each{ k, v ->
            pluginsPath += v
        }
        configureClassLoader()
        interfaceManager = new ScenarioInterfaceManager(analysedFile)
    }

    public ClassAnalyser(String fileToAnalyse){
        this()
        analysedFile = fileToAnalyse
        interfaceManager = new ScenarioInterfaceManager(analysedFile)
    }

    private generateAst(String path){
        def file = new File(path)
        SourceUnit unit = SourceUnit.create(file.name, file.text) //or file.absolutePath
        CompilationUnit compUnit = new CompilationUnit(classLoader)
        compUnit.addSource(unit)
        compUnit.compile(Phases.SEMANTIC_ANALYSIS)
        unit.getAST()
    }

    private configureClassLoader(){
        classLoader = new GroovyClassLoader()

        configurePlugins()

        //compiled code files
        classLoader.addClasspath(config.project.production.path)
        println "Compiled code path: ${config.project.production.path}"

        //compiled test code
        classLoader.addClasspath(config.project.test.path)
        println "Compiled test code path: ${config.project.test.path}"
    }

    private configurePlugins(){
        if(pluginsPath.isEmpty()){
            def jars = Utils.getJarFilesFromDirectory(config.grails.dependencyCache)
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

    private extractGSPClassesName(){
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

    private doBasicAnalysis(){
        def ast = generateAst(analysedFile)
        ClassNode classNode = ast.scriptClassDummy
        visitor = new Visitor(classNode.name, projectFiles)
        classNode.visitContents(visitor)
    }

    def doDirectAnalysis(){
        doBasicAnalysis()
        interfaceManager.generateScenarioInterface(visitor.scenarioInterface)
    }

    def doIndirectAnalysis(){
        def visitedFiles = []
        if(!visitor) doBasicAnalysis()

        def files = listFilesToVisit(visitor.scenarioInterface.calledMethods, visitedFiles)
        while(!files.isEmpty()) {
            def backupCalledMethods = visitor.scenarioInterface.calledMethods
            files.each { f ->
                def ast = generateAst(f.path)
                def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, f.methods, visitor)
                ast.classes.get(0).visitContents(auxVisitor)
            }
            visitedFiles += files
            def lastCalledMethods = visitor.scenarioInterface.calledMethods - backupCalledMethods
            files = listFilesToVisit(lastCalledMethods, visitedFiles)
        }

        extractGSPClassesName()
        interfaceManager.generateScenarioInterface(visitor.scenarioInterface)

        println "Visited files during indirect analysis: "
        def aux = (visitedFiles*.path as Set).sort()
        aux.each{ file ->
            println file
        }
        println "---------------------------------------------------------------------------------"
    }

}
