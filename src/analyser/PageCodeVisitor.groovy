package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.control.SourceUnit
import utils.Utils

class PageCodeVisitor extends ClassCodeVisitorSupport {
    SourceUnit source
    def pages
    def projectFiles
    static final PAGE_FIELD = "url" //name convention

    public PageCodeVisitor(Collection projectFiles){
        this.source = null
        this.pages = [] as Set
        this.projectFiles = projectFiles
    }

    @Override
    protected SourceUnit getSourceUnit() {
        source
    }

    @Override
    public void visitField(FieldNode node){
        super.visitField(node)
        if(node.name==PAGE_FIELD && node.initialValueExpression.value != ""){
            def name = Utils.getGspPath(node.initialValueExpression.value, projectFiles)
            if(!name.isEmpty()) pages += name
        }
    }

}

