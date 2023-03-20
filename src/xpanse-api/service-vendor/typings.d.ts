declare namespace ServiceVendor {
    interface ServiceData {
        data: Service[];
    }

    interface Service {
        content: string;
        name: string;
        versionList: Version[];
    }

    interface Version {
        version: string;
        cloudProviderList: CloudProvider[];
    }

    interface CloudProvider {
        areaList: Area[];
        name: string;
        details: CloudProviderDetails;
    }

    interface Area {
        name: string;
        regionList: Region[];
    }
    interface Region {
        name: string;
    }

    interface CloudProviderDetails {
        product?: string;
        billing?: string;
        time?: string;
        amount?: string;
        discount?: string;
        official?: string;
    }

    interface CategoryList {
        data: [];
    }

    interface Services {
        data: ServiceItem[];
    }
    interface ServiceItem {
        content: string;
        name: string;
    }

    interface VersionList {
        data: Version[];
    }
}
