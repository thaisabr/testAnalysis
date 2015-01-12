package main

import analyser.ClassAnalyser

class MainTestRGMS {

    static void main(args) {
        def projectDir = "${System.getProperty("user.home")}${File.separator}Documents${File.separator}GitHub${File.separator}rgms"
        def stepsDir = "$projectDir${File.separator}test${File.separator}cucumber${File.separator}steps"
        def testFile = "$stepsDir${File.separator}ArticleSteps.groovy"
        def shiroPath = "${System.getProperty("user.home")}${File.separator}.grails${File.separator}ivy-cache${File.separator}" +
                "org.apache.shiro${File.separator}shiro-core${File.separator}bundles${File.separator}shiro-core-1.2.0.jar"

        def analyser = new ClassAnalyser(testFile, projectDir, [shiroPath] as Set)

        println "code: $testFile"

        println ">>>>>>>>>>>>>>>>>>>>>>>>>>> DIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        analyser.doDirectAnalysis()
        analyser.printAnalysisResult()

        println ">>>>>>>>>>>>>>>>>>>>>>>>>>> INDIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        analyser.doIndirectAnalysis()
        analyser.printAnalysisResult()
    }

}
