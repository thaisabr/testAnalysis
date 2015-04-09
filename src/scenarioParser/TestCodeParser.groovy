package scenarioParser

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

    public List<Scenario> getFeatureCode(String featurePath){
        def regexList = parseStepsDefinitionFiles()
        def scenarios = ParserGherkinJson.getAllScenarios(featurePath)

        def totalResult = []
        scenarios.each{ scenario ->
            def result = []
            scenario.steps.each { step ->
                def matchedRegex = regexList.findAll{ step.name ==~ it.regex }
                if(matchedRegex && matchedRegex.size()==1) {
                    def match = matchedRegex[0]
                    result += new Match(stepLine:step.line, stepDefinition:match)
                }
                else result += null
            }

            //when it is not possible to match any step to a step definition the result is null
            if( result.contains(null) ) totalResult += new Scenario(name:scenario.name, line:scenario.line, file: featurePath, testcode:null)
            else totalResult += new Scenario(name:scenario.name, line:scenario.line, file: featurePath, testcode:result)
        }

        totalResult
    }

    public Scenario getScenarioCode(String featurePath, int scenarioLine){
        def regexList = parseStepsDefinitionFiles()
        def scenarioGherkin = ParserGherkinJson.getScenario(featurePath, scenarioLine)

        List<Match> result = []
        scenarioGherkin.steps.each { step ->
            def matchedRegex = regexList.findAll{ step.name ==~ it.regex}
            if(matchedRegex && matchedRegex.size()==1){
                def match = matchedRegex[0]
                result += new Match(stepLine:step.line, stepDefinition:match)
            }
            else result += null
        }

        //when it is not possible to match any step to a step definition the result is null
        if( result.contains(null) ) null
        else new Scenario(name:scenarioGherkin.name, line:scenarioLine, file: featurePath, testcode:result)
    }

    def parseStepsDefinitionFiles(){
        List<StepDefinition> regexs = []
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
