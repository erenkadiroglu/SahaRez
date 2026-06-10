<?php
// CANLI SUNUCU (HOSTINGER) İÇİN DB.PHP
$servername = "localhost";
$username = "u463559928_admin"; // Hostinger'da oluşturduğun tam kullanıcı adı
$password = "FENERBAHCEEren1907_"; // Veritabanını oluştururken belirlediğin şifre
$dbname = "u463559928_saharez"; // Hostinger'da oluşturduğun tam veritabanı adı

$conn = new mysqli($servername, $username, $password, $dbname);

// Uygulama genelinde Türkçe karakter desteğini garanti altına alır
$conn->set_charset("utf8mb4");

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Bağlantı başarısız: " . $conn->connect_error]));
}
?>