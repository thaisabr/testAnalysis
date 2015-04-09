package scenarioParser

import gherkin.formatter.JSONFormatter
import gherkin.formatter.JSONPrettyFormatter
import gherkin.parser.Parser
import gherkin.util.FixJava
import groovy.json.JsonSlurper
import utils.Utils

class ParserGherkinJson {

    private static parse(String featurePath, String jsonPath) {
        String gherkin = FixJava.readReader(new InputStreamReader( new FileInputStream(featurePath), "UTF-8"))
        StringBuilder json = new StringBuilder()
        JSONFormatter formatter = new JSONPrettyFormatter(json)
        Parser parser = new Parser(formatter)
        parser.parse(gherkin, featurePath, 0)
        formatter.done()
        formatter.close()
        generateJson(json, jsonPath)
    }

    static getAllScenarios(String featurePath){
        def jsonPath = Utils.getJsonFileName(featurePath)
        parse(featurePath, jsonPath)
        def slurper = new JsonSlurper()
        File file = new File(jsonPath)
        def result = slurper.parse(new FileReader(file))
        file.deleteOnExit()
        result.elements[0].findAll{ it.type == "scenario"} //other type = scenario output (improve it)
    }

    static getScenario(String featurePath, int scenarioLine){
        def scenarios = getAllScenarios(featurePath)
        scenarios.find { it.line == scenarioLine }
    }

    private static generateJson(StringBuilder json, String jsonPath){
        FileWriter file = new FileWriter(jsonPath)
        file.write(json.toString())
        file.flush()
        file.close()
    }

}
