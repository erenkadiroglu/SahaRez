<?php
include 'db.php';
$message = "";

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $first_name = $conn->real_escape_string(trim($_POST['first_name']));
    $last_name = $conn->real_escape_string(trim($_POST['last_name']));
    $username = $conn->real_escape_string(trim($_POST['username']));
    $password = $_POST['password'];
    $password_confirm = $_POST['password_confirm'];
    $email = $conn->real_escape_string(trim($_POST['email']));
    $phone = $conn->real_escape_string(trim($_POST['phone']));
    $account_type = $_POST['account_type']; 
    $secret_code = isset($_POST['secret_code']) ? trim($_POST['secret_code']) : "";

    $full_name = $first_name . " " . $last_name;

    if ($password !== $password_confirm) {
        $message = "<div class='alert error'>Şifreler birbiriyle eşleşmiyor! Lütfen kontrol edin.</div>";
    } 
    else if ($account_type === "ADMIN" && $secret_code !== "SahaRez1907") {
        $message = "<div class='alert error'>Saha Görevlisi yetki kodu hatalı! İşlem reddedildi.</div>";
    } 
    else {
        $check_sql = "SELECT id FROM users WHERE username = '$username' OR email = '$email'";
        $check_result = $conn->query($check_sql);

        if ($check_result->num_rows > 0) {
            $message = "<div class='alert error'>Bu kullanıcı adı veya E-mail adresi zaten sistemde kayıtlı.</div>";
        } else {
            $sql = "INSERT INTO users (full_name, username, password, email, phone, role) 
                    VALUES ('$full_name', '$username', '$password', '$email', '$phone', '$account_type')";
            if ($conn->query($sql) === TRUE) {
                $message = "<div class='alert success'>Kayıt başarıyla tamamlandı!<br><br><b>$full_name</b> olarak sisteme giriş yapabilirsiniz.</div>";
            } else {
                $message = "<div class='alert error'>Sistemsel bir hata oluştu: " . $conn->error . "</div>";
            }
        }
    }
}
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>SahaRez | Yeni Kayıt</title>
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
            padding: 40px 20px;
        }
        
        .orb-1, .orb-2 { position: fixed; border-radius: 50%; filter: blur(90px); z-index: -1; opacity: 0.4; animation: float 12s ease-in-out infinite; }
        .orb-1 { width: 50vw; height: 50vw; background: radial-gradient(circle, rgba(16, 185, 129, 0.3) 0%, rgba(11, 17, 32, 0) 70%); top: -10%; left: -10%; }
        .orb-2 { width: 45vw; height: 45vw; background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, rgba(11, 17, 32, 0) 70%); bottom: -5%; right: -5%; animation-direction: alternate-reverse; animation-duration: 15s; }

        @keyframes float {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-40px) scale(1.05); }
            100% { transform: translateY(0px) scale(1); }
        }
        
        .register-card { 
            background: var(--card-bg); 
            backdrop-filter: blur(20px); 
            -webkit-backdrop-filter: blur(20px);
            padding: 45px 50px; 
            border-radius: 20px; 
            border: 1px solid rgba(255, 255, 255, 0.08); 
            box-shadow: 0 25px 50px rgba(0,0,0,0.5), inset 0 0 0 1px rgba(255, 255, 255, 0.05); 
            width: 100%; 
            max-width: 650px; 
            position: relative;
            z-index: 10;
            margin: auto;
        }
        
        .header { text-align: center; margin-bottom: 30px; }
        .logo-container { display: flex; justify-content: center; align-items: center; gap: 12px; margin-bottom: 10px;}
        .logo-img { height: 50px; width: 50px; object-fit: cover; border-radius: 50%; box-shadow: 0 0 15px rgba(16, 185, 129, 0.4); border: 2px solid rgba(16, 185, 129, 0.3); }
        .header h2 { font-size: 32px; font-weight: 900; color: white; letter-spacing: 0.5px;}
        .header h2 span { background: linear-gradient(45deg, var(--primary), var(--secondary)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0px 0px 15px rgba(59, 130, 246, 0.3); }
        .header p { color: var(--text-muted); font-size: 15px; margin-top: 5px; font-weight: 500;}
        
        /* CSS GRID İLE KUSURSUZ SİMETRİ! */
        .form-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 20px;
            width: 100%;
        }
        
        .form-group { width: 100%; position: relative; }
        .form-group.full-width { grid-column: span 2; }
        
        label { display: block; margin-bottom: 8px; font-size: 12px; color: #cbd5e1; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px;}
        
        input { 
            width: 100%; 
            padding: 16px 20px; 
            border-radius: 12px; 
            border: 1px solid rgba(255, 255, 255, 0.1); 
            background-color: rgba(15, 23, 42, 0.8); 
            color: white; 
            font-size: 16px; /* Telefonun zoom yapmasını engeller */
            transition: all 0.3s ease; 
            outline: none; 
            font-family: inherit;
            appearance: none;
            -webkit-appearance: none;
        }
        input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); background-color: #0f172a;}
        
        .password-wrapper { position: relative; width: 100%; }
        .password-wrapper input { padding-right: 45px; }
        .eye-icon { position: absolute; right: 14px; top: 50%; transform: translateY(-50%); color: var(--text-muted); cursor: pointer; display: flex; align-items: center; transition: 0.3s; padding: 5px;}
        .eye-icon:hover { color: var(--primary); }
        
        /* Modern Radio Kartları - Grid Uyumlu */
        .role-selection { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; width: 100%;}
        .radio-container { width: 100%; position: relative; cursor: pointer; }
        .radio-container input { display: none; }
        .radio-card { padding: 16px; border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 12px; text-align: center; color: var(--text-muted); transition: 0.3s; background: rgba(15, 23, 42, 0.8); font-weight: bold; font-size: 14px; display: flex; align-items: center; justify-content: center; gap: 8px;}
        
        .radio-container input:checked + .radio-card { border-color: var(--primary); color: white; background: rgba(16, 185, 129, 0.15); box-shadow: 0 0 15px rgba(16, 185, 129, 0.3); }
        .radio-container.admin input:checked + .radio-card { border-color: var(--secondary); color: white; background: rgba(59, 130, 246, 0.15); box-shadow: 0 0 15px rgba(59, 130, 246, 0.3); }

        #secret_code_container { display: none; padding: 20px; background: rgba(245, 158, 11, 0.05); border-radius: 12px; animation: fadeIn 0.3s ease-in-out; border: 1px solid rgba(245, 158, 11, 0.2); border-left: 4px solid #f59e0b; }
        #secret_code_container label { color: #fbbf24; }
        #secret_code_container input:focus { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.2); }
        
        @keyframes fadeIn { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }
        
        .btn-submit { width: 100%; padding: 18px; border: none; border-radius: 12px; background: linear-gradient(135deg, #059669, #10b981); color: white; font-size: 16px; font-weight: 800; letter-spacing: 0.5px; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4); margin-top: 10px;}
        .btn-submit:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(16, 185, 129, 0.6); filter: brightness(1.1);}
        
        .alert { padding: 16px; border-radius: 10px; text-align: center; margin-bottom: 25px; font-size: 14px; font-weight: 600; line-height: 1.5; }
        .error { background-color: rgba(239, 68, 68, 0.1); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.2); }
        .success { background-color: rgba(16, 185, 129, 0.1); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.2); }
        
        .back-link { display: flex; align-items: center; justify-content: center; gap: 5px; width: 100%; margin-top: 25px; color: var(--text-muted); font-size: 14px; text-decoration: none; transition: 0.3s; font-weight: 500;}
        .back-link:hover { color: white; }
        .back-link svg { width: 16px; height: 16px; transition: 0.3s; }
        .back-link:hover svg { transform: translateX(-4px); }

        /* KUSURSUZ MOBİL UYUM EKRANI */
        @media (max-width: 600px) {
            body { padding: 20px 15px; }
            .register-card { padding: 35px 20px; border-radius: 16px; }
            
            /* İşte Dengesizliği Çözen Yer: Tüm kutuları tam genişlikte alt alta dizer */
            .form-grid { grid-template-columns: 1fr; gap: 16px; }
            .form-group.full-width { grid-column: span 1; }
            .role-selection { grid-template-columns: 1fr; gap: 12px; }
            
            .header h2 { font-size: 28px; }
            .logo-img { height: 44px; width: 44px; }
            input { padding: 14px 16px; }
            button { padding: 16px; }
        }
    </style>
</head>
<body>

<div class="orb-1"></div>
<div class="orb-2"></div>

<div class="register-card">
    <div class="header">
        <div class="logo-container">
            <img src="logo.png" alt="SahaRez Logo" class="logo-img" onerror="this.style.display='none'">
            <h2>Saha<span>Rez</span></h2>
        </div>
        <p>Sisteme Katılmak İçin Hesabınızı Oluşturun</p>
    </div>
    
    <?php if(!empty($message)) echo $message; ?>

    <?php if(strpos($message, 'başarıyla') === false): ?>
    <form action="register.php" method="POST">
        
        <!-- YENİ CSS GRID MİMARİSİ -->
        <div class="form-grid">
            <div class="form-group">
                <label>Adınız</label>
                <input type="text" name="first_name" placeholder="Örn: Eren" required autocomplete="off">
            </div>
            <div class="form-group">
                <label>Soyadınız</label>
                <input type="text" name="last_name" placeholder="Örn: Kadiroğlu" required autocomplete="off">
            </div>
            
            <div class="form-group full-width">
                <label>Kullanıcı Adı (Sisteme Giriş İçin)</label>
                <input type="text" name="username" placeholder="Sisteme girerken kullanacağınız isim" required autocomplete="off">
            </div>

            <div class="form-group">
                <label>Şifre</label>
                <div class="password-wrapper">
                    <input type="password" id="pass1" name="password" placeholder="••••••••" required>
                    <span class="eye-icon" id="eye1" onclick="togglePassword('pass1', 'eye1')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                            <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                    </span>
                </div>
            </div>
            <div class="form-group">
                <label>Şifre Tekrar</label>
                <div class="password-wrapper">
                    <input type="password" id="pass2" name="password_confirm" placeholder="••••••••" required>
                    <span class="eye-icon" id="eye2" onclick="togglePassword('pass2', 'eye2')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                            <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                    </span>
                </div>
            </div>

            <div class="form-group">
                <label>E-Mail Adresi</label>
                <input type="email" name="email" placeholder="ornek@mail.com" required>
            </div>
            <div class="form-group">
                <label>Cep Telefonu</label>
                <input type="tel" name="phone" placeholder="05XX XXX XX XX" required>
            </div>

            <div class="form-group full-width">
                <label>Hesap Türünüzü Seçin</label>
                <div class="role-selection">
                    <label class="radio-container">
                        <input type="radio" name="account_type" value="PLAYER" checked onclick="toggleSecretCode()">
                        <div class="radio-card">⚽ Oyuncu</div>
                    </label>
                    <label class="radio-container admin">
                        <input type="radio" name="account_type" value="ADMIN" onclick="toggleSecretCode()">
                        <div class="radio-card">💼 Saha Görevlisi</div>
                    </label>
                </div>
            </div>

            <div class="form-group full-width" id="secret_code_container">
                <label>Saha Görevlisi Yetki Kodu</label>
                <div class="password-wrapper">
                    <input type="password" id="pass3" name="secret_code" placeholder="Saha sahibinden aldığınız özel şifre">
                    <span class="eye-icon" id="eye3" onclick="togglePassword('pass3', 'eye3')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                            <line x1="1" y1="1" x2="23" y2="23"></line>
                        </svg>
                    </span>
                </div>
            </div>
        </div> <!-- Form Grid Bitişi -->
        
        <button type="submit" class="btn-submit">Sisteme Kayıt Ol</button>
    </form>
    <?php endif; ?>
    
    <a href="../" class="back-link">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
        Ana Sayfaya Dön
    </a>
</div>

<script>
    const eyeOpenSVG = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
    const eyeClosedSVG = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`;

    function togglePassword(inputId, iconId) {
        const input = document.getElementById(inputId);
        const icon = document.getElementById(iconId);
        
        if (input.type === "password") {
            input.type = "text"; 
            icon.innerHTML = eyeOpenSVG; 
            icon.style.color = "#10b981"; 
        } else {
            input.type = "password"; 
            icon.innerHTML = eyeClosedSVG; 
            icon.style.color = "#94a3b8"; 
        }
    }

    function toggleSecretCode() {
        var adminRadio = document.querySelector('input[name="account_type"][value="ADMIN"]');
        var secretCodeContainer = document.getElementById('secret_code_container');
        var secretCodeInput = document.getElementById('pass3');

        if (adminRadio.checked) {
            secretCodeContainer.style.display = 'block';
            secretCodeInput.required = true; 
        } else {
            secretCodeContainer.style.display = 'none';
            secretCodeInput.required = false; 
            secretCodeInput.value = ""; 
        }
    }
</script>

</body>
</html>