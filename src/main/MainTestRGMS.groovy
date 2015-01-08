package main

import analyser.ClassAnalyser
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.RootLoader

class MainTestRGMS {

    static void main(args) {
        def projectDir = "${System.getProperty("user.home")}${File.separator}Documents${File.separator}GitHub${File.separator}rgms"
        def stepsDir = "$projectDir${File.separator}test${File.separator}cucumber${File.separator}steps"
        def fileName = "$stepsDir${File.separator}ArticleSteps.groovy"

        // Exemplo de análise filtrando arquivos referenciados para considerar apenas o que está no projeto
        println "code: ArticleSteps.groovy"

        println ">>>>>>>>>>>>>>>>>>>>>>>>>>> DIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        def analyser = new ClassAnalyser(fileName, projectDir, true)
        analyser.doDirectAnalysis()
        analyser.printAnalysisResult()

        println ">>>>>>>>>>>>>>>>>>>>>>>>>>> INDIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        analyser.doIndirectAnalysis()
        analyser.printAnalysisResult()
    }

}
