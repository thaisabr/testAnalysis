package scenarioStepsMatcher

import gherkin.formatter.JSONFormatter
import gherkin.formatter.JSONPrettyFormatter
import gherkin.parser.Parser
import gherkin.util.FixJava
import groovy.json.JsonSlurper

class ParserGherkinJson {

    static parse(String featurePath, String jsonPath) {
        String gherkin = FixJava.readReader(new InputStreamReader( new FileInputStream(featurePath), "UTF-8"))
        StringBuilder json = new StringBuilder()
        JSONFormatter formatter = new JSONPrettyFormatter(json)
        Parser parser = new Parser(formatter)
        parser.parse(gherkin, featurePath, 0)
        formatter.done()
        formatter.close()
        generateJson(json, jsonPath)
    }

    static getAllScenarios(String jsonPath){
        def slurper = new JsonSlurper()
        def result = slurper.parse(new FileReader(jsonPath))
        result.elements[0].findAll{ it.type == "scenario"} //other type = scenario output (improve it)
    }

    static getScenario(int scenarioLine, String jsonPath){
        def scenarios = getAllScenarios(jsonPath)
        scenarios.find { it.line == scenarioLine }
    }

    static generateJson(StringBuilder json, String jPath){
        FileWriter file = new FileWriter(jPath)
        file.write(json.toString())
        file.flush()
        file.close()
    }

}
