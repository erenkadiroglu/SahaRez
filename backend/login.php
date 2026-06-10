<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');

include 'db.php';

// Android'den gelen veriyi yakala
$data = json_decode(file_get_contents("php://input"), true);

$username = "";
$password = "";

if ($data) {
    $username = isset($data['username']) ? $conn->real_escape_string($data['username']) : '';
    $password = isset($data['password']) ? $conn->real_escape_string($data['password']) : '';
} else {
    $username = isset($_POST['username']) ? $conn->real_escape_string($_POST['username']) : '';
    $password = isset($_POST['password']) ? $conn->real_escape_string($_POST['password']) : '';
}

if(!empty($username) && !empty($password)) {
    // BURASI ÇOK ÖNEMLİ: 'BINARY' eklenerek case-sensitivity (harf duyarlılığı) sağlandı.
    // Artık 'Admin' ile 'admin' veritabanında farklı karakterler olarak karşılaştırılacak.
    $sql = "SELECT * FROM users WHERE BINARY username = '$username' AND BINARY password = '$password'";
    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        
        echo json_encode([
            "status" => "success", 
            "message" => "Giriş başarılı", 
            "full_name" => $user['full_name'],
            "user_id" => intval($user['id']),
            "role" => $user['role']
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Hatalı kullanıcı adı veya şifre"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Veri gönderilemedi veya eksik bilgi."]);
}
$conn->close();
?>