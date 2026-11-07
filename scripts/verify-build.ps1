# AppService CI / Build Verification Harness (PowerShell)
# Verifies clean compilation and test execution across all 8 multi-module artifacts

Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   AppService Multi-Module Build & CI Verification   " -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan

$startTime = Get-Date

Write-Host "[1/3] Validating Maven and Java Environment..." -ForegroundColor Yellow
mvn -version
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven is not installed or not available on PATH."
    exit 1
}

Write-Host "`n[2/3] Running 'mvn clean test-compile' across aggregator root..." -ForegroundColor Yellow
mvn clean test-compile
if ($LASTEXITCODE -ne 0) {
    Write-Error "Aggregator compilation failed! Non-zero exit code: $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host "`n[3/3] Running full unit test suite across all submodules..." -ForegroundColor Yellow
mvn test
if ($LASTEXITCODE -ne 0) {
    Write-Error "Test suite execution failed! Non-zero exit code: $LASTEXITCODE"
    exit $LASTEXITCODE
}

$elapsed = (Get-Date) - $startTime
Write-Host "`n======================================================" -ForegroundColor Green
Write-Host " Build & Verification SUCCESSFUL (Elapsed: $($elapsed.TotalSeconds.ToString("F2"))s)" -ForegroundColor Green
Write-Host "======================================================" -ForegroundColor Green
exit 0
