package com.dn.andrewphan.bixolonprinter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
import com.dn.andrewphan.bixolonprinter.utils.PrinterSetting;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Package: com.dn.andrewphan.bixolonprinter
 * Created by andrew.phan
 * on 5/24/18 9:58 AM
 */

public class BixolonPrinterPlugin extends CordovaPlugin {
    private static final String TAG = BixolonPrinterPlugin.class.getName();

    private CallbackContext cbContext;
    private String lastActionName;
    private JSONArray lastActionArgs;
    private String actionSuccess;
    private String actionError;
    //private Queue<Integer> printerQueue;

    public boolean isValidAction;
    public boolean optAutoConnect;
    public boolean optToastMessage;

    private int statusStep = 0;

    public String mConnectedDeviceName;
    public String mConnectedDeviceAddress;
    private JSONObject mConnectedDeviceStatus;
    public boolean mIsConnected;
    static BixolonPrinter mBixolonPrinter;
    //

    // MSR Reader
    private CallbackContext msrReaderCallbackContext;
    private CallbackContext connectionListenerCallbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.d(TAG, "BixolonPrinterPlugin.initialize_START");

        super.initialize(cordova, webView);

        mBixolonPrinter = new BixolonPrinter(cordova.getActivity(), mHandler, null);
        this.mIsConnected = false;
        this.mConnectedDeviceName = null;
        this.mConnectedDeviceAddress = null;
        this.mConnectedDeviceStatus = null;

        this.actionSuccess = null;
        this.actionError = null;
        this.lastActionArgs = null;
        this.lastActionName = null;

        Log.d(TAG, "BixolonPrinterPlugin.initialize_END");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "BixolonPrinterPlugin.onDestroy_START");

        super.onDestroy();

        this.isValidAction = false;
        mBixolonPrinter.disconnect();

        Log.d(TAG, "BixolonPrinterPlugin.onDestroy_END");
    }

    /**
     * Executes the request and returns PluginResult
     *
     * @param action          Action to execute
     * @param args            JSONArray of arguments to the plugin
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return A PluginRequest object with a status
     */
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        Log.d(TAG, "BixolonPrinterPlugin.execute_START");

        this.isValidAction = true;
        this.cbContext = callbackContext;
        this.lastActionName = action;
        this.lastActionArgs = args;

        Log.i(TAG, "action: " + action);

        if (PrinterSetting.ACTION_PRINT_TEXT.equals(action)) {
            JSONObject printConfig = args.optJSONObject(1);
            this.optAutoConnect = printConfig.optBoolean("autoConnect");
            this.optToastMessage = printConfig.optBoolean("toastMessage");
        } else if (PrinterSetting.ACTION_PRINT_1D_BARCODE.equals(action)) {
            JSONObject printConfig = args.optJSONObject(1);
            this.optAutoConnect = printConfig.optBoolean("autoConnect");
            this.optToastMessage = printConfig.optBoolean("toastMessage");
        } else if (PrinterSetting.ACTION_PRINT_QRCODE.equals(action)) {
            JSONObject printConfig = args.optJSONObject(1);
            this.optAutoConnect = printConfig.optBoolean("autoConnect");
            this.optToastMessage = printConfig.optBoolean("toastMessage");
        } else if (PrinterSetting.ACTION_CUT_PAPER.equals(action)) {
            JSONObject printConfig = args.optJSONObject(0);
            this.optAutoConnect = printConfig.optBoolean("autoConnect");
            this.optToastMessage = printConfig.optBoolean("toastMessage");
        } else if (PrinterSetting.ACTION_GET_STATUS.equals(action)) {
            JSONObject printConfig = args.optJSONObject(1);
            this.optAutoConnect = printConfig.optBoolean("autoConnect");
            this.optToastMessage = printConfig.optBoolean("toastMessage");
        } else if (PrinterSetting.ACTION_START_MSR_READER_LISTENER.equals(action)) {
            this.optAutoConnect = true;
            if(!this.startMsrReaderListener()) {
                return true;
            }
        } else if (PrinterSetting.ACTION_STOP_MSR_READER_LISTENER.equals(action)) {
            this.optAutoConnect = true;
            this.stopMsrReaderListener();
        } else if (PrinterSetting.ACTION_RECONNECT.equals(action)) {
            this.optAutoConnect = true;
        } else if (PrinterSetting.ACTION_DISCONNECT.equals(action)) {
            this.disconnect();
            return true;
        } else if (PrinterSetting.ACTION_START_CONNECTION_LISTENER.equals(action)) {
            this.optAutoConnect = true;
            if(!this.startConnectionListener()) {
                return true;
            }
        } else if (PrinterSetting.ACTION_STOP_CONNECTION_LISTENER.equals(action)) {
            this.optAutoConnect = true;
            this.stopConnectionListener();
        } else {
            this.isValidAction = false;
            this.cbContext.error("Invalid Action");
            Log.d(TAG, "Invalid action : " + action
                    + " passed");
        }

        if (this.isValidAction) {
            this.connect();
        }

        //
        Log.d(TAG, "BixolonPrinterPlugin.execute_END");
        return this.isValidAction;
    }

    /**
     *
     */
    private void connect() {
        Log.d(TAG, "BixolonPrinterPlugin.connect_START");

        if (this.mIsConnected) {
            this.onConnect();
        } else {
            mBixolonPrinter.findBluetoothPrinters();
            //mBixolonPrinter.findNetworkPrinters(3000);
            //mBixolonPrinter.findUsbPrinters();
        }

        Log.d(TAG, "BixolonPrinterPlugin.connect_END");
    }

    private void onConnect() {
        Log.d(TAG, "BixolonPrinterPlugin.onConnect_START");

        this.mIsConnected = true;

        if (PrinterSetting.ACTION_PRINT_TEXT.equals(this.lastActionName)) {
            this.printText();
        } else if (PrinterSetting.ACTION_PRINT_1D_BARCODE.equals(this.lastActionName)) {
            this.printBarCode();
        } else if (PrinterSetting.ACTION_PRINT_QRCODE.equals(this.lastActionName)) {
            this.printQRCode();
        } else if (PrinterSetting.ACTION_CUT_PAPER.equals(this.lastActionName)) {
            this.cutPaper();
        } else if (PrinterSetting.ACTION_GET_STATUS.equals(this.lastActionName)) {
            this.getStatus();
        }

        this.sendConnectionData();

        Log.d(TAG, "BixolonPrinterPlugin.onConnect_END");
    }

    /**
     *
     */
    private void disconnect() {
        Log.d(TAG, "BixolonPrinterPlugin.disconnect_START");
        mBixolonPrinter.disconnect();
        Log.d(TAG, "BixolonPrinterPlugin.disconnect_END");
    }

    private void onDisconnect() {
        Log.d(TAG, "BixolonPrinterPlugin.onDisconnect_START");

        String action = this.lastActionName;
        String error = this.actionError;
        String success = this.actionSuccess;
        JSONObject status = this.mConnectedDeviceStatus;

        if (!this.mIsConnected && this.isValidAction) {
            this.cbContext.error("Connection failed");
            return;
        }

        this.mIsConnected = false;
        this.mConnectedDeviceName = null;
        this.mConnectedDeviceAddress = null;
        this.mConnectedDeviceStatus = null;

        this.actionSuccess = null;
        this.actionError = null;
        this.lastActionArgs = null;
        this.lastActionName = null;

        this.statusStep = PrinterSetting.STATUS_STEP_GET_STATUS;

        if (error != null) {
            Log.d(TAG, "End with error");
            Log.d(TAG, error);
            this.cbContext.error(error);
        } else {
            if (PrinterSetting.ACTION_GET_STATUS.equals(action)) {
                this.cbContext.success(status);
            } else {
                this.cbContext.success(success);
            }

            this.sendConnectionData();
        }

        Log.d(TAG, "BixolonPrinterPlugin.onDisconnect_END");
    }

    private void printQRCode() {
        Log.d(TAG, "BixolonPrinterPlugin.printQRCode_START");

        JSONObject data;
        JSONObject printConfig;
        boolean formFeed;

        try {
            data = this.lastActionArgs.getJSONObject(0);
            printConfig = this.lastActionArgs.getJSONObject(1);
            formFeed = printConfig.getBoolean("formFeed");
        } catch (JSONException e1) {
            this.isValidAction = false;
            this.actionError = "print error: " + e1.getMessage();
            this.disconnect();
            return;
        }

        String text = data.optString("text");
        String align = data.optString("alignment");
        int size = data.optInt("size");
        int model = data.optInt("model");
        int qrcodeAlignment = this.getAlignment(align);

        try {
            Log.d(TAG, "BixolonPrinterPlugin.printQRCode: data: " + text);
            if(formFeed) {
                mBixolonPrinter.printQrCode(text, qrcodeAlignment, model, size, false);
                mBixolonPrinter.formFeed(true);
            } else {
                mBixolonPrinter.printQrCode(text, qrcodeAlignment, model, size, true);
            }
        } catch (Exception e2) {
            this.isValidAction = false;
            this.actionError = "print error: " + e2.getMessage();
            this.disconnect();
            return;
        }

        this.actionSuccess = "print success";

        Log.d(TAG, "BixolonPrinterPlugin.printQRCode_END");
    }

    private void printBarCode() {
        Log.d(TAG, "BixolonPrinterPlugin.printBarCode_START");

        JSONObject data;
        JSONObject printConfig;
        boolean formFeed;

        try {
            data = this.lastActionArgs.getJSONObject(0);
            printConfig = this.lastActionArgs.getJSONObject(1);
            formFeed = printConfig.getBoolean("formFeed");
        } catch (JSONException e1) {
            this.isValidAction = false;
            this.actionError = "print error: " + e1.getMessage();
            this.disconnect();
            return;
        }

        String text = data.optString("text");
        String align = data.optString("alignment");
        int width = data.optInt("width");
        int height = data.optInt("height");

        int barCodeSystem = data.optInt("barcodeSystem");
        int barcodeAlignment = this.getAlignment(align);
        int characterPosition = data.optInt("characterPosition");

        try {
            Log.d(TAG, "BixolonPrinterPlugin.printBarCode: data: " + text);
            if(formFeed) {
                mBixolonPrinter.print1dBarcode(text, barCodeSystem, barcodeAlignment, width, height, characterPosition, false);
                mBixolonPrinter.formFeed(true);
            } else {
                mBixolonPrinter.print1dBarcode(text, barCodeSystem, barcodeAlignment, width, height, characterPosition, true);
            }
        } catch (Exception e2) {
            this.isValidAction = false;
            this.actionError = "print error: " + e2.getMessage();
            this.disconnect();
            return;
        }

        this.actionSuccess = "print success";

        Log.d(TAG, "BixolonPrinterPlugin.printBarCode_END");
    }

    private void printText() {
        Log.d(TAG, "BixolonPrinterPlugin.printText_START");

        String hrBCode = "[hr]";
        int paperWidth = 0;

        JSONArray textLines;
        JSONObject printConfig;
        boolean formFeed;
        int lineFeed;
        int codePage;

        try {
            textLines = this.lastActionArgs.getJSONArray(0);
            printConfig = this.lastActionArgs.getJSONObject(1);
            formFeed = printConfig.getBoolean("formFeed");
            lineFeed = printConfig.getInt("lineFeed");
            codePage = printConfig.getInt("codePage");
        } catch (JSONException e1) {
            this.isValidAction = false;
            this.actionError = "print error: " + e1.getMessage();
            this.disconnect();
            return;
        }

        String text;
        String align;
        String fontType;
        String fontStyle;
        int height;
        int width;

        int textAlignment;
        int textAttribute;
        int textSize;

        if (PrinterSetting.MAX_COL.containsKey(this.mConnectedDeviceName)) {
            paperWidth = PrinterSetting.MAX_COL.get(this.mConnectedDeviceName);
        }

        mBixolonPrinter.setSingleByteFont(codePage);

        JSONObject textLine;
        int arlength = textLines.length();

        for (int i = 0; i < arlength; i++) {
            try {

                Log.d(TAG, "BixolonPrinterPlugin.printText: line:" + (i + 1) + " of " + arlength);

                textLine = textLines.getJSONObject(i);
                text = textLine.optString("text");
                align = textLine.optString("textAlign");
                width = textLine.optInt("textWidth");
                height = textLine.optInt("textHeight");
                fontType = textLine.optString("fontType");
                fontStyle = textLine.optString("fontStyle");

                if (hrBCode.equals(text.substring(0, Math.min(4, text.length())))) {
                    String hrStr = text.substring(text.length() - 1);
                    text = "";
                    for (int j = 0; j < paperWidth; j++) {
                        text += hrStr;
                    }
                    text += "\r\n";
                }

                textAlignment = this.getAlignment(align);
                textAttribute = this.getAttribute(fontType, fontStyle);
                textSize = this.getTextSize(width, height);

                // mBixolonPrinter.printText(text + "\r\n", textAlignment, textAttribute, textSize, false);
                mBixolonPrinter.printText(text, textAlignment, textAttribute, textSize, false);

            } catch (JSONException e2) {
                this.isValidAction = false;
                this.actionError = "print error: " + e2.getMessage();
                this.disconnect();
                return;
            }
        }

        if (formFeed) {
            mBixolonPrinter.formFeed(false);
        } else {
            mBixolonPrinter.lineFeed(lineFeed, false);
        }

        mBixolonPrinter.cutPaper(true);
        mBixolonPrinter.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

        this.actionSuccess = "print success";
        //this.disconnect();

        Log.d(TAG, "BixolonPrinterPlugin.printText_END");
    }

    private void cutPaper() {
        Log.d(TAG, "BixolonPrinterPlugin.cutPaper_START");

        JSONObject printConfig;
        boolean formFeed;
        int lineFeed;

        try {
            printConfig = this.lastActionArgs.getJSONObject(0);
            formFeed = printConfig.getBoolean("formFeed");
            lineFeed = printConfig.getInt("lineFeed");
        } catch (JSONException e1) {
            this.isValidAction = false;
            this.actionError = "cut paper error: " + e1.getMessage();
            this.disconnect();
            return;
        }

        if (formFeed) {
            mBixolonPrinter.formFeed(false);
        } else {
            mBixolonPrinter.lineFeed(lineFeed, false);
        }

        mBixolonPrinter.cutPaper(true);
        mBixolonPrinter.kickOutDrawer(BixolonPrinter.DRAWER_CONNECTOR_PIN5);

        this.actionSuccess = "cut paper success";
        //this.disconnect();

        Log.d(TAG, "BixolonPrinterPlugin.cutPaper_END");
    }

    private void onPrintComplete() {
        Log.d(TAG, "BixolonPrinterPlugin.onPrintComplete_START");
        this.disconnect();
        Log.d(TAG, "BixolonPrinterPlugin.onPrintComplete_END");
    }

    private void getStatus() {
        Log.d(TAG, "BixolonPrinterPlugin.getStatus_START");

        switch (this.statusStep) {
            case PrinterSetting.STATUS_STEP_GET_STATUS:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_STATUS");
                this.mConnectedDeviceStatus = new JSONObject();
                try {
                    this.mConnectedDeviceStatus.put("printerName", this.mConnectedDeviceName);
                    this.mConnectedDeviceStatus.put("printerAddress", this.mConnectedDeviceAddress);
                } catch (JSONException e1) {
                }
                mBixolonPrinter.getStatus();
                return;
            case PrinterSetting.STATUS_STEP_GET_BATTERY_STATUS:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_BATTERY_STATUS");
                mBixolonPrinter.getBatteryStatus();
                return;
            case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_FIRMWARE_VERSION:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_PRINTER_ID_FIRMWARE_VERSION");
                mBixolonPrinter.getPrinterId(BixolonPrinter.PRINTER_ID_FIRMWARE_VERSION);
                return;
            case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_MANUFACTURER:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_PRINTER_ID_MANUFACTURER");
                mBixolonPrinter.getPrinterId(BixolonPrinter.PRINTER_ID_MANUFACTURER);
                return;
            case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_PRINTER_MODEL:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_PRINTER_ID_PRINTER_MODEL");
                mBixolonPrinter.getPrinterId(BixolonPrinter.PRINTER_ID_PRINTER_MODEL);
                return;
            case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_CODE_PAGE:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_PRINTER_ID_CODE_PAGE");
                mBixolonPrinter.getPrinterId(BixolonPrinter.PRINTER_ID_CODE_PAGE);
                return;
            case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_MODEL_ID:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_PRINTER_ID_MODEL_ID");
                mBixolonPrinter.getPrinterId(BixolonPrinter.PRINTER_ID_MODEL_ID);
                return;
            case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_PRODUCT_SERIAL:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_PRINTER_ID_PRODUCT_SERIAL");
                mBixolonPrinter.getPrinterId(BixolonPrinter.PRINTER_ID_PRODUCT_SERIAL);
                return;
            case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_TYPE_ID:
                Log.d(TAG, "BixolonPrinterPlugin.getStatus: STATUS_STEP_GET_PRINTER_ID_TYPE_ID");
                mBixolonPrinter.getPrinterId(BixolonPrinter.PRINTER_ID_TYPE_ID);
                return;

            case PrinterSetting.STATUS_STEP_COMPLETE:
                this.statusStep = PrinterSetting.STATUS_STEP_GET_STATUS;
                break;
        }

        boolean printStatus = false;

        try {
            printStatus = this.lastActionArgs.getBoolean(0);
        } catch (JSONException e1) {
            this.isValidAction = false;
            this.actionError = "get status error: " + e1.getMessage();
            this.disconnect();
            return;
        }

        if (printStatus) {
            String text = "";

            try {
                text = //"Status: " + this.mConnectedDeviceStatus.get("status") + "\n" +
                        "Cover: " + this.mConnectedDeviceStatus.get("cover") + "\n" +
                                "Paper: " + this.mConnectedDeviceStatus.get("paper") + "\n" +
                                "Battery: " + this.mConnectedDeviceStatus.get("battery") + "\n" +
                                "Firmware Version: " + this.mConnectedDeviceStatus.get("firmwareVersion") + "\n" +
                                "Manufacturer: " + this.mConnectedDeviceStatus.get("manufacturer") + "\n" +
                                "Printer Model: " + this.mConnectedDeviceStatus.get("printerModel") + "\n" +
                                "Printer Name: " + this.mConnectedDeviceStatus.get("printerName") + "\n" +
                                "Printer Address: " + this.mConnectedDeviceStatus.get("printerAddress") + "\n" +
                                //"Model ID: " + this.mConnectedDeviceStatus.get("modelId") + "\n" +
                                //"Product Serial: " + this.mConnectedDeviceStatus.get("productSerial") + "\n" +
                                //"Type ID: " + this.mConnectedDeviceStatus.get("typeId") + "\n" +
                                "Code Page: " + this.mConnectedDeviceStatus.get("codePage");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mBixolonPrinter.printText(text + "\r\n", 0, 0, 0, false);
            mBixolonPrinter.lineFeed(3, false);
            mBixolonPrinter.cutPaper(true);
        } else {
            this.disconnect();
        }

        Log.d(TAG, "BixolonPrinterPlugin.getStatus_END");
    }

    private void onMessageRead(Message msg) {
        Log.d(TAG, "BixolonPrinterPlugin.onMessageRead_START: " + msg.arg1);

        boolean isMsrTrackDatas = false;

        switch (msg.arg1) {
            case BixolonPrinter.PROCESS_GET_STATUS:
                if (msg.arg2 == BixolonPrinter.STATUS_NORMAL) {
                    try {
                        //this.mConnectedDeviceStatus.put("status", "NO ERROR");
                        this.mConnectedDeviceStatus.put("cover", "OPENED");
                        this.mConnectedDeviceStatus.put("paper", "FILL");
                    } catch (JSONException e) {
                    }
                } else {
                    if ((msg.arg2 & BixolonPrinter.STATUS_COVER_OPEN) == BixolonPrinter.STATUS_COVER_OPEN) {
                        try {
                            this.mConnectedDeviceStatus.put("cover", "OPENED");
                        } catch (JSONException e) {
                        }
                    } else {
                        try {
                            this.mConnectedDeviceStatus.put("cover", "CLOSED");
                        } catch (JSONException e) {
                        }
                    }

                    if ((msg.arg2 & BixolonPrinter.STATUS_PAPER_NOT_PRESENT) == BixolonPrinter.STATUS_PAPER_NOT_PRESENT) {
                        try {
                            this.mConnectedDeviceStatus.put("paper", "EMPTY");
                        } catch (JSONException e) {
                        }
                    } else {
                        try {
                            this.mConnectedDeviceStatus.put("paper", "FILL");
                        } catch (JSONException e) {
                        }
                    }
                }

                this.statusStep = PrinterSetting.STATUS_STEP_GET_BATTERY_STATUS;
                break;

            case BixolonPrinter.PROCESS_GET_BATTERY_STATUS:
                switch (msg.arg2) {
                    case BixolonPrinter.STATUS_BATTERY_FULL:
                        try {
                            this.mConnectedDeviceStatus.put("battery", "FULL");
                        } catch (JSONException e) {
                        }
                        break;
                    case BixolonPrinter.STATUS_BATTERY_HIGH:
                        try {
                            this.mConnectedDeviceStatus.put("battery", "HIGH");
                        } catch (JSONException e) {
                        }
                        break;
                    case BixolonPrinter.STATUS_BATTERY_MIDDLE:
                        try {
                            this.mConnectedDeviceStatus.put("battery", "MIDDLE");
                        } catch (JSONException e) {
                        }
                        break;
                    case BixolonPrinter.STATUS_BATTERY_LOW:
                        try {
                            this.mConnectedDeviceStatus.put("battery", "LOW");
                        } catch (JSONException e) {
                        }
                        break;
                }

                this.statusStep = PrinterSetting.STATUS_STEP_GET_PRINTER_ID_FIRMWARE_VERSION;
                break;

            case BixolonPrinter.PROCESS_GET_PRINTER_ID:
                Bundle data = msg.getData();
                switch (this.statusStep) {
                    case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_FIRMWARE_VERSION:
                        try {
                            this.mConnectedDeviceStatus.put("firmwareVersion", data.getString(BixolonPrinter.KEY_STRING_PRINTER_ID));
                        } catch (JSONException e) {
                        }

                        this.statusStep = PrinterSetting.STATUS_STEP_GET_PRINTER_ID_MANUFACTURER;
                        break;

                    case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_MANUFACTURER:
                        try {
                            this.mConnectedDeviceStatus.put("manufacturer", data.getString(BixolonPrinter.KEY_STRING_PRINTER_ID));
                        } catch (JSONException e) {
                        }

                        this.statusStep = PrinterSetting.STATUS_STEP_GET_PRINTER_ID_PRINTER_MODEL;
                        break;

                    case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_PRINTER_MODEL:
                        try {
                            this.mConnectedDeviceStatus.put("printerModel", data.getString(BixolonPrinter.KEY_STRING_PRINTER_ID));
                        } catch (JSONException e) {
                        }

                        this.statusStep = PrinterSetting.STATUS_STEP_GET_PRINTER_ID_CODE_PAGE;
                        break;

                    case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_CODE_PAGE:
                        try {
                            this.mConnectedDeviceStatus.put("codePage", data.getString(BixolonPrinter.KEY_STRING_PRINTER_ID));
                        } catch (JSONException e) {
                        }

                        this.statusStep = PrinterSetting.STATUS_STEP_COMPLETE;
                        break;
                    case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_MODEL_ID:
                        try {
                            this.mConnectedDeviceStatus.put("modelId", data.getString(BixolonPrinter.KEY_STRING_PRINTER_ID));
                        } catch (JSONException e) {
                        }

                        this.statusStep = PrinterSetting.STATUS_STEP_GET_PRINTER_ID_PRODUCT_SERIAL;
                        break;
                    case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_PRODUCT_SERIAL:
                        try {
                            this.mConnectedDeviceStatus.put("productSerial", data.getString(BixolonPrinter.KEY_STRING_PRINTER_ID));
                        } catch (JSONException e) {
                        }

                        this.statusStep = PrinterSetting.STATUS_STEP_GET_PRINTER_ID_TYPE_ID;
                        break;
                    case PrinterSetting.STATUS_STEP_GET_PRINTER_ID_TYPE_ID:
                        try {
                            this.mConnectedDeviceStatus.put("typeId", data.getString(BixolonPrinter.KEY_STRING_PRINTER_ID));
                        } catch (JSONException e) {
                        }

                        this.statusStep = PrinterSetting.STATUS_STEP_COMPLETE;
                        break;
                }
                break;

            case BixolonPrinter.PROCESS_MSR_TRACK:
                Bundle bundle = msg.getData();

                isMsrTrackDatas = true;

                sendMsrTrackData(getMsrTrackData(bundle), true);
                break;
        }

        Log.d(TAG, "BixolonPrinterPlugin.onMessageRead_END");

        if(!isMsrTrackDatas)
            this.getStatus();
    }

    private boolean startMsrReaderListener() {
        if(this.msrReaderCallbackContext != null) {
            this.cbContext.error("MSR Reader listener already started.");
            return false;
        }
        this.msrReaderCallbackContext = this.cbContext;
        //this.cbContext.success();
        return true;
    }

    private void sendMsrTrackData(JSONObject obj, boolean keepCallback) {
        if(msrReaderCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(keepCallback);
            this.msrReaderCallbackContext.sendPluginResult(result);
        }
    }

    private void stopMsrReaderListener() {
        this.sendMsrTrackData(new JSONObject(), false);
        this.msrReaderCallbackContext = null;
        //this.cbContext.success();
    }

    private boolean startConnectionListener() {
        if(this.connectionListenerCallbackContext != null) {
            this.cbContext.error(createConnectionData("Connection listener already started."));
            return false;
        }
        this.connectionListenerCallbackContext = this.cbContext;
        //this.cbContext.success(createConnectionData("Connection listener started."));
        return true;
    }

    private JSONObject createConnectionData(String message) {
        JSONObject obj = new JSONObject();

        String msg = "";

        if(message != null)
            msg = message;

        try {
            obj.put("isConnected", this.mIsConnected);
            obj.put("message", message);
        } catch(JSONException e) {
            Log.e(TAG, "BixolonPrinterPlugin.createConnectionData: " + e.getMessage(), e);
        }

        return obj;
    }

    private void sendConnectionData() {
        if(this.connectionListenerCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, createConnectionData(null));
            result.setKeepCallback(true);
            this.connectionListenerCallbackContext.sendPluginResult(result);
        }
    }

    private void stopConnectionListener() {
        this.sendConnectionData();
        this.connectionListenerCallbackContext = null;
        //this.cbContext.success("Connection listener stopped.");
    }

    private JSONObject getMsrTrackData(Bundle bundle) {
        JSONObject obj = new JSONObject();

        try {
            byte[] mTrack1Data = bundle.getByteArray(BixolonPrinter.KEY_STRING_MSR_TRACK1);
            byte[] mTrack2Data = bundle.getByteArray(BixolonPrinter.KEY_STRING_MSR_TRACK2);
            byte[] mTrack3Data = bundle.getByteArray(BixolonPrinter.KEY_STRING_MSR_TRACK3);

            if(mTrack1Data == null) {
                obj.put("msrTrack1", "");
            } else {
                obj.put("msrTrack1", new String(mTrack1Data));
            }

            if(mTrack2Data == null) {
                obj.put("msrTrack2", "");
            } else {
                obj.put("msrTrack2", new String(mTrack2Data));
            }

            if(mTrack3Data == null) {
                obj.put("msrTrack3", "");
            } else {
                obj.put("msrTrack3", new String(mTrack3Data));
            }

        } catch(JSONException e) {
            Log.e(TAG, "BixolonPrinterPlugin.getMsrTrackData: " + e.getMessage(), e);
        }

        return obj;
    }

	/*         METODI ACCESSORI
	 ---------------------------------------*/

    /**
     * @param fontType
     * @param fontStyle
     * @return
     */
    @SuppressLint("DefaultLocale")
    private int getAttribute(String fontType, String fontStyle) {
        // setting attribute
        int attribute = 0;

        if (fontType != null) {
            if (fontType.toUpperCase().equals("A")) {
                attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_A;
            }
            if (fontType.toUpperCase().equals("B")) {
                attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_B;
            }
            if (fontType.toUpperCase().equals("C")) {
                attribute |= BixolonPrinter.TEXT_ATTRIBUTE_FONT_C;
            }
        }

        // TODO add multiple selection
        if (fontStyle != null) {
            if (fontStyle.toUpperCase().equals("UNDERLINE")) {
                attribute |= BixolonPrinter.TEXT_ATTRIBUTE_UNDERLINE1;
            }

            if (fontStyle.toUpperCase().equals("UNDERLINE2")) {
                attribute |= BixolonPrinter.TEXT_ATTRIBUTE_UNDERLINE2;
            }

            if (fontStyle.toUpperCase().equals("BOLD")) {
                attribute |= BixolonPrinter.TEXT_ATTRIBUTE_EMPHASIZED;
            }

            if (fontStyle.toUpperCase().equals("REVERSE")) {
                attribute |= BixolonPrinter.TEXT_ATTRIBUTE_REVERSE;
            }
        }

        return attribute;
    }

    /**
     * @param align
     * @return
     */
    @SuppressLint("DefaultLocale")
    private int getAlignment(String align) {
        int alignment = BixolonPrinter.ALIGNMENT_LEFT;
        if (align != null) {
            if (PrinterSetting.ALIGNMENT_LEFT.equals(align.toUpperCase())) {
                alignment = BixolonPrinter.ALIGNMENT_LEFT;
            } else if (PrinterSetting.ALIGNMENT_CENTER.equals(align.toUpperCase())) {
                alignment = BixolonPrinter.ALIGNMENT_CENTER;
            } else if (PrinterSetting.ALIGNMENT_RIGHT.equals(align.toUpperCase())) {
                alignment = BixolonPrinter.ALIGNMENT_RIGHT;
            }
        }

        return alignment;
    }

    /**
     * @param width
     * @param height
     * @return
     */
    private int getTextSize(int width, int height) {
        int size = 0;

        switch (width) {
            case 0:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL1;
                break;
            case 1:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL2;
                break;
            case 2:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL3;
                break;
            case 3:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL4;
                break;
            case 4:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL5;
                break;
            case 5:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL6;
                break;
            case 6:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL7;
                break;
            case 7:
                size = BixolonPrinter.TEXT_SIZE_HORIZONTAL8;
                break;
        }

        switch (height) {
            case 0:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL1;
                break;
            case 1:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL2;
                break;
            case 2:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL3;
                break;
            case 3:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL4;
                break;
            case 4:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL5;
                break;
            case 5:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL6;
                break;
            case 6:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL7;
                break;
            case 7:
                size |= BixolonPrinter.TEXT_SIZE_VERTICAL8;
                break;
        }

        return size;
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    Log.d(TAG, "perform mHandler: MESSAGE_BLUETOOTH_DEVICE_SET");
                    if (msg.obj == null) {
                        BixolonPrinterPlugin.this.cbContext.error("Printer not found");
                    } else {
                        @SuppressWarnings("unchecked")
                        final Set<BluetoothDevice> pairedDevices = (Set<BluetoothDevice>) msg.obj;
                        final String[] itemsAddr = new String[pairedDevices.size()];
                        final String[] itemsName = new String[pairedDevices.size()];

                        int index = 0;
                        for (BluetoothDevice device : pairedDevices) {
                            itemsAddr[index] = device.getAddress();
                            itemsName[index] = device.getName();
                            index++;
                        }

                        if (BixolonPrinterPlugin.this.optAutoConnect) {
                            mConnectedDeviceAddress = itemsAddr[0];
                            BixolonPrinterPlugin.mBixolonPrinter.connect(itemsAddr[0]);
                        } else {
                            new AlertDialog.Builder(BixolonPrinterPlugin.this.cordova.getActivity())
                                    .setTitle("Available printers")
                                    .setItems(itemsAddr, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            mConnectedDeviceAddress = itemsAddr[which];
                                            BixolonPrinterPlugin.mBixolonPrinter.connect(itemsAddr[which]);
                                        }
                                    })
                                    .show();
                        }

                    }
                    return true;
                // END MESSAGE_BLUETOOTH_DEVICE_SET

                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:
                            Log.d(TAG, "perform mHandler: MESSAGE_STATE_CHANGE STATE_CONNECTED");
                            BixolonPrinterPlugin.this.onConnect();
                            break;

                        case BixolonPrinter.STATE_CONNECTING:
                            Log.d(TAG, "perform mHandler: MESSAGE_STATE_CHANGE STATE_CONNECTING");
                            break;

                        case BixolonPrinter.STATE_NONE:
                            Log.d(TAG, "perform mHandler: MESSAGE_STATE_CHANGE STATE_NONE");
                            // disconnect or connection error
                            BixolonPrinterPlugin.this.onDisconnect();
                            break;
                    }
                    return true;
                // END MESSAGE_STATE_CHANGE

                case BixolonPrinter.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "perform mHandler: MESSAGE_DEVICE_NAME");
                    mConnectedDeviceName = msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME);
                    return true;
                // END MESSAGE_DEVICE_NAME

                case BixolonPrinter.MESSAGE_TOAST:
                    Log.d(TAG, "perform mHandler: MESSAGE_TOAST");
                    if (BixolonPrinterPlugin.this.optToastMessage) {
                        Toast.makeText(BixolonPrinterPlugin.this.cordova.getActivity(), msg.getData().getString(BixolonPrinter.KEY_STRING_TOAST), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                // END MESSAGE_TOAST

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.d(TAG, "perform mHandler: MESSAGE_PRINT_COMPLETE");
                    BixolonPrinterPlugin.this.onPrintComplete();
                    return true;
                // END MESSAGE_PRINT_COMPLETE

                case BixolonPrinter.MESSAGE_READ:
                    Log.d(TAG, "perform mHandler: MESSAGE_READ");
                    BixolonPrinterPlugin.this.onMessageRead(msg);
                    return true;
                // END MESSAGE_PRINT_COMPLETE

            }
            return false;
        }
    });
}
