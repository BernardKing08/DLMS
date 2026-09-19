# build-and-pull-images.ps1
#
# Builds a Docker image for one or more microservices using Jib
# (mvnw compile jib:build) and then pulls the freshly-pushed image
# from Docker Hub so it exists in your local Docker daemon too.
#
# Jib doesn't need a local Docker daemon to build - it pushes straight
# to the registry named in each pom.xml's <to><image> tag. This script
# does the push (via jib:build) and then the pull for you in one step.
#
# Requirements:
#   - `docker login` already done (jib:build pushes to Docker Hub using
#     your local Docker/Maven credentials)
#   - Docker Desktop (or another daemon) running, for the `docker pull` step
#
# Usage (run from anywhere):
#   & "C:\Users\HomePC\Desktop\Spring_Projects\DLMS\docs\build-and-pull-images.ps1"
#
#   # Only build/pull specific services:
#   & "...\build-and-pull-images.ps1" -Services account,frontend
#
#   # Build/push only, skip the docker pull step:
#   & "...\build-and-pull-images.ps1" -SkipPull

param(
    [string[]]$Services = @(),
    [switch]$SkipPull
)

$root = "C:\Users\HomePC\Desktop\Spring_Projects\DLMS"

# Name -> { Path, Image }. Image is the exact <to><image> tag from each
# service's pom.xml, so it must be kept in sync if a pom's tag changes.
$allServices = [ordered]@{
    account     = @{ Path = "$root\account\acc";              Image = "bernardking08/accounts-service:cc2" }
    auth        = @{ Path = "$root\authentication\auth";      Image = "bernardking08/auth-service:cc2" }
    catalog     = @{ Path = "$root\catalog\catalogs";          Image = "bernardking08/catalog-service:cc2" }
    inventory   = @{ Path = "$root\inventory\invtry";          Image = "bernardking08/inventory-service:cc2" }
    frontend    = @{ Path = "$root\frontend";                  Image = "bernardking08/frontend-service:cc2" }
    eureka      = @{ Path = "$root\eurekaserver";               Image = "bernardking08/eureka-server:cc2" }
    gateway     = @{ Path = "$root\gatewayserver";              Image = "bernardking08/gatewayserver:cc2" }
    configserver = @{ Path = "$root\configServer";              Image = "bernardking08/config-server:cc2" }
}

$targets = if ($Services.Count -gt 0) {
    foreach ($name in $Services) {
        if (-not $allServices.Contains($name)) {
            Write-Host "Unknown service '$name'. Valid names: $($allServices.Keys -join ', ')" -ForegroundColor Red
            exit 1
        }
    }
    $Services
} else {
    $allServices.Keys
}

foreach ($name in $targets) {
    $svc = $allServices[$name]

    Write-Host ""
    Write-Host "=== $name : mvnw compile jib:build ($($svc.Image)) ===" -ForegroundColor Cyan
    Push-Location $svc.Path
    try {
        & .\mvnw.cmd compile jib:build
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Build/push FAILED for $name (exit $LASTEXITCODE)." -ForegroundColor Red
            Pop-Location
            continue
        }
    } finally {
        Pop-Location
    }

    if (-not $SkipPull) {
        Write-Host "=== $name : docker pull $($svc.Image) ===" -ForegroundColor Cyan
        docker pull $svc.Image
        if ($LASTEXITCODE -ne 0) {
            Write-Host "docker pull FAILED for $name (exit $LASTEXITCODE)." -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "Done." -ForegroundColor Green
