package com.example.yamigo.websocketclient;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class RealDataWebSocketClient extends WebSocketClient {
    private static final String TAG = "RealDataWebSocketClient";

    public RealDataWebSocketClient(URI serverUri) {
        super(serverUri, new Draft_6455());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "onOpen()");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage()");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose()");
    }

    @Override
    public void onError(Exception ex) {
        Log.d(TAG, "onError()");
    }
}
