package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.springframework.util.ClassUtils
import utils.Utils
import output.ScenarioInterface

class Visitor extends ClassCodeVisitorSupport {
    SourceUnit source
    ScenarioInterface scenarioInterface
    def className
    def projectFiles //valid files

    public Visitor (String name, Collection projectFiles){
        this.source = null
        this.className = name
        this.projectFiles = projectFiles
        this.scenarioInterface = new ScenarioInterface()
    }

    private registryMethodCall(MethodCallExpression call){
        def result = false
        def className = call.receiver.type.name
        Utils.configClassnameFromMethod(className)
        if( Utils.isValidClass(className, projectFiles) ) {
            scenarioInterface.methods += [name:call.methodAsString, type:className]
            result = true
        }
        result
    }

    private boolean registryIsInternalValidMethodCall(MethodCallExpression call){
        def result = false
        if (call.implicitThis && Utils.isValidMethod(call.methodAsString)) { //call from test code
            if( Utils.isPageMethod(call.methodAsString) ){
                def value = call.arguments.text
                scenarioInterface.calledPageMethods += [name: call.methodAsString, arg:value.substring(1,value.length()-1)]
            }
            /*else {
                //calls for other methods do not need to be registered
                //methods += [name: call.methodAsString, type: className]
            }*/
            result = true
        }
        result
    }

    private boolean registryIsExternalValidMethodCall(MethodCallExpression call){
        def result = false
        if(!call.implicitThis) { //call from other class
            if (call.receiver.dynamicTyped) {
                scenarioInterface.methods += [name: call.methodAsString, type: null]
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
        if( Utils.isValidClass(call?.type?.name, projectFiles) ) scenarioInterface.classes += call?.type?.name
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression call){
        super.visitMethodCallExpression(call)

        switch (call.receiver.class){
            case ConstructorCallExpression.class: //composite call that includes constructor call
                // ex: def path = new File(".").getCanonicalPath() + File.separator + "test" + File.separator + "files" + File.separator + "TCS.pdf"
                if (Utils.isValidClass(call.receiver.type.name, projectFiles)) {
                    scenarioInterface.classes += call.receiver.type.name
                    scenarioInterface.methods += [name:call.methodAsString, type:call.objectExpression.type.name]
                }
                break
            case VariableExpression.class: //call that uses a reference variable
                def result = registryIsInternalValidMethodCall(call)
                if(!result) registryIsExternalValidMethodCall(call)
                break
            case MethodCallExpression.class: //composite call that does not include constructor call
            case StaticMethodCallExpression.class: //composite static call
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
        if (Utils.isValidClass(call.ownerType.name, projectFiles)){
            scenarioInterface.methods += [name:call.methodAsString, type:call.ownerType.name]
        }
    }

    @Override
    public void visitField(FieldNode node){
        super.visitField(node)
        def result = [name:node.name, type:node.type.name, value:node.initialValueExpression.value]
        if(node.static) scenarioInterface.staticFields += result
        else scenarioInterface.fields += result
    }

    @Override
    //fields and constants from other classes
    public void visitPropertyExpression(PropertyExpression expression){
        super.visitPropertyExpression(expression)
        if ( Utils.isValidClass(expression.objectExpression.type.name, projectFiles) ){
            scenarioInterface.accessedProperties += [name:expression.propertyAsString, type:expression.objectExpression.type.name]
        }
    }

}
