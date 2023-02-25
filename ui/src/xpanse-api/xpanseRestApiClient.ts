import { OrchestratorApiApi, ServerConfiguration, createConfiguration } from './generated';
import { ConfigurationParameters } from './generated/configuration';

const customConfiguration: ConfigurationParameters = {};
customConfiguration.baseServer = new ServerConfiguration<{}>(process.env.REACT_APP_XPANSE_API_URL as string, {});

const configuration = createConfiguration(customConfiguration);

export const apiInstance = new OrchestratorApiApi(configuration);
