/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.orchestrator.audit;

import jakarta.validation.constraints.NotNull;

/** The interface for the Operational Audit. */
public interface OperationalAudit {

    /** Audit all API requests of the POST, PUT, and DELETE methods. */
    void auditApiRequest(@NotNull AuditLog auditLog);
}
