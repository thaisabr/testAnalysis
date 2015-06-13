package output

import utils.Utils


class ScenarioInterface {

    Set files
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
        this.files = [] as Set
        this.classes = [] as Set
        this.methods = [] as Set
        this.staticFields = [] as Set
        this.fields = [] as Set
        this.accessedProperties = [] as Set
        this.calledPageMethods = [] as Set
        this.referencedPages = [] as Set
    }

    /*At the moment, it is considered only the information that is necessary for similarity measure.*/
    int size(){
        classes?.size() + getProductionCalledMethods()?.size() + getDeclaredFields()?.size() +
        accessedProperties?.size()
    }

    Set getProductionCalledMethods(){
        methods?.findAll{ it.type!=null && !Utils.isTestCode(it.type) }
    }

    Set getDeclaredFields(){
        staticFields+fields
    }

    Set getRelevantClasses(){
        def classes =  classes?.findAll{ !Utils.isTestCode(it) }
        def methods = productionCalledMethods*.type as Set
        (classes+methods as Set).sort()
    }

    Set getRelevantFiles(Collection projectFiles){
        def relevantFiles = []
        def allfiles = getRelevantClasses()+referencedPages
        allfiles.each{
            relevantFiles += Utils.getShortClassPath(it, projectFiles)
        }
        return relevantFiles.sort()
    }

    def update(ScenarioInterface scenarioInterface){
        this.files += scenarioInterface.files
        this.classes += scenarioInterface.classes
        this.methods += scenarioInterface.methods
        this.staticFields += scenarioInterface.staticFields
        this.fields += scenarioInterface.fields
        this.accessedProperties += scenarioInterface.accessedProperties
        this.calledPageMethods += scenarioInterface.calledPageMethods
        this.referencedPages += scenarioInterface.referencedPages
    }

}
