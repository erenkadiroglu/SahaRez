<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

require 'db.php';

// SQL: Tarihleri Gün.Ay.Yıl formatından gerçek tarihe çevirerek sıralar (Böylece aylar karışmaz)
$sql = "SELECT w.id, w.match_date, w.time_slot, w.created_at, u.full_name, u.phone 
        FROM waiting_list w 
        LEFT JOIN users u ON w.user_id = u.id 
        WHERE w.status = 'WAITING'
        ORDER BY STR_TO_DATE(w.match_date, '%d.%m.%Y') ASC, w.time_slot ASC, w.id ASC";

$result = $conn->query($sql);

if (!$result) {
    echo json_encode(["status" => "error", "message" => "SQL Hatası: " . $conn->error], JSON_UNESCAPED_UNICODE);
    exit;
}

$waiting_list = array();

while($row = $result->fetch_assoc()) {
    $u_name = !empty($row['full_name']) ? $row['full_name'] : "Bilinmeyen Oyuncu";
    $u_phone = !empty($row['phone']) ? $row['phone'] : "Belirtilmemiş";

    $waiting_list[] = array(
        "id" => intval($row['id']),
        "full_name" => $u_name,
        "phone" => $u_phone,
        "time_slot" => $row['match_date'] . " | " . $row['time_slot']
    );
}

echo json_encode(["status" => "success", "message" => "Başarılı", "data" => $waiting_list], JSON_UNESCAPED_UNICODE);

$conn->close();
?>