package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.apache.commons.validator.routines.InetAddressValidator;

public class IPTool {

    private final static String[] ipCheckerUrl = new String[]{
        "http://ipecho.net/plain",
        "http://www.trackip.net/ip",
        "http://myexternalip.com/raw",
        "https://ipv4.icanhazip.com/",
        "http://checkip.amazonaws.com"};

    private String getExternalIp(String url) {
        try {
            try (BufferedReader in =
                new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
                String myIp = in.readLine();
                if (myIp != null && InetAddressValidator.getInstance().isValid(myIp)) {
                    return myIp;
                }
                return null;
            }

        } catch (IOException ex) {
            return null;
        }
    }

    public String probeExternalIp() {
        String myIp;
        for (var url : ipCheckerUrl) {
            myIp = getExternalIp(url);
            if (myIp != null) {
                return myIp;
            }
        }
        return null;
    }
}
