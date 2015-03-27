package output


class ScenarioInterface {

    def referencedClasses
    def calledMethods
    def staticFields
    def fields
    def accessedProperties
    def calledPageMethods
    def referencedPages

    public ScenarioInterface(){
        this.referencedClasses = [] as Set
        this.calledMethods = [] as Set
        this.staticFields = [] as Set
        this.fields = [] as Set
        this.accessedProperties = [] as Set
        this.calledPageMethods = [] as Set
        this.referencedPages = [] as Set
    }

}
