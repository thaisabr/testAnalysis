package output

import scenarioParser.Scenario

class Task {

    List<Scenario> scenarios
    TestInterface testInterface

    public Task(){
        this.scenarios = []
        this.testInterface = new TestInterface()
    }

}
