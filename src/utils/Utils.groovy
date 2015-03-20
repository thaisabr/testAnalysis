package utils

import org.springframework.util.ClassUtils

class Utils {

    static final TEST_COD_REGEX = /.*(steps\.|pages\.|TestDataAndOperations).*/
    static final GROOVY_FILENAME_EXTENSION = ".groovy"
    static final JAR_FILENAME_EXTENSION = ".jar"
    static final FEATURE_FILENAME_EXTENSION = ".feature"
    static final INTERFACE_FILENAME_EXTENSION = "Interface.txt"
    static final JSON_FILENAME_EXTENSION = ".json"
    static final INVALID_CLASS_REGEX = /.*(groovy|java|springframework|apache|grails|spock|geb|selenium|cucumber).*/
    static final INVALID_METHOD_REGEX = /(println|print|setBinding)/
    static final PAGE_METHODS = ['to', 'at']
    static final CONFIG_FILE_NAME = 'Config.groovy'
    static final INTERFACES_PATH = ".${File.separator}interfaces${File.separator}"
    static final JSON_PATH = ".${File.separator}json${File.separator}"
    static final STEPS = ['Given', 'When', 'Then', 'And', 'But']

    static boolean isValidClassByAPI(String referencedClass){
        if(INVALID_CLASS_REGEX) {
            if(referencedClass ==~ INVALID_CLASS_REGEX) false
            else true
        }
        else true
    }

    static boolean isValidClass(String referencedClass, List projectFiles){
        if(isValidClassByProject(referencedClass, projectFiles) && isValidClassByAPI(referencedClass)){
            true
        }
        else false
    }

    static boolean isValidMethod(String referencedMethod){
        if(referencedMethod ==~ INVALID_METHOD_REGEX) false
        else true
    }

    static boolean isPageMethod(String referencedMethod){
        if(referencedMethod in PAGE_METHODS) true
        else false
    }

    static boolean isValidClassByProject(String referencedClass, List projectFiles){
        if(projectFiles){
            def result = projectFiles?.find{ name ->
                def aux = ClassUtils.convertResourcePathToClassName(name)
                aux ==~ /.*$referencedClass\$GROOVY_FILENAME_EXTENSION/
            }
            if (result) true
            else false
        }
        else true
    }

    static getFilesFromDirectory(String directory){
        def f = new File(directory)
        def files = []
        f.eachDirRecurse{ dir ->
            files += dir.listFiles().findAll{it.isFile()}*.absolutePath
        }
        f.eachFile{
            files += it.absolutePath
        }
        files
    }

    static getGroovyFilesFromDirectory(String directory){
        def files = getFilesFromDirectory(directory)
        files.findAll{it.contains(GROOVY_FILENAME_EXTENSION)}
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

    static String getClassPath(String className, Collection projectFiles){
        def name = ClassUtils.convertClassNameToResourcePath(className)+GROOVY_FILENAME_EXTENSION
        name = name.replace("/", File.separator)
        name = name.replace("\\", File.separator)
        projectFiles?.find{it.contains(name)}
    }

    static String getGspPath(String resourcePath, List projectFiles, String projectDir){
        def name = resourcePath.replace("/", File.separator)
        name = name.replace("\\", File.separator)

        int n = name.count(File.separator)
        if(n>1){
            def index = name.lastIndexOf(File.separator)
            name = name.substring(0,index)
        }

        def match = projectFiles?.find{ it.contains(name) }
        if(match) name = match - (projectDir+File.separator)
        else name = ""

        name
    }

    static getInterfaceFileName(String path){
        def beginIndex = path.lastIndexOf(File.separator)
        def name = path.substring(beginIndex+1)
        INTERFACES_PATH + (name - GROOVY_FILENAME_EXTENSION) + INTERFACE_FILENAME_EXTENSION
    }

    static getJsonFileName(String path){
        def beginIndex = path.lastIndexOf(File.separator)
        def name = path.substring(beginIndex+1)
        JSON_PATH + (name - FEATURE_FILENAME_EXTENSION) + JSON_FILENAME_EXTENSION
    }

}
