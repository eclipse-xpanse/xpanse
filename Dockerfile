FROM eclipse-temurin:17

RUN apt-get update && \
    apt-get install unzip wget -y
ENV TERRAFORM_VERSION=1.4.4
RUN wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip
RUN unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip
RUN mv terraform /usr/bin/terraform
ENV OPENAPI_WORKDIR=openapi/
ENV OPENAPI_CLIENT_VERSION=6.5.0
RUN mkdir ${OPENAPI_WORKDIR}
RUN wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${OPENAPI_CLIENT_VERSION}/openapi-generator-cli-${OPENAPI_CLIENT_VERSION}.jar
RUN mv openapi-generator-cli-${OPENAPI_CLIENT_VERSION}.jar ${OPENAPI_WORKDIR}/openapi-generator-cli.jar
COPY runtime/target/xpanse-runtime-*.jar xpanse-runtime.jar
RUN tr -dc 'a-zA-Z0-9' < /dev/urandom | head -c 32 > aes_sec
ENTRYPOINT ["java","-jar","xpanse-runtime.jar"]