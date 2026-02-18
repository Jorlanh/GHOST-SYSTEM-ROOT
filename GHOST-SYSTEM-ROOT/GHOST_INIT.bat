@echo off
TITLE GHOST SYSTEMS - INITIALIZATION PROTOCOL
SETLOCAL

:: --- 1. VERIFICAÇÃO DE PRIVILÉGIOS (ADMIN) ---
net session >nul 2>&1
if %errorLevel% == 0 (
    goto :init
) else (
    echo [!] Solicitando permissões de nível God...
    powershell -Command "Start-Process '%0' -Verb RunAs"
    exit /b
)

:init
CLS
echo ============================================================
echo           GHOST - GLOBAL HEURISTIC OPERATIONAL
echo ============================================================
echo [STATUS] Protocolo de inicializacao iniciado...

:: --- 2. CONFIGURAÇÃO ADB WIRELESS ---
:: Substitua pelo IP estático do seu Android
SET ANDROID_IP=192.168.1.15
SET ANDROID_PORT=5555

echo [ADB] Reiniciando servidor e estabelecendo link...
adb kill-server >nul 2>&1
adb start-server >nul 2>&1
echo [ADB] Tentando conectar ao dispositivo em %ANDROID_IP%...
adb connect %ANDROID_IP%:%ANDROID_PORT%
adb devices

if errorlevel 1 (
    echo [ERRO] Falha ao conectar ADB. Verifique Wi-Fi/Debug ou cabo USB.
) else (
    echo [ADB] Link neural estabelecido com sucesso.
)

:: --- 3. SUBIR O BACKEND (GHOST-CORE) ---
echo [SYSTEM] Subindo Cérebro (Spring Boot)...
:: Altere para o caminho real do seu JAR se necessário
SET JAR_PATH=target\ghost-core-0.0.1-SNAPSHOT.jar

where java >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Java não encontrado no PATH. Instale ou configure as variáveis de ambiente.
    pause
    exit /b
)

if exist %JAR_PATH% (
    :: Inicia o Java em uma nova janela para não travar o log
    start "GHOST_CORE_LOGS" cmd /c "java -jar %JAR_PATH%"
    echo [SYSTEM] GHOST Core iniciado. Verifique a janela de logs.
) else (
    echo [ERRO] O binario .jar nao foi encontrado em %JAR_PATH%.
    echo [!] Execute 'mvn clean install' antes de iniciar.
    pause
    exit /b
)

:: --- 4. VERIFICAÇÃO FINAL ---
echo [STATUS] GHOST operando em modo Administrador.
echo [STATUS] Onipotencia Operacional confirmada.
echo ============================================================
echo Aguardando comandos, Senhor Walker...
timeout /t 5 >nul 2>&1