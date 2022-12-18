package vc.plugins.configconverter;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author xiaolei.fu
 * @version 1.0.0
 * @since 1.0.0
 */
public class YamlReadTest {

    @Test
    public void testRead() throws IOException {
//        yamlToProperties();

        propertiesToYaml();
    }

    private void propertiesToYaml() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(getClass().getResourceAsStream("/prop.properties"));

        Properties properties = new Properties();
        properties.load(inputStreamReader);

        Map<String, Object> root = new HashMap<>(properties.size());
        toHierarchyMap(root, properties);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setMaxSimpleKeyLength(100);
        dumperOptions.setIndent(10);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(dumperOptions);
        String dump = yaml.dump(root);
        System.out.println(dump);
    }

    private void toHierarchyMap(Map<String, Object> root, Map<Object, Object> properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            injectToMap(root, key, value);
        }
    }

    private static final Pattern CHILD_KEY = Pattern.compile("^.+\\[[0-9]+\\]$");

    @SuppressWarnings("unchecked")
    private void injectToMap(Map<String, Object> root, String key, String value) {
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

                    current = new HashMap<>();
                    l.set(num, current);
                } else if (l.get(num) == null) {
                    current = new HashMap<>();
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


    private void yamlToProperties() {
        InputStreamReader inputStreamReader = new InputStreamReader(getClass().getResourceAsStream("/yaml.yml"));

        Yaml yaml = new Yaml();
        Iterable<Object> objects = yaml.loadAll(inputStreamReader);

        Map<String, Object> map;
        for (Object object : objects) {
            if (object != null) {
                map = asMap(object);
                map = getFlattenedMap(map);
                System.out.println(map);
            }
        }
    }


    private static Map<String, Object> asMap(Object object) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            Object key = entry.getKey();
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                result.put("[" + key.toString() + "]", value);
            }
        }
        return result;
    }

    private static Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (!StringUtils.isBlank(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
                }
            } else {
                result.put(key, (value != null ? value.toString() : ""));
            }
        }
    }
}
