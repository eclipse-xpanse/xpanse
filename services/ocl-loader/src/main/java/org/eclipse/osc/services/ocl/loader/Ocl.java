package org.eclipse.osc.services.ocl.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            System.out.println("Not a valid JsonPath.");
            throw new IllegalAccessException(jsonPath + "is not a valid JsonPath.");
        }

        Matcher jsonPathMatter = Pattern.compile("([A-Za-z_0-9]+(?=[$\\.\\[\\]]{1}))").matcher(jsonPath);

        List<String> matches = new ArrayList<String>();
        while(jsonPathMatter.find()) {
            matches.add(jsonPathMatter.group());
        }

        Object node = this;
        for (String path: matches) {
            if (path.equals("$")) {
                continue;
            }

            try {
                Integer index = Integer.parseInt(path);
                Method getter = node.getClass().getDeclaredMethod("get", int.class);
                node = getter.invoke(node, index);
            } catch (NumberFormatException e){
                Method getter = node.getClass().getDeclaredMethod(
                        "get" + path.substring(0,1).toUpperCase(Locale.ROOT) + path.substring(1));
                node = getter.invoke(node);
            }
        }

        if (node.getClass() == valueType) {
            return (T) node;
        } else {
            throw new IllegalAccessException("Not the same type. Please check your JsonPath.");
        }
    }

}
