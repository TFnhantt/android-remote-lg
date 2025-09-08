package com.connectsdk.service.webos;

import android.os.Build;
import android.util.Log;

import com.connectsdk.core.Util;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class WebOSTVMouseSocketClient extends WebSocketClient {

    WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener listener;

    public WebOSTVMouseSocketClient(URI serverUri, WebOSTVMouseSocketConnection.WebOSTVMouseSocketListener listener) {
        super(serverUri);
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d("PtrAndKeyboardFragment", "connected to " + uri.toString());
        if (listener != null) {
            listener.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    @Override
    protected void onSetSSLParameters(SSLParameters sslParameters) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.onSetSSLParameters(sslParameters);
        }
    }

    @Override
    public void connect() {
        super.connect();

        Util.runInBackground(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    //Verify
                    HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                    SSLSocket socket = (SSLSocket) getSocket();
                    SSLSession s = socket.getSession();
                    String host = uri.getHost();
                    if (!hv.verify(host, s)) {
                        try {
                            Log.e("WebOST", "Expected " + host + ", found " + s.getPeerPrincipal());
                            throw new SSLHandshakeException("Expected " + host + ", found " + s.getPeerPrincipal());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i("WebOST", "Success");
                    }
                }
            }
        });
    }
}
