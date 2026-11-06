<#
.SYNOPSIS
    Automated installation and Knowledge Graph synchronization script for Code-Graph-RAG (CGR) in AppService.
.DESCRIPTION
    Verifies Python 3.12+, installs ripgrep, installs code-graph-rag and dependencies,
    ensures environment PATH variables, and builds the code knowledge graph.
#>

[CmdletBinding()]
param(
    [switch]$SkipEmbeddings = $true,
    [switch]$ForceRebuild = $false
)

$ErrorActionPreference = "Stop"

Write-Host "=== [1/5] Checking Python Environment ===" -ForegroundColor Cyan
if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Error "Python is not installed or not in PATH. Please install Python 3.12+ from python.org or winget."
}
$pythonVersion = python --version 2>&1
Write-Host "Found: $pythonVersion" -ForegroundColor Green

Write-Host "`n=== [2/5] Checking ripgrep (rg) Dependency ===" -ForegroundColor Cyan
if (-not (Get-Command rg -ErrorAction SilentlyContinue)) {
    Write-Host "ripgrep not found in PATH. Checking WinGet packages..." -ForegroundColor Yellow
    $rgPkg = Get-ChildItem -Path "$env:LOCALAPPDATA\Microsoft\WinGet\Packages" -Filter "rg.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($rgPkg) {
        $rgDir = $rgPkg.DirectoryName
        Write-Host "Found ripgrep at: $rgDir. Adding to PATH..." -ForegroundColor Green
        $env:Path = "$rgDir;$env:Path"
    } else {
        Write-Host "Installing ripgrep via winget..." -ForegroundColor Yellow
        winget install BurntSushi.ripgrep.MSVC --accept-package-agreements --accept-source-agreements
        $rgPkg = Get-ChildItem -Path "$env:LOCALAPPDATA\Microsoft\WinGet\Packages" -Filter "rg.exe" -Recurse | Select-Object -First 1
        if ($rgPkg) {
            $env:Path = "$($rgPkg.DirectoryName);$env:Path"
        }
    }
}
Write-Host "ripgrep is verified." -ForegroundColor Green

Write-Host "`n=== [3/5] Installing Code-Graph-RAG Packages ===" -ForegroundColor Cyan
pip install -r "$PSScriptRoot\..\requirements-cgr.txt"

# Add Python Scripts directory to PATH if needed
$pyScripts = "$env:LOCALAPPDATA\Python\pythoncore-3.14-64\Scripts"
if (Test-Path $pyScripts) {
    if ($env:Path -notlike "*$pyScripts*") {
        $env:Path = "$pyScripts;$env:Path"
    }
}

Write-Host "`n=== [4/5] Running CGR Diagnostic Check ===" -ForegroundColor Cyan
cgr doctor

Write-Host "`n=== [5/5] Building Project Knowledge Graph ===" -ForegroundColor Cyan
$repoRoot = (Resolve-Path "$PSScriptRoot\..").Path
$syncArgs = @("start", "--repo-path", $repoRoot, "--update-graph", "--exclude", "target")
if ($SkipEmbeddings) {
    $syncArgs += "--no-embeddings"
}
if ($ForceRebuild) {
    $syncArgs += "--clean"
    $syncArgs += "-y"
}

cgr @syncArgs

Write-Host "`n=== Code-Graph-RAG Setup & Knowledge Graph Generation Complete! ===" -ForegroundColor Green
cgr stats
