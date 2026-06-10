<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

include 'db.php';

$user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
$raw_date = isset($_POST['match_date']) ? trim($_POST['match_date']) : '';
$time_slot = isset($_POST['time_slot']) ? trim($_POST['time_slot']) : '';

if($user_id > 0 && !empty($raw_date) && !empty($time_slot)) {
    
    // TARİH FORMATLAMA: Ne gelirse gelsin "Gün.Ay.Yıl" (Örn: 07.06.2026) yapar
    $match_date = date('d.m.Y', strtotime($raw_date));
    
    // KAYIT ZAMANI: Hangi gün, saat kaçta sıraya girdi?
    date_default_timezone_set('Europe/Istanbul');
    $created_at = date('d.m.Y H:i:s');

    // 1. Aynı seans için zaten sırada mı?
    $check_sql = "SELECT id FROM waiting_list WHERE user_id = $user_id AND match_date = '$match_date' AND time_slot = '$time_slot'";
    $check_result = $conn->query($check_sql);

    if ($check_result && $check_result->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "Zaten bu seans için bekleyenler sırasındasınız!"]);
    } else {
        // 2. Sırada değilse tabloya ekle
        $sql = "INSERT INTO waiting_list (user_id, match_date, time_slot, status, created_at) 
                VALUES ($user_id, '$match_date', '$time_slot', 'WAITING', '$created_at')";
        
        if ($conn->query($sql) === TRUE) {
            echo json_encode(["status" => "success", "message" => "Talebiniz alındı! Eğer randevu iptal olursa listeye göre saha görevlisi size ulaşacaktır."]);
        } else {
            echo json_encode(["status" => "error", "message" => "Hata oluştu: " . $conn->error]);
        }
    }
} else {
    echo json_encode(["status" => "error", "message" => "Eksik bilgi gönderildi."]);
}
$conn->close();
?>