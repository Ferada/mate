-- phpMyAdmin SQL Dump
-- version 3.2.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 14. Dezember 2010 um 18:44
-- Server Version: 5.1.41
-- PHP-Version: 5.3.1

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `mate`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mate_channelpriorities`
--

CREATE TABLE IF NOT EXISTS `mate_channelpriorities` (
  `id` int(11) NOT NULL,
  `username` text NOT NULL,
  `channel` text NOT NULL,
  `priority` int(11) NOT NULL,
  `interruptible` int(11) NOT NULL,
  `location` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `mate_channelpriorities`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mate_deviceaccesses`
--

CREATE TABLE IF NOT EXISTS `mate_deviceaccesses` (
  `id` int(11) NOT NULL,
  `device` text NOT NULL,
  `attribute` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `mate_deviceaccesses`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mate_privacy`
--

CREATE TABLE IF NOT EXISTS `mate_privacy` (
  `id` int(11) NOT NULL,
  `subject` text NOT NULL,
  `object` text NOT NULL,
  `entity` text NOT NULL,
  `value` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `mate_privacy`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mate_roomdevices`
--

CREATE TABLE IF NOT EXISTS `mate_roomdevices` (
  `id` int(11) NOT NULL,
  `room_id` text NOT NULL,
  `channel` text NOT NULL,
  `jid` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `mate_roomdevices`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mate_userdata`
--

CREATE TABLE IF NOT EXISTS `mate_userdata` (
  `username` text NOT NULL,
  `forename` text NOT NULL,
  `surname` text NOT NULL,
  `id` int(11) NOT NULL,
  `cc` text NOT NULL,
  `room_id` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
  KEY `id_2` (`id`),
  KEY `id_3` (`id`),
  KEY `id_4` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `mate_userdata`
--


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `mate_userdevices`
--

CREATE TABLE IF NOT EXISTS `mate_userdevices` (
  `id` int(11) NOT NULL,
  `username` text NOT NULL,
  `channel` text NOT NULL,
  `jid` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id` (`id`),
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `mate_userdevices`
--


/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
