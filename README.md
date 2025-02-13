![Xpanse logo](static/full-logo.png)
<p align='center'>
<a href="https://github.com/eclipse-xpanse/xpanse/actions/workflows/ci.yml" target="_blank">
	<img src="https://github.com/eclipse-xpanse/xpanse/actions/workflows/ci.yml/badge.svg" alt="build">
</a>

<a href="https://github.com/eclipse-xpanse/xpanse/actions/workflows/coverage.yml" target="_blank">
	<img src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/eclipse-xpanse-bot/3d9c022b98734fbf615c21136abe4add/raw/xpanse-coverage.json" alt="coverage">
</a>

<a href="https://opensource.org/licenses/Apache-2.0" target="_blank">
	<img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="coverage">
</a>
</p>

Xpanse is an Open Source project allowing to easily implement native-managed service on any cloud service provider. This
project is part of the Open Services Cloud (OSC) charter.

Xpanse unleashes your cloud services by removing vendor lock-in and lock out. It standardizes and exposes cloud service
providers core services, meaning that your xpanse service is portable (multi-cloud) on any cloud topology and provider.
It also avoids tight coupling of your service to other cloud service provider services.

## Configuration Language

Details can be found on the project
website [here](https://eclipse.dev/xpanse/docs/configuration-language).

## Runtime

Details can be found on the project website [here](https://eclipse.dev/xpanse/docs/runtime).

## Database

Details can be found on the project website [here](https://eclipse.dev/xpanse/docs/database).

## Generate terra-boot client code

1. Run the terra-boot project with spring-profile `oauth` and `dev` with methods mentioned here.
This is necessary even if the terra-boot will be actually used without oauth enabled in production.
This will make the client to handle both with and without authentication usecases automatically.
2. Access http://localhost:9090/v3/api-docs to get the openapi json.
3. Copy all the JSON content of the openapi json and replace all the content in the JSON file
[terra-boot-openapi.json](modules/deployment/src/main/resources/terra-boot-openapi.json).
4. Run the below maven command to generate the REST API client and data models for terra-boot. The command can be
executed directly inside the `deployment` module.

```ssh
mvn clean generate-sources -DskipTerraBootClientGeneration=false
```

## Generate tofu-maker client code

1. Run the tofu-maker project with spring-profile `oauth` and `dev` with methods mentioned here.
This is necessary even if the tofu-maker will be actually used without oauth enabled in production.
This will make the client to handle both with and without authentication usecases automatically.
2. Access http://localhost:9092/v3/api-docs to get the openapi json.
3. Copy all the JSON content of the openapi json and replace all the content in the JSON file
[tofu-maker-openapi.json](modules/deployment/src/main/resources/tofu-maker-openapi.json).
4. Run the below maven command to generate the REST API client and data models for tofu-maker. The
command can be executed directly inside the `deployment` module.

```ssh
mvn clean generate-sources -DskipTofuMakerClientGeneration=false
```

## Generate policy-man client code

1. Run the policy-man project and access “http://localhost:8090/swagger/doc.json” to get the openapi json.
2. Copy all the JSON content of the openapi json and replace all the content in the JSON file
[policy-man-openapi.json](modules/policy/src/main/resources/policy-man-openapi.json)
3. Run the below maven command to generate the REST API client and data models for policy-man. The command can be
executed directly inside the `policy` module.

```ssh
mvn clean generate-sources -DskipPolicyManClientGeneration=false
```

## Static Code Analysis using CheckStyle

This project using `CheckStyle` framework to perform static code analysis. The configuration can be found
in [CheckStyle](checkstyle.xml). The framework also checks the code format in accordance to `Google Java Format`.

The same file can also be imported in IDE CheckStyle plugins to get the analysis results directly in IDE and also to
perform code formatting directly in IDE.

The framework is added as a maven plugin and is executed by default as part of the `verify` phase. Any violations will
result in build failure.

## License/Copyright Configuration

All files in the repository must contain a license header in the format mentioned in [License Header](license.header).

The static code analysis framework will also validate if the license exists in the specified format.

## Dependencies File

All third-party related content is listed in the [DEPENDENCIES](DEPENDENCIES) file.

## Code Formatter

The project follows [google-code-format](https://github.com/google/google-java-format).
We use the [spotless plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven#google-java-format) to format code and to validate code format.
We can automatically format the code using the command below.

```shell
mvn spotless:apply
```

To validate errors we can run the command below.

```shell
mvn spotless:check &&  mvn checkstyle:check
```
