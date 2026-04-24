<#!
  Ejecutor de comandos del proyecto a partir de registry.json.
  Uso:
    .\scripts\run.ps1 -List
    .\scripts\run.ps1 -Id eureka-up
  Variables opcionales: copia env.local.example a scripts\.env.local (KEY=valor, una por línea).
#>
param(
    [Parameter(Mandatory = $false)]
    [string] $Id,

    [switch] $List
)

$ErrorActionPreference = 'Stop'
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RegistryPath = Join-Path $ScriptDir 'registry.json'

function Import-DotEnvLocal {
    $envFile = Join-Path $ScriptDir '.env.local'
    if (-not (Test-Path $envFile)) { return }
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq '' -or $line.StartsWith('#')) { return }
        $idx = $line.IndexOf('=')
        if ($idx -lt 1) { return }
        $name = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        Set-Item -Path "Env:$name" -Value $value
    }
}

Import-DotEnvLocal

$registry = Get-Content $RegistryPath -Raw -Encoding UTF8 | ConvertFrom-Json

if ($List) {
    Write-Host "Comandos disponibles (registry.json):" -ForegroundColor Cyan
    foreach ($c in $registry.commands) {
        Write-Host ("  {0,-18} {1}" -f $c.id, $c.title)
    }
    return
}

if ([string]::IsNullOrWhiteSpace($Id)) {
    Write-Host "Indica -Id <comando> o usa -List. Ejemplo: .\scripts\run.ps1 -Id eureka-up" -ForegroundColor Yellow
    exit 1
}

$cmd = $registry.commands | Where-Object { $_.id -eq $Id } | Select-Object -First 1
if (-not $cmd) {
    Write-Host "No existe el id '$Id' en registry.json. Usa -List." -ForegroundColor Red
    exit 1
}

$cwd = Resolve-Path (Join-Path $ScriptDir $cmd.cwd)
$block = $cmd.windows
if (-not $block) {
    Write-Host "La entrada '$Id' no define bloque 'windows'." -ForegroundColor Red
    exit 1
}

$program = Join-Path $cwd $block.program
$argList = @()
if ($block.args) { $argList = @($block.args) }

Write-Host ("→ {0}" -f $cmd.title) -ForegroundColor Green
Write-Host ("  cwd: {0}" -f $cwd) -ForegroundColor DarkGray
Write-Host ("  {0} {1}" -f $program, ($argList -join ' ')) -ForegroundColor DarkGray

Push-Location $cwd
try {
    & $program @argList
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
