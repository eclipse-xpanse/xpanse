package org.eclipse.osc.modules.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class SwaggerUIUnpkgServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream()))) {
            writer.println("<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"utf-8\" />\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                    "    <meta\n" +
                    "      name=\"description\"\n" +
                    "      content=\"SwaggerUI\"\n" +
                    "    />\n" +
                    "    <title>SwaggerUI</title>\n" +
                    "    <link rel=\"stylesheet\" href=\"https://unpkg.com/swagger-ui-dist@4.5.0/swagger-ui.css\" />\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "  <div id=\"swagger-ui\"></div>\n" +
                    "  <script src=\"https://unpkg.com/swagger-ui-dist@4.5.0/swagger-ui-bundle.js\" crossorigin></script>\n" +
                    "  <script src=\"https://unpkg.com/swagger-ui-dist@4.5.0/swagger-ui-standalone-preset.js\" crossorigin></script>\n" +
                    "  <script>\n" +
                    "    window.onload = () => {\n" +
                    "      window.ui = SwaggerUIBundle({\n" +
                    "        url: '/openapi',\n" +
                    "        dom_id: '#swagger-ui',\n" +
                    "        presets: [\n" +
                    "          SwaggerUIBundle.presets.apis,\n" +
                    "          SwaggerUIStandalonePreset\n" +
                    "        ],\n" +
                    "        layout: \"StandaloneLayout\",\n" +
                    "      });\n" +
                    "    };\n" +
                    "  </script>\n" +
                    "  </body>\n" +
                    "</html>");
        }
    }

}
