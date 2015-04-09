package scenarioParser

class Scenario {

    String name
    int line
    String file
    List<Match> testcode

    @Override
    public String toString(){
        "name: $name, line:$line, file:$file, testcode:$testcode"
    }

}
