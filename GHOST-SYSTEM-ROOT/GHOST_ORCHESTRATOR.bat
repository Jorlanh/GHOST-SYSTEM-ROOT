@echo off
TITLE GHOST SYSTEMS - ORCHESTRATION & BUILD PROTOCOL
SETLOCAL

:: --- 1. VERIFICAÇÃO DE PRIVILÉGIOS ---
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [!] Solicitando permissoes de nivel God...
    powershell -Command "Start-Process '%0' -Verb RunAs"
    exit /b
)

:init
:: Força o terminal a voltar para a pasta raiz após virar Admin
cd /d "%~dp0"

CLS
color 0A
echo ============================================================
echo           GHOST - GLOBAL HEURISTIC OPERATIONAL
echo                 FULL SYSTEM ORCHESTRATION
echo ============================================================

:: --- 2. DESTRUIÇÃO DE ZUMBIS (LIMPANDO O CAMPO) ---
echo [0.5/5] Limpando portas de comunicacao (8080, 8081 e 8761)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do taskkill /F /PID %%a 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8761') do taskkill /F /PID %%a 2>nul

:: --- 3. INFRAESTRUTURA (REDIS VIA DOCKER) ---
echo.
echo [1/5] Iniciando Infraestrutura (Redis)...
docker start ghost-redis >nul 2>&1 || docker run --name ghost-redis -p 6379:6379 -d redis
echo [INFRA] Redis Ativo e Mapeado.

:: --- 4. EXECUÇÃO ORQUESTRADA ---
echo.
echo [2/5] Despachando binarios do ecossistema...

:: Discovery
echo [LAUNCH] Discovery (Cerebro)...
if exist "ghost-discovery\target\*.jar" (
    start "GHOST_DISCOVERY" cmd /c "title DISCOVERY (8761) & java -jar ghost-discovery\target\ghost-discovery-0.0.1-SNAPSHOT.jar & pause"
    timeout /t 15
) else (echo [ERRO] Binario Discovery nao encontrado.)

:: Core
echo [LAUNCH] Core (Coracao)...
if exist "ghost-core\target\*.jar" (
    start "GHOST_CORE" cmd /c "title CORE (8081) & java -jar ghost-core\target\ghost-core-0.0.1-SNAPSHOT.jar & pause"
    timeout /t 5
) else (echo [ERRO] Binario Core nao encontrado.)

:: Integrations
if exist "ghost-integrations\target\*.jar" (
    echo [LAUNCH] Integrations...
    start "GHOST_INTEGRATIONS" cmd /c "title INTEGRATIONS & java -jar ghost-integrations\target\ghost-integrations-0.0.1-SNAPSHOT.jar & pause"
)

:: Gateway (A Mágica do Java 21)
echo [LAUNCH] Gateway (Escudo)...
if exist "ghost-gateway\target\*.jar" (
    start "GHOST_GATEWAY" cmd /c "title GATEWAY (8080) & java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED -jar ghost-gateway\target\ghost-gateway-0.0.1-SNAPSHOT.jar & pause"
) else (echo [ERRO] Binario Gateway nao encontrado.)

echo.
echo ============================================================
echo [STATUS] GHOST SYSTEM OPERACIONAL.
echo ============================================================
pause