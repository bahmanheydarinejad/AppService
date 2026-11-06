#!/usr/bin/env bash
# Automated installation and Knowledge Graph synchronization script for Code-Graph-RAG (CGR)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== [1/4] Checking Python & ripgrep ==="
python3 --version || { echo "Python 3 is required."; exit 1; }
command -v rg >/dev/null 2>&1 || { echo "ripgrep ('rg') is required. Install via apt/brew/pacman."; exit 1; }

echo "=== [2/4] Installing Code-Graph-RAG ==="
pip install -r "$REPO_ROOT/requirements-cgr.txt"

echo "=== [3/4] Running CGR Diagnostics ==="
cgr doctor || true

echo "=== [4/4] Building Project Knowledge Graph ==="
cgr start --repo-path "$REPO_ROOT" --update-graph --no-embeddings --exclude target

echo "=== Knowledge Graph Status ==="
cgr stats
