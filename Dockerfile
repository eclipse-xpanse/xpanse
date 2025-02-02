FROM eclipse-temurin:21-jre-alpine

ENV XPANSE_USER=xpanse
ENV XPANSE_HOME_DIR=/home/xpanse

# Install dependencies
RUN apk update && \
    apk add --no-cache unzip wget openssh-keygen openssl

# Install Terraform with multiple versions
ENV TERRAFORM_INSTALL_PATH=/opt/terraform
ENV DEFAULT_TERRAFORM_VERSION=1.6.0
ENV TERRAFORM_VERSIONS=1.6.0,1.7.0,1.8.0,1.9.0
COPY install_terraform.sh /install_terraform.sh
RUN chmod +x /install_terraform.sh
RUN echo "Downloading and installing Terraform with multiple versions $TERRAFORM_VERSIONS into path $TERRAFORM_INSTALL_PATH"; \
    /install_terraform.sh "$TERRAFORM_INSTALL_PATH" "$DEFAULT_TERRAFORM_VERSION" "$TERRAFORM_VERSIONS"

# Install OpenTofu with multiple versions
ENV OPENTOFU_INSTALL_PATH=/opt/opentofu
ENV DEFAULT_OPENTOFU_VERSION=1.6.0
ENV OPENTOFU_VERSIONS=1.6.0,1.7.0,1.8.0
COPY install_opentofu.sh /install_opentofu.sh
RUN chmod +x /install_opentofu.sh
RUN echo "Downloading and installing OpenTofu with multiple versions $OPENTOFU_VERSIONS into path $OPENTOFU_INSTALL_PATH"; \
    /install_opentofu.sh "$OPENTOFU_INSTALL_PATH" "$DEFAULT_OPENTOFU_VERSION" "$OPENTOFU_VERSIONS"

# Create xpanse user
RUN addgroup -S -g 2000 ${XPANSE_USER} && \
    adduser -S -h ${XPANSE_HOME_DIR} -s /bin/sh -u 2000 -G ${XPANSE_USER} ${XPANSE_USER}
# Set permissions
RUN chown -R ${XPANSE_USER}:${XPANSE_USER} ${TERRAFORM_INSTALL_PATH}
RUN chown -R ${XPANSE_USER}:${XPANSE_USER} ${OPENTOFU_INSTALL_PATH}
# Switch to xpanse user and set working directory
USER ${XPANSE_USER}
WORKDIR ${XPANSE_HOME_DIR}

# Copy jar file
COPY --chown=${XPANSE_USER}:${XPANSE_USER} runtime/target/xpanse-runtime-*.jar xpanse-runtime.jar

# Set start command
ENTRYPOINT ["java", "-Ddeployer.terraform.install.dir=${TERRAFORM_INSTALL_PATH}", "-Ddeployer.terraform.default.supported.versions=${TERRAFORM_VERSIONS}", \
"-Ddeployer.opentofu.install.dir=${OPENTOFU_INSTALL_PATH}", "-Ddeployer.opentofu.default.supported.versions=${OPENTOFU_VERSIONS}", \
 "-jar", "xpanse-runtime.jar"]

