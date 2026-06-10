<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require 'db.php';

$match_date = isset($_POST['date']) ? trim($_POST['date']) : '';

if(empty($match_date)){
    echo json_encode(["status" => "success", "data" => []]);
    exit;
}

$sql = "SELECT m.id, m.time_slot, m.ad_type, m.missing_count, m.missing_positions, 
               u.full_name as creatorName, u.phone as creatorPhone 
        FROM match_ads m 
        LEFT JOIN users u ON m.user_id = u.id 
        WHERE m.match_date = ? 
        ORDER BY m.time_slot ASC";

$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Okuma Hatası: " . $conn->error]);
    exit;
}

$stmt->bind_param("s", $match_date);
$stmt->execute();
$result = $stmt->get_result();

$ads = array();
while($row = $result->fetch_assoc()) {
    $ads[] = array(
        "id" => strval($row['id']),
        "timeSlot" => $row['time_slot'],
        "creatorName" => !empty($row['creatorName']) ? $row['creatorName'] : "Bilinmeyen",
        "creatorPhone" => !empty($row['creatorPhone']) ? $row['creatorPhone'] : "", // Telefon eksiksiz gidiyor
        "type" => $row['ad_type'],
        "missingCount" => !empty($row['missing_count']) ? $row['missing_count'] : "",
        "missingPositions" => !empty($row['missing_positions']) ? $row['missing_positions'] : ""
    );
}

echo json_encode(["status" => "success", "data" => $ads], JSON_UNESCAPED_UNICODE);
$stmt->close();
$conn->close();
?>