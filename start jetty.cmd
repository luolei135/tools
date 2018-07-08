set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_79
set MAVEN_OPTS= -XXaltjvm=dcevm  -Xdebug -Xnoagent  -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=5099,server=y,suspend=n 
echo %date%_%time%
echo CLASSPATH=%CLASSPATH%
echo PATH=%PATH%

call mvn jetty:run
echo %CLASSPATH%
echo %PATH%
pause
 
