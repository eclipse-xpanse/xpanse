/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

/**
 * ServiceConfigurationChangeDetailsEntity for persistence.
 */
@Table(name = "SERVICE_CONFIGURATION_CHANGE_DETAILS")
@Entity
@Data
public class ServiceConfigurationChangeDetailsEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8759112725757851274L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID", nullable = false,
            foreignKey = @ForeignKey(name = "fk_service_configuration_deploy_service",
                    foreignKeyDefinition = "FOREIGN KEY (ORDER_ID) "
                            + "REFERENCES SERVICE_ORDER(ORDERID) ON DELETE CASCADE"))
    @Cascade(CascadeType.ALL)
    private ServiceOrderEntity serviceOrderEntity;

    @ManyToOne
    @JoinColumn(name = "SERVICE_ID", nullable = false,
            foreignKey = @ForeignKey(name = "fk_service_configuration_deploy_service",
            foreignKeyDefinition = "FOREIGN KEY (SERVICE_ID) "
                    + "REFERENCES DEPLOY_SERVICE(ID) ON DELETE CASCADE"))
    @Cascade(CascadeType.ALL)
    private DeployServiceEntity deployServiceEntity;

    private String resourceName;

    private String configManager;

    private String resultMessage;

    @Column(name = "PROPERTIES", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Map<String, Object> properties;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private ServiceConfigurationStatus status;

}
