@echo off
echo ==============================================================
echo  Compiling Online Quiz Management System
echo ==============================================================

if not exist bin mkdir bin

echo Collecting Java source files...
dir /s /b src\main\java\*.java > sources.txt

echo Running javac compiler with MySQL JDBC Driver in classpath...
javac -d bin -cp "bin;lib/*" @sources.txt

if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilation successful! Classes stored in bin\ directory.
) else (
    echo.
    echo Compilation failed! Please check errors above.
)

if exist sources.txt del sources.txt
echo ==============================================================
