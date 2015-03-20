package scenarioStepsMatcher

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import utils.Utils

class TestCodeParser {

    def config
    GroovyClassLoader classLoader
    Collection stepsDefinitionFiles
    List pluginsPath

    public TestCodeParser(){
        config = new ConfigSlurper().parse(new File(Utils.CONFIG_FILE_NAME).toURI().toURL())
        stepsDefinitionFiles = Utils.getGroovyFilesFromDirectory(config.project.test.steps.path)
        pluginsPath = []
        config.grails.plugin.path?.each{ k, v ->
            pluginsPath += v
        }
        configureClassLoader()
    }

    def getStepsDefinition(String featurePath){
        def regexList = parseStepsDefinitionFiles()

        def jsonPath = Utils.getJsonFileName(featurePath)
        ParserGherkinJson.parse(featurePath, jsonPath)
        def scenarios = ParserGherkinJson.getAllScenarios(jsonPath)

        def totalResult = []
        scenarios.each{ scenario ->
            def result = []
            scenario.steps.each { step ->
                def match = regexList.findAll{ step.name ==~ it.exp}
                if(match && match.size()==1) {
                    result += [stepLine:step.line, stepDefFile:match.file, stepDefLine:match.line, stepDefLastLine:match.lastLine]
                }
                else result += null
            }

            //when it is not possible to match any step to a step definition the result is null
            if( result.contains(null) ) totalResult += [featureFile: featurePath, scenario:scenario.name, match: null]
            else totalResult += [featureFile: featurePath, scenario:scenario.name, match: result]
        }

        totalResult
    }

    def getStepsDefinition(String featurePath, int scenarioLine){
        def regexList = parseStepsDefinitionFiles()

        def jsonPath = Utils.getJsonFileName(featurePath)
        ParserGherkinJson.parse(featurePath, jsonPath)
        def scenario = ParserGherkinJson.getScenario(scenarioLine, jsonPath)

        def result = []
        scenario.steps.each { step ->
            def match = regexList.findAll{ step.name ==~ it.exp}
            if(match && match.size()==1){
                result += [featureFile: featurePath, scenario:scenario.name, stepLine:step.line, stepDefFile:match.file,
                           stepDefLine:match.line, stepDefLastLine:match.lastLine]
            }
            else result += null
        }

        //when it is not possible to match any step to a step definition the result is null
        if( result.contains(null) ) null
        else result
    }

    def parseStepsDefinitionFiles(){
        def regexs = []
        stepsDefinitionFiles.each{ file ->
            def ast = generateAst(file)
            def visitor = new StepDefinitionVisitor(file)
            ClassNode classNode = ast.scriptClassDummy
            classNode.visitContents(visitor)
            regexs += visitor.regexList
        }
        regexs
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
        classLoader.addClasspath(config.project.production.path) //compiled code files
        classLoader.addClasspath(config.project.test.path) //compiled test code
    }

    private configurePlugins(){
        if(pluginsPath.isEmpty()){
            def jars = Utils.getJarFilesFromDirectory(config.grails.dependencyCache)
            jars?.each{
                classLoader.addClasspath(it)
            }
        }
        else{
            pluginsPath?.each{ path ->
                classLoader.addClasspath(path)
            }
        }
    }

}
