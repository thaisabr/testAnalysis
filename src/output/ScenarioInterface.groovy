package output

import utils.Utils


class ScenarioInterface {

    Set classes //instantiated classes
    Set methods //static and non-static called methods
    Set staticFields //declared static fields
    Set fields //declared fields
    Set accessedProperties //accessed fields

    /************** Specific to web-based tests. When we have a GSP parser such code should be removed! ***************/
    def calledPageMethods //help to identify referenced pages (GSP files); methods "to" and "at"
    def referencedPages
    /******************************************************************************************************************/

    public ScenarioInterface(){
        this.classes = [] as Set
        this.methods = [] as Set
        this.staticFields = [] as Set
        this.fields = [] as Set
        this.accessedProperties = [] as Set
        this.calledPageMethods = [] as Set
        this.referencedPages = [] as Set
    }

    /*At the moment, it is considered only the information that is necessary for similarity measure.*/
    public int size(){
        classes?.size() + getProductionCalledMethods()?.size() + getDeclaredFields()?.size() +
        accessedProperties?.size()
    }

    public Set getProductionCalledMethods(){
        methods?.findAll{ it.type!=null && !Utils.isTestCode(it.type) }
    }

    public Set getDeclaredFields(){
        staticFields+fields
    }

    public Set getRelevantClasses(){
        def classes =  classes?.findAll{ !Utils.isTestCode(it) }
        def methods = productionCalledMethods*.type as Set
        (classes+methods as Set).sort()
    }

    public Set getRelevantFiles(){
        (getRelevantClasses()+referencedPages).sort()
    }

    def update(ScenarioInterface scenarioInterface){
        this.classes += scenarioInterface.classes
        this.methods += scenarioInterface.methods
        this.staticFields += scenarioInterface.staticFields
        this.fields += scenarioInterface.fields
        this.accessedProperties += scenarioInterface.accessedProperties
        this.calledPageMethods += scenarioInterface.calledPageMethods
        this.referencedPages += scenarioInterface.referencedPages
    }

}
