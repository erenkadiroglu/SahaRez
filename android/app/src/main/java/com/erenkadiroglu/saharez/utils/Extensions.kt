package com.erenkadiroglu.saharez.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * showConfirmationDialog: Uygulama genelinde kullanılan merkezi UI yardımcı fonksiyonudur.
 * * Teknik Detaylar:
 * 1. Kotlin Extension Function: Context sınıfını genişleterek, herhangi bir Activity içinden
 * doğrudan bu metodu çağırmamızı sağlar (context.showConfirmationDialog şeklinde).
 * 2. DRY (Don't Repeat Yourself) Prensibi: Aynı diyalog yapısını her Activity'de tekrar tekrar
 * yazmak yerine tek bir merkezden yöneterek kod tekrarını önler ve bakım maliyetini düşürür.
 * 3. Higher-Order Function: 'onConfirm' parametresi bir lambda (kod bloğu) alır.
 * Bu sayede fonksiyon, her yerde farklı bir iş mantığıyla çalışabilecek şekilde esnek kılınmıştır.
 *
 * @param title Diyalog başlığı.
 * @param message Diyalog içeriği.
 * @param positiveText Onay butonu metni (Default: "Evet").
 * @param negativeText İptal butonu metni (Default: "Vazgeç").
 * @param onConfirm Onay butonuna tıklandığında yürütülecek işlem (Callback).
 */
fun Context.showConfirmationDialog(
    title: String,
    message: String,
    positiveText: String = "Evet",
    negativeText: String = "Vazgeç",
    onConfirm: () -> Unit
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText) { dialog, _ ->
            // Lambda fonksiyonu burada çağrılır (Execution)
            onConfirm()
            dialog.dismiss()
        }
        .setNegativeButton(negativeText) { dialog, _ ->
            // İptal durumunda sadece diyalog kapatılır, başka bir aksiyon alınmaz.
            dialog.dismiss()
        }
        // Güvenlik Önlemi: Kullanıcının yanlışlıkla diyalog dışına basarak
        // işlemi iptal etmesini engellemek için diyalog kilitlenir.
        .setCancelable(false)
        .show()
}