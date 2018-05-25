package com.dn.andrewphan.bixolonprinter.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Package: com.dn.andrewphan.bixolonprinter.utils
 * Created by andrew.phan
 * on 5/24/18 3:17 PM
 */
public class PrinterSetting {

    // Action to execute
    public static final String ACTION_PRINT_TEXT = "printText";
    public static final String ACTION_PRINT_1D_BARCODE = "printBarCode";
    public static final String ACTION_PRINT_QRCODE = "printQRCode";
    public static final String ACTION_GET_STATUS = "getStatus";
    public static final String ACTION_CUT_PAPER = "cutPaper";
    public static final String ACTION_START_MSR_READER_LISTENER = "startMsrReaderListener";
    public static final String ACTION_STOP_MSR_READER_LISTENER = "stopMsrReaderListener";
    public static final String ACTION_START_CONNECTION_LISTENER = "startConnectionListener";
    public static final String ACTION_RECONNECT = "reconnect";
    public static final String ACTION_DISCONNECT = "disconnect";
    public static final String ACTION_STOP_CONNECTION_LISTENER = "stopConnectionListener";

    // Alignment string
    public static final String ALIGNMENT_LEFT = "LEFT";
    public static final String ALIGNMENT_CENTER = "CENTER";
    public static final String ALIGNMENT_RIGHT = "RIGHT";

    // Font string
    public static final String FONT_A = "A";
    public static final String FONT_B = "B";

    @SuppressWarnings({"serial", "unused"})
    public final static Map<String, String> PRODUCT_IDS = new HashMap<String, String>() {{
        put("10", "SPP-R200");
        put("11", "SPP-R210");
        put("18", "SPP-100");
        put("22", "SRP-F310");
        put("31", "SRP-350II");
        put("29", "SRP-350plusII");
        put("35", "SRP-F312");
        put("36", "SRP-350IIK");
        put("40", "SPP-R200II");
        put("33", "SPP-R300");
        put("41", "SPP-R400");
    }};

    @SuppressWarnings("serial")
    public final static Map<String, Integer> MAX_COL = new HashMap<String, Integer>() {{
        put("SPP-R200", 0);
        put("SPP-R210",0);
        put("SPP-100", 0);
        put("SRP-F310", 0);
        put("SRP-350II", 0);
        put("SRP-350plusII", 0);
        put("SRP-F312", 0);
        put("SRP-350IIK", 0);
        put("SPP-R200II", 0);
        put("SPP-R300", 48);
        put("SPP-R400", 69);
    }};


    public static final int STATUS_STEP_GET_STATUS = 0;
    public static final int STATUS_STEP_GET_BATTERY_STATUS = 1;
    public static final int STATUS_STEP_GET_PRINTER_ID_FIRMWARE_VERSION = 2;
    public static final int STATUS_STEP_GET_PRINTER_ID_MANUFACTURER = 3;
    public static final int STATUS_STEP_GET_PRINTER_ID_PRINTER_MODEL = 4;
    public static final int STATUS_STEP_GET_PRINTER_ID_CODE_PAGE = 5;
    public static final int STATUS_STEP_GET_PRINTER_ID_MODEL_ID = 6;
    public static final int STATUS_STEP_GET_PRINTER_ID_PRODUCT_SERIAL = 7;
    public static final int STATUS_STEP_GET_PRINTER_ID_TYPE_ID = 8;
    public static final int STATUS_STEP_COMPLETE = 10;

}
