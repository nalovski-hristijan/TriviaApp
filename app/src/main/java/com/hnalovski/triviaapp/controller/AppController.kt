package com.hnalovski.triviaapp.controller

import android.app.Application
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class AppController : Application() {
    var requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                field = Volley.newRequestQueue(applicationContext)
            }
            return field
        }
        private set


    fun <T> addToRequestQueue(req: Request<T?>?) {
        this.requestQueue!!.add<T?>(req)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @get:Synchronized
        var instance: AppController? = null
            private set
    }
}
