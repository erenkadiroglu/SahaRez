<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);
session_start();
include 'db.php';

if (!isset($_SESSION['admin_logged_in']) || $_SESSION['admin_logged_in'] !== true) {
    header("Location: admin_login.php");
    exit;
}

// ==========================================
// 1. SİLME İŞLEMLERİ (CRUD - DELETE)
// ==========================================
if (isset($_POST['delete_booking_id'])) {
    $del_id = intval($_POST['delete_booking_id']);
    $conn->query("DELETE FROM payments WHERE booking_id = $del_id");
    $conn->query("DELETE FROM bookings WHERE id = $del_id");
    header("Location: admin_dashboard.php?status=deleted"); exit;
}
if (isset($_POST['delete_waiting_id'])) {
    $del_id = intval($_POST['delete_waiting_id']);
    $conn->query("DELETE FROM waiting_list WHERE id = $del_id");
    header("Location: admin_dashboard.php?status=deleted"); exit;
}
if (isset($_POST['delete_ad_id'])) {
    $del_id = intval($_POST['delete_ad_id']);
    $conn->query("DELETE FROM match_ads WHERE id = $del_id");
    header("Location: admin_dashboard.php?status=deleted"); exit;
}
if (isset($_POST['delete_user_id'])) {
    $del_id = intval($_POST['delete_user_id']);
    $conn->query("DELETE FROM users WHERE id = $del_id");
    header("Location: admin_dashboard.php?status=deleted"); exit;
}

// ==========================================
// 2. GÜNCELLEME İŞLEMLERİ (CRUD - UPDATE)
// ==========================================
if (isset($_POST['edit_booking_id'])) {
    $edit_id = intval($_POST['edit_booking_id']);
    $edit_name = $conn->real_escape_string(trim($_POST['edit_name']));
    $edit_phone = $conn->real_escape_string(trim($_POST['edit_phone']));
    $raw_date = $_POST['edit_date'];
    $edit_date = date('d.m.Y', strtotime($raw_date)); 
    $edit_time = $conn->real_escape_string(trim($_POST['edit_time']));
    $edit_shuttle = intval($_POST['edit_shuttle']);

    $update_sql = "UPDATE bookings SET full_name='$edit_name', phone='$edit_phone', match_date='$edit_date', time_slot='$edit_time', has_shuttle=$edit_shuttle WHERE id=$edit_id";
    $conn->query($update_sql);
    header("Location: admin_dashboard.php?status=updated"); exit;
}

if (isset($_POST['edit_waiting_id'])) {
    $edit_id = intval($_POST['edit_waiting_id']);
    $raw_date = $_POST['edit_w_date'];
    $edit_date = date('d.m.Y', strtotime($raw_date)); 
    $edit_time = $conn->real_escape_string(trim($_POST['edit_w_time']));

    $update_sql = "UPDATE waiting_list SET match_date='$edit_date', time_slot='$edit_time' WHERE id=$edit_id";
    $conn->query($update_sql);
    header("Location: admin_dashboard.php?status=updated"); exit;
}

if (isset($_POST['edit_ad_id'])) {
    $edit_id = intval($_POST['edit_ad_id']);
    $edit_type = $conn->real_escape_string(trim($_POST['edit_ad_type']));
    $edit_count = $conn->real_escape_string(trim($_POST['edit_ad_count']));
    $edit_pos = $conn->real_escape_string(trim($_POST['edit_ad_pos']));

    $update_sql = "UPDATE match_ads SET ad_type='$edit_type', missing_count='$edit_count', missing_positions='$edit_pos' WHERE id=$edit_id";
    $conn->query($update_sql);
    header("Location: admin_dashboard.php?status=updated"); exit;
}

if (isset($_POST['edit_user_id'])) {
    $edit_id = intval($_POST['edit_user_id']);
    $edit_name = $conn->real_escape_string(trim($_POST['edit_u_name']));
    $edit_email = $conn->real_escape_string(trim($_POST['edit_u_email']));
    $edit_phone = $conn->real_escape_string(trim($_POST['edit_u_phone']));
    $edit_role = $conn->real_escape_string(trim($_POST['edit_u_role']));

    $update_sql = "UPDATE users SET full_name='$edit_name', email='$edit_email', phone='$edit_phone', role='$edit_role' WHERE id=$edit_id";
    $conn->query($update_sql);
    header("Location: admin_dashboard.php?status=updated"); exit;
}

// ==========================================
// 3. GELİŞMİŞ İŞ ZEKASI VE İSTATİSTİK MOTORU
// ==========================================
$stat_date_raw = $_GET['stat_date'] ?? '';
$is_filtered = !empty($stat_date_raw);
$where_sql = "WHERE 1=1";
$where_sql_b = "WHERE 1=1";

if ($is_filtered) {
    $filter_date = date('d.m.Y', strtotime($stat_date_raw));
    $date_title = "$filter_date";
    $where_sql .= " AND match_date = '$filter_date'";
    $where_sql_b .= " AND b.match_date = '$filter_date'";
} else {
    $date_title = "Tüm Zamanlar";
}

$r_book = $conn->query("SELECT COUNT(DISTINCT b.id) as c FROM bookings b $where_sql");
$total_bookings = ($r_book && $r_book->num_rows > 0) ? intval($r_book->fetch_assoc()['c']) : 0;

$r_wait = $conn->query("SELECT COUNT(*) as c FROM waiting_list $where_sql");
$waiting_count = ($r_wait && $r_wait->num_rows > 0) ? intval($r_wait->fetch_assoc()['c']) : 0;

$q_pay = "SELECT 
            IFNULL(SUM(CASE WHEN b.has_shuttle = 1 THEN 250 ELSE 190 END), 0) as total_base,
            IFNULL(SUM(p.rented_cleats * 50), 0) as total_cleats,
            IFNULL(SUM(p.buffet_expense), 0) as total_buffet
          FROM payments p 
          INNER JOIN bookings b ON p.booking_id = b.id 
          $where_sql_b";
          
$r_pay = $conn->query($q_pay);
$pay_data = $r_pay->fetch_assoc();
$total_base = floatval($pay_data['total_base']);
$total_cleats = floatval($pay_data['total_cleats']);
$total_buffet = floatval($pay_data['total_buffet']);
$grand_total = $total_base + $total_cleats + $total_buffet;

$avg_revenue = ($total_bookings > 0) ? ($grand_total / $total_bookings) : 0;
$cleat_count = ($total_cleats > 0) ? intval($total_cleats / 50) : 0;
$avg_buffet = ($total_bookings > 0) ? ($total_buffet / $total_bookings) : 0;
$side_income_ratio = ($grand_total > 0) ? round((($total_cleats + $total_buffet) / $grand_total) * 100) : 0;

$r_pop = $conn->query("SELECT time_slot, COUNT(*) as c FROM bookings $where_sql GROUP BY time_slot ORDER BY c DESC LIMIT 1");
$pop_session = ($r_pop && $r_pop->num_rows > 0) ? $r_pop->fetch_assoc()['time_slot'] : "-";
$cancel_ratio = 0; 

if ($is_filtered) {
    $occupancy_rate = round(($total_bookings / 11) * 100);
    if($occupancy_rate > 100) $occupancy_rate = 100; 
    $occupancy_text = "%" . $occupancy_rate;
} else {
    $occupancy_text = "<span style='font-size:14px; color:var(--text-muted);'>Tarih Seçiniz</span>";
}

?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>SahaRez | Yönetim Merkezi</title>
    
    <link rel="stylesheet" href="https://cdn.datatables.net/1.13.4/css/jquery.dataTables.min.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/buttons/2.3.6/css/buttons.dataTables.min.css">
    <script src="https://code.jquery.com/jquery-3.5.1.js"></script>
    <script src="https://cdn.datatables.net/1.13.4/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/buttons/2.3.6/js/dataTables.buttons.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.1.3/jszip.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/pdfmake.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/vfs_fonts.js"></script>
    <script src="https://cdn.datatables.net/buttons/2.3.6/js/buttons.html5.min.js"></script>
    
    <style>
        :root {
            --primary: #10b981; --secondary: #3b82f6; --dark-bg: #0b1120;
            --card-bg: #1e293b; --text-main: #f8fafc; --text-muted: #94a3b8; --danger: #ef4444;
        }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: var(--dark-bg); margin: 0; color: var(--text-main); }
        .navbar { display: flex; justify-content: space-between; align-items: center; padding: 15px 5%; background-color: rgba(11, 17, 32, 0.9); border-bottom: 1px solid rgba(51, 65, 85, 0.5); }
        .logo-text { font-size: 24px; font-weight: bold; }
        .logo-text span { color: var(--primary); }
        .btn-logout { background-color: rgba(239, 68, 68, 0.1); color: var(--danger); padding: 8px 16px; border-radius: 6px; text-decoration: none; font-weight: bold; }
        .dashboard-wrapper { padding: 30px 5%; max-width: 1400px; margin: 0 auto; }
        
        .stat-filter-bar { background: var(--card-bg); padding: 15px 25px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.05); margin-bottom: 25px; display: flex; justify-content: space-between; align-items: center; }
        .stat-filter-bar h3 { margin: 0; font-size: 18px; color: white; display: flex; align-items: center; gap: 8px; }
        .stat-filter-bar h3 span { color: var(--primary); font-size: 15px; }
        .date-form { display: flex; gap: 10px; align-items: center; }
        .date-form input[type="date"] { background-color: #0f172a; border: 1px solid #334155; color: white; padding: 10px 15px; border-radius: 6px; font-size: 14px; color-scheme: dark; outline: none; }
        .date-form input[type="date"]:focus { border-color: var(--primary); }
        .btn-clear-filter { background: rgba(239, 68, 68, 0.1); color: var(--danger); border: 1px solid rgba(239, 68, 68, 0.3); padding: 10px 15px; border-radius: 6px; text-decoration: none; font-weight: bold; font-size: 14px; transition: 0.3s;}

        .detailed-stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 40px; }
        .stat-box { background: var(--card-bg); padding: 25px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.05); box-shadow: 0 4px 15px rgba(0,0,0,0.2); display: flex; flex-direction: column; }
        .stat-box h4 { margin: 0 0 15px 0; color: white; font-size: 16px; border-bottom: 1px solid #334155; padding-bottom: 10px; }
        
        .stat-list { list-style: none; padding: 0; margin: 0; flex-grow: 1; display: flex; flex-direction: column; justify-content: center; gap: 12px;}
        .stat-list li { display: flex; align-items: center; color: var(--text-muted); font-size: 14px; width: 100%;}
        .stat-list .label { white-space: nowrap; font-weight: 500;}
        .stat-list .dots { flex-grow: 1; border-bottom: 1px dashed rgba(255,255,255,0.15); margin: 0 10px; position: relative; top: -4px;}
        .stat-list .value { font-weight: bold; color: #e2e8f0; white-space: nowrap; text-align: right;}
        
        .stat-highlight { 
            margin-top: auto;
            padding-top: 15px; 
            border-top: 2px solid rgba(255,255,255,0.05); 
            font-size: 14px; 
            font-weight: 800; 
            color: var(--primary); 
            display: flex; 
            justify-content: space-between; 
            align-items: center; 
            width: 100%; 
        }
        .stat-highlight .highlight-title { white-space: nowrap; }
        .stat-highlight .highlight-value { font-size: 20px; color: white; text-align: right; white-space: nowrap; }
        
        .big-number-box { text-align: center; justify-content: center; align-items: center; }
        .big-number-box h4 { border: none; font-size: 16px; color: var(--text-muted); margin-bottom: 5px; justify-content: center;}
        .big-number-box .huge-number { font-size: 72px; line-height: 1; font-weight: 900; color: var(--text-main); margin-top:10px;}

        .tabs { display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 2px solid #334155; padding-bottom: 10px; overflow-x: auto;}
        .tab-btn { background: transparent; color: var(--text-muted); border: none; padding: 10px 20px; font-size: 16px; font-weight: bold; cursor: pointer; border-radius: 6px; transition: 0.3s; white-space: nowrap;}
        .tab-btn:hover { background: rgba(255,255,255,0.05); }
        .tab-btn.active { background: var(--secondary); color: white; }
        .tab-content { display: none; background: var(--card-bg); padding: 20px; border-radius: 12px; }
        .tab-content.active { display: block; animation: fadeIn 0.3s; }
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

        table.dataTable { border-collapse: collapse !important; width: 100% !important; color: white !important; }
        table.dataTable thead th { background-color: #0f172a !important; color: var(--secondary) !important; border-bottom: 2px solid #334155 !important; }
        table.dataTable tbody tr { background-color: var(--card-bg) !important; border-bottom: 1px solid #334155 !important; }
        
        .action-group { display: flex; gap: 5px; flex-wrap: nowrap; }
        .btn-action { padding: 6px 10px; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: bold; border: none; }
        .btn-edit { background: rgba(59, 130, 246, 0.1); color: var(--secondary); border: 1px solid rgba(59, 130, 246, 0.3); }
        .btn-edit:hover { background: var(--secondary); color: white; }
        .btn-delete { background: rgba(239, 68, 68, 0.1); color: var(--danger); border: 1px solid rgba(239, 68, 68, 0.3); }
        .btn-delete:hover { background: var(--danger); color: white; }
        .dataTables_wrapper input, .dataTables_wrapper select { color: black !important; }

        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(11, 17, 32, 0.8); backdrop-filter: blur(5px); justify-content: center; align-items: center; padding: 20px;}
        .modal-content { background: var(--card-bg); padding: 30px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.1); width: 100%; max-width: 500px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); position: relative; }
        .close-modal { position: absolute; right: 20px; top: 20px; color: var(--text-muted); font-size: 24px; cursor: pointer; transition: 0.3s; }
        .close-modal:hover { color: white; }
        .modal h2 { margin-top: 0; color: white; font-size: 20px; border-bottom: 1px solid #334155; padding-bottom: 15px; margin-bottom: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; color: var(--text-muted); font-size: 13px; font-weight: bold; margin-bottom: 8px; text-transform: uppercase;}
        .form-group input, .form-group select { width: 100%; padding: 12px; background: #0f172a; border: 1px solid #334155; color: white; border-radius: 6px; box-sizing: border-box; outline: none; }
        .form-group input:focus, .form-group select:focus { border-color: var(--secondary); }
        .btn-save { width: 100%; padding: 14px; background: var(--primary); color: white; border: none; border-radius: 6px; font-weight: bold; font-size: 16px; cursor: pointer; transition: 0.3s; margin-top: 10px; }
        .btn-save:hover { background: #059669; }
    </style>
</head>
<body>

    <nav class="navbar">
        <div class="logo-text">Saha<span>Rez</span> Admin</div>
        <div>
            <a href="logout.php" class="btn-logout" onclick="return confirm('Çıkış yapmak istediğinize emin misiniz?');">Çıkış Yap</a>
        </div>
    </nav>

    <div class="dashboard-wrapper">
        
        <div class="stat-filter-bar">
            <h3>Sistem İstatistikleri <span> - <?php echo $date_title; ?></span></h3>
            <form action="" method="GET" class="date-form" id="statFilterForm">
                <input type="date" name="stat_date" value="<?php echo $stat_date_raw; ?>" onchange="document.getElementById('statFilterForm').submit();">
                <?php if($is_filtered): ?>
                    <a href="admin_dashboard.php" class="btn-clear-filter">Aramayı Temizle</a>
                <?php endif; ?>
            </form>
        </div>

        <div class="detailed-stats-grid">
            <div class="stat-box big-number-box">
                <h4>Toplam Rezervasyon</h4>
                <div class="huge-number"><?php echo $total_bookings; ?></div>
            </div>

            <div class="stat-box">
                <h4>Temel Finansal Özet</h4>
                <ul class="stat-list">
                    <li><span class="label">Saha Geliri</span><span class="dots"></span><span class="value"><?php echo number_format($total_base, 0, ',', '.'); ?> ₺</span></li>
                    <li><span class="label">Krampon Geliri</span><span class="dots"></span><span class="value"><?php echo number_format($total_cleats, 0, ',', '.'); ?> ₺</span></li>
                    <li><span class="label">Büfe Geliri</span><span class="dots"></span><span class="value"><?php echo number_format($total_buffet, 0, ',', '.'); ?> ₺</span></li>
                </ul>
                <div class="stat-highlight">
                    <span class="highlight-title">TOPLAM CİRO</span> 
                    <span class="highlight-value" style="color:var(--text-main)"><?php echo number_format($grand_total, 0, ',', '.'); ?> ₺</span>
                </div>
            </div>

            <div class="stat-box">
                <h4>Gelişmiş Analizler</h4>
                <ul class="stat-list">
                    <li><span class="label">Ortalama Seans Geliri</span><span class="dots"></span><span class="value"><?php echo number_format($avg_revenue, 0, ',', '.'); ?> ₺</span></li>
                    <li><span class="label">Kiralanan Krampon</span><span class="dots"></span><span class="value"><?php echo $cleat_count; ?> Adet</span></li>
                    <li><span class="label">Maç Başı Büfe</span><span class="dots"></span><span class="value"><?php echo number_format($avg_buffet, 1, ',', '.'); ?> ₺</span></li>
                </ul>
                <div class="stat-highlight">
                    <span class="highlight-title">YAN GELİR ORANI</span> 
                    <span class="highlight-value" style="color:var(--text-main)">%<?php echo $side_income_ratio; ?></span>
                </div>
            </div>

            <div class="stat-box">
                <h4>Saha ve Operasyon Analizi</h4>
                <ul class="stat-list">
                    <li><span class="label">Bekleyen Oyuncu (Sıra)</span><span class="dots"></span><span class="value"><?php echo $waiting_count; ?> Kişi</span></li>
                    <li><span class="label">En Popüler Seans</span><span class="dots"></span><span class="value" style="color:var(--secondary)"><?php echo $pop_session; ?></span></li>
                    <li><span class="label">İptal Oranı</span><span class="dots"></span><span class="value">%<?php echo $cancel_ratio; ?></span></li>
                </ul>
                <div class="stat-highlight">
                    <span class="highlight-title">DOLULUK ORANI</span> 
                    <span class="highlight-value" style="color:var(--text-main)"><?php echo $occupancy_text; ?></span>
                </div>
            </div>
        </div>

        <div class="tabs">
            <button class="tab-btn active" onclick="openTab(event, 'tab-bookings')">📅 Randevular</button>
            <button class="tab-btn" onclick="openTab(event, 'tab-waiting')">⏳ Bekleme Listesi</button>
            <button class="tab-btn" onclick="openTab(event, 'tab-ads')">📢 İlanlar</button>
            <button class="tab-btn" onclick="openTab(event, 'tab-users')">👥 Kullanıcılar</button>
        </div>

        <div id="tab-bookings" class="tab-content active">
            <table id="bookingTable" class="display responsive nowrap">
                <thead><tr><th>ID</th><th>Ad Soyad</th><th>Telefon</th><th>Tarih</th><th>Saat</th><th>Servis</th><th>İşlem</th></tr></thead>
                <tbody>
                    <?php
                    $res = $conn->query("SELECT * FROM bookings ORDER BY id DESC");
                    while($r = $res->fetch_assoc()) {
                        $servis = $r['has_shuttle'] == 1 ? 'Servisli' : 'Yok';
                        $raw_date_for_input = date('Y-m-d', strtotime($r['match_date']));
                        echo "<tr>
                            <td data-sort='{$r['id']}'>#{$r['id']}</td><td>{$r['full_name']}</td><td>{$r['phone']}</td>
                            <td>{$r['match_date']}</td><td>{$r['time_slot']}</td><td>$servis</td>
                            <td>
                                <div class='action-group'>
                                    <button type='button' class='btn-action btn-edit' onclick=\"openEditBooking({$r['id']}, '{$r['full_name']}', '{$r['phone']}', '{$raw_date_for_input}', '{$r['time_slot']}', {$r['has_shuttle']})\">Düzenle</button>
                                    <form method='POST' style='display:inline;' onsubmit=\"return confirm('Randevuyu silmek istediğinize emin misiniz?');\">
                                        <input type='hidden' name='delete_booking_id' value='{$r['id']}'>
                                        <button class='btn-action btn-delete'>Sil</button>
                                    </form>
                                </div>
                            </td>
                        </tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>

        <div id="tab-waiting" class="tab-content">
            <table id="waitingTable" class="display responsive nowrap">
                <thead><tr><th>ID</th><th>Ad Soyad</th><th>Telefon</th><th>Tarih</th><th>Saat</th><th>Kayıt Zamanı</th><th>İşlem</th></tr></thead>
                <tbody>
                    <?php
                    $res = $conn->query("SELECT w.*, u.full_name, u.phone FROM waiting_list w LEFT JOIN users u ON w.user_id = u.id ORDER BY w.id DESC");
                    while($r = $res->fetch_assoc()) {
                        $raw_date_for_input = date('Y-m-d', strtotime($r['match_date']));
                        $c_date = !empty($r['created_at']) ? date('d.m.Y H:i', strtotime($r['created_at'])) : '-';
                        echo "<tr>
                            <td data-sort='{$r['id']}'>#{$r['id']}</td><td>{$r['full_name']}</td><td>{$r['phone']}</td>
                            <td>{$r['match_date']}</td><td>{$r['time_slot']}</td><td>{$c_date}</td>
                            <td>
                                <div class='action-group'>
                                    <button type='button' class='btn-action btn-edit' onclick=\"openEditWaiting({$r['id']}, '{$raw_date_for_input}', '{$r['time_slot']}')\">Düzenle</button>
                                    <form method='POST' style='display:inline;' onsubmit=\"return confirm('Sırayı silmek istediğinize emin misiniz?');\">
                                        <input type='hidden' name='delete_waiting_id' value='{$r['id']}'>
                                        <button class='btn-action btn-delete'>Sil</button>
                                    </form>
                                </div>
                            </td>
                        </tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>

        <div id="tab-ads" class="tab-content">
            <table id="adsTable" class="display responsive nowrap">
                <thead><tr><th>ID</th><th>İlan Sahibi</th><th>Tür</th><th>Eksik</th><th>Mevki</th><th>Tarih</th><th>İşlem</th></tr></thead>
                <tbody>
                    <?php
                    $res = $conn->query("SELECT m.*, u.full_name FROM match_ads m LEFT JOIN users u ON m.user_id = u.id ORDER BY m.id DESC");
                    while($r = $res->fetch_assoc()) {
                        echo "<tr>
                            <td data-sort='{$r['id']}'>#{$r['id']}</td><td>{$r['full_name']}</td><td>{$r['ad_type']}</td>
                            <td>{$r['missing_count']}</td><td>{$r['missing_positions']}</td><td>{$r['match_date']}</td>
                            <td>
                                <div class='action-group'>
                                    <button type='button' class='btn-action btn-edit' onclick=\"openEditAd({$r['id']}, '{$r['ad_type']}', '{$r['missing_count']}', '{$r['missing_positions']}')\">Düzenle</button>
                                    <form method='POST' style='display:inline;' onsubmit=\"return confirm('İlanı silmek istediğinize emin misiniz?');\">
                                        <input type='hidden' name='delete_ad_id' value='{$r['id']}'>
                                        <button class='btn-action btn-delete'>Sil</button>
                                    </form>
                                </div>
                            </td>
                        </tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>

        <div id="tab-users" class="tab-content">
            <table id="usersTable" class="display responsive nowrap">
                <thead><tr><th>ID</th><th>Ad Soyad</th><th>Email</th><th>Telefon</th><th>Rol</th><th>Kayıt Tarihi</th><th>İşlem</th></tr></thead>
                <tbody>
                    <?php
                    $res = $conn->query("SELECT * FROM users ORDER BY id DESC");
                    while($r = $res->fetch_assoc()) {
                        $rol = $r['role'] == 'ADMIN' ? 'Saha Görevlisi' : 'Oyuncu';
                        $c_date = !empty($r['created_at']) ? date('d.m.Y H:i', strtotime($r['created_at'])) : '-';
                        echo "<tr>
                            <td data-sort='{$r['id']}'>#{$r['id']}</td><td>{$r['full_name']}</td><td>{$r['email']}</td>
                            <td>{$r['phone']}</td><td>$rol</td><td>{$c_date}</td>
                            <td>
                                <div class='action-group'>
                                    <button type='button' class='btn-action btn-edit' onclick=\"openEditUser({$r['id']}, '{$r['full_name']}', '{$r['email']}', '{$r['phone']}', '{$r['role']}')\">Düzenle</button>
                                    <form method='POST' style='display:inline;' onsubmit=\"return confirm('Kullanıcıyı tamamen silmek istediğinize emin misiniz?');\">
                                        <input type='hidden' name='delete_user_id' value='{$r['id']}'>
                                        <button class='btn-action btn-delete'>Sil</button>
                                    </form>
                                </div>
                            </td>
                        </tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>
    </div>

    <div id="editBookingModal" class="modal" style="display:none;">
        <div class="modal-content">
            <span class="close-modal" onclick="closeModal('editBookingModal')">&times;</span>
            <h2>Randevuyu Düzenle</h2>
            <form action="" method="POST">
                <input type="hidden" name="edit_booking_id" id="modal_edit_booking_id">
                <div class="form-group"><label>Ad Soyad</label><input type="text" name="edit_name" id="modal_edit_name" required></div>
                <div class="form-group"><label>Telefon</label><input type="text" name="edit_phone" id="modal_edit_phone" required></div>
                <div style="display:flex; gap:10px;">
                    <div class="form-group" style="flex:1;"><label>Tarih</label><input type="date" name="edit_date" id="modal_edit_date" required></div>
                    <div class="form-group" style="flex:1;"><label>Saat Seansı</label><input type="text" name="edit_time" id="modal_edit_time" required></div>
                </div>
                <div class="form-group"><label>Servis</label><select name="edit_shuttle" id="modal_edit_shuttle"><option value="0">Servissiz (190₺)</option><option value="1">Servisli (250₺)</option></select></div>
                <button type="submit" class="btn-save">Değişiklikleri Kaydet</button>
            </form>
        </div>
    </div>

    <div id="editWaitingModal" class="modal" style="display:none;">
        <div class="modal-content">
            <span class="close-modal" onclick="closeModal('editWaitingModal')">&times;</span>
            <h2>Sırayı Düzenle</h2>
            <form action="" method="POST">
                <input type="hidden" name="edit_waiting_id" id="modal_edit_waiting_id">
                <div class="form-group"><label>Yeni Tarih</label><input type="date" name="edit_w_date" id="modal_edit_w_date" required></div>
                <div class="form-group"><label>Yeni Saat</label><input type="text" name="edit_w_time" id="modal_edit_w_time" required></div>
                <button type="submit" class="btn-save">Değişiklikleri Kaydet</button>
            </form>
        </div>
    </div>

    <div id="editAdModal" class="modal" style="display:none;">
        <div class="modal-content">
            <span class="close-modal" onclick="closeModal('editAdModal')">&times;</span>
            <h2>İlanı Düzenle</h2>
            <form action="" method="POST">
                <input type="hidden" name="edit_ad_id" id="modal_edit_ad_id">
                <div class="form-group"><label>İlan Türü</label><select name="edit_ad_type" id="modal_edit_ad_type"><option value="OYUNCU EKSİK">OYUNCU EKSİK</option><option value="RAKİP ARANIYOR">RAKİP ARANIYOR</option></select></div>
                <div class="form-group"><label>Eksik Sayısı</label><input type="text" name="edit_ad_count" id="modal_edit_ad_count"></div>
                <div class="form-group"><label>Mevki</label><input type="text" name="edit_ad_pos" id="modal_edit_ad_pos"></div>
                <button type="submit" class="btn-save">Değişiklikleri Kaydet</button>
            </form>
        </div>
    </div>

    <div id="editUserModal" class="modal" style="display:none;">
        <div class="modal-content">
            <span class="close-modal" onclick="closeModal('editUserModal')">&times;</span>
            <h2>Kullanıcıyı Düzenle</h2>
            <form action="" method="POST">
                <input type="hidden" name="edit_user_id" id="modal_edit_user_id">
                <div class="form-group"><label>Ad Soyad</label><input type="text" name="edit_u_name" id="modal_edit_u_name" required></div>
                <div class="form-group"><label>Email</label><input type="text" name="edit_u_email" id="modal_edit_u_email" required></div>
                <div class="form-group"><label>Telefon</label><input type="text" name="edit_u_phone" id="modal_edit_u_phone" required></div>
                <div class="form-group"><label>Rol</label><select name="edit_u_role" id="modal_edit_u_role"><option value="PLAYER">Oyuncu</option><option value="ADMIN">Saha Görevlisi</option></select></div>
                <button type="submit" class="btn-save">Değişiklikleri Kaydet</button>
            </form>
        </div>
    </div>

    <script>
        // MİMARİ GÜNCELLEME: DataTables İhracat (Export) Optimizasyonu
        $(document).ready(function() {
            $('#bookingTable, #waitingTable, #adsTable, #usersTable').DataTable({
                "language": { "url": "//cdn.datatables.net/plug-ins/1.13.4/i18n/tr.json" },
                "responsive": true,
                "dom": '<"top"Bf>rt<"bottom"lip><"clear">', 
                "columnDefs": [
                    // Son sütun olan "İşlem" sütununda yukarı/aşağı sıralama oklarını gizler
                    { "orderable": false, "targets": -1 }
                ],
                "buttons": [ 
                    {
                        extend: 'excelHtml5',
                        text: 'Excel İndir',
                        exportOptions: {
                            // "İşlem" (Action) sütununu (her tablodaki son sütun) Excel'e almaz
                            columns: ':visible:not(:last-child)' 
                        }
                    },
                    {
                        extend: 'pdfHtml5',
                        text: 'PDF İndir',
                        orientation: 'landscape', // Sütun genişliğini artırmak için yatay sayfa
                        pageSize: 'A4',
                        exportOptions: {
                            // "İşlem" sütununu PDF'e de almaz
                            columns: ':visible:not(:last-child)' 
                        },
                        customize: function(doc) {
                            // PDF içindeki tablonun genişliğini %100 yapar ve sütunları eşit yayar
                            // Bu sayede veriler alt alta sıkışmaz, tek satırda rahatça okunur
                            doc.content[1].table.widths = Array(doc.content[1].table.body[0].length + 1).join('*').split('');
                            
                            // Metinleri ortalar
                            doc.styles.tableBodyEven.alignment = 'center';
                            doc.styles.tableBodyOdd.alignment = 'center';
                            doc.styles.tableHeader.alignment = 'center';
                        }
                    }
                ]
            });
        });

        function openTab(evt, tabId) {
            var i, tabcontent, tablinks;
            tabcontent = document.getElementsByClassName("tab-content");
            for (i = 0; i < tabcontent.length; i++) {
                tabcontent[i].style.display = "none";
                tabcontent[i].classList.remove("active");
            }
            tablinks = document.getElementsByClassName("tab-btn");
            for (i = 0; i < tablinks.length; i++) {
                tablinks[i].classList.remove("active");
            }
            document.getElementById(tabId).style.display = "block";
            document.getElementById(tabId).classList.add("active");
            evt.currentTarget.classList.add("active");
        }

        function closeModal(modalId) { document.getElementById(modalId).style.display = "none"; }
        
        function openEditBooking(id, name, phone, date, time, shuttle) {
            document.getElementById('editBookingModal').style.display = "flex";
            document.getElementById('modal_edit_booking_id').value = id;
            document.getElementById('modal_edit_name').value = name;
            document.getElementById('modal_edit_phone').value = phone;
            document.getElementById('modal_edit_date').value = date;
            document.getElementById('modal_edit_time').value = time;
            document.getElementById('modal_edit_shuttle').value = shuttle;
        }

        function openEditWaiting(id, date, time) {
            document.getElementById('editWaitingModal').style.display = "flex";
            document.getElementById('modal_edit_waiting_id').value = id;
            document.getElementById('modal_edit_w_date').value = date;
            document.getElementById('modal_edit_w_time').value = time;
        }

        function openEditAd(id, type, count, pos) {
            document.getElementById('editAdModal').style.display = "flex";
            document.getElementById('modal_edit_ad_id').value = id;
            document.getElementById('modal_edit_ad_type').value = type;
            document.getElementById('modal_edit_ad_count').value = count;
            document.getElementById('modal_edit_ad_pos').value = pos;
        }

        function openEditUser(id, name, email, phone, role) {
            document.getElementById('editUserModal').style.display = "flex";
            document.getElementById('modal_edit_user_id').value = id;
            document.getElementById('modal_edit_u_name').value = name;
            document.getElementById('modal_edit_u_email').value = email;
            document.getElementById('modal_edit_u_phone').value = phone;
            document.getElementById('modal_edit_u_role').value = role;
        }
    </script>
</body>
</html>