package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.SourceUnit

class MethodVisitor extends ClassCodeVisitorSupport {
    SourceUnit source
    def methods
    def className
    def projectFiles
    def methodCallVisitor

    /*CONSTRUTOR CRIADO SÓ POR CAUSA DA CLASSE DE TESTE MethodCallVisitor. APAGAR DEPOIS.*/
    public MethodVisitor(String name, List projectFiles, List methods){
        className = name
        this.projectFiles = projectFiles
        this.methods = methods
        methodCallVisitor = new MethodCallVisitor()
    }

    public MethodVisitor(String name, List projectFiles, List methods, Visitor visitor){
        className = name
        this.projectFiles = projectFiles
        this.methods = methods
        methodCallVisitor = visitor
    }

    @Override
    protected SourceUnit getSourceUnit() {
        source
    }

    @Override
    public void visitMethod(MethodNode node){
        super.visitMethod(node)
        if(node.name in methods){
            node.code.visit(methodCallVisitor)
        }
    }

}
