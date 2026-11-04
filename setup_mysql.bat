@echo off
echo ==============================================================
echo   Online Quiz Management System - MySQL Setup Utility
echo ==============================================================
echo.
echo Connecting to local MySQL server to initialize 'online_quiz_db'...
echo.

set /p DB_USER=Enter MySQL Username [default: root]: 
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASS=Enter MySQL Password [default: root]: 
if "%DB_PASS%"=="" set DB_PASS=root

echo.
echo Running SQL script against MySQL...
mysql -u %DB_USER% -p%DB_PASS% < sql\online_quiz_db.sql

if %ERRORLEVEL% equ 0 (
    echo.
    echo ==============================================================
    echo  SUCCESS! Database 'online_quiz_db' and tables created.
    echo  All candidate records and schema successfully initialized!
    echo ==============================================================
) else (
    echo.
    echo [Notice] Standard mysql CLI not found or login rejected.
    echo Note: The application's Java backend also automatically initializes
    echo 'online_quiz_db' through JDBC on startup when MySQL server is running.
)
echo.
pause
