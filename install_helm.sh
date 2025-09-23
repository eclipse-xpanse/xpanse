#!/bin/sh

#
# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: Huawei Inc.
#

# This script is a wrapper to the Helm's official installer script.
# We use the same script since it already has all details specific to helm is covered.

validateArguments() {
  if [ -z "$1" ] || [ -z "$2" ]; then
      echo "Error：HELM_INSTALL_PATH and DEFAULT_HELM_VERSION cannot be empty."
      exit 1
  fi
}

setupInstallationDirectory() {
  mkdir -p "${HELM_INSTALL_PATH}"
  export PATH="$HELM_INSTALL_PATH:$PATH"
}

downloadOfficialHelmInstallerScript() {
  HELM_SCRIPT_URL="https://raw.githubusercontent.com/helm/helm/refs/heads/main/scripts/get-helm-3"
  wget -q -P /tmp "$HELM_SCRIPT_URL"
  chmod +x /tmp/get-helm-3
}

installDefaultHelmVersion() {
  # Install default version of Helm in system path and custom path
  echo "Start installing Helm with default version ${DEFAULT_HELM_VERSION}"
  /tmp/get-helm-3 --version "${DEFAULT_HELM_VERSION}"
}

installOtherSupportedVersions() {
  if [ -z "$HELM_VERSIONS" ]; then
      echo "No Helm versions specified, skip installing Terraform versions."
      exit 0
  fi

  VERSIONS=$(echo "$HELM_VERSIONS" | tr ',' '\n' | tr -d ' ')
  for version in $VERSIONS; do
    export BINARY_NAME=helm-"${version}"
    export HELM_INSTALL_DIR="${HELM_INSTALL_PATH}"
    /tmp/get-helm-3 --version "${version}"
  done
}

HELM_INSTALL_PATH="$1"
DEFAULT_HELM_VERSION="$2"
HELM_VERSIONS="$3"

validateArguments "$@"
setupInstallationDirectory
downloadOfficialHelmInstallerScript
installDefaultHelmVersion
installOtherSupportedVersions

