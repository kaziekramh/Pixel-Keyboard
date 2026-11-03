package bn.pixelkb

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject

class KeyboardService : InputMethodService() {
    private lateinit var web: WebView
    private lateinit var cm: ClipboardManager
    private var ready = false
    @Volatile private var last = 0L

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val t = clipText()
        if (t.isNotEmpty()) js("addClip(${JSONObject.quote(t)})")
    }

    override fun onCreate() {
        super.onCreate()
        cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.addPrimaryClipChangedListener(clipListener)
    }

    override fun onDestroy() {
        cm.removePrimaryClipChangedListener(clipListener)
        super.onDestroy()
    }

    override fun onEvaluateFullscreenMode() = false

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    override fun onCreateInputView(): View {
        ready = false
        web = WebView(this).apply {
            // কীবোর্ডের উচ্চতা: Gboard-এর মতো ২৯১dp
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (291 * resources.displayMetrics.density).toInt()
            )
            setBackgroundColor(Color.BLACK)
            overScrollMode = View.OVER_SCROLL_NEVER
            isLongClickable = false
            setOnLongClickListener { true }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            addJavascriptInterface(Bridge(), "Android")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    ready = true
                    syncJs(true)
                }
            }
            loadUrl("file:///android_asset/keyboard.html")
        }
        return web
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        syncJs(true)
    }

    // কার্সর বাইরে থেকে সরে গেলে JS-এর কম্পোজিশন রিসেট হবে
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        if (SystemClock.uptimeMillis() - last > 250) syncJs(false)
    }

    private fun js(code: String) {
        if (::web.isInitialized) web.post { web.evaluateJavascript(code, null) }
    }

    private fun syncJs(force: Boolean) {
        if (!ready) return
        val before = currentInputConnection?.getTextBeforeCursor(50, 0)?.toString() ?: ""
        js("sync(${JSONObject.quote(before)},$force)")
    }

    private fun clipText(): String = try {
        cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString() ?: ""
    } catch (e: Exception) {
        ""
    }

    private fun pressEnter() {
        val ei = currentInputEditorInfo
        val action = ei.imeOptions and EditorInfo.IME_MASK_ACTION
        val multiLine = (ei.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        val noAction = (ei.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (!multiLine && !noAction &&
            action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED
        ) {
            currentInputConnection?.performEditorAction(action)
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    inner class Bridge {
        /** d অক্ষর কার্সরের আগে থেকে মুছে t বসায় */
        @JavascriptInterface
        fun put(d: Int, t: String) {
            last = SystemClock.uptimeMillis()
            val ic = currentInputConnection ?: return
            if (t == "\n") { pressEnter(); return }
            ic.beginBatchEdit()
            val hasSel = !ic.getSelectedText(0).isNullOrEmpty()
            if (!hasSel && d > 0) ic.deleteSurroundingText(d, 0)
            if (t.isNotEmpty() || hasSel) ic.commitText(t, 1)
            ic.endBatchEdit()
        }

        @JavascriptInterface
        fun sel(): String {
            val s = currentInputConnection?.getSelectedText(0)?.toString() ?: ""
            if (s.isNotEmpty()) cm.setPrimaryClip(ClipData.newPlainText("text", s))
            return s
        }

        @JavascriptInterface
        fun clip(): String = clipText()

        @JavascriptInterface
        fun v() {
            web.post { web.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
        }
    }
}
