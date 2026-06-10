<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

include 'db.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!$data) {
    echo json_encode(["status" => "error", "message" => "Geçersiz veri formatı."]);
    exit;
}

$user_id = intval($data['user_id']);
$full_name = $conn->real_escape_string($data['full_name']); 
$phone = $conn->real_escape_string($data['phone']);         
$raw_date = $conn->real_escape_string($data['match_date']);
$time_slot = $conn->real_escape_string($data['time_slot']);
$has_shuttle = intval($data['has_shuttle']); 
$players = isset($data['players']) ? $data['players'] : [];

$match_date = date('d.m.Y', strtotime($raw_date));
date_default_timezone_set('Europe/Istanbul');
$created_at = date('d.m.Y H:i:s');

if ($user_id > 0 && !empty($match_date) && !empty($time_slot)) {
    $conn->begin_transaction();

    try {
        $check_sql = "SELECT id FROM bookings WHERE match_date = '$match_date' AND time_slot = '$time_slot'";
        $check_result = $conn->query($check_sql);

        $booking_id = 0;

        if ($check_result->num_rows > 0) {
            $row = $check_result->fetch_assoc();
            $booking_id = $row['id'];
            
            $update_sql = "UPDATE bookings SET user_id = $user_id, full_name = '$full_name', phone = '$phone', has_shuttle = $has_shuttle, created_at = '$created_at' WHERE id = $booking_id";
            $conn->query($update_sql);

            // Önceki ödeme kayıtlarını silip, sıfırdan gelenleri yazacağız.
            $delete_payments = "DELETE FROM payments WHERE booking_id = $booking_id";
            $conn->query($delete_payments);
        } else {
            $insert_sql = "INSERT INTO bookings (user_id, full_name, phone, match_date, time_slot, has_shuttle, created_at) VALUES ($user_id, '$full_name', '$phone', '$match_date', '$time_slot', $has_shuttle, '$created_at')";
            $conn->query($insert_sql);
            $booking_id = $conn->insert_id;
        }

        // KRİTİK GÜNCELLEME: Oyuncu kayıtları.
        // Boş dizi kontrolü ve isimlendirme standartizasyonu
        if (!empty($players) && is_array($players) && $booking_id > 0) {
            $stmt = $conn->prepare("INSERT INTO payments (booking_id, player_name, rented_cleats, buffet_expense) VALUES (?, ?, ?, ?)");
            foreach ($players as $player) {
                // Hem snake_case hem camelCase desteği
                $p_name = isset($player['name']) ? $player['name'] : (isset($player['playerName']) ? $player['playerName'] : 'İsimsiz');
                $p_cleats = (!empty($player['rented_cleats']) || !empty($player['rentedCleats'])) ? 1 : 0;
                $p_buffet = isset($player['buffet_expense']) ? floatval($player['buffet_expense']) : (isset($player['buffetExpense']) ? floatval($player['buffetExpense']) : 0);
                
                $stmt->bind_param("isid", $booking_id, $p_name, $p_cleats, $p_buffet);
                $stmt->execute();
            }
            $stmt->close();
        }

        $conn->commit();
        echo json_encode(["status" => "success", "message" => "İşlem başarılı!"]);

    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(["status" => "error", "message" => "Kayıt hatası: " . $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Eksik bilgi."]);
}
$conn->close();
?>