package autodraw

import android.app.Application
import com.ue.library.util.SPUtils

/**
 * Created by hawk on 2018/1/12.
 */
class DebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SPUtils.init(this)
    }
}