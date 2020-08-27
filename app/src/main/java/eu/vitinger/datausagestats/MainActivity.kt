package eu.vitinger.datausagestats

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_item.view.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkPermission()) {
            return
        }

        recycler.layoutManager = LinearLayoutManager(this)

        launch {
            val apps = mutableListOf<AppDataUsage>()

            withContext(Dispatchers.Default) {
                for (appInfo in packageManager.getInstalledApplications(0)) {
                    val appDataUsage = AppDataUsage(
                        appInfo.name ?: appInfo.packageName,
                        getDataUsage(ConnectivityManager.TYPE_WIFI, appInfo.uid),
                        getDataUsage(ConnectivityManager.TYPE_MOBILE, appInfo.uid)
                    )
                    apps.add(appDataUsage)
                    Log.d(localClassName, "Processed $appDataUsage")
                }
                apps.sortByDescending { it.wifiBytes + it.mobileBytes }
            }
            recycler.adapter = AppsAdapter(apps)
            progress.visibility = View.GONE
        }
    }

    private fun checkPermission(): Boolean {
        if (!PermissionsUtils.hasUsageStatsPermission(this)) {
            PermissionsUtils.askForUsageStatsPermission(this)
            return false
        }
        return true
    }

    private fun getDataUsage(networkType: Int, uid: Int): Long {
        return getBytes(getNetworkStats(networkType, uid))
    }

    private fun getNetworkStats(networkType: Int, uid: Int): NetworkStats? {
        return (getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager).queryDetailsForUid(
            networkType,
            "",
            0,
            System.currentTimeMillis(),
            uid
        )
    }

    private fun getBytes(networkStats: NetworkStats?): Long {
        if (networkStats != null) {
            val bucket = NetworkStats.Bucket()
            networkStats.getNextBucket(bucket)
            return bucket.rxBytes + bucket.txBytes
        }
        return 0
    }

    class AppsAdapter(private val apps: List<AppDataUsage>) :
        RecyclerView.Adapter<AppsAdapter.AppItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            AppItemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
            )

        override fun getItemCount() = apps.size

        override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
            apps[position].let { app ->
                holder.packageName.text = app.packageName
                holder.wifi.text = ConvertUtils.getSizeWithUnit(app.wifiBytes)
                holder.mobile.text = ConvertUtils.getSizeWithUnit(app.mobileBytes)
            }
        }

        class AppItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val packageName: TextView = view.packageName
            val wifi: TextView = view.wifi
            val mobile: TextView = view.mobile
        }
    }

    data class AppDataUsage(
        val packageName: String,
        val wifiBytes: Long,
        val mobileBytes: Long
    )
}