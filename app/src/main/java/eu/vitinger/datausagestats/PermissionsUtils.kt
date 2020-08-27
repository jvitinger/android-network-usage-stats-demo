package eu.vitinger.datausagestats

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

object PermissionsUtils {

    /**
     * Checks if user granted permission to Usage Stats API.
     *
     * @param context application context
     * @return true if user granted the permission, false if user didn't granted permission
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager =
                context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * Fires the Intent which asks for Usage Stats API permission.
     *
     * @param activity    Current activity.
     * @return True if settings activity was started.
     */
    fun askForUsageStatsPermission(activity: Activity): Boolean {
        try {
            activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            return true
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Unable to open Usage Access Settings.", e)
        }
        return false
    }
}