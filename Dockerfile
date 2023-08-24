FROM eclipse-temurin:17-jre-alpine

ENV TERRAFORM_VERSION=1.4.4
ENV XPANSE_USER=xpanse
ENV XPANSE_HOME_DIR=/home/xpanse

# Install dependencies
RUN apk update && \
    apk add --no-cache unzip wget openssh-keygen openssl

# Install Terraform
RUN wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip
RUN unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip
RUN mv terraform /usr/bin/terraform

# Copy the script for starting xpanse with private key
COPY start-xpanse-with-private_key.sh /start-xpanse-with-private_key.sh
RUN chmod +x /start-xpanse-with-private_key.sh

# Create xpanse user
RUN addgroup -S -g 2000 ${XPANSE_USER} && \
    adduser -S -h ${XPANSE_HOME_DIR} -s /bin/sh -u 2000 -G ${XPANSE_USER} ${XPANSE_USER}
USER ${XPANSE_USER}
WORKDIR ${XPANSE_HOME_DIR}

# Copy jar file
COPY --chown=${XPANSE_USER}:${XPANSE_USER} runtime/target/xpanse-runtime-*.jar xpanse-runtime.jar

# Set the entrypoint script to start xpanse with private key
ENTRYPOINT ["/start-xpanse-with-private_key.sh"]
