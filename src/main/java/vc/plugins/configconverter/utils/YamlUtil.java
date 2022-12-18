package vc.plugins.configconverter.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author xiaolei.fu
 * @version 1.0.0
 * @since 1.0.0
 */
public final class YamlUtil {

    private static final Pattern CHILD_KEY = Pattern.compile("^.+\\[[0-9]+\\]$");

    public static String propertiesToYaml(Properties properties) {
        Map<String, Object> root = new HashMap<>(properties.size());
        toHierarchyMap(root, properties);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setMaxSimpleKeyLength(100);
        dumperOptions.setIndent(10);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(dumperOptions);
        return yaml.dump(root);
    }

    private static void toHierarchyMap(Map<String, Object> root, Map<Object, Object> properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            injectToMap(root, key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static void injectToMap(Map<String, Object> root, String key, String value) {
        String[] split = key.split("\\.");

        Map<String, Object> current = root;
        int end = split.length - 1;

        for (int i = 0; i < end; i++) {
            String childKey = split[i];
            if (CHILD_KEY.matcher(childKey).matches()) {
                int startIndex = childKey.lastIndexOf("[");
                String ck = childKey.substring(0, startIndex);
                int num = Integer.parseInt(childKey.substring(startIndex + 1, childKey.length() - 1));

                List<Object> l = (List<Object>) current.computeIfAbsent(ck, k -> new ArrayList<>());

                if (l.size() < num + 1) {
                    for (int j = 0; j < num + 2 - l.size(); j++) {
                        l.add(null);
                    }

                    current = new HashMap<>(4);
                    l.set(num, current);
                } else if (l.get(num) == null) {
                    current = new HashMap<>(4);
                    l.set(num, current);
                } else {
                    current = (Map<String, Object>) l.get(num);
                }
            } else {
                current = ((Map<String, Object>) current.computeIfAbsent(childKey, k -> new HashMap<>()));
            }
        }

        String lastChildKey = split[end];

        if (CHILD_KEY.matcher(lastChildKey).matches()) {
            int startIndex = lastChildKey.lastIndexOf("[");
            String ck = lastChildKey.substring(0, startIndex);
            int num = Integer.parseInt(lastChildKey.substring(startIndex + 1, lastChildKey.length() - 1));

            List<Object> l = (List<Object>) current.computeIfAbsent(ck, k -> new ArrayList<>());
            l.set(num, value);
        } else {
            Object lastValue = current.get(lastChildKey);
            if (lastValue == null) {
                current.put(lastChildKey, value);
            } else if (lastValue instanceof Map) {
                throw new IllegalArgumentException(String.format("%s can't set to %s", key, value));
            } else {
                current.put(lastChildKey, value);
            }
        }
    }
}
