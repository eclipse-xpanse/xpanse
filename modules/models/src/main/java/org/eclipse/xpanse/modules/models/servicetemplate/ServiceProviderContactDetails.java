/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

/** The contact details of the Cloud Service Provider. */
@Data
public class ServiceProviderContactDetails {

    @Valid
    @Size(min = 1)
    @UniqueElements
    @Schema(
            description =
                    "List of the email details of the service provider. "
                            + "The list elements must be unique.")
    private List<String> emails;

    @Valid
    @Size(min = 1)
    @UniqueElements
    @Schema(
            description =
                    "List of the phone details of the service provider. "
                            + "The list elements must be unique.")
    private List<String> phones;

    @Valid
    @Size(min = 1)
    @UniqueElements
    @Schema(
            description =
                    "List of the chat details of the service provider. "
                            + "The list elements must be unique.")
    private List<String> chats;

    @Valid
    @Size(min = 1)
    @UniqueElements
    @Schema(
            description =
                    "List of the website details of the service provider. "
                            + "The list elements must be unique.")
    private List<String> websites;
}
