package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.control.SourceUnit

class MethodVisitor extends ClassCodeVisitorSupport {
    SourceUnit source
    def methods
    def className
    def projectFiles
    def methodCallVisitor

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
