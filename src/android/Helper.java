package com.dn.andrewphan.bixolonprinter;

/**
 * Package: com.dn.andrewphan.bixolonprinter
 * Created by andrew.phan
 * on 5/24/18 10:00 AM
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import com.dn.andrewphan.bixolonprinter.BixolonPrinterManager;

public class Helper {

    /**
     *
     * @param str
     * @return
     */
    public static String toDBC(String str) {
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }

        }
        return new String(c);
    }

    public static String unicodeEncoding(String str) {
        String result = "";
        for(char ch : str.toCharArray())
            result += "\\u" + Integer.toHexString(ch | 0x10000).substring(1);
        System.out.print(result);
        return result;
    }

    /**
     *
     * @param receipt
     * @return
     */
    public static String formatReceipt(String receipt){
        receipt = receipt.replace("|1B", "");
        receipt = receipt.replace("|N", "\r\n");
        return receipt;
    }

    /**
     *
     * @param fileUrl
     * @param out
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static byte[] readFileFromHttpService(final String fileUrl, final BixolonPrinterManager.OutValue out) throws MalformedURLException, IOException
    {
        final URL url = new URL(fileUrl);
        final InputStream reader = url.openStream();
        final byte[] data = new byte[25000];
        int ret = 0;
        int count = 0;
        do {
            ret = reader.read(data,count, 25000-count);
            count += ret;
        }
        while (ret != -1);

        reader.close();
        out.intValue = count + 1;
        return data;
    }

    /**
     *
     * @param inputText
     * @return
     */
    public static byte[] createCpUTF8(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static String determineFileNameExtension(final String fileName){
        String extension = "";
        final int i = fileName.lastIndexOf('.');
        if (i>=0){
            extension = fileName.substring(i+1);
        }
        return extension;
    }
}
