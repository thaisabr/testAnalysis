package analyser

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit


class ClassAnalyser {
    def config
    def projectFiles
    List pluginsPath
    GroovyClassLoader classLoader
    def visitor

    public ClassAnalyser(){
        this.config = new ConfigSlurper().parse(new File('Config.groovy').toURL())
        this.projectFiles = Utils.getFilesFromDirectory(config.project.path)
        this.pluginsPath = []
        config.grails.plugin.path?.each{ k, v ->
            this.pluginsPath += v
        }
        configureClassLoader()
        println "Code to analyse: ${config.project.test.file}\n"
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

    private getExternalValidClasses(int index){
        def externalValidClasses = []

        def set = visitor.calledMethods*.type as Set
        if(set.isEmpty()) externalValidClasses

        def list = set as List
        def classes = list?.subList(index, list.size())
        externalValidClasses = classes?.findAll{ it!= null && Utils.isTestCode(it) }

        externalValidClasses
    }

    private getMethodsToVisit(String className, String file, List visitedFiles){
        def methods = visitor.calledMethods.findAll{it.type == className}.sort{it.name}
        methods = methods*.name
        def alreadyVisitedFile = visitedFiles?.find{ it.path == file }

        if(alreadyVisitedFile){
            if(alreadyVisitedFile.methods != methods){
                methods = (alreadyVisitedFile.methods+methods as Set).sort()
            }
            else{
                methods = []
            }
        }

        methods
    }

    private listFilesToVisit(int index, List visitedFiles){
        def files = []
        def externalValidClasses = getExternalValidClasses(index)

        externalValidClasses.each{ className ->
            def file = Utils.getClassPath(className, projectFiles)
            if(file){
                def methods = getMethodsToVisit(className, file, visitedFiles)
                if(!methods.isEmpty()) files += [path:file, methods:methods] //file to analyse and its methods
            }
        }

        files
    }

    def doDirectAnalysis(){
        def ast = generateAst(config.project.test.file)
        ClassNode classNode = ast.scriptClassDummy
        visitor = new Visitor(classNode.name, projectFiles)
        classNode.visitContents(visitor)
    }

    def doIndirectAnalysis(){
        def visitedFiles = []
        if(!visitor) doDirectAnalysis()

        def files = listFilesToVisit(0, visitedFiles)
        while(!files.isEmpty()) {
            files.each { f ->
                def ast = generateAst(f.path)
                def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, f.methods, visitor)
                ast.classes.get(0).visitContents(auxVisitor)
            }
            visitedFiles += files
            files = listFilesToVisit(files.size(), visitedFiles)
        }

        println "Visited files during indirect analysis: "
        visitedFiles.each{ arq ->
            println arq.path
        }
        println "---------------------------------------------------------------------------------"
    }

    def extractGSPClassesName(){
        def pageCodeVisitor = new PageCodeVisitor(projectFiles, config.project.path)
        def filesToVisit = visitor?.calledPageMethods*.arg as Set
        filesToVisit?.each{ f ->
            def file = Utils.getClassPath(f, projectFiles)
            if(file){
                def ast = generateAst(file)
                ast.classes.get(0).visitContents(pageCodeVisitor)
            }
        }
        pageCodeVisitor.pages
    }

    def printAnalysisResult(){
        listReferencedClasses()
        listCalledMethods()
        listFields()
        listProperties()
        listStaticFields()
        listCalledPageMethods()
    }

    def listReferencedClasses(){
        println "Referenced classes: "
        visitor?.referencedClasses?.eachWithIndex{ obj, i ->
            println "($i) $obj.name"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listCalledMethods(){
        println "Called methods: "
        visitor?.calledMethods?.eachWithIndex{ obj, i ->
            println "($i) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listFields(){
        println "Fields: "
        visitor?.fields?.eachWithIndex{ obj, i ->
            println "($i) $obj.name: $obj.type: $obj.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listProperties(){
        println "Properties: "
        visitor?.accessedProperties?.eachWithIndex{ obj, i ->
            println "($i) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listStaticFields(){
        println "Static fields: "
        visitor?.staticFields?.eachWithIndex{ obj, i ->
            println "($i) $obj.name: $obj.type: $obj.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listCalledPageMethods(){
        println "Called page methods: "
        visitor?.calledPageMethods?.eachWithIndex{ obj, i ->
            println "($i) $obj.name: $obj.arg"
        }
        println "---------------------------------------------------------------------------------"
    }

    def printAnalysisResultFromProductionCode(){
        listCalledProductionMethods()
        listReferencedProductionClasses()
        listGSP()
        println "---------------------------------------------------------------------------------"
    }

    def listCalledProductionMethods(){
        println "<Called production Methods>"
        def methods = visitor?.calledMethods?.findAll{ it.type!=null && !Utils.isTestCode(it.type) }
        methods?.eachWithIndex{ obj, i ->
            println "($i) $obj.name: $obj.type"
        }
    }

    def listGSP(){
        println "<Referenced production files>"
        def pages = extractGSPClassesName()
        pages?.eachWithIndex{ obj, i ->
            println "($i) $obj"
        }
    }

    def listReferencedProductionClasses(){
        println "<Referenced production classes>"
        def classes =  visitor?.referencedClasses?.findAll{ !Utils.isTestCode(it.name) }
        classes?.eachWithIndex{ obj, i ->
            println "($i) $obj.name"
        }
    }

}
