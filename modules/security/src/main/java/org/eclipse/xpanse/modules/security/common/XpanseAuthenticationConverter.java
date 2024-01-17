/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Interface to later create bean to convert Jwt object to XpanseAuthentication.
 */
public interface XpanseAuthenticationConverter extends Converter<Jwt, XpanseAuthentication> {
}