-- phpMyAdmin SQL Dump
-- version 3.2.4
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 14. Dezember 2010 um 18:44
-- Server Version: 5.1.41
-- PHP-Version: 5.3.1

DROP TABLE mate_channelpriorities;
DROP TABLE mate_deviceaccesses;
DROP TABLE mate_privacy;
DROP TABLE mate_roomdevices;
DROP TABLE mate_userdata;
DROP TABLE mate_userdevices;

--
-- Datenbank: mate
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle mate_channelpriorities
--

CREATE TABLE mate_channelpriorities (
  id int NOT NULL,
  username text NOT NULL,
  channel text NOT NULL,
  priority int NOT NULL,
  interruptible int NOT NULL,
  location text NOT NULL,
  PRIMARY KEY (id)
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle mate_deviceaccesses
--

CREATE TABLE mate_deviceaccesses (
  id int NOT NULL,
  device text NOT NULL,
  attribute text NOT NULL,
  PRIMARY KEY (id)
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle mate_privacy
--

CREATE TABLE mate_privacy (
  id int NOT NULL,
  subject text NOT NULL,
  object text NOT NULL,
  entity text NOT NULL,
  value int NOT NULL,
  PRIMARY KEY (id)
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle mate_roomdevices
--

CREATE TABLE mate_roomdevices (
  id int NOT NULL,
  room_id text NOT NULL,
  channel text NOT NULL,
  jid text NOT NULL,
  PRIMARY KEY (id)
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle mate_userdata
--

CREATE TABLE mate_userdata (
  id int NOT NULL,
  username text NOT NULL,
  forename text NOT NULL,
  surname text NOT NULL,
  cc text NOT NULL,
  room_id text NOT NULL,
  PRIMARY KEY (id)
);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle mate_userdevices
--

CREATE TABLE mate_userdevices (
  id int NOT NULL,
  username text NOT NULL,
  channel text NOT NULL,
  jid text NOT NULL,
  PRIMARY KEY (id)
);
