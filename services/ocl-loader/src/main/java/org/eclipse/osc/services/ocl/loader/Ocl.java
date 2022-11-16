package org.eclipse.osc.services.ocl.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
public class Ocl {

    private String name;
    private String category;
    private String namespace;
    private Map<String, Object> properties;
    private Image image;

    private Billing billing;
    private Compute compute;
    private Network network;
    private List<Storage> storage;
    private Console console;

    private static ObjectMapper theMapper = new ObjectMapper();

    /**
     * an OCL object might be passed to different plugins for processing,
     * in case any plugin want to change the property of Ocl, we should not change
     * the original
     * Object, we should change a deep copy. The method is for deep copy
     * 
     * @return
     */
    public Ocl deepCopy() {
        try {
            StringWriter out = new StringWriter();
            theMapper.writeValue(out, this);
            return theMapper.readValue(new StringReader(out.toString()), Ocl.class);
        } catch (IOException ex) {
            // Should not happen , since we don't actually touch any real I/O device
            throw new IllegalStateException("Deep copy failed", ex);

        }
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> Optional<T> referTo(String jsonPath, Class<T> valueType) {

        if (!jsonPath.startsWith("$.")) {
            log.warn(jsonPath + "is not a valid JsonPath.");
            return Optional.empty();
        }

        Matcher matcher = Pattern.compile("([A-Za-z_0-9]+(?=[$\\.\\[\\]]{1}))").matcher(jsonPath);

        Object object = this;
        while (matcher.find()) {
            String matchStr = matcher.group();
            if (matchStr.equals("$")) {
                continue;
            }

            try {
                try {
                    Integer index = Integer.parseInt(matchStr);
                    Method getter = object.getClass().getDeclaredMethod("get", int.class);
                    object = getter.invoke(object, index);
                } catch (NumberFormatException e) {
                    Method getter = object.getClass().getDeclaredMethod("get" +
                            matchStr.substring(0, 1).toUpperCase(Locale.ROOT) + matchStr.substring(1));
                    object = getter.invoke(object);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage() + "\nStack Info:\n", ex);
                return Optional.empty();
            }
        }

        if (object.getClass() == valueType) {
            return Optional.ofNullable((T) object);
        } else {
            log.warn("Not the same type. Please check your JsonPath.");
            return Optional.empty();
        }
    }

}
