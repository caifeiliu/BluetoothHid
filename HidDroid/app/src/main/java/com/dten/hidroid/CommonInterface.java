package com.dten.hidroid;

public class CommonInterface {
    public interface ConnectionStateChangeListener {
        void onConnecting();
        void onConnected();
        void onDisConnected();
    }
}
