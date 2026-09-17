package com.hbc.telegramgifcleaner

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.drinkless.tdlib.JsonClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : Activity() {
    private lateinit var content: LinearLayout
    private lateinit var statusView: TextView

    private var clientId = 0
    private var apiId = 0
    private var apiHash = ""
    private val receiverStarted = AtomicBoolean(false)

    private val savedAnimationIds = mutableListOf<Int>()
    private var countView: TextView? = null
    private var deleteButton: Button? = null
    private var refreshButton: Button? = null
    private var logoutButton: Button? = null
    private var progressBar: ProgressBar? = null
    private var progressText: TextView? = null

    private var deleting = false
    private var deleteIndex = 0
    private var deletedCount = 0
    private var failedCount = 0
    private var loggingOut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(23, 33, 43)
        window.navigationBarColor = Color.rgb(23, 33, 43)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL

        val scroll = ScrollView(this).apply {
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            isFillViewport = true
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(22), dp(28), dp(22), dp(28))
        }
        scroll.addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(scroll)

        startReceiverOnce()
        showCredentialsScreen()
    }

    private fun startReceiverOnce() {
        if (!receiverStarted.compareAndSet(false, true)) return
        Thread({
            while (true) {
                try {
                    val message = JsonClient.receive(1.0) ?: continue
                    handleIncoming(message)
                } catch (_: Throwable) {
                    // Never log credentials or TDLib payloads. UI receives only a generic local error.
                    runOnUiThread { setStatus("حدث خطأ داخلي في TDLib. أعد فتح التطبيق إذا استمر الخطأ.", true) }
                }
            }
        }, "tdlib-receiver").apply {
            isDaemon = true
            start()
        }
    }

    private fun showCredentialsScreen() = runOnUiThread {
        deleting = false
        loggingOut = false
        savedAnimationIds.clear()
        clearScreen()
        addTitle("Telegram GIF Cleaner")
        addParagraph("يحذف ملفات GIF من قائمة Saved GIFs في حساب Telegram نفسه، وليس من ذاكرة التخزين المؤقت للهاتف.")
        addParagraph("أدخل API ID و API Hash الخاصين بك. تبقى القيم في ذاكرة التطبيق فقط أثناء التشغيل ولا يتم حفظ API Hash على الجهاز أو إرساله لأي جهة غير Telegram عبر TDLib.")

        val apiIdInput = addInput("API ID", InputType.TYPE_CLASS_NUMBER)
        val apiHashInput = addInput("API Hash", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addButton("بدء تسجيل الدخول") {
            val id = apiIdInput.text.toString().trim().toIntOrNull()
            val hash = apiHashInput.text.toString().trim()
            if (id == null || id <= 0 || hash.length < 16) {
                toast("تحقق من API ID و API Hash")
                return@addButton
            }
            apiId = id
            apiHash = hash
            apiHashInput.text?.clear()
            initializeTdlib()
        }
        statusView = addStatus()
        setStatus("جاهز للبدء")
    }

    private fun initializeTdlib() {
        setStatus("جاري تهيئة اتصال Telegram الرسمي…")
        try {
            JsonClient.setLogMessageHandler(0) { _, _ -> /* Intentionally discard TDLib logs */ }
            JsonClient.execute(JSONObject().put("@type", "setLogVerbosityLevel").put("new_verbosity_level", 0).toString())
            clientId = JsonClient.createClientId()
            JsonClient.send(clientId, JSONObject().put("@type", "getAuthorizationState").toString())
        } catch (_: Throwable) {
            setStatus("تعذر تحميل TDLib على هذا الجهاز.", true)
        }
    }

    private fun handleIncoming(raw: String) {
        val obj = try { JSONObject(raw) } catch (_: Throwable) { return }
        val type = obj.optString("@type")
        val extra = obj.opt("@extra")?.toString().orEmpty()

        when (type) {
            "updateAuthorizationState" -> handleAuthorizationState(obj.optJSONObject("authorization_state"))
            "authorizationStateWaitTdlibParameters",
            "authorizationStateWaitPhoneNumber",
            "authorizationStateWaitCode",
            "authorizationStateWaitPassword",
            "authorizationStateWaitEmailAddress",
            "authorizationStateWaitEmailCode",
            "authorizationStateWaitOtherDeviceConfirmation",
            "authorizationStateWaitRegistration",
            "authorizationStateReady",
            "authorizationStateLoggingOut",
            "authorizationStateClosing",
            "authorizationStateClosed" -> handleAuthorizationState(obj)
            "animations" -> handleAnimations(obj, extra)
            "ok" -> handleOk(extra)
            "error" -> handleError(obj, extra)
            "updateSavedAnimations" -> if (!deleting) loadSavedAnimations()
        }
    }

    private fun handleAuthorizationState(state: JSONObject?) {
        if (state == null) return
        when (state.optString("@type")) {
            "authorizationStateWaitTdlibParameters" -> sendTdlibParameters()
            "authorizationStateWaitPhoneNumber" -> showPhoneScreen()
            "authorizationStateWaitCode" -> showCodeScreen()
            "authorizationStateWaitPassword" -> showPasswordScreen()
            "authorizationStateWaitEmailAddress" -> showEmailScreen()
            "authorizationStateWaitEmailCode" -> showEmailCodeScreen()
            "authorizationStateWaitOtherDeviceConfirmation" -> showOtherDeviceScreen(state.optString("link"))
            "authorizationStateWaitRegistration" -> runOnUiThread {
                setStatus("هذا الحساب يحتاج إلى تسجيل مستخدم جديد. استخدم تطبيق Telegram الرسمي لإنشاء الحساب أولًا ثم أعد المحاولة.", true)
            }
            "authorizationStateReady" -> showMainScreen()
            "authorizationStateLoggingOut" -> runOnUiThread { setStatus("جاري تسجيل الخروج وحذف الجلسة المحلية…") }
            "authorizationStateClosing" -> Unit
            "authorizationStateClosed" -> {
                if (loggingOut) {
                    deleteLocalSession()
                    apiId = 0
                    apiHash = ""
                    clientId = 0
                    showCredentialsScreen()
                }
            }
        }
    }

    private fun sendTdlibParameters() {
        val dbDir = File(filesDir, "tdlib").apply { mkdirs() }.absolutePath
        val params = JSONObject()
            .put("@type", "setTdlibParameters")
            .put("database_directory", dbDir)
            .put("use_message_database", false)
            .put("use_secret_chats", false)
            .put("api_id", apiId)
            .put("api_hash", apiHash)
            .put("system_language_code", "ar")
            .put("device_model", Build.MODEL ?: "Android")
            .put("application_version", "1.0.0")
            .put("@extra", "auth_parameters")
        send(params)
    }

    private fun showPhoneScreen() = runOnUiThread {
        clearScreen()
        addTitle("تسجيل الدخول إلى Telegram")
        addParagraph("أدخل رقم الهاتف بصيغة دولية، مثال: +9665XXXXXXXX")
        val input = addInput("رقم الهاتف", InputType.TYPE_CLASS_PHONE)
        addButton("إرسال رمز التحقق") {
            val phone = input.text.toString().trim()
            if (!phone.startsWith("+") || phone.length < 8) {
                toast("أدخل رقم الهاتف بصيغة دولية")
                return@addButton
            }
            input.text?.clear()
            send(JSONObject().put("@type", "setAuthenticationPhoneNumber").put("phone_number", phone).put("@extra", "auth_phone"))
            setStatus("جاري طلب رمز التحقق…")
        }
        statusView = addStatus()
    }

    private fun showCodeScreen() = runOnUiThread {
        clearScreen()
        addTitle("رمز التحقق")
        addParagraph("أدخل الرمز الذي أرسله Telegram إلى حسابك أو رقمك.")
        val input = addInput("رمز التحقق", InputType.TYPE_CLASS_NUMBER)
        addButton("تحقق") {
            val code = input.text.toString().trim()
            if (code.isBlank()) return@addButton
            input.text?.clear()
            send(JSONObject().put("@type", "checkAuthenticationCode").put("code", code).put("@extra", "auth_code"))
            setStatus("جاري التحقق…")
        }
        statusView = addStatus()
    }

    private fun showPasswordScreen() = runOnUiThread {
        clearScreen()
        addTitle("التحقق بخطوتين")
        addParagraph("الحساب محمي بكلمة مرور إضافية.")
        val input = addInput("كلمة مرور التحقق بخطوتين", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        addButton("متابعة") {
            val password = input.text.toString()
            if (password.isEmpty()) return@addButton
            input.text?.clear()
            send(JSONObject().put("@type", "checkAuthenticationPassword").put("password", password).put("@extra", "auth_password"))
            setStatus("جاري التحقق…")
        }
        statusView = addStatus()
    }

    private fun showEmailScreen() = runOnUiThread {
        clearScreen()
        addTitle("البريد الإلكتروني")
        addParagraph("طلب Telegram بريدًا إلكترونيًا لاستكمال تسجيل الدخول.")
        val input = addInput("البريد الإلكتروني", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        addButton("متابعة") {
            val email = input.text.toString().trim()
            if (!email.contains("@")) return@addButton
            input.text?.clear()
            send(JSONObject().put("@type", "setAuthenticationEmailAddress").put("email_address", email).put("@extra", "auth_email"))
        }
        statusView = addStatus()
    }

    private fun showEmailCodeScreen() = runOnUiThread {
        clearScreen()
        addTitle("رمز البريد الإلكتروني")
        val input = addInput("الرمز", InputType.TYPE_CLASS_TEXT)
        addButton("تحقق") {
            val code = input.text.toString().trim()
            if (code.isBlank()) return@addButton
            input.text?.clear()
            val codeObject = JSONObject().put("@type", "emailAddressAuthenticationCode").put("code", code)
            send(JSONObject().put("@type", "checkAuthenticationEmailCode").put("code", codeObject).put("@extra", "auth_email_code"))
        }
        statusView = addStatus()
    }

    private fun showOtherDeviceScreen(link: String) = runOnUiThread {
        clearScreen()
        addTitle("تأكيد من جهاز آخر")
        addParagraph("طلب Telegram تأكيد تسجيل الدخول من جهاز مسجل مسبقًا. افتح Telegram على الجهاز الآخر وأكمل التأكيد.")
        if (link.isNotBlank()) addParagraph(link)
        statusView = addStatus()
        setStatus("بانتظار تأكيد Telegram…")
    }

    private fun showMainScreen() = runOnUiThread {
        clearScreen()
        addTitle("Telegram GIF Cleaner")
        addParagraph("تم تسجيل الدخول بنجاح. القائمة أدناه هي Saved GIFs الموجودة على حساب Telegram نفسه.")

        countView = TextView(this).apply {
            text = "عدد ملفات GIF المحفوظة: …"
            textSize = 22f
            setTextColor(Color.rgb(23, 33, 43))
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(22))
        }
        content.addView(countView)

        deleteButton = addButton("حذف جميع GIFs المحفوظة") { confirmDeleteAll() }.also { it.isEnabled = false }
        refreshButton = addButton("تحديث القائمة") { loadSavedAnimations() }

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            visibility = View.GONE
            max = 1
        }
        content.addView(progressBar, matchParams(dp(10)))
        progressText = addParagraph("").apply { visibility = View.GONE }

        logoutButton = addButton("تسجيل الخروج وحذف الجلسة المحلية") { confirmLogout() }
        statusView = addStatus()
        setStatus("جاري جلب Saved GIFs…")
        loadSavedAnimations()
    }

    private fun loadSavedAnimations() {
        if (clientId == 0) return
        send(JSONObject().put("@type", "getSavedAnimations").put("@extra", "get_saved"))
    }

    private fun handleAnimations(obj: JSONObject, extra: String) {
        if (extra != "get_saved") return
        val array = obj.optJSONArray("animations") ?: JSONArray()
        val ids = mutableListOf<Int>()
        for (i in 0 until array.length()) {
            val animation = array.optJSONObject(i) ?: continue
            val file = animation.optJSONObject("animation") ?: continue
            val id = file.optInt("id", 0)
            if (id != 0) ids.add(id)
        }
        savedAnimationIds.clear()
        savedAnimationIds.addAll(ids)
        runOnUiThread {
            countView?.text = "عدد ملفات GIF المحفوظة: ${ids.size}"
            deleteButton?.isEnabled = ids.isNotEmpty() && !deleting
            setStatus(if (ids.isEmpty()) "لا توجد ملفات GIF محفوظة." else "تم تحميل القائمة.")
        }
    }

    private fun confirmDeleteAll() {
        if (savedAnimationIds.isEmpty() || deleting) return
        AlertDialog.Builder(this)
            .setTitle("تأكيد الحذف")
            .setMessage("سيتم حذف ${savedAnimationIds.size} ملف GIF من Saved GIFs في حساب Telegram. هل تريد المتابعة؟")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف") { _, _ -> startDeleteAll() }
            .show()
    }

    private fun startDeleteAll() {
        deleting = true
        deleteIndex = 0
        deletedCount = 0
        failedCount = 0
        deleteButton?.isEnabled = false
        refreshButton?.isEnabled = false
        logoutButton?.isEnabled = false
        progressBar?.apply {
            max = savedAnimationIds.size.coerceAtLeast(1)
            progress = 0
            visibility = View.VISIBLE
        }
        progressText?.apply {
            text = "تم حذف 0 من ${savedAnimationIds.size}"
            visibility = View.VISIBLE
        }
        setStatus("جاري الحذف من حساب Telegram…")
        removeNextAnimation()
    }

    private fun removeNextAnimation() {
        if (deleteIndex >= savedAnimationIds.size) {
            finishDeleteAll()
            return
        }
        val fileId = savedAnimationIds[deleteIndex]
        val inputFile = JSONObject().put("@type", "inputFileId").put("id", fileId)
        send(JSONObject()
            .put("@type", "removeSavedAnimation")
            .put("animation", inputFile)
            .put("@extra", "delete_${deleteIndex}"))
    }

    private fun handleOk(extra: String) {
        if (extra.startsWith("delete_") && deleting) {
            deletedCount++
            advanceDeletion()
        }
    }

    private fun handleError(obj: JSONObject, extra: String) {
        if (extra.startsWith("delete_") && deleting) {
            failedCount++
            advanceDeletion()
            return
        }
        val message = obj.optString("message", "Telegram error")
        runOnUiThread { setStatus("رفض Telegram الطلب: $message", true) }
    }

    private fun advanceDeletion() {
        deleteIndex++
        runOnUiThread {
            progressBar?.progress = deleteIndex
            progressText?.text = "تم حذف $deletedCount من ${savedAnimationIds.size}"
        }
        removeNextAnimation()
    }

    private fun finishDeleteAll() = runOnUiThread {
        val total = savedAnimationIds.size
        deleting = false
        refreshButton?.isEnabled = true
        logoutButton?.isEnabled = true
        progressBar?.progress = total
        progressText?.text = "تم حذف $deletedCount من $total"

        val message = if (failedCount == 0) {
            "اكتملت العملية بنجاح. تم حذف $deletedCount ملف GIF من حساب Telegram."
        } else {
            "اكتملت العملية: تم حذف $deletedCount وتعذر حذف $failedCount. سيتم تحديث القائمة للتحقق."
        }
        setStatus(message, failedCount > 0)
        AlertDialog.Builder(this)
            .setTitle(if (failedCount == 0) "تم الحذف" else "اكتملت العملية")
            .setMessage(message)
            .setPositiveButton("حسنًا", null)
            .show()
        loadSavedAnimations()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("تسجيل الخروج")
            .setMessage("سيتم تسجيل الخروج من Telegram وحذف ملفات جلسة TDLib المحلية من هذا التطبيق.")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("تسجيل الخروج") { _, _ ->
                loggingOut = true
                deleteButton?.isEnabled = false
                logoutButton?.isEnabled = false
                setStatus("جاري تسجيل الخروج…")
                send(JSONObject().put("@type", "logOut").put("@extra", "logout"))
            }
            .show()
    }

    private fun deleteLocalSession() {
        try { File(filesDir, "tdlib").deleteRecursively() } catch (_: Throwable) {}
        try { File(filesDir, "tdfiles").deleteRecursively() } catch (_: Throwable) {}
    }

    private fun send(obj: JSONObject) {
        if (clientId == 0) return
        try {
            JsonClient.send(clientId, obj.toString())
        } catch (_: Throwable) {
            runOnUiThread { setStatus("تعذر إرسال الطلب إلى TDLib.", true) }
        }
    }

    private fun clearScreen() {
        content.removeAllViews()
        countView = null
        deleteButton = null
        refreshButton = null
        logoutButton = null
        progressBar = null
        progressText = null
    }

    private fun addTitle(text: String): TextView {
        val view = TextView(this).apply {
            this.text = text
            textSize = 28f
            setTextColor(Color.rgb(23, 33, 43))
            gravity = Gravity.CENTER
            textDirection = View.TEXT_DIRECTION_RTL
            setPadding(0, 0, 0, dp(18))
        }
        content.addView(view, matchParams())
        return view
    }

    private fun addParagraph(text: String): TextView {
        val view = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.rgb(70, 80, 90))
            gravity = Gravity.RIGHT
            textDirection = View.TEXT_DIRECTION_RTL
            setPadding(0, dp(7), 0, dp(7))
        }
        content.addView(view, matchParams())
        return view
    }

    private fun addInput(hint: String, inputTypeValue: Int): EditText {
        val view = EditText(this).apply {
            this.hint = hint
            inputType = inputTypeValue
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            textDirection = View.TEXT_DIRECTION_LTR
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            isSingleLine = true
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        val params = matchParams().apply { setMargins(0, dp(8), 0, dp(8)) }
        content.addView(view, params)
        return view
    }

    private fun addButton(text: String, click: () -> Unit): Button {
        val button = Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setOnClickListener { click() }
        }
        val params = matchParams(dp(52)).apply { setMargins(0, dp(8), 0, dp(8)) }
        content.addView(button, params)
        return button
    }

    private fun addStatus(): TextView {
        val view = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            textDirection = View.TEXT_DIRECTION_RTL
            setPadding(0, dp(14), 0, dp(8))
        }
        content.addView(view, matchParams())
        return view
    }

    private fun setStatus(text: String, error: Boolean = false) = runOnUiThread {
        if (!::statusView.isInitialized) return@runOnUiThread
        statusView.text = text
        statusView.setTextColor(if (error) Color.rgb(180, 45, 45) else Color.rgb(35, 110, 75))
    }

    private fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private fun matchParams(height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
