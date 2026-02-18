# PROTOCOLO DE PERSISTÊNCIA GHOST
$ShortcutName = "GHOST_AutoStart"
$WScriptShell = New-Object -ComObject WScript.Shell

# Define os caminhos
$BatchPath = Join-Path -Path $PSScriptRoot -ChildPath "GHOST_INIT.bat"
$StartupFolder = [System.Environment]::GetFolderPath("Startup")
$ShortcutPath = Join-Path -Path $StartupFolder -ChildPath "$ShortcutName.lnk"

# Cria o atalho na pasta de Inicialização do Windows
$Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = "cmd.exe"
$Shortcut.Arguments = "/c start `"`" `"$BatchPath`""
$Shortcut.WorkingDirectory = $PSScriptRoot
$Shortcut.WindowStyle = 7 # Inicia minimizado para não poluir sua tela inicial
$Shortcut.Save()

# Define o atalho para sempre rodar como Administrador (Nível God)
$Bytes = [System.IO.File]::ReadAllBytes($ShortcutPath)
$Bytes[0x15] = $Bytes[0x15] -bor 0x20 # Seta o bit de Administrador
[System.IO.File]::WriteAllBytes($ShortcutPath, $Bytes)

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "   PROTOCOLO DE PERSISTÊNCIA ATIVADO COM SUCESSO" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "O GHOST agora despertará automaticamente com o Windows."
Write-Host "Local do Atalho: $StartupFolder"
pause