import stepsFileParser.ClassAnalyser
import analyser.ScenarioAnalyser

//Parsing a groovy file
def analyser = new ClassAnalyser()
analyser.doIndirectAnalysis()

//Parsing a scenario
def scenarioAnalyser = new ScenarioAnalyser()
def featurePath = "${System.getProperty("user.home")}${File.separator}Documents${File.separator}GitHub" +
        "${File.separator}rgms${File.separator}test${File.separator}cucumber${File.separator}Book.feature"
scenarioAnalyser.analyseScenario(featurePath, 7)

//Parsing a feature (all its scenarios)
scenarioAnalyser.analyseFeature(featurePath)

