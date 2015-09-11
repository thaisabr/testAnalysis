package utils

import org.springframework.util.ClassUtils
import java.util.regex.Matcher


class Utils {

    static config = new ConfigSlurper().parse(Utils.class.classLoader.getResource("ConfigAnathema.groovy"))
    static final PROJECT_PATH = config.project.path
    static final GROOVY_FILENAME_EXTENSION = ".groovy"
    static final JAVA_FILENAME_EXTENSION = ".java"
    static final JAR_FILENAME_EXTENSION = ".jar"
    static final FEATURE_FILENAME_EXTENSION = ".feature"
    static final TEXT_FILENAME_EXTENSION = ".txt"
    static final COMPOUND_INTERFACE_FILENAME = "ManyFiles"
    static final INTERFACE_FILENAME_EXTENSION = "Interface"
    static final JSON_FILENAME_EXTENSION = ".json"
    static final TEST_COD_REGEX = /.*(steps\.|pages\.|TestDataAndOperations).*/
    static final INVALID_CLASS_REGEX = /.*(groovy|java|springframework|apache|grails|spock|geb|selenium|cucumber).*/
    static final INVALID_METHOD_REGEX = /(println|print|setBinding)/
    static final FILE_SEPARATOR_REGEX = /(\\|\/)/
    static final PAGE_METHODS = ['to', 'at']
    static final INTERFACES_PATH = "${System.getProperty("user.dir")}${File.separator}interfaces${File.separator}"
    static final JSON_PATH = "${System.getProperty("user.dir")}${File.separator}json${File.separator}"
    static final STEPS = ['Given', 'When', 'Then', 'And', 'But']

    static List fillPluginsPath(){
        List pluginsPath = []
        config.grails.plugin.path?.each{ k, v ->
            pluginsPath += v
        }
        return pluginsPath
    }

    static configClassnameFromMethod(String className){
        if (className.startsWith(ClassUtils.NON_PRIMITIVE_ARRAY_PREFIX) && className.endsWith(";")) {
            className = className.substring(ClassUtils.NON_PRIMITIVE_ARRAY_PREFIX.length(), className.length() - 1)
        }
        return className
    }

    static boolean isValidClassByAPI(String referencedClass){
        if(INVALID_CLASS_REGEX) {
            if(referencedClass ==~ INVALID_CLASS_REGEX) false
            else true
        }
        else true
    }

    static boolean isValidClass(String referencedClass, String path){
        if(path!=null && !path.isEmpty() && isValidClassByAPI(referencedClass)) true
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

    static getJavaFilesFromDirectory(String directory){
        def files = getFilesFromDirectory(directory)
        files.findAll{it.contains(JAVA_FILENAME_EXTENSION)}
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
        if(classPath != null && !classPath.isEmpty()) {
            def diff = classPath - PROJECT_PATH
            if(!diff.isEmpty()) return diff.substring(1)
            else return classPath
        }
        else return ""
    }

    static String getGspPath(String resourcePath, List projectFiles){
        def name = resourcePath.replaceAll(FILE_SEPARATOR_REGEX, Matcher.quoteReplacement(File.separator))
        int n = name.count(File.separator)
        if(n>1){
            def index = name.lastIndexOf(File.separator)
            name = name.substring(0,index)
        }

        def match = projectFiles?.find{ it.contains(name) } //the best is to define a gsp directory as grails-app\views\
        if(match) name = match - PROJECT_PATH
        else name = ""
        name
    }

    static getInterfaceFileName(String path){
        def beginIndex = path.lastIndexOf(File.separator)
        def name = path.substring(beginIndex+1)
        INTERFACES_PATH + (name - GROOVY_FILENAME_EXTENSION) + INTERFACE_FILENAME_EXTENSION + TEXT_FILENAME_EXTENSION
    }

    static getInterfaceFileName(String path, String scenarioName){
        def beginIndex = path.lastIndexOf(File.separator)
        def name = path.substring(beginIndex+1)
        INTERFACES_PATH + (name-FEATURE_FILENAME_EXTENSION) + INTERFACE_FILENAME_EXTENSION + "-" + scenarioName + TEXT_FILENAME_EXTENSION
    }

    static getJsonFileName(String path){
        def beginIndex = path.lastIndexOf(File.separator)
        def name = path.substring(beginIndex+1)
        JSON_PATH + (name - FEATURE_FILENAME_EXTENSION) + JSON_FILENAME_EXTENSION
    }

    static getCompoundInterfaceFileName(){
        INTERFACES_PATH+COMPOUND_INTERFACE_FILENAME+INTERFACE_FILENAME_EXTENSION+TEXT_FILENAME_EXTENSION
    }

}
