package com.ue.coloring.feature.paint

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ue.coloring.R
import com.ue.coloring.event.OnAddWordsSuccessListener
import com.ue.coloring.event.OnChangeBorderListener
import com.ue.coloring.factory.DialogHelper
import com.ue.coloring.widget.DragedTextView
import com.ue.coloring.widget.TipDialog
import com.ue.library.util.ImageLoaderUtils
import kotlinx.android.synthetic.main.co_dialog_after_effect.view.*


/**
 * Created by hawk on 2017/12/28.
 */
class AfterEffectDialog : DialogFragment() {

    private lateinit var rootView: View
    private lateinit var imageUri: String
    private lateinit var mDialogHelper: DialogHelper
    private lateinit var presenter: PaintPresenter
    private lateinit var tipDialog: TipDialog

    private var effectListener: PaintPresenter.OnSaveImageListener? = null
    private var hasEffectAdded = false

    fun setEffectListener(listener: PaintPresenter.OnSaveImageListener) {
        effectListener = listener
    }

    companion object {
        private const val ARG_PICTURE_PATH = "arg_picture_path"

        fun newInstance(picturePath: String): AfterEffectDialog {
            val dialog = AfterEffectDialog()
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)

            val arguments = Bundle()
            arguments.putString(ARG_PICTURE_PATH, picturePath)
            dialog.arguments = arguments
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx = context as AppCompatActivity
        presenter = PaintPresenter(ctx)
        tipDialog = TipDialog.newInstance()
        mDialogHelper = DialogHelper(ctx)
        imageUri = arguments!!.getString(ARG_PICTURE_PATH)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView = LayoutInflater.from(context).inflate(R.layout.co_dialog_after_effect, null)

        ImageLoaderUtils.display(rootView.current_image, "file://$imageUri")

        val listener = View.OnClickListener { v ->
            when (v.id) {
                R.id.addWords ->
                    mDialogHelper.showAddWordsDialog(object : OnAddWordsSuccessListener {
                        override fun addWordsSuccess(dragedTextView: DragedTextView) {
                            (rootView.current_image.parent as ViewGroup).addView(dragedTextView)
                            hasEffectAdded = true
                        }
                    })

                R.id.addBorder ->
                    mDialogHelper.showAddBorderDialog(object : OnChangeBorderListener {
                        override fun changeBorder(drawableId: Int, pt: Int, pd: Int, pl: Int, pr: Int) {
                            if (drawableId != 0) {
                                rootView.border.setBackgroundResource(drawableId)
                                rootView.current_image.setPadding(pl, pt, pr, pd)
                                rootView.current_image.requestLayout()
                                hasEffectAdded = true
                            }
                            rootView.paintview.requestLayout()
                        }
                    })
            }
        }

        rootView.addWords.setOnClickListener(listener)
        rootView.addBorder.setOnClickListener(listener)

        return AlertDialog.Builder(context!!)
                .setTitle(R.string.co_after_effect)
                .setView(rootView)
                .setPositiveButton(R.string.co_ok) { _, _ -> saveEffectWork() }
                .setNegativeButton(R.string.co_cancel, null)
                .setCancelable(false)
                .create()
    }

    private fun saveEffectWork() {
        if (!hasEffectAdded) {
            dismiss()
            return
        }

        tipDialog.showTip(childFragmentManager, getString(R.string.co_saving_image))

        rootView.paintview.buildDrawingCache(true)
        val bitmap = rootView.paintview.getDrawingCache(true).copy(Bitmap.Config.RGB_565, false)
        rootView.paintview.destroyDrawingCache()

        presenter.saveImageLocally(
                bitmap,
                getBorderWorkName(imageUri),
                object : PaintPresenter.OnSaveImageListener {
                    override fun onSaved(path: String) {
                        effectListener?.onSaved(path)
                        dismiss()
                    }
                })
    }

    private fun getBorderWorkName(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1).replace(".png", "_bd.png")
    }
}