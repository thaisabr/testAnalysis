package analyser

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.springframework.util.ClassUtils


class ClassAnalyser {
    def fileName
    def projectSource
    def isGrailsProject
    def projectFiles
    def testPath
    def visitor

    static final GRAILS_PATH = "${System.getProperty("user.home")}${File.separator}.grails${File.separator}ivy-cache"
    static final CUCUMBER_PATH = "$GRAILS_PATH${File.separator}info.cukes${File.separator}cucumber-groovy${File.separator}jars${File.separator}cucumber-groovy-1.1.1.jar"
    static final GEB_PATH = "$GRAILS_PATH${File.separator}org.codehaus.geb${File.separator}geb-core${File.separator}jars${File.separator}geb-core-0.7.1.jar"
    static final SHIRO_PATH = "$GRAILS_PATH${File.separator}org.apache.shiro${File.separator}shiro-core${File.separator}bundles${File.separator}shiro-core-1.2.0.jar"

    public ClassAnalyser(String fileName, String projectSource, boolean isGrailsProject){
        this.fileName = fileName
        this.projectSource = projectSource
        this.isGrailsProject = isGrailsProject
        this.projectFiles = Utils.getFilesFromDirectory(projectSource)
        this.testPath = "$projectSource${File.separator}test"
    }

    private generateAstForGrailsProject(String path){
        def classLoader = new GroovyClassLoader()

        //plugin Cucumber
        classLoader.addClasspath(CUCUMBER_PATH)

        //plugin Geb
        classLoader.addClasspath(GEB_PATH)

        //plugin Shiro
        classLoader.addClasspath(SHIRO_PATH)

        //arquivos .class do projeto
        classLoader.addClasspath("${projectSource}${File.separator}target${File.separator}classes")

        //diretório principal dos arquivos .class de testes
        classLoader.addClasspath("${projectSource}${File.separator}target${File.separator}test-classes${File.separator}functional")

        classLoader.addClasspath("${projectSource}${File.separator}grails-app")
        classLoader.addClasspath("${projectSource}${File.separator}src")

        def file = new File(path)
        SourceUnit unit = SourceUnit.create(file.name, file.text) //ou file.absolutePath
        CompilationUnit compUnit = new CompilationUnit(classLoader)
        compUnit.addSource(unit)
        compUnit.compile(Phases.SEMANTIC_ANALYSIS)

        unit.getAST()
    }

    private generateAst(String path){
        def classLoader = new GroovyClassLoader()

        //plugin Cucumber
        classLoader.addClasspath(CUCUMBER_PATH)

        //plugin Geb
        classLoader.addClasspath(GEB_PATH)

        //arquivos .class do projeto
        classLoader.addClasspath(projectSource)

        def file = new File(path)
        SourceUnit unit = SourceUnit.create(file.name, file.text) //ou file.absolutePath
        CompilationUnit compUnit = new CompilationUnit(classLoader)
        compUnit.addSource(unit)
        compUnit.compile(Phases.SEMANTIC_ANALYSIS)

        unit.getAST()
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

    private getClassPath(String className){
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

    /* Retorna uma lista de classes e seus respectivos métodos para serem analisados. */
    private listFilesToVisit(int index, List visitedFiles){
        def files = []
        def externalValidClasses = getExternalValidClasses(index)

        externalValidClasses.each{ className ->
            def file = getClassPath(className)
            if(file){
                def methods = getMethodsToVisit(className, file, visitedFiles)
                if(!methods.isEmpty()) files += [path:file, methods:methods]
            }
        }

        files
    }

    def doDirectAnalysis(){
        def ast = isGrailsProject? generateAstForGrailsProject(fileName):generateAst(fileName)
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
                def ast = isGrailsProject ? generateAstForGrailsProject(f.path) : generateAst(f.path)
                def auxVisitor = new MethodVisitor(ast.scriptClassDummy.name, projectFiles, f.methods, visitor)
                ast.classes.get(0).visitContents(auxVisitor)
            }
            visitedFiles += files
            files = listFilesToVisit(files.size(), visitedFiles)
        }

        println "ARQUIVOS VISITADOS NA ANÁLISE INDIRETA"
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
        visitor?.referencedClasses.each{
            println "$it.name"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listCalledMethods(){
        println "Called methods: "
        visitor?.calledMethods.eachWithIndex{ obj, i ->
            println "($i) $obj.name: $obj.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listFields(){
        println "Fields: "
        visitor?.fields.each{
            println "$it.name: $it.type: $it.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listProperties(){
        println "Properties: "
        visitor?.accessedProperties.each{
            println "$it.name: $it.type"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listStaticFields(){
        println "Static Fields: "
        visitor?.staticFields.each{
            println "$it.name: $it.type: $it.value"
        }
        println "---------------------------------------------------------------------------------"
    }

    def listCalledPageMethods(){
        println "Called page methods: "
        visitor?.calledPageMethods.each{
            println "$it.name: $it.arg"
        }
        println "---------------------------------------------------------------------------------"
    }

}
