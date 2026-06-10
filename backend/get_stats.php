<?php
// JSON formatının bozulmasını önlemek için hataları gizliyoruz
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *'); 
header('Access-Control-Allow-Methods: POST, GET'); 

include 'db.php';

// Android'den gelen tarihi yakala
$raw_date = isset($_POST['date']) ? $_POST['date'] : (isset($_GET['date']) ? $_GET['date'] : '');
$raw_date = trim($raw_date);

// SQL Filtre Şartı
$where_sql = "WHERE 1=1";

if (!empty($raw_date)) {
    $db_date = date('d.m.Y', strtotime($raw_date));
    $where_sql .= " AND b.match_date = '$db_date'";
}

// Güvenli Varsayılan Yanıt Şablonu
$response = [
    "status" => "success",
    "message" => "İşlem başarılı.",
    "data" => [
        "total_bookings" => 0,
        "total_base" => 0.0,
        "total_cleats" => 0.0,
        "total_buffet" => 0.0,
        "grand_total" => 0.0,
        "avg_revenue" => 0.0,
        "cleat_count" => 0,
        "avg_buffet" => 0.0,
        "side_income_ratio" => 0,
        "popular_time" => "-",
        "cancel_ratio" => 0,
        "occupancy_rate" => 0
    ]
];

if ($conn->connect_error) {
    $response["status"] = "error";
    $response["message"] = "Veritabanı bağlantı hatası.";
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    // 1. Toplam Randevu Sayısı (Tüm tabloda o güne ait randevular)
    $r_book = $conn->query("SELECT COUNT(DISTINCT b.id) as c FROM bookings b $where_sql");
    $total_bookings = $r_book ? intval($r_book->fetch_assoc()['c']) : 0;

    // 2. En Popüler Seans
    $r_pop = $conn->query("SELECT b.time_slot FROM bookings b $where_sql GROUP BY b.time_slot ORDER BY COUNT(*) DESC LIMIT 1");
    $popular_time = ($r_pop && $r_pop->num_rows > 0) ? $r_pop->fetch_assoc()['time_slot'] : "-";

    // 3. YENİ GELİR MOTORU (Gerçekleşen Ödemeler Üzerinden)
    // Sadece 'payments' tablosunda kaydı olan oyuncuların hesapları toplanır.
    // Randevu boş açılmışsa (oyuncu eklenmemişse) ciroya 0 yansır.
    $q_pay = "SELECT 
                IFNULL(SUM(CASE WHEN b.has_shuttle = 1 THEN 250 ELSE 190 END), 0) as total_base,
                IFNULL(SUM(p.rented_cleats * 50), 0) as total_cleats,
                IFNULL(SUM(p.buffet_expense), 0) as total_buffet
              FROM payments p 
              INNER JOIN bookings b ON p.booking_id = b.id 
              $where_sql";
              
    $r_pay = $conn->query($q_pay);
    $pay_data = $r_pay ? $r_pay->fetch_assoc() : ['total_base'=>0, 'total_cleats'=>0, 'total_buffet'=>0];
    
    $total_base = floatval($pay_data['total_base']);
    $total_cleats = floatval($pay_data['total_cleats']);
    $total_buffet = floatval($pay_data['total_buffet']);
    
    $grand_total = $total_base + $total_cleats + $total_buffet;

    // --- GELİŞMİŞ ANALİZLER (KPI) ---
    // Eğer o gün randevu var ama oyuncu yoksa ortalama 0 olur
    $avg_revenue = ($total_bookings > 0) ? ($grand_total / $total_bookings) : 0.0;
    $cleat_count = ($total_cleats > 0) ? intval($total_cleats / 50) : 0;
    $avg_buffet = ($total_bookings > 0) ? ($total_buffet / $total_bookings) : 0.0;
    
    $total_side_income = $total_cleats + $total_buffet;
    $side_income_ratio = ($grand_total > 0) ? round(($total_side_income / $grand_total) * 100) : 0;
    
    $cancel_ratio = 0; 

    // Seans Kapasitesi (Günlük Maksimum 11 Seans)
    if (!empty($raw_date)) {
        $occupancy_rate = round(($total_bookings / 11) * 100);
        if($occupancy_rate > 100) $occupancy_rate = 100; 
    } else {
        $occupancy_rate = -1;
    }

    // Değerleri Android'e Atama
    $response["data"]["total_bookings"] = $total_bookings;
    $response["data"]["total_base"] = $total_base;
    $response["data"]["total_cleats"] = $total_cleats;
    $response["data"]["total_buffet"] = $total_buffet;
    $response["data"]["grand_total"] = $grand_total;
    $response["data"]["avg_revenue"] = round($avg_revenue, 2);
    $response["data"]["cleat_count"] = $cleat_count;
    $response["data"]["avg_buffet"] = round($avg_buffet, 1);
    $response["data"]["side_income_ratio"] = $side_income_ratio;
    $response["data"]["popular_time"] = $popular_time;
    $response["data"]["cancel_ratio"] = $cancel_ratio;
    $response["data"]["occupancy_rate"] = $occupancy_rate;

} catch (Exception $e) {
    $response["message"] = "SQL Hatası: " . $e->getMessage();
}

// JSON Paketini Android'e Fırlat
echo json_encode($response, JSON_UNESCAPED_UNICODE);
?>