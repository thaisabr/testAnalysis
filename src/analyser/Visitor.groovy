package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
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
    def projectFiles //só são consideradas válidas essas classes

    /************************* expressão regular para identificar classes e métodos inválidos *************************/
    static INVALID_CLASS_REGEX = /.*(groovy|java|springframework|apache|grails|spock|geb|selenium|cucumber).*/
    static final INVALID_METHOD_REGEX = /(println|print|setBinding)/
    /******************************************************************************************************************/

    static final PAGE_METHODS = ['to', 'at']
    //static final STEPS = ['Given', 'When', 'Then', 'And', 'But']

    public Visitor (String name, List projectFiles){
        source = null
        referencedClasses = [] as Set
        calledMethods = [] as Set
        staticFields = [] as Set
        fields = [] as Set
        accessedProperties = [] as Set
        calledPageMethods = [] as Set
        className = name
        this.projectFiles = projectFiles
    }

    private boolean isValidClassByProject(String referencedClass){
        if(projectFiles){
            def result = projectFiles?.find{ name ->
                def aux = ClassUtils.convertResourcePathToClassName(name)
                aux ==~ /.*$referencedClass\.groovy/
            }
            if (result) true
            else false
        }
        else true
    }

    private boolean isValidClassByAPI(String referencedClass){
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

    private boolean isValidMethod(referencedMethod){
        if(referencedMethod ==~ INVALID_METHOD_REGEX) false
        else true
    }

    private boolean isPageMethod(referencedMethod){
        if(referencedMethod in PAGE_METHODS) true
        else false
    }

    private registryMethodCall(MethodCallExpression call){
        def result = false
        def className = call.receiver.type.name.replace("[L", "")
        className = className.replace(";","")
        if( isValidClass(className) ) {
            calledMethods += [name:call.methodAsString, type:className]
            result = true
        }
        result
    }

    private boolean registryIsInternalValidMethodCall(MethodCallExpression call){
        def result = false
        if (call.implicitThis && isValidMethod(call.methodAsString)) { //chamada de método da classe
            if( isPageMethod(call.methodAsString) ){
                def value = call.arguments.text
                calledPageMethods += [name: call.methodAsString, arg:value.substring(1,value.length()-1)]
            } /*else {
                //chamada de método da própria classe não interessa!!!!
                //calledMethods += [name: call.methodAsString, type: className]
            }*/
            result = true
        }
        result
    }

    private boolean registryIsExternalValidMethodCall(MethodCallExpression call){
        def result = false
        if(!call.implicitThis) { //chamada de método de outra classe
            if (call.receiver.dynamicTyped) {
                calledMethods += [name: call.methodAsString, type: null]
                result = true
            } else {
                result = registryMethodCall(call)
            }
        }
        result
    }

    private printMethodCall(MethodCallExpression call){
        println "!!!!!!!!!!! múltiplas chamadas !!!!!!!!!!!"
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
        if( isValidClass(call?.type.name) ) referencedClasses += [name: call.type.name]
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression call){
        super.visitMethodCallExpression(call)

        switch (call.receiver.class){
            case ConstructorCallExpression.class: //caso de múltiplas chamadas em uma linha, com chamada de construtor
                // ex: def path = new File(".").getCanonicalPath() + File.separator + "test" + File.separator + "files" + File.separator + "TCS.pdf"
                if (isValidClass(call.receiver.type.name)) {
                    referencedClasses += [name: call.receiver.type.name]
                    calledMethods += [name:call.methodAsString, type:call.objectExpression.type.name]
                }
                break
            case VariableExpression.class: //chamada comum através de variável de referência
                def result = registryIsInternalValidMethodCall(call)
                if(!result) registryIsExternalValidMethodCall(call)
                break
            case MethodCallExpression.class: //caso de múltiplas chamadas em uma única linha, sem chamada de construtor
            case PropertyExpression.class:
            case ClassExpression.class: //chamada de método static de outra classe (se chamado usando o nome da classe)
                registryMethodCall(call)
                break
            case RangeExpression.class: //não precisa registrar nada porque é chamada da API somente
                break
            default:
                printMethodCall(call)
                //RangeExpression.class poderia ser tratado aqui, mas ficou separado por já ter sido identificado
                //(vide método isSorted em ArticleTestDataAndOperations)
        }
    }

    @Override
    //Quando é método static da própria classe ou método step (static import). Esses 2 casos, inclusive, são desprezados
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call){
        super.visitStaticMethodCallExpression(call)
        if (isValidClass(call.ownerType.name)){
            calledMethods += [name:call.methodAsString, type:call.ownerType.name]
        }
    }

    @Override
    public void visitField(FieldNode node){
        super.visitField(node)
        if(node.static) staticFields += [name:node.name, type:node.type.name, value:node.initialValueExpression.value]
        else fields += [name:node.name, type:node.type.name, value:node.initialValueExpression.value]
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression){ //atributos e constantes de outras classes
        super.visitPropertyExpression(expression)
        if ( isValidClass(expression.objectExpression.type.name) ){
            accessedProperties += [name:expression.propertyAsString, type:expression.objectExpression.type.name]
        }

        /*
          Exemplo: Periodico p = new Periodico
          p.volume
          Se p for dinamicamente tipada, o tipo vai ser object, daí que não passa no if e não é computado o acesso
          File.separator não entra porque é da API de Java, não passa no if
        */
    }

}
