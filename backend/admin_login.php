<?php
session_start();
include 'db.php';

if (isset($_SESSION['admin_logged_in']) && $_SESSION['admin_logged_in'] === true) {
    header("Location: admin_dashboard.php");
    exit;
}

$message = "";

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $username = $conn->real_escape_string(trim($_POST['username']));
    $password = $_POST['password'];

    $sql = "SELECT * FROM users WHERE username = '$username' AND role = 'ADMIN' LIMIT 1";
    $result = $conn->query($sql);

    if ($result && $result->num_rows > 0) {
        $row = $result->fetch_assoc();
        if ($password == $row['password'] || password_verify($password, $row['password'])) {
            $_SESSION['admin_logged_in'] = true;
            $_SESSION['admin_username'] = $row['username'];
            $_SESSION['admin_fullname'] = $row['full_name'];
            header("Location: admin_dashboard.php");
            exit;
        } else {
            $message = "<div class='alert error'>Hatalı şifre girdiniz! Lütfen tekrar deneyin.</div>";
        }
    } else {
        $message = "<div class='alert error'>Bu kullanıcı adıyla eşleşen bir yönetici hesabı bulunamadı.</div>";
    }
}
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>SahaRez | Yönetici Girişi</title>
    <style>
        :root {
            --primary: #10b981;
            --secondary: #3b82f6;
            --dark-bg: #0b1120;
            --card-bg: rgba(30, 41, 59, 0.65);
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
        }

        * { box-sizing: border-box; margin: 0; padding: 0; }

        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            background-color: var(--dark-bg); 
            display: flex; 
            justify-content: center; 
            align-items: center; 
            min-height: 100vh; 
            color: var(--text-main); 
            padding: 30px 20px;
        }
        
        .orb-1, .orb-2 { position: fixed; border-radius: 50%; filter: blur(90px); z-index: -1; opacity: 0.5; animation: float 10s ease-in-out infinite; }
        .orb-1 { width: 50vw; height: 50vw; background: radial-gradient(circle, rgba(59, 130, 246, 0.3) 0%, rgba(11, 17, 32, 0) 70%); top: -10%; left: -10%; }
        .orb-2 { width: 40vw; height: 40vw; background: radial-gradient(circle, rgba(16, 185, 129, 0.2) 0%, rgba(11, 17, 32, 0) 70%); bottom: -5%; right: -5%; animation-direction: alternate-reverse; animation-duration: 12s; }

        @keyframes float {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-40px) scale(1.05); }
            100% { transform: translateY(0px) scale(1); }
        }
        
        .login-card { 
            background: var(--card-bg); 
            backdrop-filter: blur(20px); 
            -webkit-backdrop-filter: blur(20px);
            padding: 40px; 
            border-radius: 20px; 
            border: 1px solid rgba(255, 255, 255, 0.08); 
            box-shadow: 0 25px 50px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(255, 255, 255, 0.05); 
            width: 100%; 
            max-width: 420px; 
            position: relative;
            z-index: 10;
            margin: auto; /* Telefondaki kesilmeleri önler */
        }
        
        .header { text-align: center; margin-bottom: 35px; }
        .logo-container { display: flex; justify-content: center; align-items: center; gap: 12px; margin-bottom: 10px;}
        .logo-img { height: 50px; width: 50px; object-fit: cover; border-radius: 50%; box-shadow: 0 0 15px rgba(59, 130, 246, 0.5); border: 2px solid rgba(59, 130, 246, 0.3); }
        .header h2 { font-size: 32px; font-weight: 900; color: white; letter-spacing: 0.5px;}
        .header h2 span { background: linear-gradient(45deg, var(--primary), var(--secondary)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0px 0px 15px rgba(59, 130, 246, 0.3); }
        .header p { color: var(--text-muted); font-size: 15px; margin-top: 5px; font-weight: 500;}
        
        .form-grid { display: grid; gap: 20px; width: 100%; }
        .form-group { width: 100%; position: relative; }
        
        label { display: block; margin-bottom: 8px; font-size: 13px; color: #cbd5e1; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;}
        
        input { 
            width: 100%; 
            padding: 16px 20px; 
            border-radius: 12px; 
            border: 1px solid rgba(255, 255, 255, 0.1); 
            background-color: rgba(15, 23, 42, 0.8); 
            color: white; 
            font-size: 16px; /* Mobil Zoom'u Engeller */
            transition: all 0.3s ease; 
            outline: none; 
            font-family: inherit;
            appearance: none; /* Telefonun kendi çirkin stillerini ezer */
            -webkit-appearance: none;
        }
        input:focus { border-color: var(--secondary); box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2); background-color: #0f172a;}
        
        .password-wrapper { position: relative; width: 100%; }
        .password-wrapper input { padding-right: 50px; } 
        .eye-icon { position: absolute; right: 15px; top: 50%; transform: translateY(-50%); color: var(--text-muted); cursor: pointer; display: flex; align-items: center; transition: 0.3s; padding: 5px;}
        .eye-icon:hover { color: var(--secondary); }
        
        button { width: 100%; padding: 18px; border: none; border-radius: 12px; background: linear-gradient(135deg, #2563eb, #3b82f6); color: white; font-size: 16px; font-weight: 800; letter-spacing: 0.5px; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4); margin-top: 10px;}
        button:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(59, 130, 246, 0.6); filter: brightness(1.1);}
        
        .alert { padding: 14px; border-radius: 10px; text-align: center; margin-bottom: 20px; font-size: 14px; font-weight: 600; line-height: 1.5;}
        .error { background-color: rgba(239, 68, 68, 0.1); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.2); }
        
        .back-link { display: flex; align-items: center; justify-content: center; gap: 5px; width: 100%; margin-top: 25px; color: var(--text-muted); font-size: 14px; text-decoration: none; transition: 0.3s; font-weight: 500;}
        .back-link:hover { color: white; }
        .back-link svg { width: 16px; height: 16px; transition: 0.3s; }
        .back-link:hover svg { transform: translateX(-4px); }

        /* KUSURSUZ MOBİL UYUM */
        @media (max-width: 480px) {
            body { padding: 15px; }
            .login-card { padding: 35px 25px; border-radius: 16px; }
            .header h2 { font-size: 28px; }
            .logo-img { height: 44px; width: 44px; }
            .form-grid { gap: 16px; }
        }
    </style>
</head>
<body>

<div class="orb-1"></div>
<div class="orb-2"></div>

<div class="login-card">
    <div class="header">
        <div class="logo-container">
            <img src="logo.png" alt="SahaRez Logo" class="logo-img" onerror="this.style.display='none'">
            <h2>Saha<span>Rez</span></h2>
        </div>
        <p>Sistem Yönetimi Oturum Açma</p>
    </div>
    
    <?php echo $message; ?>

    <form action="" method="POST">
        <div class="form-grid">
            <div class="form-group">
                <label>Kullanıcı Adı</label>
                <input type="text" name="username" placeholder="Yetkili kullanıcı adınızı girin" required autocomplete="off" autofocus>
            </div>
            
            <div class="form-group">
                <label>Şifre</label>
                <div class="password-wrapper">
                    <input type="password" id="adminPass" name="password" placeholder="••••••••" required>
                    <span class="eye-icon" id="eyeAdmin" onclick="togglePassword('adminPass', 'eyeAdmin')" title="Şifreyi Göster">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                            <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                    </span>
                </div>
            </div>
        </div>
        <button type="submit">Panele Giriş Yap</button>
    </form>
    
    <a href="../" class="back-link">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        Ana Sayfaya Dön
    </a>
</div>

<script>
    const eyeOpen = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
    const eyeClosed = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`;

    function togglePassword(inputId, iconId) {
        const input = document.getElementById(inputId);
        const icon = document.getElementById(iconId);
        if (input.type === "password") {
            input.type = "text"; icon.innerHTML = eyeOpen; icon.style.color = "#3b82f6";
        } else {
            input.type = "password"; icon.innerHTML = eyeClosed; icon.style.color = "#94a3b8";
        }
    }
</script>
</body>
</html>