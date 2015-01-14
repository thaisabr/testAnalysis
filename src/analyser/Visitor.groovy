package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.springframework.util.ClassUtils

class Visitor extends ClassCodeVisitorSupport {
    SourceUnit source
    def referencedClasses
    def calledMethods
    def staticFields
    def fields
    def accessedProperties
    def calledPageMethods
    def className
    def projectFiles //valid files

    /******************************* Regex to identify invalid classes and methods ************************************/
    static INVALID_CLASS_REGEX = /.*(groovy|java|springframework|apache|grails|spock|geb|selenium|cucumber).*/
    static final INVALID_METHOD_REGEX = /(println|print|setBinding)/
    /******************************************************************************************************************/

    static final PAGE_METHODS = ['to', 'at']
    //static final STEPS = ['Given', 'When', 'Then', 'And', 'But']

    public Visitor (String name, List projectFiles){
        this.source = null
        this.referencedClasses = [] as Set
        this.calledMethods = [] as Set
        this.staticFields = [] as Set
        this.fields = [] as Set
        this.accessedProperties = [] as Set
        this.calledPageMethods = [] as Set
        this.className = name
        this.projectFiles = projectFiles
    }

    private boolean isValidClassByProject(String referencedClass){
        if(projectFiles){
            def result = projectFiles?.find{ name ->
                def aux = ClassUtils.convertResourcePathToClassName(name)
                aux ==~ /.*$referencedClass\$Utils.GROOVY_FILENAME_EXTENSION/
            }
            if (result) true
            else false
        }
        else true
    }

    private static boolean isValidClassByAPI(String referencedClass){
        if(INVALID_CLASS_REGEX) {
            if(referencedClass ==~ INVALID_CLASS_REGEX) false
            else true
        }
        else true
    }

    private boolean isValidClass(referencedClass){
        if(isValidClassByProject(referencedClass) && isValidClassByAPI(referencedClass)){
            true
        }
        else false
    }

    private static boolean isValidMethod(String referencedMethod){
        if(referencedMethod ==~ INVALID_METHOD_REGEX) false
        else true
    }

    private static boolean isPageMethod(String referencedMethod){
        if(referencedMethod in PAGE_METHODS) true
        else false
    }

    private registryMethodCall(MethodCallExpression call){
        def result = false
        def className = call.receiver.type.name.replace("[L", "") //deals with
        className = className.replace(";","")
        if( isValidClass(className) ) {
            calledMethods += [name:call.methodAsString, type:className]
            result = true
        }
        result
    }

    private boolean registryIsInternalValidMethodCall(MethodCallExpression call){
        def result = false
        if (call.implicitThis && isValidMethod(call.methodAsString)) { //call from test code
            if( isPageMethod(call.methodAsString) ){
                def value = call.arguments.text
                calledPageMethods += [name: call.methodAsString, arg:value.substring(1,value.length()-1)]
            }
            /*else {
                //calls for other methods do not need to be registered
                //calledMethods += [name: call.methodAsString, type: className]
            }*/
            result = true
        }
        result
    }

    private boolean registryIsExternalValidMethodCall(MethodCallExpression call){
        def result = false
        if(!call.implicitThis) { //call from other class
            if (call.receiver.dynamicTyped) {
                calledMethods += [name: call.methodAsString, type: null]
                result = true
            } else {
                result = registryMethodCall(call)
            }
        }
        result
    }

    private static printMethodCall(MethodCallExpression call){
        println "!!!!!!!!!!!!! composite call !!!!!!!!!!!!!"
        println "call text: $call.text"
        println "call.receiver.class: ${call.receiver.toString()}"
        call.properties.each{ k, v ->
            println "$k: $v"
        }
        println "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    }

    @Override
    protected SourceUnit getSourceUnit() {
        source
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call){
        super.visitConstructorCallExpression(call)
        if( isValidClass(call?.type?.name) ) referencedClasses += [name: call?.type?.name]
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression call){
        super.visitMethodCallExpression(call)

        switch (call.receiver.class){
            case ConstructorCallExpression.class: //composite call that includes constructor call
                // ex: def path = new File(".").getCanonicalPath() + File.separator + "test" + File.separator + "files" + File.separator + "TCS.pdf"
                if (isValidClass(call.receiver.type.name)) {
                    referencedClasses += [name: call.receiver.type.name]
                    calledMethods += [name:call.methodAsString, type:call.objectExpression.type.name]
                }
                break
            case VariableExpression.class: //call that uses a reference variable
                def result = registryIsInternalValidMethodCall(call)
                if(!result) registryIsExternalValidMethodCall(call)
                break
            case MethodCallExpression.class: //composite call that does not include constructor call
            case PropertyExpression.class:
            case ClassExpression.class: //static method call from another class that uses the class name
                registryMethodCall(call)
                break
            case RangeExpression.class: //API call
                break
            default:
                printMethodCall(call)
        }
    }

    @Override
    //Static method or step method(static import)
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call){
        super.visitStaticMethodCallExpression(call)
        if (isValidClass(call.ownerType.name)){
            calledMethods += [name:call.methodAsString, type:call.ownerType.name]
        }
    }

    @Override
    public void visitField(FieldNode node){
        super.visitField(node)
        def result = [name:node.name, type:node.type.name, value:node.initialValueExpression.value]
        if(node.static) staticFields += result
        else fields += result
    }

    @Override
    //fields and constants from other classes
    public void visitPropertyExpression(PropertyExpression expression){
        super.visitPropertyExpression(expression)
        if ( isValidClass(expression.objectExpression.type.name) ){
            accessedProperties += [name:expression.propertyAsString, type:expression.objectExpression.type.name]
        }
    }

}
