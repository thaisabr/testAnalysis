package main

import analyser.ClassAnalyser

class MainTestRGMS {

    static void main(args) {
        def projectDir = "${System.getProperty("user.home")}${File.separator}Documents${File.separator}GitHub${File.separator}rgms"
        def stepsDir = "$projectDir${File.separator}test${File.separator}cucumber${File.separator}steps"
        def testFile = "$stepsDir${File.separator}ArticleSteps.groovy"

        /* //Manual configuration of plugin path
            def GRAILS_PATH = "${System.getProperty("user.home")}${File.separator}.grails${File.separator}ivy-cache"
            def CUCUMBER_PATH = "$GRAILS_PATH${File.separator}info.cukes${File.separator}cucumber-groovy${File.separator}jars${File.separator}cucumber-groovy-1.1.1.jar"
            def GEB_PATH = "$GRAILS_PATH${File.separator}org.codehaus.geb${File.separator}geb-core${File.separator}jars${File.separator}geb-core-0.7.1.jar"
            def SHIRO_PATH = "$GRAILS_PATH${File.separator}org.apache.shiro${File.separator}shiro-core${File.separator}bundles${File.separator}shiro-core-1.2.0.jar"
            def plugins = []
            plugins += CUCUMBER_PATH
            plugins += GEB_PATH
            plugins += SHIRO_PATH
            def analyser = new ClassAnalyser(testFile, projectDir, plugins)
         */

        def analyser = new ClassAnalyser(testFile, projectDir)

        println "Code to analyse: $testFile\n"

        println ">>>>>>>>>>>>>>>>>>>>>>>>>>> DIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        analyser.doDirectAnalysis()
        analyser.printAnalysisResult()

        println ">>>>>>>>>>>>>>>>>>>>>>>>>>> INDIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        analyser.doIndirectAnalysis()
        analyser.printAnalysisResult()
    }

}
