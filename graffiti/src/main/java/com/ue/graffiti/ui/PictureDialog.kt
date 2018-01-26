package com.ue.graffiti.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ue.graffiti.R
import com.ue.graffiti.model.PictureItem
import com.ue.library.util.getXmlImageArray
import java.util.*

class PictureDialog : DialogFragment() {
    var pickPictureListener: OnPickPictureListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.gr_dialog_picture, null)

        val rvPictures = rootView as RecyclerView
        rvPictures.setHasFixedSize(true)

        val pictureNameArray = resources.getStringArray(R.array.gr_pictureNameArray)
        val pictureResArray = context.getXmlImageArray(R.array.gr_pictureResArray)

        val pictureItems = ArrayList<PictureItem>(pictureNameArray.size)
        pictureNameArray.indices.mapTo(pictureItems) { PictureItem(pictureNameArray[it], pictureResArray[it]) }

        rvPictures.adapter = PictureAdapter(pictureItems).apply {
            itemClickListener = object : PictureAdapter.OnPictureItemListener {
                override fun onItemClick(position: Int, pictureItem: PictureItem) {
                    pickPictureListener?.onPicturePicked(pictureItem.res)
                    dismiss()
                }
            }
        }

        return rootView
    }

    interface OnPickPictureListener {
        fun onPicturePicked(contentId: Int)
    }

    companion object {
        fun newInstance(): PictureDialog {
            return PictureDialog().apply { setStyle(DialogFragment.STYLE_NO_TITLE, R.style.gr_GraffitiDialog) }
        }
    }
}