package id.umkm.bukutani;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.Manifest;
import android.content.pm.PackageManager;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private WebView web;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER = 101;
    private static final int REQ_LOC = 202;
    private String geoOrigin; private GeolocationPermissions.Callback geoCb;
    private WebView printView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setTextZoom(100);
        s.setGeolocationEnabled(true);
        web.addJavascriptInterface(new Bridge(), "Android");

        // Serve assets at https://appassets.androidplatform.net/assets/ so that localStorage works on a secure origin.
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("appassets.androidplatform.net".equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                } else {
                    geoOrigin = origin; geoCb = callback;
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOC);
                }
            }
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try { startActivityForResult(Intent.createChooser(i, "Pilih berkas cadangan"), FILE_CHOOSER); }
                catch (Exception e) { filePathCallback = null; return false; }
                return true;
            }
        });
        if (savedInstanceState == null) web.loadUrl("https://appassets.androidplatform.net/assets/www/index.html");
        else web.restoreState(savedInstanceState);
    }

    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == FILE_CHOOSER && filePathCallback != null) {
            Uri[] r = (res == Activity.RESULT_OK && data != null && data.getData() != null) ? new Uri[]{data.getData()} : null;
            filePathCallback.onReceiveValue(r);
            filePathCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == REQ_LOC && geoCb != null) {
            boolean ok = false; for (int r : res) if (r == PackageManager.PERMISSION_GRANTED) ok = true;
            geoCb.invoke(geoOrigin, ok, false); geoCb = null;
        }
    }

    @Override
    public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }

    class Bridge {
        // Simpan ke folder Download lalu tawarkan bagikan (WhatsApp, Drive, dsb).
        @JavascriptInterface
        public void saveFile(final String name, final String content, final String mime) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Downloads.DISPLAY_NAME, name);
                        cv.put(MediaStore.Downloads.MIME_TYPE, mime);
                        cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/BukuTani");
                        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                        if (uri != null) { try (OutputStream os = getContentResolver().openOutputStream(uri)) { os.write(bytes); } }
                    } else {
                        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BukuTani");
                        dir.mkdirs();
                        try (FileOutputStream fo = new FileOutputStream(new File(dir, name))) { fo.write(bytes); }
                    }
                    Toast.makeText(MainActivity.this, "Tersimpan di Download/BukuTani/" + name, Toast.LENGTH_LONG).show();
                    // Bagikan salinan
                    File ex = new File(getCacheDir(), "export"); ex.mkdirs();
                    File f = new File(ex, name);
                    try (FileOutputStream fo = new FileOutputStream(f)) { fo.write(bytes); }
                    Uri su = FileProvider.getUriForFile(MainActivity.this, "id.umkm.bukutani.files", f);
                    Intent sh = new Intent(Intent.ACTION_SEND);
                    sh.setType(mime); sh.putExtra(Intent.EXTRA_STREAM, su); sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(sh, "Bagikan " + name));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Gagal menyimpan berkas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        // Cetak laporan HTML ke PDF lewat dialog cetak Android.
        @JavascriptInterface
        public void print(final String html) {
            runOnUiThread(() -> {
                try {
                    printView = new WebView(MainActivity.this);
                    printView.getSettings().setJavaScriptEnabled(false);
                    printView.setWebViewClient(new WebViewClient() {
                        @Override public void onPageFinished(WebView v, String url) {
                            PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                            String job = "Buku Usahatani " + new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(new java.util.Date());
                            pm.print(job, v.createPrintDocumentAdapter(job), new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());
                        }
                    });
                    printView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                    Toast.makeText(MainActivity.this, "Pilih \"Simpan sebagai PDF\" pada dialog cetak", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Gagal mencetak: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        @JavascriptInterface public void toast(final String m) { runOnUiThread(() -> Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show()); }
    }
}
