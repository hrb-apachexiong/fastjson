package org.n3r.fastjson;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.apache.commons.io.FileUtils.*;
import static org.junit.Assert.*;

public class EjsonEncoderTest {
    @Test
    public void testCompressArray() throws Exception {
        String complex = "{\"billstr\":\"月固定费:126.00;语音通话费:22.95;增值业务费:10.20;调增减项:-6.00;消费合计:153.15;抵扣合计:153.15;实际应缴合计:0.00;|3G业务M（多媒体）值【WCDMA(3G)-126元基本套餐B(首月套餐资费）】0M,3G业务T（文本）值【WCDMA(3G)-126元基本套餐B(首月套餐资费）】0T,数据业务流量【WCDMA(3G)-126元基本套餐B(首月套餐资费）】45.0542MB(非定向流量),可视电话（分钟数）【WCDMA(3G)-126元基本套餐B(首月套餐资费）】0分钟(时长),普通语音通话（分钟数）【WCDMA(3G)-126元基本套餐B(首月套餐资费）】680分钟(时长),;\"}";
        JSONObject decode1 = new EjsonDecoder().decode(complex);
        String encode1 = new EjsonEncoder().bare().encode(decode1);
        System.out.println(encode1);

        String nake = "{b:{busiorder:[B,U,S,I,201],numinfo:[{areacode:0431,optflag:{a:1},relanumid:862222,servicetype:02},{areacode:0431,optflag:1,relanumid:8279,servicetype:02},{areacode:0431,optflag:1,relanumid:1868666,servicetype:01}],productid:99122,servicetype:01,usernumber:15520},pub_params:{businesscode:13021081,channelcode:111001,citycode:901,customid:57130,eoptransid:GWAY201300,nettype:02,paytype:1,provincecode:090,transid:111302},reqbusicode:cu.tran.familynumset}";
        JSONObject obj = new EjsonDecoder().unbare().decode(nake);
        String encode2 = new EjsonEncoder().bare().encode(obj);
        // System.out.println(obj);
        Map<String, String> mapKey = Maps.newHashMap();
        Map<String, String> mapValue = Maps.newHashMap();
        String encode = new EjsonEncoder().bare().map(mapKey, mapValue).encode(obj);

        String keyMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(mapKey));
        String valueMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(mapValue));
        System.out.println("valueMapJson : " + valueMapJson);
        JSONObject keyMap = new EjsonDecoder().unbare().decode(keyMapJson);
        JSONObject valMap = new EjsonDecoder().unbare().decode(valueMapJson);


        Object decode = new EjsonDecoder().unbare().unmap(keyMap, valMap).decode(encode);
        assertEquals(encode2, new EjsonEncoder().bare().encode(decode));

        mapKey = Maps.newHashMap();
        mapValue = Maps.newHashMap();
        encode = new EjsonEncoder().bare().map(mapKey, mapValue).compact().encode(obj);

        keyMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(mapKey));
        valueMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(mapValue));
        keyMap = new EjsonDecoder().unbare().decode(keyMapJson);
        valMap = new EjsonDecoder().unbare().decode(valueMapJson);

        decode = new EjsonDecoder().unbare().unmap(keyMap, valMap).uncompact().decode(encode);
        assertEquals(encode2, new EjsonEncoder().bare().encode(decode));

        String jsonStr = "{data:[{eee:12345,fff:678},{eee:12345,fff:678,hhh:q234},{eee:12345,fff:678},{eee:12345,fff:678,hhh:q234}]}";
        String mapkeyjson = "{}";
        String mapvaluejson = "{0:12345}";
        String jsonE = "{data:[{eee:@0,fff:678},{eee:@0,fff:678,hhh:q234},{eee:@0,fff:678},{eee:@0,fff:678,hhh:q234}]}";
        JSONObject mapKeyT = new EjsonDecoder().unbare().decode(mapkeyjson);
        JSONObject mapValueT = new EjsonDecoder().unbare().decode(mapvaluejson);
        JSONObject jsons = new EjsonDecoder().unbare().unmap(mapKeyT, mapValueT).decode(jsonE);
        
        assertEquals(jsonStr, new EjsonEncoder().bare().encode(jsons));
        
        String jsonArr1 = "[{eee:12345,fff:678},{eee:12345,fff:678,hhh:q234}]";
        String mapkeyArr1 = "{0:eee,1:fff,2:hhh}";
        String mapvalueArr1 = "{0:12345}";
        String jsonA1 = "[{0:@0,1:678},{0:@0,1:678,2:q234}]";
        JSONObject mapKeyA1 = new EjsonDecoder().unbare().decode(mapkeyArr1);
        JSONObject mapValueA1 = new EjsonDecoder().unbare().decode(mapvalueArr1);
        JSONArray jsona1 = new EjsonDecoder().unbare().unmap(mapKeyA1, mapValueA1).decode(jsonA1);
      
        assertEquals(jsonArr1, new EjsonEncoder().bare().encode(jsona1));
        
        String jsonArr2 = "[[{eee:12345},{fff:678}]]";
        String mapkeyArr2 = "{0:eee,1:fff}";
        String mapvalueArr2 = "{0:12345}";
        String jsonA2 = "[[{0:@0},{1:678}]]";
        JSONObject mapKeyA2 = new EjsonDecoder().unbare().decode(mapkeyArr2);
        JSONObject mapValueA2 = new EjsonDecoder().unbare().decode(mapvalueArr2);
        JSONArray jsona2 = new EjsonDecoder().unbare().unmap(mapKeyA2, mapValueA2).decode(jsonA2);
      
        assertEquals(jsonArr2, new EjsonEncoder().bare().encode(jsona2));
        
        
        
        String jsonTest = "[{ab:2}, {bc:3Hank}, {ef:4Hill},{}, {fg:Peter}, {dd:Griffin},]";
        JSONArray objects = new EjsonDecoder().unbare().decode(jsonTest);
        System.out.println("objects:" + objects);
        assertEquals(7, objects.size());
        String jsonOut = new EjsonEncoder().bare().encode(objects);
        System.out.println(jsonOut);

        String json = "{all:5.35,record:[{calldate:20130101},{calldate:20130102},{calldate:20130103}]}";
        JSONObject jsonObject = new EjsonDecoder().unbare().decode(json);
        String compressJson = new EjsonEncoder().bare().compact().encode(jsonObject);

        String jsonarray = "[2,ewe,4rty,dthyh]";
        JSONArray objectsarr = new EjsonDecoder().unbare().decode(jsonarray);
        String compressJsonarray = new EjsonEncoder().bare().compact().encode(objectsarr);
        
        String expect = "{all:5.35,record:{_d:[20130101,20130102,20130103],_h:[calldate]}}";
        String expectT = "{all:5.35,record:{D:[20130101,20130102,20130103],E:[calldate]}}";
        assertEquals(expect, compressJson);

        JSONObject decompressObj = new EjsonDecoder().unbare().uncompact().decode(compressJson);
        JSONObject decompressObjT = new EjsonDecoder().unbare().uncompact().decode(expect);
        String originJson = new EjsonEncoder().bare().encode(decompressObj);
        assertEquals(json, originJson);

        obj = new EjsonDecoder().unbare().decode("{0:d^20130401}");
        System.out.println(obj);
    }

    @Test
    public void testBig() throws Exception {
        File file = new File("cdr/免填单.json");
        String s = FileUtils.readFileToString(file, "UTF-8");
        String gzip = Gzips.gzip(s);
        System.out.println(s.length() + " vs " + gzip.length());
        // 1188 vs 1424
        File cdrDir = new File("cdr");
        for (File cdr : cdrDir.listFiles()) {
            if (cdr.getName().matches(".+\\.json")) {
                System.out.println(cdr.getName());
                compressJson(cdr, true);
            }
        }
    }

    private File writeFile(File cdr, String suffix, String value)  {
        if (cdr == null)  return null;

        String baseName = FilenameUtils.removeExtension(cdr.getName());
        File file = new File(cdr.getParent(), baseName + suffix);
        System.out.println("write file: " + file);
        try {
            writeStringToFile(file, value, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private void compressJson(File cdr, boolean createTempFiles) throws Exception {
        cdr = createTempFiles ? cdr : null;
        String cdrJson = FileUtils.readFileToString(cdr, "UTF-8");
        writeFile(cdr, "_1原始_json", cdrJson);
        JSON cdrObject = new EjsonDecoder().decode(cdrJson);

        String naked = new EjsonEncoder().bare().encode(cdrObject);
        writeFile(cdr, "_2裸奔_json", naked);

        final Map<String, String> keyMapping = Maps.newHashMap();
        final Map<String, String> valueMapping = Maps.newHashMap();
        String jsonStr = new EjsonEncoder().bare().map(keyMapping, valueMapping).encode(cdrObject);

        String keyMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(keyMapping));
        writeFile(cdr, "_0瘦身键表_json", keyMapJson);
        String valueMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(valueMapping));
        writeFile(cdr, "_0瘦身值表_json", valueMapJson);

        writeFile(cdr, "_3裸奔_瘦身_json", jsonStr);
        cdrObject = new EjsonDecoder().unbare().decode(jsonStr);
        String compressJson = new EjsonEncoder().bare().compact().encode(cdrObject);
        File file = writeFile(cdr, "_4裸奔_瘦身_浓缩_json", compressJson);
        if (file != null) Gzips.gzip(file);

        JSON origin = new EjsonDecoder().unbare().uncompact().decode(compressJson);
        writeFile(cdr, "_5裸奔_瘦身_反浓缩_json", new EjsonEncoder().bare().encode(origin));

        JSONObject keyMap = new EjsonDecoder().unbare().decode(keyMapJson);
        JSONObject valMap = new EjsonDecoder().unbare().decode(valueMapJson);
        JSON old =  new EjsonDecoder().unbare().unmapKeys(keyMap).unmapValues(valMap).decode(origin);
        String oldNaked = new EjsonEncoder().bare().encode(old);
        writeFile(cdr, "_6裸奔_反瘦身_json", oldNaked);
        writeFile(cdr, "_7反裸奔_json", new EjsonEncoder().encode(old));
    }

    @Test
    public void testLog() throws Exception {
        // testBigLog("cdr/gateway.EcsSSPGTWServer_05.log.gz", "cdr/gateway_EcsSSPGTWServer_05.log");
        // testBigLog("cdr/gateway_EcsSSPGTWServer_01_20130501.log.gz", "cdr/gateway_EcsSSPGTWServer_01_20130501.log");
        // testBigLog("cdr/gateway.log.EcsSSPGTWServer_02.2013-05-01.gz", "cdr/gateway_EcsSSPGTWServer_02_20130501
        // .log");
    }

    private void testBigLog(String gzFileName, String baseName) throws IOException {
        File file = new File(gzFileName);
        File filebase = new File(baseName);
        FileInputStream fis = new FileInputStream(file);
        GZIPInputStream gis = new GZIPInputStream(fis);
        BufferedReader reader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        long fileStart = System.currentTimeMillis();
        int lineNo = 1;
        int totalJsonsNum = 0;
        for(String line; (line = reader.readLine()) != null; ++lineNo) {
            long start = System.currentTimeMillis();
            System.out.print("processing " + lineNo);
            List<String> jsons = parseLine(line);
            int size = jsons.size();
            totalJsonsNum += size;
            for (int i = 0, ii = size; i < ii; ++i)
                prossesLineJson(file, jsons.get(i), lineNo, i + 1);

            System.out.println(", cost " + (System.currentTimeMillis() - start) /1000.);
        }
        System.out.println("file cost " + (System.currentTimeMillis() - fileStart) /1000. / 60. + "min");
        System.out.println("total json num: " + totalJsonsNum);
        System.out.println("process total old: " + totalOldSize / 1024. + "K , " +
                "compact total size: " + totalCompactSize/1024. + "K");
        gis.close();
        fis.close();
    }

    @Test
    public void testParseLine() throws Exception {
        String line = FileUtils.readFileToString(new File("line18041.txt"), "UTF-8");
        List<String> strings = parseLine(line);
        System.out.println(strings);

    }

    private List<String> parseLine(String line) {
        ArrayList<String> jsons = new ArrayList<String>();
        int lastPos = -1;
        int leftNum = 0;
        boolean openQuote = false; // 双引号开始
        for( int i = 0, ii = line.length(); i < ii; ++i) {
            char ch = line.charAt(i);
            if (ch == '\\' && i + 1 < ii) {
                ++i;
                continue;
            }

            if (ch == '{' && !openQuote) {
                ++leftNum;
                if (leftNum == 1) lastPos = i;
            }

            if (lastPos < 0) continue;

            if (ch == '"') {
                openQuote = !openQuote;
            }
            else if (ch == '}' && !openQuote) {
                --leftNum;
            }

            if (lastPos >= 0 && leftNum == 0) {
                String fullJson = line.substring(lastPos, i + 1);
                jsons.add(fullJson);
                lastPos = -1;
            }
        }

        return jsons;
    }

    private void prossesLineJson(File file, String fullJson, int lineNo, int jsonNum) {
        try {
            //if (compressJson(fullJson, null, null)) return;
            if (compressJson2(fullJson)) return;
            compressJson(fullJson, file, "_" + lineNo + "_" + jsonNum + "_");
        } catch (Exception ex){
            compressJson(fullJson, file, "_" + lineNo + "_" + jsonNum + "_");
            throw new RuntimeException(ex);
        }
        throw new RuntimeException("error!");
    }

    private long totalOldSize = 0;
    private long totalCompactSize = 0;

    private boolean compressJson(String cdrJson, File cdr, String suffix) {
        writeFile(cdr, suffix + "_1原始_json", cdrJson);

        JSON cdrObject = new EjsonDecoder().decode(cdrJson);
        String naked = new EjsonEncoder().bare().encode(cdrObject);
        writeFile(cdr, suffix + "_2裸奔_json", naked);

        final Map<String, String> keyMapping = Maps.newHashMap();
        final Map<String, String> valueMapping = Maps.newHashMap();
        String jsonStr = new EjsonEncoder().bare().map(keyMapping, valueMapping).encode(cdrObject);

        String keyMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(keyMapping));
        writeFile(cdr, suffix + "_0瘦身键表_json", keyMapJson);
        String valueMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(valueMapping));
        writeFile(cdr, suffix + "_0瘦身值表_json", valueMapJson);

        writeFile(cdr,  suffix + "_3裸奔_瘦身_json", jsonStr);

        cdrObject = new EjsonDecoder().unbare().decode(jsonStr);
        String compressJson = new EjsonEncoder().bare().compact().encode(cdrObject);
        File file = writeFile(cdr,  suffix + "_4裸奔_瘦身_浓缩_json", compressJson);
        if (file != null) Gzips.gzip(file);

        JSON origin = new EjsonDecoder().unbare().uncompact().decode(compressJson);
        writeFile(cdr,  suffix + "_5裸奔_瘦身_反浓缩_json", new EjsonEncoder().bare().encode(origin));

        JSONObject keyMap = new EjsonDecoder().unbare().decode(keyMapJson);
        JSONObject valMap = new EjsonDecoder().unbare().decode(valueMapJson);
        JSON old =  new EjsonDecoder().unbare().unmap(keyMap, valMap).decode(origin);
        String oldNaked = new EjsonEncoder().bare().encode(old);
        writeFile(cdr,  suffix + "_6裸奔_反瘦身_json", oldNaked);
        writeFile(cdr, suffix + "_7反裸奔_json", new EjsonEncoder().encode(old));

        return naked.equals(oldNaked) ;
    }

    private boolean compressJson2(String cdrJson) {
        totalOldSize += cdrJson.length();

        JSON cdrObject = new EjsonDecoder().decode(cdrJson);

        final Map<String, String> keyMapping = Maps.newHashMap();
        final Map<String, String> valueMapping = Maps.newHashMap();
        String json = new EjsonEncoder().bare().map(keyMapping, valueMapping)
                .compact().encode(cdrObject);
        totalCompactSize += json.length();

        String keyMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(keyMapping));
        String valueMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(valueMapping));

        JSONObject keyMap = new EjsonDecoder().unbare().decode(keyMapJson);
        JSONObject valMap = new EjsonDecoder().unbare().decode(valueMapJson);
        JSON old =  new EjsonDecoder().unbare().unmap(keyMap, valMap).uncompact().decode(json);
        String naked = new EjsonEncoder().bare().encode(cdrObject);
        String oldNaked = new EjsonEncoder().bare().encode(old);

        return naked.equals(oldNaked) ;
    }

}
