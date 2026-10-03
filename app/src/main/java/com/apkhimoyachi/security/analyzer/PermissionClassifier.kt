package com.apkhimoyachi.security.analyzer

import com.apkhimoyachi.security.model.RiskLevel

object PermissionClassifier {

    data class PermissionDetails(
        val permission: String,
        val uzbekName: String,
        val uzbekDescription: String,
        val riskLevel: RiskLevel,
        val scoreWeight: Int
    )

    private val permissionMap = mapOf(
        // Bank va SMS xavflari
        "android.permission.RECEIVE_SMS" to PermissionDetails(
            "android.permission.RECEIVE_SMS",
            "SMS qabul qilish",
            "Ilova kiruvchi SMS xabarlarni, shu jumladan bank va tasdiqlash (OTP) kodlarini o'qishi mumkin.",
            RiskLevel.CRITICAL,
            25
        ),
        "android.permission.READ_SMS" to PermissionDetails(
            "android.permission.READ_SMS",
            "SMS larni o'qish",
            "Qurilmadagi barcha shaxsiy SMS yozishmalarni o'qish imkoniyati.",
            RiskLevel.HIGH,
            20
        ),
        "android.permission.SEND_SMS" to PermissionDetails(
            "android.permission.SEND_SMS",
            "SMS yuborish",
            "Foydalanuvchi ruxsatisiz pullik xizmatlarga yoki firibgarlarga yashirin SMS yuborishi mumkin.",
            RiskLevel.HIGH,
            20
        ),

        // Maxsus imkoniyatlar va ekran ustiga chiqish (Troyanlar)
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to PermissionDetails(
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "Maxsus imkoniyatlar xizmati (Accessibility)",
            "Eng xavfli ruxsat! Ekrandagi barcha ma'lumotlarni o'qish (keylogger), parollarni o'g'irlash va avtomatik bosish imkonini beradi.",
            RiskLevel.CRITICAL,
            35
        ),
        "android.permission.SYSTEM_ALERT_WINDOW" to PermissionDetails(
            "android.permission.SYSTEM_ALERT_WINDOW",
            "Boshqa ilovalar ustidan ko'rsatish (Overlay)",
            "Bank yoki to'lov ilovalari ustiga soxta oyna qo'yib, karta ma'lumotlari va parollarni o'g'irlashda ishlatiladi.",
            RiskLevel.HIGH,
            20
        ),

        // Ilova o'rnatish (Dropper / Yuklovchi viruslar)
        "android.permission.REQUEST_INSTALL_PACKAGES" to PermissionDetails(
            "android.permission.REQUEST_INSTALL_PACKAGES",
            "Noma'lum ilovalarni o'rnatish",
            "Qurilmaga foydalanuvchiga bildirmasdan boshqa zararli dasturlarni (viruslarni) yuklab o'rnatishi mumkin.",
            RiskLevel.HIGH,
            20
        ),

        // Qurilma ma'muri (Ransomware / Bloklovchi)
        "android.permission.BIND_DEVICE_ADMIN" to PermissionDetails(
            "android.permission.BIND_DEVICE_ADMIN",
            "Qurilma ma'muri huquqi",
            "Qurilmani bloklash, ekranni qulflash, ma'lumotlarni o'chirish va ilovani o'chirishga yo'l qo'ymaslik huquqini beradi.",
            RiskLevel.CRITICAL,
            30
        ),

        // Yashirin josuslik (Spyware)
        "android.permission.RECORD_AUDIO" to PermissionDetails(
            "android.permission.RECORD_AUDIO",
            "Mikrofondan yozib olish",
            "Atrofdagi tovushlarni va suhbatlarni yashirin yozib olishi mumkin.",
            RiskLevel.MEDIUM,
            12
        ),
        "android.permission.CAMERA" to PermissionDetails(
            "android.permission.CAMERA",
            "Kameradan foydalanish",
            "Ruxsatsiz suratga yoki videoga olish ehtimoli mavjud.",
            RiskLevel.MEDIUM,
            10
        ),

        // Joylashuv va kuzatish
        "android.permission.ACCESS_FINE_LOCATION" to PermissionDetails(
            "android.permission.ACCESS_FINE_LOCATION",
            "Aniq geolokatsiya",
            "Qurilmaning aniq joylashuvini GPS orqali real vaqtda kuzatish.",
            RiskLevel.LOW,
            6
        ),
        "android.permission.ACCESS_BACKGROUND_LOCATION" to PermissionDetails(
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "Orqa fonda joylashuvni kuzatish",
            "Ilova yopiq bo'lsa ham foydalanuvchi qayerda ekanligini doimiy kuzatib boradi.",
            RiskLevel.MEDIUM,
            15
        ),

        // Kontaktlar va qo'ng'iroqlar
        "android.permission.READ_CONTACTS" to PermissionDetails(
            "android.permission.READ_CONTACTS",
            "Kontaktlar kitobini o'qish",
            "Barcha telefon raqamlar va yaqinlaringiz haqidagi ma'lumotlarni ko'rish.",
            RiskLevel.LOW,
            8
        ),
        "android.permission.READ_CALL_LOG" to PermissionDetails(
            "android.permission.READ_CALL_LOG",
            "Qo'ng'iroqlar tarixini o'qish",
            "Kim bilan qachon va qancha gaplashilganini ko'rish.",
            RiskLevel.MEDIUM,
            10
        ),
        "android.permission.PROCESS_OUTGOING_CALLS" to PermissionDetails(
            "android.permission.PROCESS_OUTGOING_CALLS",
            "Chiquvchi qo'ng'iroqlarni tutib qolish",
            "Qo'ng'iroqlarni boshqa raqamga yo'naltirish yoki bloklash.",
            RiskLevel.HIGH,
            15
        ),

        // Boshqa tizim ruxsatlari
        "android.permission.RECEIVE_BOOT_COMPLETED" to PermissionDetails(
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "Telefon yoqilganda avtomatik ishga tushish",
            "Qurilma o'chib-yonganida yashirin holda fon rejimida o'z-o'zidan ish boshlaydi.",
            RiskLevel.LOW,
            5
        ),
        "android.permission.QUERY_ALL_PACKAGES" to PermissionDetails(
            "android.permission.QUERY_ALL_PACKAGES",
            "Barcha ilovalarni tekshirish",
            "Telefonda qanday bank yoki hamyon ilovalari borligini aniqlash maqsadida ishlatilishi mumkin.",
            RiskLevel.LOW,
            7
        )
    )

    fun getDetails(permission: String): PermissionDetails? {
        return permissionMap[permission]
    }

    fun isDangerous(permission: String): Boolean {
        return permissionMap.containsKey(permission)
    }

    val knownDangerousList: List<String>
        get() = permissionMap.keys.toList()
}
