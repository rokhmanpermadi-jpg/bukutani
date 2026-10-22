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
import android.webkit.JavascriptInterface;
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
        @JavascriptInterface public void toast(final String m) { runOnUiThread(() -> Toast.makeText(MainActivity.this, m, Toast.LENGTH_SHORT).show()); }
    }
}
