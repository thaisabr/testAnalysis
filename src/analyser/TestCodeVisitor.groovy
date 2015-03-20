package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.control.SourceUnit

class TestCodeVisitor extends ClassCodeVisitorSupport {
    SourceUnit source
    List lines
    Visitor methodCallVisitor

    public TestCodeVisitor(List lines, Visitor methodCallVisitor){
        this.lines = lines
        this.methodCallVisitor = methodCallVisitor
    }

    @Override
    protected SourceUnit getSourceUnit() {
        source
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call){
        super.visitStaticMethodCallExpression(call)
        if (call.lineNumber in lines) {
            call.visit(methodCallVisitor)
        }
    }

}

