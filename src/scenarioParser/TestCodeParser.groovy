package scenarioParser

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import utils.Utils

class TestCodeParser {

    GroovyClassLoader classLoader
    List pluginsPath
    List<StepDefinition> regexList

    public TestCodeParser(){
        pluginsPath = []
        Utils.config.grails.plugin.path?.each{ k, v ->
            pluginsPath += v
        }
        configureClassLoader()
        def stepsDefinitionFiles = Utils.getGroovyFilesFromDirectory(Utils.config.project.test.steps.path)
        regexList = parseStepsDefinitionFiles(stepsDefinitionFiles)
    }

    /* The return is the list of scenarios to analyse */
    public List<Scenario> getFeatureCode(String featurePath){
        def scenarios = ParserGherkinJson.getAllScenarios(featurePath)

        def totalResult = []
        scenarios.each{ scenario ->
            def result = getScenarioStepCode(scenario)
            //when it is not possible to match at least a step definition for the scenario, it is not possible to use it to compute task interface
            if( result.isEmpty() ) totalResult += new Scenario(name:scenario.name, line:scenario.line, file: featurePath, testcode:null)
            else totalResult += new Scenario(name:scenario.name, line:scenario.line, file: featurePath, testcode:result)
        }

        totalResult
    }

    List<Scenario> getScenariosCode(String featurePath, List lines){
        List<Scenario> scenarios = []
        def scenariosGherkin = ParserGherkinJson.getScenarios(featurePath, lines)
        scenariosGherkin.each{ scenarioGherkin ->
            List<Match> result = getScenarioStepCode(scenarioGherkin)
            //when it is not possible to match at least a step definition for the scenario, it is not possible to use it to compute task interface
            if( !result.isEmpty() ) {
                scenarios += new Scenario(name:scenarioGherkin.name, line:scenarioGherkin.line, file: featurePath, testcode:result)
            }
        }
        return scenarios
    }

    Scenario getScenarioCode(String featurePath, int scenarioLine){
        def scenarioGherkin = ParserGherkinJson.getScenario(featurePath, scenarioLine)
        List<Match> result = getScenarioStepCode(scenarioGherkin)
        //when it is not possible to match at least a step definition for the scenario, it is not possible to use it to compute task interface
        if( result.isEmpty() ) null
        else new Scenario(name:scenarioGherkin.name, line:scenarioGherkin.line, file: featurePath, testcode:result)
    }

    private List<Match> getScenarioStepCode(def scenarioGherkin){
        List<Match> result = []
        scenarioGherkin?.steps?.each { step ->
            def matchedRegex = regexList.findAll{ step.name ==~ it.regex}
            if(matchedRegex && matchedRegex.size()==1){
                def match = matchedRegex[0]
                result += new Match(stepLine:step.line, stepDefinition:match)
            }
            else { //no step definition was found for the step
                //result += null
            }
        }
        return result
    }

    private List<StepDefinition> parseStepsDefinitionFiles(def stepsDefinitionFiles){
        List<StepDefinition> regexs = []
        stepsDefinitionFiles?.each{ file ->
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
        classLoader.addClasspath(Utils.config.project.production.path) //compiled code files
        classLoader.addClasspath(Utils.config.project.test.path) //compiled test code
    }

    private configurePlugins(){
        if(pluginsPath.isEmpty()){
            def jars = Utils.getJarFilesFromDirectory(Utils.config.grails.dependencyCache)
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
