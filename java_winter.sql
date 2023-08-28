-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 28, 2023 at 05:05 PM
-- Server version: 10.4.22-MariaDB
-- PHP Version: 8.0.13

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `java_winter`
--

-- --------------------------------------------------------

--
-- Table structure for table `autor`
--

CREATE TABLE `autor` (
  `id` int(11) NOT NULL,
  `jmeno` varchar(100) COLLATE utf8_czech_ci NOT NULL,
  `narodnost` varchar(50) COLLATE utf8_czech_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

--
-- Dumping data for table `autor`
--

INSERT INTO `autor` (`id`, `jmeno`, `narodnost`) VALUES
(1, 'Karel Čapek', 'Czechia'),
(2, 'William Shakespeare', 'British'),
(3, 'Božena Němcová', 'Czechia'),
(4, 'Nguyen Nhat Anh', 'Vietnam'),
(9, 'Ernest Hemingway', 'American'),
(14, 'Autor test', 'American'),
(25, 'autor test 2', 'American Samoa'),
(26, 'asdasda', 'Algeria');

-- --------------------------------------------------------

--
-- Stand-in structure for view `document`
-- (See below for the actual view)
--
CREATE TABLE `document` (
`dokladID` int(11)
,`nazev` varchar(255)
,`datum` timestamp
,`datumTo` timestamp
,`amount` int(11)
,`jmeno` varchar(100)
,`zakaznikID` int(11)
,`bookID` int(11)
);

-- --------------------------------------------------------

--
-- Table structure for table `doklad`
--

CREATE TABLE `doklad` (
  `id` int(11) NOT NULL,
  `datum` timestamp NOT NULL DEFAULT current_timestamp(),
  `datumTo` timestamp NULL DEFAULT NULL,
  `totalPrice` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

--
-- Dumping data for table `doklad`
--

INSERT INTO `doklad` (`id`, `datum`, `datumTo`, `totalPrice`) VALUES
(46, '2023-07-27 13:34:40', NULL, 1320),
(51, '2023-07-28 13:56:30', NULL, 796),
(55, '2023-07-28 14:36:23', NULL, 0),
(62, '2023-07-29 18:33:26', '2023-07-30 22:00:00', 3234),
(71, '2023-08-15 10:07:53', '2023-08-23 22:00:00', 30),
(72, '2023-08-28 14:58:13', '2023-08-30 22:00:00', 3111),
(73, '2023-08-28 15:04:12', '2023-08-30 22:00:00', 1995);

-- --------------------------------------------------------

--
-- Table structure for table `doklad_kniha`
--

CREATE TABLE `doklad_kniha` (
  `id` int(11) NOT NULL,
  `id_doklad` int(11) NOT NULL,
  `id_kniha` int(11) NOT NULL,
  `amount` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

--
-- Dumping data for table `doklad_kniha`
--

INSERT INTO `doklad_kniha` (`id`, `id_doklad`, `id_kniha`, `amount`) VALUES
(58, 46, 42, 1),
(59, 46, 37, 1),
(60, 46, 13, 1),
(71, 51, 4, 4),
(87, 62, 46, 1),
(88, 62, 45, 1),
(91, 64, 46, 1),
(92, 65, 45, 1),
(93, 66, 4, 1),
(94, 67, 37, 1),
(95, 68, 4, 1),
(96, 69, 3, 1),
(97, 70, 3, 1),
(98, 71, 5, 1),
(99, 72, 45, 1),
(100, 73, 4, 5),
(101, 73, 3, 5);

-- --------------------------------------------------------

--
-- Table structure for table `doklad_zakaznik`
--

CREATE TABLE `doklad_zakaznik` (
  `id` int(11) NOT NULL,
  `id_doklad` int(11) NOT NULL,
  `id_zakaznik` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

--
-- Dumping data for table `doklad_zakaznik`
--

INSERT INTO `doklad_zakaznik` (`id`, `id_doklad`, `id_zakaznik`) VALUES
(26, 46, 3),
(31, 51, 5),
(35, 55, 1),
(42, 62, 4),
(51, 71, 1),
(52, 72, 1),
(53, 73, 2);

-- --------------------------------------------------------

--
-- Table structure for table `kniha`
--

CREATE TABLE `kniha` (
  `id` int(11) NOT NULL,
  `nazev` varchar(255) COLLATE utf8_czech_ci NOT NULL,
  `rok_vydani` smallint(4) NOT NULL,
  `cena` double NOT NULL,
  `zanr` varchar(255) COLLATE utf8_czech_ci NOT NULL,
  `amount` int(11) NOT NULL,
  `popis` text COLLATE utf8_czech_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

--
-- Dumping data for table `kniha`
--

INSERT INTO `kniha` (`id`, `nazev`, `rok_vydani`, `cena`, `zanr`, `amount`, `popis`) VALUES
(2, 'Bílá nemoc', 1937, 100, 'Drama', 100, 'Bílá nemoc je divadelní hra – drama Karla Čapka z roku 1937. Dílo varuje před nastupujícím fašismem.'),
(3, 'Romeo and Juliet', 1597, 200, 'Drama', 92, 'Romeo a Julie (Romeo and Juliet) je divadelní hra, kterou napsal William Shakespeare. Premiéru měla v roce 1595. Jedná se o milostnou tragédii, patrně jeden z nejznámějších milostných příběhů v historii světového dramatu.'),
(4, 'Mat Biec', 1990, 199, 'Drama', 93, 'Mat Biec je román spisovatele Nguyen Nhat Anh v této autorské sérii příběhů o lásce teenagerů s Malým ďáblem, Dívka ze včerejška,... '),
(5, 'Babička', 1855, 30, 'Historický', 104, 'Babička je novela české spisovatelky Boženy Němcové z roku 1855. Je jejím nejoblíbenějším dílem a je považována za klasiku české literatury. '),
(13, 'Stařec a moře', 1946, 220, 'Drama', 122, 'Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document or a typeface without relying on meaningful content. '),
(37, 'Kniha test', 110, 100, 'Horor', 93, 'popis test'),
(42, 'test 2', 1999, 1000, 'Horor', 203, 'Hello Hello'),
(43, 'kniha 1', 2100, 200, 'Romantika', 0, 'In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document or a typeface without relying on meaningful content. Lorem ipsum may be used as a placeholder before final copy is available'),
(45, 'asdfasdfasdfa', 1111, 3111, 'Akční', 996, '5645'),
(46, 'khina adfsdf', 123, 123, 'Fantasy', 1234, '123qwedq');

-- --------------------------------------------------------

--
-- Table structure for table `kniha_autor`
--

CREATE TABLE `kniha_autor` (
  `id` int(11) NOT NULL,
  `id_kniha` int(11) NOT NULL,
  `id_autor` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

--
-- Dumping data for table `kniha_autor`
--

INSERT INTO `kniha_autor` (`id`, `id_kniha`, `id_autor`) VALUES
(84, 37, 1),
(90, 3, 2),
(91, 4, 4),
(92, 5, 14),
(94, 13, 9),
(100, 37, 3),
(128, 42, 2),
(129, 42, 3),
(130, 43, 14),
(131, 43, 25),
(132, 44, 1),
(133, 44, 3),
(134, 45, 1),
(135, 45, 2),
(136, 46, 1),
(137, 46, 2),
(138, 2, 1);

-- --------------------------------------------------------

--
-- Stand-in structure for view `kniha_autor_view`
-- (See below for the actual view)
--
CREATE TABLE `kniha_autor_view` (
`id` int(11)
,`nazev` varchar(255)
,`cena` double
,`amount` int(11)
,`jmeno` varchar(100)
,`autor_id` int(11)
);

-- --------------------------------------------------------

--
-- Table structure for table `zakaznik`
--

CREATE TABLE `zakaznik` (
  `id` int(11) NOT NULL,
  `jmeno` varchar(100) COLLATE utf8_czech_ci NOT NULL,
  `datum_narozeni` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

--
-- Dumping data for table `zakaznik`
--

INSERT INTO `zakaznik` (`id`, `jmeno`, `datum_narozeni`) VALUES
(1, 'Petr Novák', '1976-02-14'),
(2, 'Jan Kovák', '1988-02-15'),
(3, 'Marie Novotná', '1999-08-24'),
(4, 'Anna Nováková', '1991-05-21'),
(5, 'Duong Xuan Anh', '1998-09-30'),
(6, 'Zakaznik 1', '1977-04-01'),
(25, 'za1', '2023-07-28');

-- --------------------------------------------------------

--
-- Structure for view `document`
--
DROP TABLE IF EXISTS `document`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `document`  AS SELECT `doklad`.`id` AS `dokladID`, `kniha`.`nazev` AS `nazev`, `doklad`.`datum` AS `datum`, `doklad`.`datumTo` AS `datumTo`, `kd`.`amount` AS `amount`, `zakaznik`.`jmeno` AS `jmeno`, `zakaznik`.`id` AS `zakaznikID`, `kniha`.`id` AS `bookID` FROM ((((`doklad` join `doklad_kniha` `kd` on(`doklad`.`id` = `kd`.`id_doklad`)) join `kniha` on(`kniha`.`id` = `kd`.`id_kniha`)) join `doklad_zakaznik` `dz` on(`dz`.`id_doklad` = `doklad`.`id`)) join `zakaznik` on(`zakaznik`.`id` = `dz`.`id_zakaznik`)) WHERE `doklad`.`datumTo` is not null ;

-- --------------------------------------------------------

--
-- Structure for view `kniha_autor_view`
--
DROP TABLE IF EXISTS `kniha_autor_view`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `kniha_autor_view`  AS SELECT `k`.`id` AS `id`, `k`.`nazev` AS `nazev`, `k`.`cena` AS `cena`, `k`.`amount` AS `amount`, `a`.`jmeno` AS `jmeno`, `a`.`id` AS `autor_id` FROM ((`kniha` `k` join `kniha_autor` `ka` on(`k`.`id` = `ka`.`id_kniha`)) join `autor` `a` on(`a`.`id` = `ka`.`id_autor`)) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `autor`
--
ALTER TABLE `autor`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `doklad`
--
ALTER TABLE `doklad`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `doklad_kniha`
--
ALTER TABLE `doklad_kniha`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `doklad_zakaznik`
--
ALTER TABLE `doklad_zakaznik`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `kniha`
--
ALTER TABLE `kniha`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `kniha_autor`
--
ALTER TABLE `kniha_autor`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `zakaznik`
--
ALTER TABLE `zakaznik`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `autor`
--
ALTER TABLE `autor`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT for table `doklad`
--
ALTER TABLE `doklad`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=74;

--
-- AUTO_INCREMENT for table `doklad_kniha`
--
ALTER TABLE `doklad_kniha`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=102;

--
-- AUTO_INCREMENT for table `doklad_zakaznik`
--
ALTER TABLE `doklad_zakaznik`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;

--
-- AUTO_INCREMENT for table `kniha`
--
ALTER TABLE `kniha`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- AUTO_INCREMENT for table `kniha_autor`
--
ALTER TABLE `kniha_autor`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=139;

--
-- AUTO_INCREMENT for table `zakaznik`
--
ALTER TABLE `zakaznik`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
