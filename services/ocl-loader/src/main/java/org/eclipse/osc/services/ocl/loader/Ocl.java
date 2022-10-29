package org.eclipse.osc.services.ocl.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.java.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
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
    public <T> T referTo(String jsonPath, Class<T> valueType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (!jsonPath.startsWith("$.")) {
            log.log(Level.WARNING, jsonPath + "is not a valid JsonPath.");
            throw new IllegalAccessException(jsonPath + "is not a valid JsonPath.");
        }

        Matcher matcher = Pattern.compile("([A-Za-z_0-9]+(?=[$\\.\\[\\]]{1}))").matcher(jsonPath);

        Object object = this;
        while(matcher.find()) {
            String matchStr = matcher.group();
            if (matchStr.equals("$")) {
                continue;
            }

            try {
                Integer index = Integer.parseInt(matchStr);
                Method getter = object.getClass().getDeclaredMethod("get", int.class);
                object = getter.invoke(object, index);
            } catch (NumberFormatException e){
                Method getter = object.getClass().getDeclaredMethod("get" +
                        matchStr.substring(0,1).toUpperCase(Locale.ROOT) + matchStr.substring(1));
                object = getter.invoke(object);
            }
        }

        if (object.getClass() == valueType) {
            return (T) object;
        } else {
            log.log(Level.WARNING, "Not the same type. Please check your JsonPath.");
            throw new IllegalAccessException("Not the same type. Please check your JsonPath.");
        }
    }

}
