# GUI for Xpanse App

This is the UI for the Xpanse App which allows cloud service providers to deploy managed services.

## Development Setup

Project is built on `ReactJS` and `TypeScript`. Ensure all objects have type explicity defined.
GUI components are built using `antd` library.

## Starting local development server

In the project directory, you can run the below command to start the local development server. This also additionally needs `nodejs` to be installed on the development machine.

```shell
$ npm run start
```

Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

## Build for production

```shell
$ npm run build
```

Builds the app for production to the `build` folder. Contents can be copied to any webserver to host the frontend files.

## Generate Rest Client for Xpanse API

We use the openapi generator to generate data models and rest client from the openapi json file.
The following steps must be followed to generate new client and datamodels whenever there is a new version if the swagger json.

1. Copy the openapi file to [OpenApi JSON File](src/xpanse-api/api.json)
2. Downland generator jar from maven central. For example from - https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/6.4.0/openapi-generator-cli-6.4.0.jar
3. Run the jar as below
    ```shell
    $cd ui/src/xpanse-gui
    $java -jar openapi-generator-cli-6.3.0.jar generate -i api.json -g typescript -o generated
    ```
    This step will generate all required models and client
4. Delete all additional files and keep only the TypeScript files.
5. Auto generated classes will have some compile errors mainly in the imports. They must be fixed manually. This is because of the open issues in the generator.
