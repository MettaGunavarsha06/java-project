@echo off
echo ==============================================================
echo  Launching Online Quiz Management System (Java Full-Stack)
echo ==============================================================

if not exist bin\com\quiz\server\AppServer.class (
    echo Binary classes not found. Building project first...
    call build.bat
)

echo Starting Server...
java -cp bin com.quiz.server.AppServer

