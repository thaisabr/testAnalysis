package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.control.SourceUnit
import utils.Utils

class PageCodeVisitor extends ClassCodeVisitorSupport {
    SourceUnit source
    def pages
    def projectFiles
    def projectDir
    static final PAGE_FIELD = "url" //name convention

    public PageCodeVisitor(Collection projectFiles){
        this.source = null
        this.pages = [] as Set
        this.projectFiles = projectFiles
        this.projectDir = Utils.config.project.path
    }

    @Override
    protected SourceUnit getSourceUnit() {
        source
    }

    @Override
    public void visitField(FieldNode node){
        super.visitField(node)
        if(node.name==PAGE_FIELD && node.initialValueExpression.value != ""){
            def name = Utils.getGspPath(node.initialValueExpression.value, projectFiles, projectDir)
            if(!name.isEmpty()) pages += name
        }
    }

}

