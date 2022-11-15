package org.eclipse.osc.services.ocl.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
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

    @JsonIgnore
    public <T> Optional<T> referTo(String jsonPath, Class<T> valueType) {

        if (!jsonPath.startsWith("$.")) {
            log.warn("{} is not a valid JsonPath.", jsonPath);
            return Optional.empty();
        }

        Matcher matcher = Pattern.compile("([A-Za-z_0-9]+(?=[$\\.\\[\\]]{1}))").matcher(jsonPath);

        Object object = this;
        while(matcher.find()) {
            String matchStr = matcher.group();
            if (matchStr.equals("$")) {
                continue;
            }

            try {
                try {
                    Integer index = Integer.parseInt(matchStr);
                    Method getter = object.getClass().getDeclaredMethod("get", int.class);
                    object = getter.invoke(object, index);
                } catch (NumberFormatException e){
                    Method getter = object.getClass().getDeclaredMethod("get" +
                            matchStr.substring(0,1).toUpperCase(Locale.ROOT) + matchStr.substring(1));
                    object = getter.invoke(object);
                }
            } catch (Exception ex) {
                log.warn("Refer failed", ex);
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
