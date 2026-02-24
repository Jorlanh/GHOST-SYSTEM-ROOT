:: --- 3. EXECUÇÃO ORQUESTRADA (PROTOCOLO DE FORÇA BRUTA) ---
echo.
echo [3/3] Despachando binários...

:: Discovery
echo [LAUNCH] Discovery...
start "GHOST_DISCOVERY" cmd /c "java -jar ghost-discovery\target\ghost-discovery-0.0.1-SNAPSHOT.jar & pause"
timeout /t 10

:: Core
echo [LAUNCH] Core...
start "GHOST_CORE" cmd /c "java -jar ghost-core\target\ghost-core-0.0.1-SNAPSHOT.jar & pause"
timeout /t 5

:: Gateway
echo [LAUNCH] Gateway...
start "GHOST_GATEWAY" cmd /c "java -jar ghost-gateway\target\ghost-gateway-0.0.1-SNAPSHOT.jar & pause"

echo.
echo ============================================================
echo [STATUS] Comandos enviados. Se as janelas fecharam, verifique:
echo 1. Se o nome do arquivo JAR é exatamente o citado acima.
echo 2. Se o Java está no PATH (digite 'java -version' no terminal).
echo ============================================================
pause