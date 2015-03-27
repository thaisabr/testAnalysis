package scenarioParser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.control.SourceUnit
import utils.Utils

class StepDefinitionVisitor extends ClassCodeVisitorSupport {
    SourceUnit source
    List<StepDefinition> regexList
    def file

    public StepDefinitionVisitor(String fileName){
        regexList = []
        file = fileName
    }

    @Override
    protected SourceUnit getSourceUnit() {
        source
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call){
        super.visitStaticMethodCallExpression(call)
       if (call.methodAsString in Utils.STEPS) {
           regexList += new StepDefinition(file: file, line:call.lineNumber, lastLine: call.lastLineNumber,
                   type: call.methodAsString, regex: call.arguments[0].text)
       }
    }

}
