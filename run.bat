@echo off
cd /d "%~dp0"
set "JAVA_HOME=E:\DevTools\jdk-21"
set "PATH=E:\DevTools\jdk-21\bin;%PATH%"

if not exist "build\classes\edu\oel\tasknest\TaskNestApp.class" (
    echo Compiling TaskNest...
    javac -encoding UTF-8 -d build\classes src\main\java\edu\oel\tasknest\*.java
)

start "" javaw -cp build\classes edu.oel.tasknest.TaskNestApp
