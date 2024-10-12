#!/bin/sh
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Error：TERRAFORM_INSTALL_PATH and DEFAULT_TERRAFORM_VERSION could not be empty."
    exit 1
fi
TERRAFORM_INSTALL_PATH="$1"
DEFAULT_TERRAFORM_VERSION="$2"
TERRAFORM_VERSIONS="$3"
mkdir -p "${TERRAFORM_INSTALL_PATH}"
# Install default version of Terraform in system path and custom path
echo "Start installing Terraform with default version ${DEFAULT_TERRAFORM_VERSION}"
wget -c "https://releases.hashicorp.com/terraform/${DEFAULT_TERRAFORM_VERSION}/terraform_${DEFAULT_TERRAFORM_VERSION}_linux_amd64.zip"
if [ -f "terraform_${DEFAULT_TERRAFORM_VERSION}_linux_amd64.zip" ]; then
    unzip -o "terraform_${DEFAULT_TERRAFORM_VERSION}_linux_amd64.zip"
    cp -f terraform /usr/bin/terraform
    chmod +x /usr/bin/terraform
    mv -f terraform "${TERRAFORM_INSTALL_PATH}/terraform-${DEFAULT_TERRAFORM_VERSION}"
    chmod +x "${TERRAFORM_INSTALL_PATH}/terraform-${DEFAULT_TERRAFORM_VERSION}"
    rm -f "terraform_${DEFAULT_TERRAFORM_VERSION}_linux_amd64.zip"
    echo "Installed Terraform with default version ${DEFAULT_TERRAFORM_VERSION} into path ${TERRAFORM_INSTALL_PATH} successfully."
else
    echo "Failed to download zip package of Terraform with default version terraform_${DEFAULT_TERRAFORM_VERSION}_linux_amd64.zip."
fi
if [ -z "$TERRAFORM_VERSIONS" ]; then
    echo "No Terraform versions specified, skip installing Terraform versions."
    exit 0
fi
# Install versions of Terraform specified in TERRAFORM_VERSIONS into custom path
VERSIONS=$(echo "$TERRAFORM_VERSIONS" | tr ',' '\n' | tr -d ' ')
for version in $VERSIONS; do
    echo "Start installing Terraform with version ${version} into path ${TERRAFORM_INSTALL_PATH}"
    wget -c "https://releases.hashicorp.com/terraform/${version}/terraform_${version}_linux_amd64.zip"
    if [ ! -f "terraform_${version}_linux_amd64.zip" ]; then
        echo "Failed to download zip package of Terraform with version terraform_${version}_linux_amd64.zip."
        continue
    fi
    unzip -o "terraform_${version}_linux_amd64.zip"
    mv -f terraform "${TERRAFORM_INSTALL_PATH}/terraform-${version}"
    chmod +x "${TERRAFORM_INSTALL_PATH}/terraform-${version}"
    rm -f "terraform_${version}_linux_amd64.zip"
    echo "Installed Terraform with version ${version} into path ${TERRAFORM_INSTALL_PATH} successfully."
done
