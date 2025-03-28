package com.example.moodsync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;

public class NetworkUtils {

    /**
     * Checks basic network connectivity (device has an active network interface)
     * Note: Doesn't guarantee internet access, just network availability
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    /**
     * Advanced check that actually tests internet connectivity by pinging a reliable server
     * Should be run on background thread to avoid NetworkOnMainThreadException
     */
    public static boolean isInternetAvailable() {
        try {
            // Using Google's public DNS server to check connectivity
            Process process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8");
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Combined check that first verifies network then internet connectivity
     */
    public static boolean isConnected(Context context) {
        return isNetworkAvailable(context) && isInternetAvailable();
    }

    // Optional: Continuous monitoring with callback
    public static void registerNetworkCallback(Context context, ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        cm.registerNetworkCallback(request, callback);
    }
}
