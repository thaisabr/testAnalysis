package output

import utils.Utils


class ScenarioInterface {

    Set files
    Set classes //instantiated classes
    Set methods //static and non-static called methods
    Set staticFields //declared static fields
    Set fields //declared fields
    Set accessedProperties //accessed fields and constants, for example: "foo.bar"

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

    Set getProductionCalledMethods(){
        methods?.findAll{ it.type!=null && !Utils.isTestCode(it.type) } //format: [name, type, file]
    }

    Set getDeclaredFields(){
        staticFields+fields //format: [name, type, value, file]
    }

    /* At the moment, it is only considered classes from called methods. */
    Set getRelevantClasses(){
        def classes =  (classes*.name).findAll{ !Utils.isTestCode(it) }
        def methods = productionCalledMethods*.type
        (classes+methods as Set)?.sort()
    }

    /* At the moment, it is only considered files from called methods. */
    Set getRelevantFiles(){
        def classes =  (classes.findAll{ !Utils.isTestCode(it.name) })*.file
        def methods = productionCalledMethods*.file
        def allfiles = ((classes+methods+referencedPages) as Set).collect{ Utils.getShortClassPath(it) }
        return allfiles?.sort()
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
