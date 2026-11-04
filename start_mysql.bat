@echo off
echo ==============================================================
echo   Online Quiz Management System - MySQL Server Launcher
echo ==============================================================

netstat -ano | findstr 3306 >nul
if %ERRORLEVEL% equ 0 (
    echo MySQL is already running on port 3306.
    goto end
)

echo Starting MySQL / MariaDB Server...
if exist "C:\Program Files\MariaDB 13.0\bin\mysqld.exe" (
    start "MySQL Database Server" /B "C:\Program Files\MariaDB 13.0\bin\mysqld.exe" --defaults-file="C:\Program Files\MariaDB 13.0\data\my.ini" --console
    timeout /t 2 /nobreak >nul
    echo MySQL server started successfully on port 3306!
) else (
    net start mysql 2>nul || net start MySQL80 2>nul
)

:end
echo ==============================================================
