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

        String nake = "{biz_params:{busiorder:BUSI201,numinfo:[{areacode:0431,optflag:1,relanumid:862222,servicetype:02},{areacode:0431,optflag:1,relanumid:8279,servicetype:02},{optflag:1,relanumid:1868666,servicetype:01}],productid:99122,servicetype:01,usernumber:15520},pub_params:{businesscode:13021081,channelcode:111001,citycode:901,customid:57130,eoptransid:GWAY201300,nettype:02,paytype:1,provincecode:090,transid:111302},reqbusicode:cu.tran.familynumset}";
        JSONObject obj = new EjsonDecoder().unbare().decode(nake);
        String encode2 = new EjsonEncoder().bare().encode(obj);
        // System.out.println(obj);
        Map<String, String> mapKey = Maps.newHashMap();
        Map<String, String> mapValue = Maps.newHashMap();
        String encode = new EjsonEncoder().bare().map(mapKey, mapValue).encode(obj);

        String keyMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(mapKey));
        String valueMapJson = new EjsonEncoder().bare().encode(EjsonDecoder.reverse(mapValue));
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

        String jsonTest = "[2, 3Hank, 4Hill,, Peter, Griffin,]";
        JSONArray objects = new EjsonDecoder().unbare().decode(jsonTest);
        System.out.println(objects);
        assertEquals(7, objects.size());
        String jsonOut = new EjsonEncoder().bare().encode(objects);
        System.out.println(jsonOut);

        String json = "{all:5.35,record:[{calldate:20130101},{calldate:20130102},{calldate:20130103}]}";
        JSONObject jsonObject = new EjsonDecoder().unbare().decode(json);
        String compressJson = new EjsonEncoder().bare().compact().encode(jsonObject);

        String expect = "{all:5.35,record:{_d:[20130101,20130102,20130103],_h:[calldate]}}";
        assertEquals(expect, compressJson);

        JSONObject decompressObj = new EjsonDecoder().unbare().uncompact().decode(compressJson);
        String originJson = new EjsonEncoder().bare().encode(decompressObj);
        assertEquals(json, originJson);

        obj = new EjsonDecoder().unbare().decode("{0:d^20130401}");
        System.out.println(obj);
    }

    @Test
    public void testBig() throws Exception {
        File cdrDir = new File("cdr");
        for (File cdr : cdrDir.listFiles()) {
            if (cdr.getName().matches(".+\\.json")) {
                System.out.println(cdr.getName());
                compressJson(cdr, false);
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
        String cdrJson = FileUtils.readFileToString(cdr, "UTF-8");
        writeFile(cdr, "_1原始_json", cdrJson);
        JSON cdrObject = new EjsonDecoder().decode(cdrJson);

        cdr = createTempFiles ? cdr : null;
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
        //testBigLog("cdr/gateway.EcsSSPGTWServer_05.log.gz", "cdr/gateway_EcsSSPGTWServer_05.log");
        // testBigLog("cdr/gateway_EcsSSPGTWServer_01_20130501.log.gz", "cdr/gateway_EcsSSPGTWServer_01_20130501.log");
    }

    private void testBigLog(String gzFileName, String baseName) throws IOException {
        File file = new File(gzFileName);
        File filebase = new File(baseName);
        FileInputStream fis = new FileInputStream(file);
        GZIPInputStream gis = new GZIPInputStream(fis);
        BufferedReader reader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        long fileStart = System.currentTimeMillis();
        int lineNo = 1;
        for(String line; (line = reader.readLine()) != null; ++lineNo) {
            long start = System.currentTimeMillis();
            System.out.print("processing " + lineNo);
            List<String> jsons = parseLine(line);
            for (int i = 0, ii = jsons.size(); i < ii; ++i)
                prossesLineJson(file, jsons.get(i), lineNo, i + 1);

            System.out.println(", cost " + (System.currentTimeMillis() - start) /1000.);
        }
        System.out.println("file cost " + (System.currentTimeMillis() - fileStart) /1000. * 60.);
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
            if (compressJson(fullJson, null, null)) return;
            if (compressJson2(fullJson)) return;
            compressJson(fullJson, file, "_" + lineNo + "_" + jsonNum + "_");
        } catch (Exception ex){
            compressJson(fullJson, file, "_" + lineNo + "_" + jsonNum + "_");
            throw new RuntimeException(ex);
        }
        throw new RuntimeException("error!");
    }

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
        JSON cdrObject = new EjsonDecoder().decode(cdrJson);

        final Map<String, String> keyMapping = Maps.newHashMap();
        final Map<String, String> valueMapping = Maps.newHashMap();
        String json = new EjsonEncoder().bare().map(keyMapping, valueMapping)
                .compact().encode(cdrObject);

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
