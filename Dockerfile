FROM eclipse-temurin:17

ENV TERRAFORM_VERSION=1.4.4
ENV XPANSE_USER=xpanse
ENV XPANSE_HOME_DIR=/home/xpanse

# Install dependencies
RUN apt-get update && \
    apt-get install unzip wget -y

# Install Terraform
RUN wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip
RUN unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip
RUN mv terraform /usr/bin/terraform

# Create xpanse user
RUN groupadd -r -g 2000 ${XPANSE_USER} && useradd -m -d ${XPANSE_HOME_DIR} -s /bin/bash -u 2000 -r -g ${XPANSE_USER} ${XPANSE_USER}
USER ${XPANSE_USER}
WORKDIR ${XPANSE_HOME_DIR}

# Copy jar file
COPY --chown=${XPANSE_USER}:${XPANSE_USER} runtime/target/xpanse-runtime-*.jar xpanse-runtime.jar

# Generate random key
RUN tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 32 > aes_sec

ENTRYPOINT ["java","-jar","xpanse-runtime.jar"]