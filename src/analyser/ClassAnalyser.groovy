package analyser

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.springframework.util.ClassUtils


class ClassAnalyser {
    def testFile
    def projectDir
    def projectFiles
    def pluginsPath
    def codeFilesPath
    def testFilesPath
    def visitor

    static final GRAILS_PATH = "${System.getProperty("user.home")}${File.separator}.grails${File.separator}ivy-cache"
    static final CUCUMBER_PATH = "$GRAILS_PATH${File.separator}info.cukes${File.separator}cucumber-groovy${File.separator}jars${File.separator}cucumber-groovy-1.1.1.jar"
    static final GEB_PATH = "$GRAILS_PATH${File.separator}org.codehaus.geb${File.separator}geb-core${File.separator}jars${File.separator}geb-core-0.7.1.jar"

    public ClassAnalyser(String testFile, String projectDir, Set pluginPaths){
        this(testFile, projectDir)
        this.pluginsPath = pluginPaths
    }

    public ClassAnalyser(String testFile, String projectDir){
        this.testFile = testFile
        this.projectDir = projectDir
        this.projectFiles = Utils.getFilesFromDirectory(projectDir)
        this.codeFilesPath = "${projectDir}${File.separator}target${File.separator}classes"
        this.testFilesPath = "${projectDir}${File.separator}target${File.separator}test-classes${File.separator}functional"
        this.pluginsPath = []
    }

    private static generateAst(String path, GroovyClassLoader classLoader){
        def file = new File(path)
        SourceUnit unit = SourceUnit.create(file.name, file.text) //or file.absolutePath
        CompilationUnit compUnit = new CompilationUnit(classLoader)
        compUnit.addSource(unit)
        compUnit.compile(Phases.SEMANTIC_ANALYSIS)
        unit.getAST()
    }

    private configureClassLoader(){
        def classLoader = new GroovyClassLoader()

        //plugin Cucumber
        classLoader.addClasspath(CUCUMBER_PATH)

        //plugin Geb
        classLoader.addClasspath(GEB_PATH)

        configurePlugins(classLoader)

        //compiled code files
        classLoader.addClasspath(codeFilesPath)

        //compiled code test
        classLoader.addClasspath(testFilesPath)

        classLoader
    }

    private configurePlugins(GroovyClassLoader classLoader){
        pluginsPath.each{ path ->
            classLoader.addClasspath(path)
        }
    }

    private getAst(String path){
        def classLoader = configureClassLoader()
        generateAst(path, classLoader)
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

    private String getClassPath(String className){
        def name = ClassUtils.convertClassNameToResourcePath(className)+".groovy"
        name = name.replace("/", "\\")
        projectFiles.find{it.contains(name)}
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
            def file = getClassPath(className)
            if(file){
                def methods = getMethodsToVisit(className, file, visitedFiles)
                if(!methods.isEmpty()) files += [path:file, methods:methods] //file to analyse and its methods
            }
        }

        files
    }

    def doDirectAnalysis(){
        def ast = getAst(testFile)
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
                def ast = getAst(f.path)
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

    def getCalledMethods(){
        visitor?.calledMethods
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
        visitor?.referencedClasses?.each{
            println "$it.name"
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
        visitor?.fields?.each{
            println "$it.name: $it.type: $it.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listProperties(){
        println "Properties: "
        visitor?.accessedProperties?.each{
            println "$it.name: $it.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listStaticFields(){
        println "Static Fields: "
        visitor?.staticFields?.each{
            println "$it.name: $it.type: $it.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listCalledPageMethods(){
        println "Called page methods: "
        visitor?.calledPageMethods?.each{
            println "$it.name: $it.arg"
        }
        println "---------------------------------------------------------------------------------"
    }

}
