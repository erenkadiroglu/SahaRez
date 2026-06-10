<?php
session_start();
session_unset();
session_destroy(); // Oturumu tamamen yok et
header("Location: admin_login.php"); // Giriş ekranına geri fırlat
exit;
?>