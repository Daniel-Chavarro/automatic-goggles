param(
    [switch]$SkipInfra,
    [switch]$SkipBuild,
    [switch]$DryRun,
    [int]$CommercePort = 8080
)

$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
if (-not $root) {
    $root = (Get-Location).Path
}

Set-Location -LiteralPath $root

$mavenCommand = "mvn"
if (Test-Path -LiteralPath ".\mvnw.cmd") {
    $mavenCommand = Join-Path -Path $root -ChildPath "mvnw.cmd"
}

$escapedRoot = $root.Replace("'", "''")
$escapedMavenCommand = $mavenCommand.Replace("'", "''")
$commerceCommand = "Set-Location -LiteralPath '$escapedRoot'; `$env:SERVER_PORT = '$CommercePort'; & '$escapedMavenCommand' -f commerce-app/pom.xml spring-boot:run"
$paymentsCommand = "Set-Location -LiteralPath '$escapedRoot'; & '$escapedMavenCommand' -f payments-service/pom.xml spring-boot:run"

if ($DryRun) {
    Write-Host "Dry run only. No files, containers, builds, or service windows will be changed."
    Write-Host "Maven command: $mavenCommand"
    if (-not $SkipInfra) {
        Write-Host "Would run: docker compose up -d postgres rabbitmq"
    }
    if (-not $SkipBuild) {
        Write-Host "Would run: $mavenCommand -DskipTests install"
    }
    Write-Host "Would set SERVER_PORT=$CommercePort for commerce-app"
    Write-Host "Would open commerce-app with: $commerceCommand"
    Write-Host "Would open payments-service with: $paymentsCommand"
    exit 0
}

if (-not (Test-Path -LiteralPath ".env") -and (Test-Path -LiteralPath ".env.example")) {
    Copy-Item -LiteralPath ".env.example" -Destination ".env"
    Write-Host "Created .env from .env.example. Review it if you need custom credentials."
}

if (-not $SkipInfra) {
    Write-Host "Starting local dependencies: postgres and rabbitmq..."
    docker compose up -d postgres rabbitmq
}

if (-not $SkipBuild) {
    Write-Host "Installing Maven modules without tests so runnable modules can resolve local dependencies..."
    & $mavenCommand -DskipTests install
}

Write-Host "Opening commerce-app on http://localhost:$CommercePort ..."
Start-Process powershell.exe -ArgumentList @("-NoExit", "-Command", $commerceCommand)

Write-Host "Opening payments-service worker..."
Start-Process powershell.exe -ArgumentList @("-NoExit", "-Command", $paymentsCommand)

Write-Host "Project startup launched. Use Ctrl+C in each service window to stop the services."
Write-Host "RabbitMQ management UI: http://localhost:15672"
