/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicetemplaterequest;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eclipse.xpanse.modules.database.common.CreateModifiedTime;
import org.eclipse.xpanse.modules.database.common.ObjectJsonConverter;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;
import org.hibernate.annotations.Type;

/** Represents the SERVICE_TEMPLATE_REQUEST_HISTORY table in the database. */
@Table(name = "SERVICE_TEMPLATE_REQUEST_HISTORY")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceTemplateRequestHistoryEntity extends CreateModifiedTime {

    @Id
    @Column(name = "REQUEST_ID", nullable = false)
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_TEMPLATE_ID", nullable = false)
    @ToString.Exclude
    private ServiceTemplateEntity serviceTemplate;

    @Column(name = "REQUEST_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceTemplateRequestType requestType;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceTemplateRequestStatus requestStatus;

    @Column(name = "SERVICE_TEMPLATE_REQUEST", columnDefinition = "json")
    @Type(value = JsonType.class)
    @Convert(converter = ObjectJsonConverter.class)
    private Ocl ocl;

    @Column(name = "REVIEW_COMMENT", length = Integer.MAX_VALUE)
    private String reviewComment;

    @Column(name = "BLOCK_TEMPLATE_UNTIL_REVIEWED", nullable = false)
    private Boolean blockTemplateUntilReviewed = false;
}
