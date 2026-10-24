# Script de PowerShell para iniciar ServiClient CRM Helpdesk con MySQL
$OutputEncoding = [System.Text.Encoding]::UTF8
Clear-Host

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          🚀 SERVICLIENT CRM HELPDESK - INICIO RÁPIDO (MySQL)" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# Configurar variables de entorno si existen rutas locales
if (Test-Path "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot") {
    $env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

if (Test-Path "C:\Users\DANNY\apache-maven-3.9.9\bin") {
    $env:PATH = "C:\Users\DANNY\apache-maven-3.9.9\bin;$env:PATH"
}

Write-Host "[✓] Entorno configurado" -ForegroundColor Green
Write-Host "[✓] Base de Datos: MySQL (serviclient)" -ForegroundColor Green
Write-Host "[✓] URL: http://localhost:8080" -ForegroundColor Green
Write-Host "[✓] Credenciales: admin@serviclient.com / admin123" -ForegroundColor Green
Write-Host ""
Write-Host "Iniciando servidor y abriendo navegador en 6 segundos..." -ForegroundColor Gray
Write-Host "Para detener el servidor presiona Ctrl + C" -ForegroundColor DarkYellow
Write-Host "----------------------------------------------------------------------" -ForegroundColor Gray
Write-Host ""

# Abrir el navegador tras 6 segundos en un hilo separado
Start-Job -ScriptBlock {
    Start-Sleep -Seconds 6
    Start-Process "http://localhost:8080"
} | Out-Null

# Ejecutar la aplicación con perfil MySQL
if (Test-Path "target\crm-helpdesk-1.0.0.jar") {
    java -jar target\crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql
} else {
    mvn spring-boot:run -Dspring-boot.run.profiles=mysql
}
