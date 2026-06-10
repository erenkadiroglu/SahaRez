<?php
include 'db.php';
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <title>SahaRez Yönetim Paneli</title>
    <link rel="stylesheet" href="https://cdn.datatables.net/1.13.4/css/jquery.dataTables.min.css">
    <script src="https://code.jquery.com/jquery-3.5.1.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <style>body { font-family: sans-serif; padding: 20px; }</style>
</head>
<body>
    <h2>SahaRez Randevu Yönetim Paneli</h2>
    <table id="bookingTable" class="display">
        <thead>
            <tr>
                <th>ID</th>
                <th>Müşteri Adı</th>
                <th>Telefon</th>
                <th>Tarih</th>
                <th>Saat</th>
                <th>Servis</th>
                <th>Oluşturulma</th>
            </tr>
        </thead>
        <tbody>
            <?php
            $sql = "SELECT * FROM bookings ORDER BY id DESC";
            $result = $conn->query($sql);
            while($row = $result->fetch_assoc()) {
                echo "<tr>
                    <td>{$row['id']}</td>
                    <td>{$row['full_name']}</td>
                    <td>{$row['phone']}</td>
                    <td>{$row['match_date']}</td>
                    <td>{$row['time_slot']}</td>
                    <td>" . ($row['has_shuttle'] == 1 ? 'Servisli' : 'Servissiz') . "</td>
                    <td>{$row['created_at']}</td>
                </tr>";
            }
            ?>
        </tbody>
    </table>
    <script>$(document).ready(function() { $('#bookingTable').DataTable(); });</script>
</body>
</html>