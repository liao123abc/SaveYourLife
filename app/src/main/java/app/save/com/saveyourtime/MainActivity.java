package app.save.com.saveyourtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.InputStream;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    public final static int WHITE = 0xFFFFFFFF;
    public final static int BLACK = 0xFF000000;
    public final static int WIDTH = 400;
    public final static int HEIGHT = 400;
    private String STR ;

    private ImageView imageView;
    private EditText phoneEditText;
    //8002|phone|timestamp-后面三位用0来替换|440106B008

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnClickListener((l) -> {
            showQCCode();
        });

        phoneEditText = (EditText)findViewById(R.id.editText);
        String phoneNum = getPhone();
        if (phoneNum != null && !phoneNum.equals("")) {
            phoneEditText.setText(phoneNum);
        }
        showQCCode();

    }

    private void showQCCode() {
        String phone = phoneEditText.getText().toString();
        if (phone == null || phone.equals("")) {
            Toast.makeText(this, "Plz input phone number", Toast.LENGTH_SHORT).show();
        } else {
            setBrightness(255);
            saveToSharePreference(phone);
            setBarcode(phone);
        }
    }

    private void saveToSharePreference(String phone) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.my_app_share_preference_key), phone);
        editor.commit();
    }

    /**
     *
     * @return
     */
    private String getPhone() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "";
        String phone = sharedPref.getString(getString(R.string.my_app_share_preference_key), defaultValue);
        return phone;
    }

    private void setBarcode(String phone) {
        Long tsLong = System.currentTimeMillis();
        String ts = tsLong.toString();
        STR = ts.substring(0, ts.length() - 3) + "000";
        //STR = "8002|phone|timestamp-后面三位用0来替换|440106B008";
        STR = "8002|" + phone +"|" + STR + "|" + "440106B008";
        Log.d("loudaer", STR);

        BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

        int wh = dip2px(this, 200);
        Bitmap bitmap = encodeAsBitmap(STR, barcodeFormat, wh, wh);
        Bitmap logbmp= BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        bitmap = addLogo(bitmap, logbmp);
        imageView.setImageBitmap(bitmap);
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    protected static Bitmap encodeAsBitmap(String contents,
                                           BarcodeFormat format, int desiredWidth, int desiredHeight) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();

        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 0);
        try {
            result = writer.encode(contents, format, desiredWidth,
                    desiredHeight, hints);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    private void setBrightness(int brightValue) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float f = brightValue / 255.0F;
        lp.screenBrightness = f;
        getWindow().setAttributes(lp);
    }
}
