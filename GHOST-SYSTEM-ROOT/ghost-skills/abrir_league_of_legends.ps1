$lolPath = $null
# Common installation paths for League of Legends
$searchPaths = @(
    "C:\Riot Games\League of Legends",
    "C:\Program Files\Riot Games\League of Legends",
    "C:\Program Files (x86)\Riot Games\League of Legends"
)

foreach ($path in $searchPaths) {
    if (Test-Path -Path $path) {
        $clientPath = Join-Path -Path $path -ChildPath "LeagueClient.exe"
        if (Test-Path -Path $clientPath) {
            $lolPath = $clientPath
            break
        }
    }
}

if ($lolPath) {
    Write-Host "League of Legends client found at: $lolPath"
    Start-Process $lolPath
    Write-Output "League of Legends launched successfully."
} else {
    Write-Warning "League of Legends client executable (LeagueClient.exe) not found in common installation directories."
    Write-Output "Attempting to launch via Start-Process using application name (less reliable)."
    try {
        Start-Process "League of Legends"
        Write-Output "Attempted to launch 'League of Legends' via Start-Process."
    } catch {
        Write-Error "Failed to launch League of Legends. Please ensure it is installed and accessible."
        Write-Output "Consider manually launching or providing the exact path if issues persist."
    }
}