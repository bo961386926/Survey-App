#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PNPM_VERSION="${PNPM_VERSION:-9.15.9}"

echo "==> Checking Node.js"
if ! command -v node >/dev/null 2>&1; then
  echo "Node.js is required. Please install Node.js 22 LTS or newer first."
  exit 1
fi

node -v

if ! command -v pnpm >/dev/null 2>&1; then
  echo "==> pnpm not found, installing pnpm ${PNPM_VERSION}"

  if command -v corepack >/dev/null 2>&1; then
    corepack enable
    corepack prepare "pnpm@${PNPM_VERSION}" --activate
  elif command -v npm >/dev/null 2>&1; then
    npm install -g "pnpm@${PNPM_VERSION}"
  else
    echo "Neither corepack nor npm is available in PATH."
    echo "Install a full Node.js distribution, then rerun this script."
    exit 1
  fi
fi

echo "==> Installing admin web dependencies"
cd "${ROOT_DIR}/admin-web-soybean"
SIMPLE_GIT_HOOKS_SKIP_INSTALL=1 pnpm install --frozen-lockfile

echo "==> Installing mobile app dependencies"
cd "${ROOT_DIR}/mobile-app"
if command -v npm >/dev/null 2>&1; then
  npm ci
else
  echo "npm is required for mobile-app because it uses package-lock.json."
  exit 1
fi

echo "==> Frontend local environment is ready"
echo "Admin web: cd admin-web-soybean && pnpm dev"
echo "Mobile H5: cd mobile-app && npm run dev:h5"
