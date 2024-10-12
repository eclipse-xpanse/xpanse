#!/bin/sh
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Error：OPENTOFU_INSTALL_PATH and DEFAULT_OPENTOFU_VERSION could not be empty."
    exit 1
fi
OPENTOFU_INSTALL_PATH="$1"
DEFAULT_OPENTOFU_VERSION="$2"
OPENTOFU_VERSIONS="$3"

mkdir -p "${OPENTOFU_INSTALL_PATH}"
# install default version of OpenTofu into system path and custom path
echo "Start installing OpenTofu with default version ${DEFAULT_OPENTOFU_VERSION}";
wget -c "https://github.com/opentofu/opentofu/releases/download/v${DEFAULT_OPENTOFU_VERSION}/tofu_${DEFAULT_OPENTOFU_VERSION}_linux_amd64.zip";
if [ -f "tofu_${DEFAULT_OPENTOFU_VERSION}_linux_amd64.zip" ]; then
    unzip -o "tofu_${DEFAULT_OPENTOFU_VERSION}_linux_amd64.zip";
    cp -f tofu /usr/bin/tofu;
    chmod +x /usr/bin/tofu;
    mv -f tofu "${OPENTOFU_INSTALL_PATH}/tofu-${DEFAULT_OPENTOFU_VERSION}";
    chmod +x "${OPENTOFU_INSTALL_PATH}/tofu-${DEFAULT_OPENTOFU_VERSION}";
    rm "tofu_${DEFAULT_OPENTOFU_VERSION}_linux_amd64.zip";
    echo "Installed OpenTofu with default version ${DEFAULT_OPENTOFU_VERSION} into path ${OPENTOFU_INSTALL_PATH} successfully."
else
    echo "Failed to download zip package of OpenTofu with default version tofu_${DEFAULT_OPENTOFU_VERSION}_linux_amd64.zip"
fi
if [ -z "$OPENTOFU_VERSIONS" ]; then
    echo "No OpenTofu versions specified, skip installing OpenTofu versions."
    exit 0
fi
# Install versions of OpenTofu specified in OPENTOFU_VERSIONS into custom path
VERSIONS=$(echo "$OPENTOFU_VERSIONS" | tr ',' '\n' | tr -d ' ')
for version in $VERSIONS; do
    echo "Start installing OpenTofu with version ${version} into path ${OPENTOFU_INSTALL_PATH}";
    wget -c "https://github.com/opentofu/opentofu/releases/download/v${version}/tofu_${version}_linux_amd64.zip";
    if [ ! -f "tofu_${version}_linux_amd64.zip" ]; then
        echo "Failed to download zip package of OpenTofu with version tofu_${version}_linux_amd64.zip"
        continue
    fi
    unzip -o "tofu_${version}_linux_amd64.zip";
    mv -f tofu "${OPENTOFU_INSTALL_PATH}/tofu-${version}";
    chmod +x "${OPENTOFU_INSTALL_PATH}/tofu-${version}";
    rm -rf "tofu_${version}_linux_amd64.zip";
    echo "Installed OpenTofu with version ${version} into path ${OPENTOFU_INSTALL_PATH} successfully."
done