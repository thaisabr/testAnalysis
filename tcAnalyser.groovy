import analyser.ClassAnalyser

def analyser = new ClassAnalyser()

println ">>>>>>>>>>>>>>>>>>>>>>>>>>> DIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
analyser.doDirectAnalysis()
analyser.printAnalysisResult()
analyser.printAnalysisResultFromProductionCode()

println ">>>>>>>>>>>>>>>>>>>>>>>>>>> INDIRECT ANALYSIS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
analyser.doIndirectAnalysis()
analyser.printAnalysisResult()
analyser.printAnalysisResultFromProductionCode()


