package org.n3r.fastjson;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:zlex.dongliang@gmail.com">梁栋</a>
 * @since 1.0
 */
public class GzipsTest {

    private String inputStr = "zlex@zlex.org,snowolf@zlex.org,zlex.snowolf@zlex.org";

    @Test
    public final void testDataCompress() throws Exception {
        System.err.println("原文:\t" + inputStr);

        byte[] input = inputStr.getBytes();
        System.err.println("长度:\t" + input.length);

        byte[] data = Gzips.gzip(input);
        System.err.println("压缩后:\t");
        System.err.println("长度:\t" + data.length);

        byte[] output = Gzips.ungzip(data);
        String outputStr = new String(output);
        System.err.println("解压缩后:\t" + outputStr);
        System.err.println("长度:\t" + output.length);

        assertEquals(inputStr, outputStr);

        final String gzip = Gzips.gzip(inputStr);
        System.out.println(gzip);
        assertEquals(inputStr, Gzips.ungzip(gzip));

    }

    @Test
    public final void testFileCompress() throws Exception {
        FileOutputStream fos = new FileOutputStream("f.txt");

        fos.write(inputStr.getBytes());
        fos.flush();
        fos.close();


        File file = new File("f.txt");
        File fileGz = new File("f.txt.gz");
        Gzips.gzip(file, false);
        Gzips.ungzip(fileGz, false);


        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] data = new byte[(int) file.length()];
        dis.readFully(data);

        fis.close();

        String outputStr = new String(data);
        assertEquals(inputStr, outputStr);

        FileUtils.deleteQuietly(file);
        FileUtils.deleteQuietly(fileGz);
    }
}