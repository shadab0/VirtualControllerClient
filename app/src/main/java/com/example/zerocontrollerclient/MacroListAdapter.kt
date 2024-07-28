package com.example.zerocontrollerclient

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MacroListAdapter(val context: Context, private val items: ArrayList<List<Any>>) : RecyclerView.Adapter<MacroListAdapter.ViewHolder>() {
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imageView = v.findViewById<ImageView>(R.id.item_image)
        val textView = v.findViewById<TextView>(R.id.item_text)
        val deleteButton = v.findViewById<ImageButton>(R.id.item_delete).setOnClickListener {
            items.removeAt(adapterPosition)
            notifyItemRemoved(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.macro_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = items[position][0] as Int
        val text = items[position][1] as String
        val data = items[position][2] as String
        val isDelay = items[position][3] as Boolean
        holder.imageView.setImageResource(id)
        if (id != R.mipmap.key_circular_button) {
            holder.imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
        holder.textView.text = text
        if (text.contains("Cancel")) {
            holder.textView.setTextColor(Color.RED)
            items[position] = listOf(id,text,data.replace("Cancel","Up"),isDelay)
        }
        if(isDelay) {
            holder.textView.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder.setTitle("Enter Delay:")
                val parentLayout = LinearLayout(context)
                val editText = EditText(context)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(50,0,50,0)
                editText.layoutParams = layoutParams
                editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                parentLayout.addView(editText)
                dialogBuilder.setView(parentLayout)

                dialogBuilder.setPositiveButton("OK") { _, _ ->
                    val sec = editText.text.toString().trim()
                    holder.textView.text = "${sec}ms"
                    items[position] = listOf(id,"${sec}ms","${sec}ms",true)
                }

                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = dialogBuilder.create()
                editText.requestFocus()
                dialog.show()
                dialog.window?.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)}
                dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_background)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}