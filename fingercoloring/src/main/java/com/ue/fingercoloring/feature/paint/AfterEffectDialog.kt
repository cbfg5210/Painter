package com.ue.fingercoloring.feature.paint

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ue.fingercoloring.R
import com.ue.fingercoloring.event.OnAddWordsSuccessListener
import com.ue.fingercoloring.event.OnChangeBorderListener
import com.ue.fingercoloring.factory.DialogHelper
import com.ue.fingercoloring.widget.DragedTextView
import com.ue.fingercoloring.widget.TipDialog
import com.ue.library.util.PicassoUtils
import kotlinx.android.synthetic.main.dialog_after_effect.view.*


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
        private val ARG_PICTURE_PATH = "arg_picture_path"

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

        presenter = PaintPresenter(context as AppCompatActivity)
        tipDialog = TipDialog.newInstance()
        mDialogHelper = DialogHelper(context)

        imageUri = arguments.getString(ARG_PICTURE_PATH)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_after_effect, null)

        PicassoUtils.displayImage(context, rootView.current_image, "file://$imageUri")

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

        return AlertDialog.Builder(context)
                .setTitle(R.string.after_effect)
                .setView(rootView)
                .setPositiveButton(R.string.ok) { _, _ -> saveEffectWork() }
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .create()
    }

    private fun saveEffectWork() {
        if (!hasEffectAdded) {
            dismiss()
            return
        }

        tipDialog.showTip(childFragmentManager, getString(R.string.savingimage))

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