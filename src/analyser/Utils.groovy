package analyser

class Utils {

     static final TEST_COD_REGEX = /.*(steps\.|pages\.|TestDataAndOperations).*/

     static getFilesFromDirectory(String directory){
        def f = new File(directory)
        def files = []
        f.eachDirRecurse{ dir ->
            files += dir.listFiles().findAll{it.isFile()}*.absolutePath
        }
        files
    }

    static boolean isTestCode(String referencedClass){
        if(TEST_COD_REGEX) {
            if(referencedClass ==~ TEST_COD_REGEX) true
            else false
        }
        else false
    }

}
