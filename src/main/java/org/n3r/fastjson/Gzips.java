package org.n3r.fastjson;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class Gzips {
    public static final int BUFFER = 1024;
    public static final String EXT = ".gz";

    public static byte[] gzip(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        gzip(bais, baos);
        byte[] output = baos.toByteArray();

        try {
            baos.flush();
            baos.close();
            bais.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    public static String gzip(String str) {
        try {
            final byte[] bytes = str.getBytes("UTF-8");
            final byte[] zip = gzip(bytes);
            return DatatypeConverter.printBase64Binary(zip);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件压缩
     *
     * @param file
     * @throws Exception
     */
    public static void gzip(File file) {
        gzip(file, false);
    }

    /**
     * 文件压缩
     *
     * @param file
     * @param delete 是否删除原始文件
     * @throws Exception
     */
    public static void gzip(File file, boolean delete) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(file.getPath() + EXT);

            gzip(fis, fos);

            fis.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (delete) {
            file.delete();
        }
    }

    /**
     * 数据压缩
     *
     * @param is
     * @param os
     * @throws Exception
     */
    public static void gzip(InputStream is, OutputStream os) {
        try {
            GZIPOutputStream gos = new GZIPOutputStream(os) {{
                def.setLevel(Deflater.BEST_COMPRESSION);
            }};

            byte data[] = new byte[BUFFER];
            for (int count; (count = is.read(data, 0, BUFFER)) != -1; ) {
                gos.write(data, 0, count);
            }

            gos.finish();
            gos.flush();
            gos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 数据解压缩
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] ungzip(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 解压缩

        ungzip(bais, baos);

        data = baos.toByteArray();

        try {
            baos.flush();
            baos.close();

            bais.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public static String ungzip(String data) throws Exception {
        final byte[] bytes = DatatypeConverter.parseBase64Binary(data);
        final byte[] ungzip = ungzip(bytes);
        return new String(ungzip, "UTF-8");
    }

    /**
     * 文件解压缩
     *
     * @param file
     * @throws Exception
     */
    public static void ungzip(File file) throws Exception {
        ungzip(file, false);
    }


    public static void ungzip(File file, boolean delete) {
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(file.getPath().replace(EXT, ""));
            ungzip(fis, fos);
            fis.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (delete) {
            file.delete();
        }
    }

    public static void ungzip(InputStream is, OutputStream os) {
        try {
            GZIPInputStream gis = new GZIPInputStream(is);

            byte data[] = new byte[BUFFER];
            for (int count; (count = gis.read(data, 0, BUFFER)) != -1; ) {
                os.write(data, 0, count);
            }

            gis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
