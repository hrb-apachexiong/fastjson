package org.n3r.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;

/**
 * 测试JSON字符串中KEY和VALUE都不带双引号的裸奔模式.
 * User: Bingoo
 * Date: 13-5-8
 */
public class NakeJsonTest {
    @Test
    public void testSimple() {
        JSONObject jsonObject = JSON.parseObject("{name:\"bingoo\"}");
        System.out.println(jsonObject);
        JSONObject jsonObject2 = JSON.parseObject("{name:bingoo}", Feature.TryUnqotedValue);
        System.out.println(jsonObject2);

        jsonObject2 = JSON.parseObject("{name:}", Feature.TryUnqotedValue);
        System.out.println(jsonObject2);
        jsonObject2 = JSON.parseObject("{name:,age:32}", Feature.TryUnqotedValue);
        System.out.println(jsonObject2);
        jsonObject2 = JSON.parseObject("{record:[{calldate:20130101},{calldate:20130101}]}");
        System.out.println(jsonObject2);
        jsonObject2 = JSON.parseObject("{all:5.35,record:[{calldate:20130101},{calldate:20130101}]}", Feature.TryUnqotedValue);
        System.out.println(jsonObject2);


    }

    @Test
    public void testMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", "bingoo");
        String jsonStr = new EjsonEncoder().encode(map);
        Assert.assertEquals("{\"name\":\"bingoo\"}", jsonStr);
        jsonStr = new EjsonEncoder().bareKey().encode(map);
        Assert.assertEquals("{name:\"bingoo\"}", jsonStr);
        jsonStr = new EjsonEncoder().bare().encode(map);
        Assert.assertEquals("{name:bingoo}", jsonStr);
        map.put("name", "[bingoo]");
        jsonStr = new EjsonEncoder().bare().encode(map);
        Assert.assertEquals("{name:\"[bingoo]\"}", jsonStr);

        map.put("name", "123bingoo");
        jsonStr = new EjsonEncoder().bare().encode(map);
        Assert.assertEquals("{name:123bingoo}", jsonStr);
        JSONObject jsonObject = JSON.parseObject(jsonStr, Feature.TryUnqotedValue);
        System.out.println(jsonObject);
        jsonObject = JSON.parseObject("{name:123}", Feature.TryUnqotedValue);
        Assert.assertEquals(123, jsonObject.getIntValue("name"));
        jsonObject = JSON.parseObject("{name:@1}", Feature.TryUnqotedValue);
        Assert.assertEquals("@1", jsonObject.getString("name"));

    }

    @Test
    public void testBean() {
        final Bingoo map = new Bingoo();
        map.setName("bingoo");
        String jsonStr = new EjsonEncoder().encode(map);
        Assert.assertEquals("{\"name\":\"bingoo\"}", jsonStr);
        jsonStr = new EjsonEncoder().bareKey().encode(map);
        Assert.assertEquals("{name:\"bingoo\"}", jsonStr);
        jsonStr = new EjsonEncoder().bare().encode(map);
        Assert.assertEquals("{name:bingoo}", jsonStr);

        map.setName("bi\"ngoo");
        jsonStr = new EjsonEncoder().bare().encode(map);
        Assert.assertEquals("{name:bi\\\"ngoo}", jsonStr);
        JSONObject jsonObject2 = JSON.parseObject(jsonStr, Feature.TryUnqotedValue);
        System.out.println(jsonObject2);
    }

    public static class Bingoo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
