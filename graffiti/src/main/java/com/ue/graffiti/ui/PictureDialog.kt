package com.ue.graffiti.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ue.graffiti.R
import com.ue.graffiti.model.PictureItem
import java.util.*

class PictureDialog : DialogFragment() {

    private var mPickPictureListener: OnPickPictureListener? = null

    fun setPickPictureListener(pickPictureListener: OnPickPictureListener) {
        mPickPictureListener = pickPictureListener
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.dialog_picture, null)

        val rvPictures = rootView as RecyclerView
        rvPictures.setHasFixedSize(true)

        val resources = resources
        val pictureNameArray = resources.getStringArray(R.array.pictureNameArray)
        val pictureResArrayTa = resources.obtainTypedArray(R.array.pictureResArray)

        val pictureItems = ArrayList<PictureItem>(pictureNameArray.size)
        var i = 0
        val len = pictureNameArray.size
        while (i < len) {
            pictureItems.add(PictureItem(pictureNameArray[i], pictureResArrayTa.getResourceId(i, -1)))
            i++
        }
        pictureResArrayTa.recycle()

        val adapter = PictureAdapter(pictureItems)
        adapter.setItemClickListener(object : PictureAdapter.OnPictureItemListener {
            override fun onItemClick(position: Int, pictureItem: PictureItem) {
                mPickPictureListener?.onPicturePicked(pictureItem.res)
                dismiss()
            }
        })
        rvPictures.adapter = adapter

        return rootView
    }

    interface OnPickPictureListener {
        fun onPicturePicked(contentId: Int)
    }

    companion object {

        fun newInstance(): PictureDialog {
            val dialog = PictureDialog()
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.GraffitiDialog)
            return dialog
        }
    }
}