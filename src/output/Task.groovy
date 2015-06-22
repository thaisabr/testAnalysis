package output

import scenarioParser.Scenario

class Task {

    List<Scenario> scenarios
    ScenarioInterface scenarioInterface

    public Task(){
        this.scenarios = []
        this.scenarioInterface = new ScenarioInterface()
    }

}
