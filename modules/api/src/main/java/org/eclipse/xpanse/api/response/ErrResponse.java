/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;

import lombok.Data;

/**
 * Error response for the REST API.
 */
@Data
public class ErrResponse {

    private String code;
    private String errMsg;

    public ErrResponse(ErrCode errCode, String errMsg) {
        this.code = errCode.getCode();
        this.errMsg = errCode.getErrMsg() + ". -- " + errMsg;
    }

}