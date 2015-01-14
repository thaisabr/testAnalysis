package analyser

import org.springframework.util.ClassUtils

class Utils {

    static final TEST_COD_REGEX = /.*(steps\.|pages\.|TestDataAndOperations).*/
    static final GROOVY_FILENAME_EXTENSION = ".groovy"
    static final JAR_FILENAME_EXTENSION = ".jar"

    static getFilesFromDirectory(String directory){
        def f = new File(directory)
        def files = []
        f.eachDirRecurse{ dir ->
            files += dir.listFiles().findAll{it.isFile()}*.absolutePath
        }
        files
    }

    static getJarFilesFromDirectory(String directory){
        def files = getFilesFromDirectory(directory)
        files.findAll{it.contains(JAR_FILENAME_EXTENSION)}
    }

    static boolean isTestCode(String referencedClass){
        if(TEST_COD_REGEX) {
            if(referencedClass ==~ TEST_COD_REGEX) true
            else false
        }
        else false
    }

    static String getClassPath(String className, List projectFiles){
        def name = ClassUtils.convertClassNameToResourcePath(className)+GROOVY_FILENAME_EXTENSION
        name = name.replace("/", "\\")
        projectFiles?.find{it.contains(name)}
    }

}
