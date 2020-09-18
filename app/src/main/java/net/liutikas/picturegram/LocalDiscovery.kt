package net.liutikas.picturegram

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.core.content.ContextCompat.startActivity


fun setupLocalDiscovery(context: Context, discovered: (NsdServiceInfo) -> Unit) {
    val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            println("failed $serviceType $errorCode ")
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
        }

        override fun onDiscoveryStarted(serviceType: String?) {
        }

        override fun onDiscoveryStopped(serviceType: String?) {
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            if (serviceInfo == null) return
            discovered(serviceInfo)
            println("Service discovered: " + serviceInfo.getServiceName() + " host:" + serviceInfo.getHost() + " port:"
                    + serviceInfo.getPort() + " type:" + serviceInfo.getServiceType());
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
        }
    }
    val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    nsdManager.discoverServices(
            "_http._tcp.",
            NsdManager.PROTOCOL_DNS_SD,
            discoveryListener)
}

fun resolveService(context: Context, serviceInfo: NsdServiceInfo?) {
    val discoveryListener = object: NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            println("Service resolve FAILED $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            if (serviceInfo == null) return
            println("Service resolved: " + serviceInfo.getServiceName() + " host:" + serviceInfo.getHost() + " port:"
                    + serviceInfo.getPort() + " type:" + serviceInfo.getServiceType());
            val url = "http://${serviceInfo.host.hostAddress}"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            context.startActivity(i)
        }

    }
    val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    nsdManager.resolveService(serviceInfo, discoveryListener)
}