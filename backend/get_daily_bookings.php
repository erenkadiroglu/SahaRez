<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require 'db.php';

$data = json_decode(file_get_contents("php://input"), true);
$raw_date = isset($data['date']) ? trim($data['date']) : (isset($_POST['date']) ? trim($_POST['date']) : (isset($_GET['date']) ? trim($_GET['date']) : ''));

if (empty($raw_date)) {
    echo json_encode(["status" => "error", "message" => "Tarih parametresi eksik.", "data" => []]);
    exit;
}

// Android'den gelen format (10.06.2026) zaten kusursuz. strtotime ile bozmamak için direkt alıyoruz.
$db_date = $conn->real_escape_string($raw_date);

// 1. ADIM: O güne ait bekleme listesini çek ve seanslara göre grupla
$waiting_data = [];
$w_stmt = $conn->prepare("SELECT time_slot, user_id FROM waiting_list WHERE match_date = ? ORDER BY id ASC");
$w_stmt->bind_param("s", $db_date);
$w_stmt->execute();
$w_res = $w_stmt->get_result();
while($w_row = $w_res->fetch_assoc()){
    $waiting_data[$w_row['time_slot']][] = (int)$w_row['user_id'];
}
$w_stmt->close();

// 2. ADIM: O güne ait randevuları çek
$stmt = $conn->prepare("SELECT id, time_slot as timeSlot, full_name as fullName, phone, has_shuttle as isDepositPaid FROM bookings WHERE match_date = ?");
$stmt->bind_param("s", $db_date);
$stmt->execute();
$result = $stmt->get_result();

$bookings = [];
while ($row = $result->fetch_assoc()) {
    $booking_id = intval($row['id']);
    $ts = $row['timeSlot'];
    
    // Bekleme listesi verilerini bu seansa ekliyoruz
    $row['waitingCount'] = isset($waiting_data[$ts]) ? count($waiting_data[$ts]) : 0;
    $row['waitingUsers'] = isset($waiting_data[$ts]) ? $waiting_data[$ts] : [];
    
    // O randevuya ait oyuncuları çekiyoruz (Güvenli Sorgu)
    $players = [];
    $p_stmt = $conn->prepare("SELECT player_name as name, rented_cleats as rentedCleats, buffet_expense as buffetExpense FROM payments WHERE booking_id = ?");
    $p_stmt->bind_param("i", $booking_id);
    $p_stmt->execute();
    $p_res = $p_stmt->get_result();
    
    while($p_row = $p_res->fetch_assoc()) {
        $p_row['rentedCleats'] = ($p_row['rentedCleats'] == 1);
        $p_row['buffetExpense'] = intval($p_row['buffetExpense']);
        $players[] = $p_row;
    }
    $p_stmt->close();
    
    $row['players'] = $players;
    $bookings[] = $row;
}

echo json_encode([
    "status" => "success",
    "message" => "Randevular başarıyla getirildi.",
    "data" => $bookings
], JSON_UNESCAPED_UNICODE);

$stmt->close();
$conn->close();
?>