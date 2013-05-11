package org.n3r.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.*;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 生成标准JSON、KEY不带引号的JSON、KEY和VALUE都不带引号的JSON字符串。
 * 同时，提供KEY映射/VALUE映射，以压缩JSON字符串。
 */
public class EjsonEncoder {
    private String sep = "^";
    private Set<SerializerFeature> features = new HashSet<SerializerFeature>();

    {
        features.add(SerializerFeature.SortField);
        features.add(SerializerFeature.QuoteFieldNames);
        features.add(SerializerFeature.WriteMapNullValue);
        features.add(SerializerFeature.WriteNullStringAsEmpty);
    }

    private NameFilter nameFilter;
    private ValueFilter firstValueFilter;
    private Multiset<String> valueBag;  // 存放相同name-value的数量
    private ValueFilter lastValueFilter;
    private boolean compact;

    private static int getBytesLen(String strValue) {
        try {
            return strValue.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动裸奔模式（不带双引号)
     *
     * @return
     */
    public EjsonEncoder bare() {
        features.add(SerializerFeature.UnquoteFieldValues);
        features.remove(SerializerFeature.QuoteFieldNames);
        return this;
    }

    /**
     * 编码为JSON字符串。
     *
     * @param object
     * @return
     */
    public String encode(Object object) {
        Object target = object;
        String json = toJSONString(target);

        if (!compact) return json;

        JSON jsonObject = new EjsonDecoder().unbare().decode(json);
        return new EjsonEncoder().features(features).encode(compact(jsonObject));
    }

    private EjsonEncoder features(Set<SerializerFeature> features) {
        this.features = features;
        return this;
    }

    private String toJSONString(Object target) {
        SerializeConfig mapping = new SerializeConfig();
        SerializerFeature[] serializerFeatures = features.toArray(new SerializerFeature[0]);
        SerializeWriter out = new SerializeWriter(serializerFeatures);

        String jsonString = null;
        try {
            JSONSerializer serializer = new JSONSerializer(out, mapping);
            if (nameFilter != null) serializer.getNameFilters().add(nameFilter);
            if (firstValueFilter != null) serializer.getValueFilters().add(firstValueFilter);
            serializer.write(target);

            jsonString = out.toString();
        } finally {
            out.close();
        }

        if (lastValueFilter == null) return jsonString;

        out = new SerializeWriter(serializerFeatures);
        try {
            JSONSerializer serializer = new JSONSerializer(out, mapping);
            if (nameFilter != null) serializer.getNameFilters().add(nameFilter);
            serializer.getValueFilters().add(lastValueFilter);
            serializer.write(target);

            return out.toString();
        } finally {
            out.close();
        }
    }

    /**
     * 启动键映射瘦身。
     *
     * @param keyMapping
     * @return
     */
    public EjsonEncoder mapKeys(final Map<String, String> keyMapping) {
        nameFilter = new NameFilter() {

            public String process(Object source, String name, Object value) {
                String mappedName = keyMapping.get(name);
                if (mappedName != null) return mappedName;

                mappedName = Base62.toBase62(keyMapping.size());
                keyMapping.put(name, mappedName);

                return mappedName;
            }
        };

        return this;
    }

    /**
     * 启用值映射瘦身。
     *
     * @param valueMapping
     * @return
     */
    public EjsonEncoder mapValue(final Map<String, String> valueMapping) {
        valueBag = HashMultiset.create();
        // 第一遍过滤器，如果启动了值映射分析，则还需要第二个过滤器，在第一次过滤器中进行值映射分析。
        firstValueFilter = new ValueFilter() {
            public Object process(Object source, String name, Object value) {
                if (!(value instanceof String)) return value;

                String strValue = (String) value;
                if (getBytesLen(strValue) <= 3) return value;

                String mappedValue = valueMapping.get(name + sep + strValue);
                if (mappedValue != null) return mappedValue;

                valueBag.add(name + sep + value);

                return value;
            }

        };

        lastValueFilter = new

                ValueFilter() {
                    public Object process(Object source, String name, Object value) {
                        if (!(value instanceof String)) return value;

                        String strValue = (String) value;
                        if (getBytesLen(strValue) <= 3) return value;

                        final String key = name + sep + strValue;
                        String mappedValue = valueMapping.get(key);
                        if (mappedValue != null) return '@' + mappedValue;

                        if (valueBag.count(key) >= 3) {
                            mappedValue = Base62.toBase62(valueMapping.size());
                            valueMapping.put(key, mappedValue);
                            return '@' + mappedValue;
                        }

                        return value;
                    }
                };
        return this;
    }

    /**
     * 启用浓缩模式。
     * 比如[{c:d1,d:1},{c:d2,d:2},{c:d3,d:3}]
     * 浓缩{_h:[c,d],_d:[d1,1,d2,2,d3,3]}
     *
     * @return
     */
    public EjsonEncoder compact() {
        this.compact = true;
        return this;
    }

    private JSON compact(Object object) {
        if (object instanceof JSONObject) {
            return compactConvert((JSONObject) object);
        } else if (object instanceof JSONArray) {
            return compactConvert((JSONArray) object);
        }

        String json = new EjsonEncoder().encode(object);
        JSON decode = new EjsonDecoder().decode(json);
        return compact(decode);
    }

    private JSONObject compactConvert(JSONObject jsonObject) {
        JSONObject compressJsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof JSONArray) {
                compressJsonObject.put(entry.getKey(), compactConvert((JSONArray) value));
            } else if (value instanceof JSONObject) {
                compressJsonObject.put(entry.getKey(), compactConvert((JSONObject) value));
            } else {
                compressJsonObject.put(entry.getKey(), value);
            }
        }
        return compressJsonObject;
    }

    private JSON compactConvert(JSONArray value) {
        if (value == null || value.size() == 0) return value;

        JSONObject compressedObject = new JSONObject();
        JSONArray header = null;
        JSONArray data = null;
        int lineNo = 0;
        int arraySize = value.size();

        for (Object item : value) {
            ++lineNo;
            if (!(item instanceof JSONObject)) return value; // 不转换

            JSONObject objItem = (JSONObject) item;
            if (lineNo == 1) {
                header = new JSONArray();
                if (arraySize == 1) {
                    header.add(compact(objItem));
                    return header;
                }

                data = new JSONArray();
                TreeSet<String> headSet = new TreeSet<String>();
                for (String key : objItem.keySet()) {
                    headSet.add(key);
                }

                for (String head : headSet) {
                    header.add(head);
                    data.add(objItem.get(head));
                }
                continue;
            }

            if (!hasSameColumns(header, objItem)) return value;

            for (Object key : header) data.add(objItem.get(key));
        }

        compressedObject.put("_h", header);
        compressedObject.put("_d", data);

        return compressedObject;
    }

    private boolean hasSameColumns(JSONArray header, JSONObject objItem) {
        if (objItem.size() != header.size()) return false;

        for (String obj : objItem.keySet())
            if (!header.contains(obj)) return false;

        return true;
    }

    public EjsonEncoder bareKey() {
        features.remove(SerializerFeature.QuoteFieldNames);
        return this;
    }

    public EjsonEncoder map(Map<String, String> keyMapping, Map<String, String> valueMapping) {
        return mapKeys(keyMapping).mapValue(valueMapping);
    }

}
