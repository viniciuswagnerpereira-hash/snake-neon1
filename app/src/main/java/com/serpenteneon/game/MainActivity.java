package com.serpenteneon.game;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class MainActivity extends Activity {
    private static final String TAG = "ArenaMonstrinhos";
    private static final String APP_URL =
        "https://appassets.androidplatform.net/assets/index.html";
    private static final String HISTORY_FILE = "historico.json";

    private WebView webView;
    private boolean smokeTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        smokeTest = getIntent().getBooleanExtra("smoke_test", false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();

        webView = new WebView(this);
        webView.setBackgroundColor(0xFF111827);
        webView.addJavascriptInterface(new HistoryBridge(), "HistoricoAndroid");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(
                WebView view,
                WebResourceRequest request
            ) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            @SuppressWarnings("deprecation")
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(Uri.parse(url));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "PAGE_READY url=" + url);

                view.postDelayed(() -> {
                    view.evaluateJavascript(
                        "(function(){return window.appReady===true && " +
                        "!!document.getElementById('monsterGrid') && " +
                        "document.querySelectorAll('.monster-card').length===16 && " +
                        "getComputedStyle(document.querySelector('.monster-art')).backgroundImage.includes('monster-sprites.svg');})()",
                        value -> Log.i(TAG, "DOM_READY=" + value)
                    );
                }, 900);

                if (smokeTest) {
                    view.postDelayed(() -> {
                        view.evaluateJavascript(
                            "(function(){" +
                            "if(window.appReady!==true)return false;" +
                            "var n=document.getElementById('trainerName');" +
                            "var card=document.querySelector('.monster-card');" +
                            "var start=document.getElementById('startBattleBtn');" +
                            "if(!n||!card||!start)return false;" +
                            "n.value='CI_SMOKE';" +
                            "n.dispatchEvent(new Event('input',{bubbles:true}));" +
                            "card.click();" +
                            "start.click();" +
                            "return document.getElementById('battleScreen').classList.contains('active') && " +
                            "document.querySelectorAll('.attack').length===2 && " +
                            "getComputedStyle(document.getElementById('playerArt')).backgroundImage.includes('monster-sprites.svg');" +
                            "})()",
                            value -> Log.i(TAG, "GAME_START=" + value)
                        );
                    }, 1400);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    Log.e(TAG, "JS_ERROR=" + message.message() +
                        " @" + message.sourceId() + ":" + message.lineNumber());
                } else {
                    Log.d(TAG, "JS=" + message.message());
                }
                return true;
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        setContentView(webView);
        webView.post(this::hideSystemUi);
        webView.loadUrl(APP_URL);
    }

    private final class HistoryBridge {
        @JavascriptInterface
        public String carregar() {
            try {
                File arquivo = garantirHistorico();
                FileInputStream input = new FileInputStream(arquivo);
                byte[] dados = new byte[(int) arquivo.length()];
                int lidos = input.read(dados);
                input.close();
                if (lidos <= 0) return "{\"batalhas\":[]}";
                return new String(dados, 0, lidos, StandardCharsets.UTF_8);
            } catch (Exception e) {
                Log.e(TAG, "HISTORY_READ_ERROR", e);
                return "{\"batalhas\":[]}";
            }
        }

        @JavascriptInterface
        public boolean salvar(String json) {
            try {
                // Valida que o texto recebido é JSON antes de gravar historico.json.
                new JSONObject(json);
                File arquivo = new File(getFilesDir(), HISTORY_FILE);
                FileOutputStream output = new FileOutputStream(arquivo, false);
                output.write(json.getBytes(StandardCharsets.UTF_8));
                output.flush();
                output.close();
                Log.i(TAG, "HISTORY_SAVED path=" + arquivo.getAbsolutePath());
                return true;
            } catch (Exception e) {
                Log.e(TAG, "HISTORY_WRITE_ERROR", e);
                return false;
            }
        }
    }

    private File garantirHistorico() throws Exception {
        File destino = new File(getFilesDir(), HISTORY_FILE);
        if (destino.exists()) return destino;

        InputStream origem = getAssets().open(HISTORY_FILE);
        FileOutputStream output = new FileOutputStream(destino);
        byte[] buffer = new byte[4096];
        int lidos;
        while ((lidos = origem.read(buffer)) != -1) {
            output.write(buffer, 0, lidos);
        }
        output.flush();
        output.close();
        origem.close();
        return destino;
    }

    private void hideSystemUi() {
        Window window = getWindow();
        View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller == null) {
                decorView.post(this::hideSystemUi);
                return;
            }
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
