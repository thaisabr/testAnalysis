package output

import utils.Utils


class ScenarioInterface {

    Set referencedClasses //instantiated classes
    Set calledMethods //static and non-static called methods
    Set staticFields //declared static fields
    Set fields //declared fields
    Set accessedProperties //accessed fields

    /****************************************** Specific to web-based tests *******************************************/
    def calledPageMethods //help to identify referenced pages (GSP files); methods "to" and "at"
    def referencedPages
    /******************************************************************************************************************/

    public ScenarioInterface(){
        this.referencedClasses = [] as Set
        this.calledMethods = [] as Set
        this.staticFields = [] as Set
        this.fields = [] as Set
        this.accessedProperties = [] as Set
        this.calledPageMethods = [] as Set
        this.referencedPages = [] as Set
    }

    /*At the moment, it is considered only the information that is necessary for similarity measure.*/
    public int size(){
        referencedClasses?.size() + getProductionCalledMethods()?.size() + getDeclaredFields()?.size() +
        accessedProperties?.size()
    }

    public Set getProductionCalledMethods(){
        calledMethods?.findAll{ it.type!=null && !Utils.isTestCode(it.type) }
    }

    public Set getDeclaredFields(){
        staticFields+fields
    }

}
