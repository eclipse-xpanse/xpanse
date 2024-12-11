/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicetemplaterequest.ServiceTemplateRequestHistoryEntity;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceProviderContactDetails;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.JsonObjectSchema;
import org.hibernate.annotations.Type;

/**
 * Represents the SERVICE_TEMPLATE table in the database.
 */
@Table(name = "SERVICE_TEMPLATE", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "NAME", "VERSION", "CSP", "CATEGORY", "SERVICE_HOSTING_TYPE"})
})
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceTemplateEntity extends CreateModifiedTime {

    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "VERSION", nullable = false)
    private String version;

    @Column(name = "CSP", nullable = false)
    @Enumerated(EnumType.STRING)
    private Csp csp;

    @Column(name = "CATEGORY")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "NAMESPACE")
    private String namespace;

    @Column(name = "SERVICE_HOSTING_TYPE")
    @Enumerated(EnumType.STRING)
    private ServiceHostingType serviceHostingType;

    @Column(name = "OCL", columnDefinition = "json", nullable = false)
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Ocl ocl;

    @Column(name = "SERVICE_TEMPLATE_REGISTRATION_STATE")
    @Enumerated(EnumType.STRING)
    private ServiceTemplateRegistrationState serviceTemplateRegistrationState;

    @Column(name = "IS_UPDATE_PENDING", nullable = false)
    private Boolean isUpdatePending;

    @Column(name = "AVAILABLE_IN_CATALOG", nullable = false)
    private Boolean availableInCatalog;

    @Column(name = "SERVICE_PROVIDER_CONTACT_DETAILS", columnDefinition = "json", nullable = false)
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private ServiceProviderContactDetails serviceProviderContactDetails;

    @Column(name = "SERVICE_VARIABLES_JSON_SCHEMA", columnDefinition = "json", nullable = false)
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private JsonObjectSchema jsonObjectSchema;

    @OneToMany(mappedBy = "serviceTemplate", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude
    private List<ServicePolicyEntity> servicePolicyList;

    @OneToMany(mappedBy = "serviceTemplate", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @ToString.Exclude
    private List<ServiceTemplateRequestHistoryEntity> serviceTemplateHistory;

}
