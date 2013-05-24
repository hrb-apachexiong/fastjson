package org.n3r.fastjson;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Objects.firstNonNull;

public class EjsonDecoder {
    int featureValues = JSON.DEFAULT_PARSER_FEATURE;
    private Map<String, String> keyMapping;
    private Map<String, String> valueMapping;

    {
        featureValues = Feature.config(featureValues, Feature.AllowArbitraryCommas, false);
    }

    private boolean uncompact;

    public static <K, V> Map<V, K> reverse(Map<K, V> source) {
        Map<V, K> reversedMap = new HashMap<V, K>(source.size());
        for (Map.Entry<K, V> entry : source.entrySet())
            reversedMap.put(entry.getValue(), entry.getKey());

        return reversedMap;
    }

    private JSONObject decodeObject(String json) {
        return (JSONObject) JSON.parse(json, featureValues);
    }

    private JSONArray decodeArray(String json) {
        return JSON.parseArray(json, featureValues);
    }

    public <T> T decode(JSON json) {
        Object result = decodeJSON(json);

        return (T) (keyMapping == null ? result : unmap(result, false));
    }

    public <T> T decode(String json) {
        Object result = decodeJSON(json);

        return (T) (keyMapping == null ? result : unmap(result, false));
    }

    private <T> T decodeJSON(String json) {
        if (json.startsWith("[")) return (T) decodeArray(json);

        JSONObject jsonObject = decodeObject(json);
        return (T) (uncompact ? deCompact(jsonObject) : jsonObject);
    }

    private <T> T decodeJSON(JSON jsonObject) {
        return (T) (uncompact ? deCompact(jsonObject) : jsonObject);
    }

    /**
     * 在解析中支持裸奔模式。
     *
     * @return
     */
    public EjsonDecoder unbare() {
        featureValues = Feature.config(featureValues, Feature.TryUnqotedValue, true);
        return this;
    }

    private JSON deCompact(JSON object) {
        if (object instanceof JSONObject) return deCompact((JSONObject) object);
        if (object instanceof JSONArray) return deCompact((JSONArray) object);

        return object;
    }

    private JSON deCompact(JSONObject object) {
        if (isCompactedArrayFormat(object)) {
            return deCompactArrayFormat(object);
        }

        JSONObject decompressed = new JSONObject();
        for (Map.Entry<String, Object> item : object.entrySet()) {
            Object value = item.getValue();

            decompressed.put(item.getKey(), value instanceof JSON ? deCompact((JSON) value) : value);
        }

        return decompressed;
    }

    private JSONArray deCompact(JSONArray arr) {
        JSONArray processedArr = new JSONArray(arr.size());
        for (Object item : arr)
            processedArr.add(item instanceof JSON ? deCompact((JSON) item) : item);

        return processedArr;
    }

    private boolean isCompactedArrayFormat(JSONObject object) {
        return object.size() == 2 && object.containsKey("_h") && object.containsKey("_d");
    }

    private JSONArray deCompactArrayFormat(JSONObject jsonValue) {
        JSONArray head = jsonValue.getJSONArray("_h");
        JSONArray data = jsonValue.getJSONArray("_d");
        int arraySize = data.size() / head.size();
        JSONArray decompressJsonArray = new JSONArray(arraySize);
        for (int i = 0; i < arraySize; i++) {
            JSONObject item = new JSONObject(head.size());
            decompressJsonArray.add(item);
            for (int j = 0, jj = head.size(); j < jj; ++j)
                item.put(head.getString(j), data.get(i * jj + j));
        }

        return decompressJsonArray;
    }

    public EjsonDecoder uncompact() {
        this.uncompact = true;
        return this;
    }

    public EjsonDecoder unmapKeys(Map keyMapping) {
        this.keyMapping = keyMapping;
        return this;
    }

    public EjsonDecoder unmapValues(Map valueMapping) {
        this.valueMapping = valueMapping;
        return this;
    }

    private Object unmap(Object object, boolean isValueObject) {
        if (object instanceof JSONObject)
            return unmap((JSONObject) object);

        if (object instanceof JSONArray)
            return unmap((JSONArray) object);

        if (isValueObject && object instanceof String) {
            String strValue = (String) object;
            if (strValue.startsWith("@"))
                return firstNonNull(valueMapping.get(strValue.substring(1)), object);
        }

        return object;
    }

    private JSONObject unmap(JSONObject origin) {
        JSONObject unmapped = new JSONObject();
        for (Map.Entry<String, Object> item : origin.entrySet()) {
            Object value = item.getValue();
            String key = item.getKey();
            String mappedKey = keyMapping.get(key);
            mappedKey = mappedKey == null ? key : mappedKey;
            unmapped.put(mappedKey, unmap(value, true));
        }

        return unmapped;
    }

    private JSONArray unmap(JSONArray array) {
        JSONArray ummapped = new JSONArray(array.size());
        for (int i = 0, ii = array.size(); i < ii; ++i)
            ummapped.add(unmap(array.get(i), false));

        return ummapped;
    }

    public EjsonDecoder unmap(JSONObject keyMap, JSONObject valMap) {
        return unmapKeys(keyMap).unmapValues(valMap);
    }
}
