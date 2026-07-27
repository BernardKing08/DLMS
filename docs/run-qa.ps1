# run-qa.ps1
# Starts account, auth, catalog, and inventory locally (no Docker) on the
# "qa" Spring profile - each backed by in-memory H2 instead of MySQL.
# Every service launches in its own PowerShell window so you can watch
# each one's logs separately. Close a window (or Ctrl+C inside it) to
# stop that service.
#
# Paths below are absolute, so this script can be run from anywhere:
#   & "C:\Users\HomePC\Desktop\Spring_Projects\DLMS\docs\run-qa.ps1"

$services = @(
    @{ Name = "account";   Path = "C:\Users\HomePC\Desktop\Spring_Projects\DLMS\account\acc" },
    @{ Name = "auth";      Path = "C:\Users\HomePC\Desktop\Spring_Projects\DLMS\authentication\auth" },
    @{ Name = "catalog";   Path = "C:\Users\HomePC\Desktop\Spring_Projects\DLMS\catalog\catalogs" },
    @{ Name = "inventory"; Path = "C:\Users\HomePC\Desktop\Spring_Projects\DLMS\inventory\invtry" }
)

foreach ($svc in $services) {
    Write-Host "Starting $($svc.Name) on qa profile..."
    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$($svc.Path)'; `$env:SPRING_PROFILES_ACTIVE = 'qa'; Write-Host '=== $($svc.Name) (qa) ===' -ForegroundColor Cyan; ./mvnw spring-boot:run"
    )
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "All four services are launching in separate windows:"
Write-Host "  account   -> http://localhost:8080"
Write-Host "  auth      -> http://localhost:8090"
Write-Host "  catalog   -> http://localhost:9000"
Write-Host "  inventory -> http://localhost:9010"
Write-Host ""
Write-Host "Note: each service still tries to register with Eureka at localhost:8761." -ForegroundColor Yellow
Write-Host "If eurekaserver isn't running, you'll see harmless connection-retry warnings" -ForegroundColor Yellow
Write-Host "in the logs - the services still start and work fine without it." -ForegroundColor Yellow
