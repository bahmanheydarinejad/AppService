#!/usr/bin/env bash
# AppService CI / Build Verification Harness (POSIX Bash)
# Verifies clean compilation and test execution across all 8 multi-module artifacts

set -euo pipefail

echo "======================================================"
echo "   AppService Multi-Module Build & CI Verification   "
echo "======================================================"

echo "[1/3] Validating Maven and Java Environment..."
mvn -version

echo ""
echo "[2/3] Running 'mvn clean test-compile' across aggregator root..."
mvn clean test-compile

echo ""
echo "[3/3] Running full unit test suite across all submodules..."
mvn test

echo ""
echo "======================================================"
echo " Build & Verification SUCCESSFUL (All Modules Clean)  "
echo "======================================================"
