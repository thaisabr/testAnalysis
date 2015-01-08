package analyser

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit

/*
* 05/01/15
* CRIEI ESSA CLASSE SÓ PARA TESTE, POIS ELA FICA REDUNDANTE COM A CLASSE VISITOR, QUE TEM TUDO NECESSÁRIO PARA RASTREAR
* CHAMADAS DE METODOS, JÁ QUE É NECESSARIO NAO APENAS IDENTIFICAR A CHAMADA, MAS A SUA ORIGEM (A CLASSE QUE CONTÉM O
* MÉTODO CHAMADO).
* */

class MethodCallVisitor extends ClassCodeVisitorSupport {
    SourceUnit source

    @Override
    protected SourceUnit getSourceUnit() {
        source
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression call){
        super.visitMethodCallExpression(call)
        println "METHOD CALL: $call.methodAsString"

    }
}
