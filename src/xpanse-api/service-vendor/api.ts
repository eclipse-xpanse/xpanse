import axios from 'axios';

export const getServiceVendorData = async () => {
    return axios.get<ServiceVendor.ServiceData>(`/xpanse/service`);
};

export const getCategoryList = async () => {
    return axios.get<ServiceVendor.CategoryList>(`/xpanse/register/categories`);
};

export const getServiceList = async () => {
    return axios.get<ServiceVendor.Services>(`/xpanse/serviceList`);
};

// export const getVersionList = async (serviceName: string, subServiceName: string) => {
//     return axios.get<ServiceVendor.VersionList>(`/xpanse/versionList/${serviceName}/${subServiceName}`);
// };

export const getVersionList = async () => {
    return axios.get<ServiceVendor.VersionList>(`/xpanse/versionList`);
};
