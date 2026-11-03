package bn.pixelkb

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        root.addView(TextView(this).apply {
            text = "ধাপ ১: কীবোর্ড চালু করুন\nধাপ ২: কীবোর্ড বেছে নিন\nধাপ ৩: নিচের বক্সে টাইপ করে দেখুন"
            textSize = 16f
        })
        root.addView(Button(this).apply {
            text = "১. কীবোর্ড চালু করুন"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "২. কীবোর্ড বেছে নিন"
            setOnClickListener {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
            }
        })
        root.addView(EditText(this).apply { hint = "এখানে টাইপ করুন" })
        setContentView(root)
    }
}
