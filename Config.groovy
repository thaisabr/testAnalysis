//Path of the Grails project (to change)
project.path = "${System.getProperty("user.home")}${File.separator}Documents${File.separator}GitHub${File.separator}rgms"

//Path of the test source code to analyse (to change)
project.test.file = "${project.path}${File.separator}test${File.separator}cucumber${File.separator}steps${File.separator}ArticleSteps.groovy"

//Default path of compiled code of Grails project that uses Cucumber
project.production.path = "${project.path}${File.separator}target${File.separator}classes"

//Default path of compiled test code of Grails project that uses Cucumber
project.test.path = "${project.path}${File.separator}target${File.separator}test-classes${File.separator}functional"

//Grails default path of dependency cache: local file system at user.home/.grails/ivy-cache or user.home/.m2/repository when using Aether
grails.dependencyCache = "${System.getProperty("user.home")}${File.separator}.grails${File.separator}ivy-cache"

//It is possible to manually configure the path of installed plugins; if there is no configuration here, the grails default dependency cache will be used
grails.plugin.path.cucumber = "${grails.dependencyCache}${File.separator}info.cukes${File.separator}cucumber-groovy${File.separator}jars${File.separator}cucumber-groovy-1.1.1.jar"
grails.plugin.path.geb = "${grails.dependencyCache}${File.separator}org.codehaus.geb${File.separator}geb-core${File.separator}jars${File.separator}geb-core-0.7.1.jar"
grails.plugin.path.shiro = "${grails.dependencyCache}${File.separator}org.apache.shiro${File.separator}shiro-core${File.separator}bundles${File.separator}shiro-core-1.2.0.jar"
grails.plugin.path.selenium = "${grails.dependencyCache}${File.separator}org.seleniumhq.selenium${File.separator}selenium-api${File.separator}jars${File.separator}selenium-api-2.22.0.jar"