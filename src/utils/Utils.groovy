package utils

import org.springframework.util.ClassUtils
import java.util.regex.Matcher


class Utils {

    static config = new ConfigSlurper().parse(Utils.class.classLoader.getResource("Config.groovy"))
    static final PROJECT_PATH = config.project.path
    static final GROOVY_FILENAME_EXTENSION = ".groovy"
    static final JAR_FILENAME_EXTENSION = ".jar"
    static final FEATURE_FILENAME_EXTENSION = ".feature"
    static final INTERFACE_FILENAME_EXTENSION = "Interface.txt"
    static final JSON_FILENAME_EXTENSION = ".json"
    static final TEST_COD_REGEX = /.*(steps\.|pages\.|TestDataAndOperations).*/
    static final INVALID_CLASS_REGEX = /.*(groovy|java|springframework|apache|grails|spock|geb|selenium|cucumber).*/
    static final INVALID_METHOD_REGEX = /(println|print|setBinding)/
    static final FILE_SEPARATOR_REGEX = /(\\|\/)/
    static final PAGE_METHODS = ['to', 'at']
    static final INTERFACES_PATH = "${System.getProperty("user.dir")}${File.separator}interfaces${File.separator}"
    static final JSON_PATH = "${System.getProperty("user.dir")}${File.separator}json${File.separator}"
    static final STEPS = ['Given', 'When', 'Then', 'And', 'But']

    static fillPluginsPath(List pluginsPath){
        config.grails.plugin.path?.each{ k, v ->
            pluginsPath += v
        }
    }

    static boolean isValidClassByAPI(String referencedClass){
        if(INVALID_CLASS_REGEX) {
            if(referencedClass ==~ INVALID_CLASS_REGEX) false
            else true
        }
        else true
    }

    static boolean isValidClass(String referencedClass, List projectFiles){
        if(isValidClassByAPI(referencedClass) && isValidClassByProject(referencedClass, projectFiles)){
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
        def result = true
        //def filename = ClassUtils.convertClassNameToResourcePath(referencedClass)
        if(projectFiles){
            //result = projectFiles?.find{ it == filename } ? true : false
            def searchResult = projectFiles?.find{ name ->
                def aux = ClassUtils.convertResourcePathToClassName(name)
                aux ==~ /.*$referencedClass\$GROOVY_FILENAME_EXTENSION/
            }
            if (!searchResult) result = false
        }
        return result
    }

    static getFilesFromDirectory(String directory){
        def f = new File(directory)
        def files = []
        f.eachDirRecurse{ dir ->
            dir.listFiles().each{
                if(it.isFile()){
                    files += it.absolutePath.replaceAll(FILE_SEPARATOR_REGEX, Matcher.quoteReplacement(File.separator))
                }
            }
        }
        f.eachFile{
            files += it.absolutePath.replaceAll(FILE_SEPARATOR_REGEX, Matcher.quoteReplacement(File.separator))
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
        name = name.replaceAll(FILE_SEPARATOR_REGEX, Matcher.quoteReplacement(File.separator))
        projectFiles?.find{it.contains(name)}
    }

    static String getShortClassPath(String className, Collection projectFiles){
        def filename = getClassPath(className, projectFiles)
        return getShortClassPath(filename)
    }

    static String getShortClassPath(String classPath){
        if(!classPath?.isEmpty()) return (classPath - PROJECT_PATH).substring(1)
        else return ""
    }

    static String getGspPath(String resourcePath, List projectFiles, String projectDir){
        def name = resourcePath.replaceAll(FILE_SEPARATOR_REGEX, Matcher.quoteReplacement(File.separator))
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

    static getInterfaceFileName(String path, String scenarioName){
        def beginIndex = path.lastIndexOf(File.separator)
        def name = path.substring(beginIndex+1)
        INTERFACES_PATH + (name - FEATURE_FILENAME_EXTENSION) +"-"+ scenarioName.replace(" ", "_") +"-"+ INTERFACE_FILENAME_EXTENSION
    }

    static getJsonFileName(String path){
        def beginIndex = path.lastIndexOf(File.separator)
        def name = path.substring(beginIndex+1)
        JSON_PATH + (name - FEATURE_FILENAME_EXTENSION) + JSON_FILENAME_EXTENSION
    }

}
