/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.ServiceConfigurationEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.statemanagement.enums.ServiceState;
import org.eclipse.xpanse.modules.models.servicetemplate.Region;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

/** ServiceDeploymentEntity for persistence. */
@Table(name = "SERVICE_DEPLOYMENT")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceDeploymentEntity extends CreateModifiedTime {

    @Id private UUID id;

    /** The id of user who deployed the service. */
    @Column(name = "USER_ID")
    private String userId;

    /** The category of the Service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false)
    private Category category;

    /** The name of the Service. */
    @Column(name = "NAME")
    private String name;

    /** The version of the Service. */
    @Column(name = "VERSION")
    private String version;

    /** The customer provided name for the service deployment. */
    @Column(name = "CUSTOMER_SERVICE_NAME")
    private String customerServiceName;

    /** ServiceVendor of the user who registered service template. */
    @Column(name = "SERVICE_VENDOR", nullable = false)
    private String serviceVendor;

    /** The csp of the Service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "CSP", nullable = false)
    private Csp csp;

    /** The hosting type of the Service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "SERVICE_HOSTING_TYPE", nullable = false)
    private ServiceHostingType serviceHostingType;

    /** The region of the Service. */
    @Column(name = "REGION", columnDefinition = "json", nullable = false)
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Region region;

    /** The availability zones of the Service. */
    @Column(name = "AVAILABILITY_ZONES", columnDefinition = "json")
    @Type(value = JsonType.class)
    private Map<String, String> availabilityZones;

    /** The flavor of the Service. */
    @Column(name = "FLAVOR", nullable = false)
    private String flavor;

    /** The billing mode of the service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "BILLING_MODE", nullable = false)
    private BillingMode billingMode;

    /** The input properties of the service deployment. */
    @ElementCollection
    @CollectionTable(
            name = "SERVICE_DEPLOYMENT_INPUT",
            joinColumns = @JoinColumn(name = "SERVICE_ID", nullable = false))
    @MapKeyColumn(name = "P_KEY")
    @Column(name = "P_VALUE")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<String, String> inputProperties;

    /** The deployment id of the Service. */
    @Column(name = "IS_EULA_ACCEPTED", nullable = false)
    private Boolean isEulaAccepted;

    /** The deployment state of the Service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "SERVICE_DEPLOYMENT_STATE", nullable = false)
    private ServiceDeploymentState serviceDeploymentState;

    /** The run state of the Service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "SERVICE_STATE")
    private ServiceState serviceState = ServiceState.NOT_RUNNING;

    /** The id of the Service Template. */
    @Column(name = "SERVICE_TEMPLATE_ID", nullable = false)
    private UUID serviceTemplateId;

    @OneToMany(
            mappedBy = "serviceDeploymentEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @ToString.Exclude
    private List<ServiceResourceEntity> deployResources;

    @OneToOne(mappedBy = "serviceDeploymentEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private ServiceConfigurationEntity serviceConfiguration;

    @OneToMany(
            mappedBy = "serviceDeploymentEntity",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    @ToString.Exclude
    private List<ServiceOrderEntity> serviceOrders;

    /** The output properties of the service deployment. */
    @ElementCollection
    @CollectionTable(
            name = "SERVICE_DEPLOYMENT_OUTPUT",
            joinColumns = @JoinColumn(name = "SERVICE_ID", nullable = false))
    @MapKeyColumn(name = "P_KEY")
    @Column(name = "P_VALUE", length = Integer.MAX_VALUE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<String, String> outputProperties;

    /**
     * The deployment generated files of the service deployment. This is not returned to the
     * customer. This can be used by the deployer for storing any internal data.
     */
    @ElementCollection
    @CollectionTable(
            name = "SERVICE_DEPLOYMENT_GENERATED_FILES",
            joinColumns = @JoinColumn(name = "SERVICE_ID", nullable = false))
    @MapKeyColumn(name = "P_KEY")
    @Column(name = "P_VALUE", length = Integer.MAX_VALUE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<String, String> deploymentGeneratedFiles;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @Column(name = "LAST_STARTED_AT")
    private OffsetDateTime lastStartedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss XXX")
    @Column(name = "LAST_STOPPED_AT")
    private OffsetDateTime lastStoppedAt;

    @Column(name = "LOCK_CONFIG", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private ServiceLockConfig lockConfig;
}
