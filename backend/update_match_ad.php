<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
require 'db.php';

$ad_id = isset($_POST['ad_id']) ? intval($_POST['ad_id']) : 0;
$ad_type = isset($_POST['ad_type']) ? trim($_POST['ad_type']) : '';
$missing_count = isset($_POST['missing_count']) ? trim($_POST['missing_count']) : '';
$missing_positions = isset($_POST['missing_positions']) ? trim($_POST['missing_positions']) : '';

if($ad_id > 0 && !empty($ad_type)) {
    $stmt = $conn->prepare("UPDATE match_ads SET ad_type=?, missing_count=?, missing_positions=? WHERE id=?");
    
    if (!$stmt) {
        echo json_encode(["status" => "error", "message" => "SQL Hatası: " . $conn->error]);
        exit;
    }

    $stmt->bind_param("sssi", $ad_type, $missing_count, $missing_positions, $ad_id);

    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "İlan başarıyla güncellendi!"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Güncelleme Hatası: " . $stmt->error]);
    }
    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Eksik bilgi gönderildi."]);
}
$conn->close();
?>