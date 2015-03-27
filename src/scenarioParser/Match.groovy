package scenarioParser


class Match {

    int stepLine
    StepDefinition stepDefinition

    @Override
    public String toString(){
        "[stepLine: $stepLine, stepDefinition:$stepDefinition]"
    }

}
