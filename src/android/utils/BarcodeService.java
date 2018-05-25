package com.dn.andrewphan.bixolonprinter.utils;

/**
 * Package: com.dn.andrewphan.bixolonprinter.utils
 * Created by andrew.phan
 * on 5/24/18 9:58 AM
 */

public class BarcodeService {
    private static final int[][] leftPartMaps = {{48, 48, 48, 48, 48}, {48, 64, 48, 64, 64}, {48, 64, 64, 48, 64}, {48, 64, 64, 64, 48}, {64, 48, 48, 64, 64}, {64, 64, 48, 48, 64}, {64, 64, 64, 48, 48}, {64, 48, 64, 48, 64}, {64, 48, 64, 64, 48}, {64, 64, 48, 64, 48}};
    private static final int[] rightPartMap = {80, 80, 80, 80, 80, 112};
    private static final int firstDigitArg = 33, secondDigitArg = 96;
    private static final char delimiter = 124;


    public String parseInput(String code) {
        char[] data = new char[13];
        char[] givenData = code.toCharArray();

        int length = (givenData.length < 12) ? givenData.length : 12;
        System.arraycopy(givenData, 0, data, 0, length);

        int checkSumDigit = generateCheckSum(data);
        data[12] = String.valueOf(checkSumDigit).charAt(0);

        return String.valueOf(data);
    }

    public String generateCode(String code) {
        char[] data = code.toCharArray();
        if (data.length < 13) {
            System.err.println("Bad data input");
            return null;
        }

        String result = null;
        try {
            result = generateEanString(data);
        } catch (NumberFormatException e) {
            System.err.println("Input data had unconvertable characters: " + e.fillInStackTrace());
            result = "";
        }
        return result;
    }

    protected int generateCheckSum(char[] data) {
        int result = 0;
        for (int i = 0; i < 12; i++) {
            int num = Character.getNumericValue(data[i]);
            num = (i % 2 == 0) ? num : num * 3;
            result += num;
        }

        result = (result % 10 == 0) ? 0 : ((result / 10) + 1) * 10 - result;
        return result;
    }

    protected String generateEanString(char[] data) throws NumberFormatException {
        char[] resultData = new char[14];

        resultData[0] = (char) (Character.getNumericValue(data[0]) + firstDigitArg);
        resultData[1] = (char) (Character.getNumericValue(data[1]) + secondDigitArg);


        fillLeftPart(data, resultData);
        resultData[7] = delimiter;
        fillRightPart(data, resultData);

        return String.valueOf(resultData);
    }

    protected void fillLeftPart(char[] inputData, char[] resultData) {
        int[] chars = new int[]{
                Character.getNumericValue(inputData[2]),
                Character.getNumericValue(inputData[3]),
                Character.getNumericValue(inputData[4]),
                Character.getNumericValue(inputData[5]),
                Character.getNumericValue(inputData[6])
        };

        int pointer = Character.getNumericValue(inputData[0]);
        for (int i = 0; i < leftPartMaps[pointer].length; i++) {
            int n = i + 2;
            resultData[n] = (char) (chars[i] + leftPartMaps[pointer][i]);
        }

    }

    protected void fillRightPart(char[] inputData, char[] resultData) {
        int[] chars = new int[]{
                Character.getNumericValue(inputData[7]),
                Character.getNumericValue(inputData[8]),
                Character.getNumericValue(inputData[9]),
                Character.getNumericValue(inputData[10]),
                Character.getNumericValue(inputData[11]),
                Character.getNumericValue(inputData[12])
        };

        for (int i = 0; i < rightPartMap.length; i++) {
            int n = i + 8;
            resultData[n] = (char) (chars[i] + rightPartMap[i]);
        }
    }

}