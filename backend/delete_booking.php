<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require 'db.php';

// Hem JSON hem de Form verisi destekleyen güvenli veri alımı
$data = json_decode(file_get_contents("php://input"), true);
$match_date = isset($data['match_date']) ? $data['match_date'] : (isset($_POST['match_date']) ? $_POST['match_date'] : '');
$time_slot = isset($data['time_slot']) ? $data['time_slot'] : (isset($_POST['time_slot']) ? $_POST['time_slot'] : '');

if (!empty($match_date) && !empty($time_slot)) {
    // SQL Injection Koruması
    $match_date = $conn->real_escape_string($match_date);
    $time_slot = $conn->real_escape_string($time_slot);

    $sql = "SELECT id FROM bookings WHERE match_date = '$match_date' AND time_slot = '$time_slot'";
    $result = $conn->query($sql);
    
    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $booking_id = $row['id'];
        
        // İlişkisel Veritabanı Mantığı: Önce o randevuya ait oyuncuları, sonra ana randevuyu siliyoruz.
        $conn->query("DELETE FROM payments WHERE booking_id = $booking_id");
        $conn->query("DELETE FROM bookings WHERE id = $booking_id");
        
        echo json_encode(["status" => "success", "message" => "Randevu tamamen silindi."]);
    } else {
        echo json_encode(["status" => "error", "message" => "Silinecek randevu bulunamadı."]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Tarih veya seans bilgisi eksik."]);
}
$conn->close();
?>