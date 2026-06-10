package com.erenkadiroglu.saharez.data.model

/**
 * LoginRequest: Kimlik doğrulama (Authentication) işlemi için sunucuya gönderilen
 * verileri kapsülleyen bir DTO (Data Transfer Object) sınıfıdır.
 * * * Teknik Amaç:
 * 1. Encapsulation: Kullanıcı adı ve şifreyi bir nesne içinde gruplayarak veri bütünlüğünü sağlar.
 * 2. Serialization: Retrofit/Gson kütüphanesinin bu sınıfı otomatik olarak
 * JSON formatına dönüştürmesini (Serialization) sağlar.
 * 3. Contract (Sözleşme): API ile uygulama arasındaki veri transfer sözleşmesini temsil eder.
 */
data class LoginRequest(
    // Bu değişken isimleri, sunucudaki (PHP) $_POST['username'] ile eşleşecek şekilde
    // JSON'a dönüştürülür.
    val username: String,
    val password: String
)