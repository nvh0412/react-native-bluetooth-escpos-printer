package cn.jystudio.bluetooth.tsc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import cn.jystudio.bluetooth.BluetoothService;
import cn.jystudio.bluetooth.BluetoothServiceStateObserver;
import com.facebook.react.bridge.*;
import com.nyear.lib.sdk.bean.NyearElementBean;

import java.util.Map;
import java.util.Vector;

/**
 * Created by januslo on 2018/9/22.
 */
public class RNBluetoothTscPrinterModule extends ReactContextBaseJavaModule
implements BluetoothServiceStateObserver{
    private static final String TAG="BluetoothTscPrinter";
    private BluetoothService mService;

    public RNBluetoothTscPrinterModule(ReactApplicationContext reactContext,BluetoothService bluetoothService) {
        super(reactContext);
        this.mService = bluetoothService;
        this.mService.addStateObserver(this);
    }

    @Override
    public String getName() {
        return "BluetoothTscPrinter";
    }

    @ReactMethod
    public void printLabel(final ReadableMap options, final Promise promise) {
        int width = options.getInt("width");
        int height = options.getInt("height");

        ReadableArray texts = options.hasKey("text")? options.getArray("text"):null;

        TscCommand tsc = new TscCommand();

        tsc.addSize(width,height);
        tsc.addCls();

        for (int i = 0;texts!=null&& i < texts.size(); i++) {
            final NyearElementBean bean = new NyearElementBean();
            ReadableMap text = texts.getMap(i);
            String t = text.getString("text");

            int x = text.getInt("x");
            int y = text.getInt("y");

            bean.setXBit(x);
            bean.setYBit(y);

            boolean bold = text.hasKey("bold") && text.getBoolean("bold");

            try {
                byte[] temp = t.getBytes("UTF-8");
                String temStr = new String(temp, "UTF-8");
                t = new String(temStr.getBytes("UTF-8"), "UTF-8");

                bean.setContent(t);
            } catch (Exception e) {
                promise.reject("INVALID_TEXT", e);
                return;
            }

            if(bold){
                bean.setBold(true);
            }
        }

        promise.resolve(null);
    }

    private TscCommand.BARCODETYPE findBarcodeType(String type) {
        TscCommand.BARCODETYPE barcodeType = TscCommand.BARCODETYPE.CODE128;
        for (TscCommand.BARCODETYPE t : TscCommand.BARCODETYPE.values()) {
            if ((""+t.getValue()).equalsIgnoreCase(type)) {
                barcodeType = t;
                break;
            }
        }
        return barcodeType;
    }

    private TscCommand.READABLE findReadable(int readable) {
        TscCommand.READABLE ea = TscCommand.READABLE.EANBLE;
        if (TscCommand.READABLE.DISABLE.getValue() == readable) {
            ea = TscCommand.READABLE.DISABLE;
        }
        return ea;
    }

    private TscCommand.FONTMUL findFontMul(int scan) {
        TscCommand.FONTMUL mul = TscCommand.FONTMUL.MUL_1;
        for (TscCommand.FONTMUL m : TscCommand.FONTMUL.values()) {
            if (m.getValue() == scan) {
                mul = m;
                break;
            }
        }
        return mul;
    }

    private TscCommand.ROTATION findRotation(int rotation) {
        TscCommand.ROTATION rt = TscCommand.ROTATION.ROTATION_0;
        for (TscCommand.ROTATION r : TscCommand.ROTATION.values()) {
            if (r.getValue() == rotation) {
                rt = r;
                break;
            }
        }
        return rt;
    }

    private TscCommand.FONTTYPE findFontType(String fonttype) {
        TscCommand.FONTTYPE ft = TscCommand.FONTTYPE.FONT_CHINESE;
        for (TscCommand.FONTTYPE f : TscCommand.FONTTYPE.values()) {
            if ((""+f.getValue()).equalsIgnoreCase(fonttype)) {
                ft = f;
                break;
            }
        }
        return ft;
    }


    private TscCommand.SPEED findSpeed(int speed){
        TscCommand.SPEED sd = null;
        switch(speed){
            case 1:
                sd = TscCommand.SPEED.SPEED1DIV5;
                break;
            case 2:
                sd = TscCommand.SPEED.SPEED2;
                break;
            case 3:
                sd = TscCommand.SPEED.SPEED3;
                break;
            case 4:
                sd = TscCommand.SPEED.SPEED4;
                break;
        }
        return sd;
    }

    private TscCommand.EEC findEEC(String level) {
        TscCommand.EEC eec = TscCommand.EEC.LEVEL_L;
        for (TscCommand.EEC e : TscCommand.EEC.values()) {
            if (e.getValue().equalsIgnoreCase(level)) {
                eec = e;
                break;
            }
        }
        return eec;
    }

    private TscCommand.DENSITY findDensity(int density){
        TscCommand.DENSITY ds = null;
        for (TscCommand.DENSITY d : TscCommand.DENSITY.values()) {
            if (d.getValue() == density) {
                ds = d;
                break;
            }
        }
        return ds;
    }
    private TscCommand.BITMAP_MODE findBitmapMode(int mode){
        TscCommand.BITMAP_MODE bm = TscCommand.BITMAP_MODE.OVERWRITE;
        for (TscCommand.BITMAP_MODE m : TscCommand.BITMAP_MODE.values()) {
            if (m.getValue() == mode) {
                bm = m;
                break;
            }
        }
        return bm;
    }

    private boolean sendDataByte(byte[] data) {
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            return false;
        }
        mService.write(data);
        return true;
    }

    @Override
    public void onBluetoothServiceStateChanged(int state, Map<String, Object> boundle) {

    }
}
