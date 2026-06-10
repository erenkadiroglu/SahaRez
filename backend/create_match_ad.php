<?php
// Hata ayıklama açık (Sorun olursa Android'e JSON olarak hatayı fırlatacak)
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require 'db.php';

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
// Android'den zaten 07.06.2026 şeklinde geliyor, bozmadan direkt alıyoruz.
$match_date = isset($_POST['match_date']) ? trim($_POST['match_date']) : '';
$time_slot = isset($_POST['time_slot']) ? trim($_POST['time_slot']) : '';
$ad_type = isset($_POST['ad_type']) ? trim($_POST['ad_type']) : '';
$missing_count = isset($_POST['missing_count']) ? trim($_POST['missing_count']) : '';
$missing_positions = isset($_POST['missing_positions']) ? trim($_POST['missing_positions']) : '';

if($user_id > 0 && !empty($match_date) && !empty($time_slot) && !empty($ad_type)) {
    date_default_timezone_set('Europe/Istanbul');
    $created_at = date('d.m.Y H:i:s');

    $stmt = $conn->prepare("INSERT INTO match_ads (user_id, match_date, time_slot, ad_type, missing_count, missing_positions, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)");
    
    // SQL'de bir tablo/sütun uyuşmazlığı varsa hemen yakala
    if (!$stmt) {
        echo json_encode(["status" => "error", "message" => "SQL Hatası: " . $conn->error]);
        exit;
    }

    $stmt->bind_param("issssss", $user_id, $match_date, $time_slot, $ad_type, $missing_count, $missing_positions, $created_at);

    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "İlan başarıyla yayınlandı!"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Kayıt Hatası: " . $stmt->error]);
    }
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Eksik bilgi gönderildi. Tarih: $match_date, Saat: $time_slot"]);
}
$conn->close();
?>