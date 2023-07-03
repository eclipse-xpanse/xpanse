/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of Response.
 */
class ResponseTest {

    private static final ResultType successResultType = ResultType.SUCCESS;
    private static final List<String> successMsg = List.of("success");
    private static final ResultType errorResultType = ResultType.RUNTIME_ERROR;
    private static final List<String> errorMsg = List.of("error");
    private static Response successResponse;
    private static Response errorResponse;

    @BeforeEach
    void setUp() {
        successResponse = new Response();
        successResponse.setResultType(successResultType);
        successResponse.setDetails(successMsg);
        successResponse.setSuccess(true);

        errorResponse = new Response();
        errorResponse.setResultType(errorResultType);
        errorResponse.setDetails(errorMsg);
        errorResponse.setSuccess(false);
    }

    @Test
    void testConstructor() {
        Response errorResponse = Response.errorResponse(errorResultType, errorMsg);
        Response successResponse = Response.successResponse(successMsg);

        assertEquals(errorResultType, errorResponse.getResultType());
        assertEquals(errorMsg, errorResponse.getDetails());
        assertEquals(false, errorResponse.getSuccess());

        assertEquals(ResultType.SUCCESS, successResponse.getResultType());
        assertEquals(successMsg, successResponse.getDetails());
        assertEquals(true, successResponse.getSuccess());
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(successResultType, successResponse.getResultType());
        assertEquals(successMsg, successResponse.getDetails());
        assertEquals(true, successResponse.getSuccess());

        assertEquals(errorResultType, errorResponse.getResultType());
        assertEquals(errorMsg, errorResponse.getDetails());
        assertEquals(false, errorResponse.getSuccess());
    }

    @Test
    void testSuccessResponseEqualsAndHashCode() {
        assertEquals(successResponse, successResponse);
        assertEquals(successResponse.hashCode(), successResponse.hashCode());

        Object obj = new Object();
        assertNotEquals(successResponse, obj);
        assertNotEquals(successResponse, null);
        assertNotEquals(successResponse.hashCode(), obj.hashCode());

        Response response1 = new Response();
        Response response2 = new Response();
        assertNotEquals(successResponse, response1);
        assertNotEquals(successResponse, response2);
        assertEquals(response1, response2);
        assertNotEquals(successResponse.hashCode(), response1.hashCode());
        assertNotEquals(successResponse.hashCode(), response2.hashCode());
        assertEquals(response1.hashCode(), response2.hashCode());

        response1.setResultType(successResultType);
        assertNotEquals(successResponse, response1);
        assertNotEquals(response1, response2);
        assertNotEquals(successResponse.hashCode(), response1.hashCode());
        assertNotEquals(response1.hashCode(), response2.hashCode());

        response1.setDetails(successMsg);
        assertNotEquals(successResponse, response1);
        assertNotEquals(response1, response2);
        assertNotEquals(successResponse.hashCode(), response1.hashCode());
        assertNotEquals(response1.hashCode(), response2.hashCode());

        response1.setSuccess(true);
        assertEquals(successResponse, response1);
        assertNotEquals(response1, response2);
        assertEquals(successResponse.hashCode(), response1.hashCode());
        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testErrorResponseEqualsAndHashCode() {
        assertEquals(errorResponse, errorResponse);
        assertEquals(errorResponse.hashCode(), errorResponse.hashCode());

        Object obj = new Object();
        assertNotEquals(errorResponse, obj);
        assertNotEquals(errorResponse, null);
        assertNotEquals(errorResponse.hashCode(), obj.hashCode());

        Response response1 = new Response();
        Response response2 = new Response();
        assertNotEquals(errorResponse, response1);
        assertNotEquals(errorResponse, response2);
        assertEquals(response1, response2);
        assertNotEquals(errorResponse.hashCode(), response1.hashCode());
        assertNotEquals(errorResponse.hashCode(), response2.hashCode());
        assertEquals(response1.hashCode(), response2.hashCode());

        response1.setResultType(errorResultType);
        assertNotEquals(errorResponse, response1);
        assertNotEquals(response1, response2);
        assertNotEquals(errorResponse.hashCode(), response1.hashCode());
        assertNotEquals(response1.hashCode(), response2.hashCode());

        response1.setDetails(errorMsg);
        assertNotEquals(errorResponse, response1);
        assertNotEquals(response1, response2);
        assertNotEquals(errorResponse.hashCode(), response1.hashCode());
        assertNotEquals(response1.hashCode(), response2.hashCode());

        response1.setSuccess(false);
        assertEquals(errorResponse, response1);
        assertNotEquals(response1, response2);
        assertEquals(errorResponse.hashCode(), response1.hashCode());
        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        String expectedSuccessToString = "Response(" +
                "resultType=" + successResultType + ", " +
                "details=" + successMsg + ", " +
                "success=" + true + ")";
        String expectedErrorToString = "Response(" +
                "resultType=" + errorResultType + ", " +
                "details=" + errorMsg + ", " +
                "success=" + false + ")";
        assertEquals(expectedSuccessToString, successResponse.toString());
        assertEquals(expectedErrorToString, errorResponse.toString());
    }

}
