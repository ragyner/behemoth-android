@ECHO OFF
SET DIR=%~dp0
SET APP_BASE_NAME=%~n0
SET APP_HOME=%DIR%

IF NOT "%JAVA_HOME%"=="" (
    SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
) ELSE (
    SET JAVA_EXE=java.exe
)

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
