@echo off
chcp 65001 >nul
title ServiClient CRM Helpdesk - Servidor
cls

echo ======================================================================
echo           🚀 SERVICLIENT CRM HELPDESK - INICIO RÁPIDO (MySQL)
echo ======================================================================
echo.

:: 1. Configuración de entorno (Java 21 y Maven)
if exist "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot" (
    set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

if exist "C:\Users\DANNY\apache-maven-3.9.9\bin" (
    set "PATH=C:\Users\DANNY\apache-maven-3.9.9\bin;%PATH%"
)

echo [✓] Entorno configurado correctamente.
echo [✓] Base de Datos: MySQL (serviclient)
echo [✓] URL de la aplicación: http://localhost:8080
echo [✓] Credenciales demo: admin@serviclient.com / admin123
echo.
echo Abriendo navegador automáticamente en unos segundos...
echo Presiona Ctrl + C en esta ventana cuando desees detener el servidor.
echo ----------------------------------------------------------------------
echo.

:: 2. Lanzador del navegador en segundo plano con retardo
start "" cmd /c "timeout /t 6 /nobreak >nul & start http://localhost:8080"

:: 3. Ejecución del proyecto con perfil MySQL
if exist "target\crm-helpdesk-1.0.0.jar" (
    echo Iniciando aplicación desde archivo JAR con perfil MySQL...
    java -jar target\crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql
) else (
    echo Archivo JAR no encontrado. Compilando y ejecutando con Maven...
    mvn spring-boot:run -Dspring-boot.run.profiles=mysql
)

pause
