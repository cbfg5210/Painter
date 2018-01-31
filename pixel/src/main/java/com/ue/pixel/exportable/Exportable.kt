package com.ue.pixel.exportable

import android.content.Context

import com.ue.pixel.widget.PxerView

/**
 * Created by BennyKok on 10/17/2016.
 */

abstract class Exportable {
    abstract fun runExport(context: Context, name: String, pxerView: PxerView)
}
