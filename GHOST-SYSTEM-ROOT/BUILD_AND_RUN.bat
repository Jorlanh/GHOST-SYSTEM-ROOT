:: --- 3. EXECUÇÃO ORQUESTRADA (PROTOCOLO DE FORÇA BRUTA) ---
echo.
echo [3/3] Despachando binários e subindo Córtex Vocal...

:: Ghost-Voice (DOCKER)
echo [LAUNCH] Ghost-Voice (Vegeta Mode)...
:: O comando 'up -d' garante que ele suba em segundo plano
docker-compose up -d ghost-voice
timeout /t 5

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
echo [STATUS] Comandos enviados. Protocolo Vegeta Ativo.
echo 1. Ghost-Voice rodando no Docker (Porta 5000).
echo 2. Se a voz não sair, verifique 'docker logs ghost-voice'.
echo ============================================================
pause