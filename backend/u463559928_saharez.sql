-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Anamakine: 127.0.0.1:3306
-- Üretim Zamanı: 10 Haz 2026, 17:55:56
-- Sunucu sürümü: 11.8.6-MariaDB-log
-- PHP Sürümü: 7.2.34

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Veritabanı: `u463559928_saharez`
--

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `bookings`
--

CREATE TABLE `bookings` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `match_date` varchar(20) DEFAULT NULL,
  `time_slot` varchar(20) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `has_shuttle` tinyint(1) DEFAULT 0,
  `created_at` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `bookings`
--

INSERT INTO `bookings` (`id`, `user_id`, `match_date`, `time_slot`, `full_name`, `phone`, `has_shuttle`, `created_at`) VALUES
(1, 1, '08.06.2026', '20.00 - 21.00', 'Eren KADİROĞLU', '05458448676', 1, '08.06.2026 17:13:11'),
(2, 1, '08.06.2026', '23.00 - 00.00', 'asd', '123', 0, '10.06.2026 19:37:30'),
(3, 1, '08.06.2026', '17.00 - 18.00', 'offlıne deneme', '123', 0, '09.06.2026 01:56:11'),
(4, 1, '08.06.2026', '13.00 - 14.00', 'deneme offlıne', '1234', 0, '09.06.2026 01:56:11'),
(5, 1, '08.06.2026', '15.00 - 16.00', 'deneme3', '12356', 0, '09.06.2026 01:56:12'),
(6, 1, '08.06.2026', '18.00 - 19.00', 'asdad', '124124', 0, '09.06.2026 01:56:12'),
(7, 1, '08.06.2026', '21.00 - 22.00', 'a1', '123', 0, '09.06.2026 01:56:12'),
(8, 1, '09.06.2026', '23.00 - 00.00', 'Muhammet ATALAY', '123123123', 1, '10.06.2026 19:36:36'),
(9, 1, '09.06.2026', '19.00 - 20.00', 'Zeki Ergün', '11111', 0, '09.06.2026 14:53:33'),
(13, 1, '10.06.2026', '23.00 - 00.00', 'Mehmet Zeki Ergün', '123123123', 0, '10.06.2026 19:56:15');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `match_ads`
--

CREATE TABLE `match_ads` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `match_date` varchar(15) NOT NULL,
  `time_slot` varchar(20) NOT NULL,
  `ad_type` varchar(50) NOT NULL,
  `missing_count` varchar(10) DEFAULT NULL,
  `missing_positions` varchar(100) DEFAULT NULL,
  `created_at` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Tablo döküm verisi `match_ads`
--

INSERT INTO `match_ads` (`id`, `user_id`, `match_date`, `time_slot`, `ad_type`, `missing_count`, `missing_positions`, `created_at`) VALUES
(1, 2, '08.06.2026', '20.00 - 21.00', 'OYUNCU EKSİK', '1', 'Forvet', '09.06.2026 15:39:39'),
(2, 5, '10.06.2026', '23.00 - 00.00', 'RAKİP ARANIYOR', '', '', '09.06.2026 15:50:11');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `payments`
--

CREATE TABLE `payments` (
  `id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `player_name` varchar(100) NOT NULL,
  `rented_cleats` tinyint(1) DEFAULT 0,
  `buffet_expense` decimal(10,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `payments`
--

INSERT INTO `payments` (`id`, `booking_id`, `player_name`, `rented_cleats`, `buffet_expense`, `created_at`) VALUES
(41, 13, 'a1', 0, 20.00, '2026-06-10 16:56:15');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` varchar(20) DEFAULT 'PLAYER',
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Tablo döküm verisi `users`
--

INSERT INTO `users` (`id`, `full_name`, `username`, `password`, `email`, `phone`, `role`, `created_at`) VALUES
(1, 'Eren KADİROĞLU', 'Admin', '123456', 'erenkdroglu@gmail.com', '05458448676', 'ADMIN', '2026-06-08 00:11:51'),
(2, 'Eren KADİROĞLU', 'Eren', '123456', 'eren_kdroglu@gmail.com', '05458448676', 'PLAYER', '2026-06-08 08:09:15'),
(3, 'User1Ad User1Soyad', 'User1', '123456', 'user1@gmail.com', '01234567890', 'PLAYER', '2026-06-08 08:12:36'),
(4, 'User2Ad User2Soyad', 'User2', '123456', 'user2@gmail.com', '01234567890', 'PLAYER', '2026-06-08 14:54:33'),
(5, 'Mehmet Zeki Ergün', 'Zekiergun', '160321', 'mehmetzekiergun445@gmail.con', '05374292544', 'PLAYER', '2026-06-09 12:47:10');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `waiting_list`
--

CREATE TABLE `waiting_list` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `match_date` varchar(15) NOT NULL,
  `time_slot` varchar(20) NOT NULL,
  `status` varchar(20) DEFAULT 'WAITING',
  `created_at` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Tablo döküm verisi `waiting_list`
--

INSERT INTO `waiting_list` (`id`, `user_id`, `match_date`, `time_slot`, `status`, `created_at`) VALUES
(2, 4, '08.06.2026', '20.00 - 21.00', 'WAITING', '08.06.2026 17:54:52'),
(3, 4, '08.06.2026', '23.00 - 00.00', 'WAITING', '08.06.2026 17:55:06'),
(4, 2, '08.06.2026', '23.00 - 00.00', 'WAITING', '09.06.2026 15:39:50'),
(5, 5, '08.06.2026', '20.00 - 21.00', 'WAITING', '10.06.2026 10:23:02'),
(6, 5, '08.06.2026', '21.00 - 22.00', 'WAITING', '10.06.2026 10:23:06'),
(7, 5, '08.06.2026', '18.00 - 19.00', 'WAITING', '10.06.2026 10:23:09'),
(8, 5, '08.06.2026', '17.00 - 18.00', 'WAITING', '10.06.2026 10:23:10'),
(9, 5, '08.06.2026', '15.00 - 16.00', 'WAITING', '10.06.2026 10:23:16'),
(10, 5, '08.06.2026', '13.00 - 14.00', 'WAITING', '10.06.2026 10:23:21'),
(11, 5, '08.06.2026', '23.00 - 00.00', 'WAITING', '10.06.2026 10:23:29');

--
-- Dökümü yapılmış tablolar için indeksler
--

--
-- Tablo için indeksler `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Tablo için indeksler `match_ads`
--
ALTER TABLE `match_ads`
  ADD PRIMARY KEY (`id`);

--
-- Tablo için indeksler `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `booking_id` (`booking_id`);

--
-- Tablo için indeksler `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- Tablo için indeksler `waiting_list`
--
ALTER TABLE `waiting_list`
  ADD PRIMARY KEY (`id`);

--
-- Dökümü yapılmış tablolar için AUTO_INCREMENT değeri
--

--
-- Tablo için AUTO_INCREMENT değeri `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Tablo için AUTO_INCREMENT değeri `match_ads`
--
ALTER TABLE `match_ads`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Tablo için AUTO_INCREMENT değeri `payments`
--
ALTER TABLE `payments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- Tablo için AUTO_INCREMENT değeri `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Tablo için AUTO_INCREMENT değeri `waiting_list`
--
ALTER TABLE `waiting_list`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- Dökümü yapılmış tablolar için kısıtlamalar
--

--
-- Tablo kısıtlamaları `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Tablo kısıtlamaları `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
