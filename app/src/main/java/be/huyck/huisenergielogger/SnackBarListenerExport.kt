package be.huyck.huisenergielogger

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.provider.AlarmClock


class SnackBarListenerExport(context: Context): View.OnClickListener {

    lateinit var context: Context

    fun SnackBarListenerExport(context: Context) {
        this.context = context
    }

    override fun onClick(v: View) {
        // Code to undo the user's last action

        createAlarm(context,"Alarm voor boe",20,30)

        //val intent = Intent("android.intent.action.VIEW")
        //intent.data = pad
        //if (intent.resolveActivity(PackageManager) != null) {
        //    startActivity(intent)
        //}
    }

    fun createAlarm(context: Context, message: String, hour: Int, minutes: Int) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
        }
        //if (intent.resolveActivity(packageManager) != null) {
            startActivity(context,  intent,null)
        //}
    }
}

