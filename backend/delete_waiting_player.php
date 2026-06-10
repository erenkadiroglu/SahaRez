<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

include 'db.php';

$id = isset($_POST['id']) ? intval($_POST['id']) : 0;

if($id > 0) {
    $sql = "DELETE FROM waiting_list WHERE id = $id";
    
    if ($conn->query($sql) === TRUE) {
        echo json_encode(["status" => "success", "message" => "Oyuncu listeden silindi."]);
    } else {
        echo json_encode(["status" => "error", "message" => "Hata oluştu: " . $conn->error]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Eksik bilgi gönderildi."]);
}
?>