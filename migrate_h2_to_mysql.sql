-- =============================================================
-- Vehicle Management System: H2 → MySQL Migration Script
-- Target database: vehicle_main
-- =============================================================
-- Run with:
--   mysql -u root -p vehicle_main < migrate_h2_to_mysql.sql
-- OR inside MySQL client:
--   SOURCE /path/to/migrate_h2_to_mysql.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS `vehicle_main`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `vehicle_main`;

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- TABLE: users
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id`          VARCHAR(36)  NOT NULL,
    `email`       VARCHAR(255),
    `password`    VARCHAR(255) NOT NULL,
    `name`        VARCHAR(255) NOT NULL,
    `currency`    VARCHAR(255),
    `logo_url`    VARCHAR(255),
    `created_at`  DATETIME(6),
    `updated_at`  DATETIME(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_users_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: vehicles
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `vehicles` (
    `id`                    VARCHAR(36)  NOT NULL,
    `name`                  VARCHAR(255) NOT NULL,
    `brand`                 VARCHAR(255),
    `model`                 VARCHAR(255),
    `model_year`            INT,
    `license_plate`         VARCHAR(255),
    `color`                 VARCHAR(255),
    `currency`              VARCHAR(255),
    `initial_odometer`      INT,
    `current_odometer`      INT,
    `last_service_odometer` INT,
    `user_id`               VARCHAR(36)  NOT NULL,
    `created_at`            DATETIME(6),
    `updated_at`            DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_vehicles_user_id` (`user_id`),
    CONSTRAINT `FK_vehicles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: vehicle_access
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `vehicle_access` (
    `id`            VARCHAR(36)  NOT NULL,
    `user_id`       VARCHAR(36)  NOT NULL,
    `vehicle_id`    VARCHAR(36)  NOT NULL,
    `access_status` VARCHAR(255) NOT NULL,
    `created_at`    DATETIME(6),
    `updated_at`    DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_vehicle_access_user_id` (`user_id`),
    KEY `FK_vehicle_access_vehicle_id` (`vehicle_id`),
    CONSTRAINT `FK_vehicle_access_user`    FOREIGN KEY (`user_id`)    REFERENCES `users` (`id`),
    CONSTRAINT `FK_vehicle_access_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`id`),
    CONSTRAINT `CHK_vehicle_access_status` CHECK (`access_status` IN ('PENDING', 'APPROVED', 'REJECTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: fuel_logs
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fuel_logs` (
    `id`           VARCHAR(36) NOT NULL,
    `odometer`     INT,
    `fuel_amount`  DOUBLE,
    `total_cost`   DOUBLE,
    `log_date`     DATETIME(6),
    `fuel_economy` DOUBLE,
    `vehicle_id`   VARCHAR(36),
    `created_at`   DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_fuel_logs_vehicle_id` (`vehicle_id`),
    CONSTRAINT `FK_fuel_logs_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: maintenance_logs
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `maintenance_logs` (
    `id`           VARCHAR(36)  NOT NULL,
    `vehicle_id`   VARCHAR(36)  NOT NULL,
    `service_name` VARCHAR(255) NOT NULL,
    `description`  TEXT,
    `type`         VARCHAR(255) NOT NULL,
    `odometer`     INT          NOT NULL,
    `cost`         DOUBLE       NOT NULL,
    `service_date` DATETIME(6)  NOT NULL,
    `created_at`   DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_maintenance_logs_vehicle_id` (`vehicle_id`),
    CONSTRAINT `FK_maintenance_logs_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`id`),
    CONSTRAINT `CHK_maintenance_logs_type`   CHECK (`type` IN ('PREVENTIVE', 'CORRECTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: maintenance_settings
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `maintenance_settings` (
    `id`           VARCHAR(36) NOT NULL,
    `vehicle_id`   VARCHAR(36),
    `interval_km`  INT,
    `last_service` DATETIME(6),
    `next_service` DATETIME(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_maintenance_settings_vehicle_id` (`vehicle_id`),
    CONSTRAINT `FK_maintenance_settings_vehicle` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: meters
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `meters` (
    `id`          VARCHAR(36)  NOT NULL,
    `owner_id`    VARCHAR(36)  NOT NULL,
    `name`        VARCHAR(255) NOT NULL,
    `identifier`  VARCHAR(255) NOT NULL,
    `description` VARCHAR(255),
    `created_at`  DATETIME(6),
    `updated_at`  DATETIME(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_meters_identifier` (`identifier`),
    KEY `FK_meters_owner_id` (`owner_id`),
    CONSTRAINT `FK_meters_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: meter_readings
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `meter_readings` (
    `id`                 VARCHAR(36)    NOT NULL,
    `meter_id`           VARCHAR(36)    NOT NULL,
    `reading_date`       DATE           NOT NULL,
    `reading_value`      DECIMAL(12, 2) NOT NULL,
    `consumption`        DECIMAL(12, 2),
    `notes`              TEXT,
    `recorded_by`        VARCHAR(36)    NOT NULL,
    `recorded_by_manual` VARCHAR(255),
    `created_at`         DATETIME(6),
    `updated_at`         DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_meter_readings_meter_id` (`meter_id`),
    KEY `FK_meter_readings_recorded_by` (`recorded_by`),
    CONSTRAINT `FK_meter_readings_meter` FOREIGN KEY (`meter_id`)    REFERENCES `meters` (`id`),
    CONSTRAINT `FK_meter_readings_user`  FOREIGN KEY (`recorded_by`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: meter_access
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `meter_access` (
    `id`            VARCHAR(36)  NOT NULL,
    `user_id`       VARCHAR(36)  NOT NULL,
    `meter_id`      VARCHAR(36)  NOT NULL,
    `access_status` VARCHAR(255) NOT NULL,
    `created_at`    DATETIME(6),
    `updated_at`    DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_meter_access_user_id` (`user_id`),
    KEY `FK_meter_access_meter_id` (`meter_id`),
    CONSTRAINT `FK_meter_access_user`   FOREIGN KEY (`user_id`)  REFERENCES `users` (`id`),
    CONSTRAINT `FK_meter_access_meter`  FOREIGN KEY (`meter_id`) REFERENCES `meters` (`id`),
    CONSTRAINT `CHK_meter_access_status` CHECK (`access_status` IN ('PENDING', 'APPROVED', 'REJECTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: budgets
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `budgets` (
    `id`          VARCHAR(36)  NOT NULL,
    `name`        VARCHAR(255) NOT NULL,
    `description` VARCHAR(255),
    `type`        VARCHAR(255) NOT NULL,
    `owner_id`    VARCHAR(36)  NOT NULL,
    `created_at`  DATETIME(6),
    `updated_at`  DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_budgets_owner_id` (`owner_id`),
    CONSTRAINT `FK_budgets_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `CHK_budgets_type` CHECK (`type` IN ('HOME', 'BUSINESS', 'PERSONAL', 'OTHER'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: budget_access
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `budget_access` (
    `id`            VARCHAR(36)  NOT NULL,
    `budget_id`     VARCHAR(36)  NOT NULL,
    `user_id`       VARCHAR(36)  NOT NULL,
    `access_status` VARCHAR(255) NOT NULL,
    `created_at`    DATETIME(6),
    `updated_at`    DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_budget_access_budget_id` (`budget_id`),
    KEY `FK_budget_access_user_id` (`user_id`),
    CONSTRAINT `FK_budget_access_budget` FOREIGN KEY (`budget_id`) REFERENCES `budgets` (`id`),
    CONSTRAINT `FK_budget_access_user`   FOREIGN KEY (`user_id`)   REFERENCES `users` (`id`),
    CONSTRAINT `CHK_budget_access_status` CHECK (`access_status` IN ('PENDING', 'APPROVED', 'REJECTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: budget_categories
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `budget_categories` (
    `id`        VARCHAR(36)  NOT NULL,
    `name`      VARCHAR(255) NOT NULL,
    `color`     VARCHAR(255) NOT NULL,
    `is_system` TINYINT(1)   NOT NULL DEFAULT 0,
    `owner_id`  VARCHAR(36),
    PRIMARY KEY (`id`),
    KEY `FK_budget_categories_owner_id` (`owner_id`),
    CONSTRAINT `FK_budget_categories_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: budget_transactions
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `budget_transactions` (
    `id`          VARCHAR(36)    NOT NULL,
    `budget_id`   VARCHAR(36)    NOT NULL,
    `added_by_id` VARCHAR(36)    NOT NULL,
    `amount`      DECIMAL(15, 2) NOT NULL,
    `type`        VARCHAR(255)   NOT NULL,
    `category`    VARCHAR(255)   NOT NULL,
    `description` VARCHAR(255),
    `date`        DATE           NOT NULL,
    `created_at`  DATETIME(6),
    PRIMARY KEY (`id`),
    KEY `FK_budget_transactions_budget_id` (`budget_id`),
    KEY `FK_budget_transactions_added_by_id` (`added_by_id`),
    CONSTRAINT `FK_budget_transactions_budget` FOREIGN KEY (`budget_id`)   REFERENCES `budgets` (`id`),
    CONSTRAINT `FK_budget_transactions_user`   FOREIGN KEY (`added_by_id`) REFERENCES `users` (`id`),
    CONSTRAINT `CHK_budget_transactions_type`  CHECK (`type` IN ('INCOME', 'EXPENSE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- TABLE: password_reset_tokens
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
    `id`          VARCHAR(36)  NOT NULL,
    `token`       VARCHAR(255) NOT NULL,
    `user_id`     VARCHAR(36)  NOT NULL,
    `expiry_date` DATETIME(6)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_password_reset_tokens_token`   (`token`),
    UNIQUE KEY `UK_password_reset_tokens_user_id` (`user_id`),
    CONSTRAINT `FK_password_reset_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =============================================================
-- DATA MIGRATION
-- (INSERT IGNORE skips rows that already exist by primary key)
-- =============================================================

-- -----------------------------------------------------------
-- users
-- H2 column order: ID, CREATED_AT, EMAIL, NAME, PASSWORD, UPDATED_AT, CURRENCY, LOGO_URL
-- -----------------------------------------------------------
INSERT IGNORE INTO `users`
    (`id`, `email`, `password`, `name`, `currency`, `logo_url`, `created_at`, `updated_at`)
VALUES
('236525df-91c9-43d9-9b9b-1364443f520d', 'test@example.com',                '$2a$10$1RTfVKBAQ8bRLzuGphuY8.Nadv62V4tMlHm5KHP1bL/kh6GRv5v9q', 'Test User',       NULL,  NULL, '2026-03-26 19:47:09.888217', '2026-03-26 19:47:09.888273'),
('6d4055e6-0744-4752-90ae-33062604a7fa', 'italha.saeedsbr@gmail.com',        '$2a$10$qlN1m2/TyW9xIi1HcFMOseJy5nmNnt8zk.OD6S3VnjvphrwS4L0Be', 'talha',           'Rs', NULL, '2026-03-26 19:54:35.420198', '2026-03-31 11:24:01.510662'),
('832fad90-a319-448a-8d01-06068d29c386', 'test_meter@example.com',           '$2a$10$SgCcIZaL7RiaH.qIEeb.3evRgUtUT02Srj6cgIILzKwQ88gMl.Lgq', 'Test Meter User', NULL,  NULL, '2026-03-27 10:00:44.661756', '2026-03-27 10:00:44.661809'),
('a1db7295-1aea-4f78-9319-488ee88a9a8e', 'testcase@example.com',             '$2a$10$u5McPbBsAUd.G2JU17Q7Ju6hpyXWFeGYpb0Pbm2MzIvWl8JV2BM6i', 'Case Test',       NULL,  NULL, '2026-03-27 15:24:22.507901', '2026-03-27 15:24:22.507942'),
('3b39747e-e5bf-4165-b1a7-5261ca0e5107', 'test3@example.com',                '$2a$10$S5LtvIgFasgXJoN/2jlTXeOwLfMp1zIpGDmnfgsGJtKtVNJb8LyTy', 'Test User 3',     NULL,  NULL, '2026-03-28 12:05:56.071796', '2026-03-28 12:05:56.071837'),
('67b630d1-7421-47dd-8a85-80b820f2e68c', 'owner@example.com',                '$2a$10$ppqktfFfWoPS72Ksca75f.DrINVVDL7CzTQ01HulroIr2F0thlFY.', 'Owner',           NULL,  NULL, '2026-03-28 16:02:56.549905', '2026-03-28 16:02:56.549934'),
('2e79e2c9-b45f-4f85-ba9d-4cae49aa8855', 'owner2@example.com',               '$2a$10$bEXCDTOLea0n0K0yTijHEO4TjJoN3Ph/DIxVGNPa8q0hnGoUjUi6m', 'Owner2',          NULL,  NULL, '2026-03-28 16:15:46.355631', '2026-03-28 16:15:46.355652'),
('629f905e-acbe-4235-b643-b2dce593f850', 'owner3@example.com',               '$2a$10$EnyVSuPsFXQTbWVbJt15pOFNsGNX/IAWYKDfmgocUULYfEQmcThjy', 'Owner3',          NULL,  NULL, '2026-03-28 16:17:53.016059', '2026-03-28 16:17:53.01608'),
('e3e6040a-0859-48e2-a3bc-cce366ec06ce', 'collab@example.com',               '$2a$10$EZmoezCNq6WwHrdOQB2.UO86tas2BeC3BlqotOrJvlf0UnZuU2wU6', 'Collab',           NULL,  NULL, '2026-03-28 16:45:17.378719', '2026-03-28 16:45:17.378747'),
('50791e16-a4d3-4a6b-a203-4d7ea7ff9b10', 'life.pulse.test1@example.com',     '$2a$10$X6pMBK1Y0iNJt1ChjdBT8.33iLYyqBsmZA/fs6sRrcbkTCoc8cCRW', 'Life Pulse User', NULL,  NULL, '2026-03-28 18:37:37.169084', '2026-03-28 18:37:37.169103'),
('d4deb720-a34d-4be7-af37-cd4f858fa73c', 'talha@example.com',                '$2a$10$7h93iENfsS5mBUDX9gLiiuTcjvV.WKPBh.mbyS.AwfI0vICcLuCya', 'talha',           '$',   NULL, '2026-03-28 20:14:56.724258', '2026-03-28 20:14:56.724276'),
('961c590f-99f6-4849-a3c6-f8117dccc7de', 'admin@vms.com',                    '$2a$10$/kSTEXubPowc6un8RyUEwOzLdynxtuN3nMNHk/MCF3LAotRArzx8K',  'Admin User',      '$',   NULL, '2026-03-29 11:01:39.865693', '2026-03-29 11:01:39.865712'),
('e7b70edd-9f26-4d82-b65c-49d0f6c48bf0', 'testuser@example.com',             '$2a$10$DvU13jZEdfaR8T3VyCIcJ.ptGWPA/NoPny0MzSe6B5emmqNff5Vm.', 'Test User',       '$',   NULL, '2026-03-29 17:55:17.057181', '2026-03-29 17:55:17.057278'),
('87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c', 'google.user@example.com',          '$2a$10$2MHcAzvu/OLR0BBvvsu9O.0rrO8MGsIlClRginbo2CCMpU33nDCv.', 'Google User',     '$',   NULL, '2026-03-29 18:35:05.776978', '2026-03-29 18:35:05.776997');

-- -----------------------------------------------------------
-- vehicles
-- H2 column order: ID, BRAND, CREATED_AT, CURRENT_ODOMETER, LAST_SERVICE_ODOMETER,
--                  LICENSE_PLATE, MODEL, MODEL_YEAR, NAME, UPDATED_AT, USER_ID,
--                  CURRENCY, INITIAL_ODOMETER, COLOR
-- -----------------------------------------------------------
INSERT IGNORE INTO `vehicles`
    (`id`, `name`, `brand`, `model`, `model_year`, `license_plate`, `color`,
     `currency`, `initial_odometer`, `current_odometer`, `last_service_odometer`,
     `user_id`, `created_at`, `updated_at`)
VALUES
('271e046f-a871-46f6-a5e9-f4d525fa8f69', 'Test Car',    'Toyota',     'Corolla',    20262024, 'TST-1234', NULL,          NULL, NULL,  11500,  10500, '236525df-91c9-43d9-9b9b-1364443f520d', '2026-03-26 19:49:25.737474', '2026-03-30 07:25:07.653431'),
('eea9df2f-9fe9-4825-b7de-85e77c8eec42', 'Euro Car',    'BMW',        'X5',         20262023, 'EUR-555',  NULL,          NULL, NULL,  1150,   1300,  '961c590f-99f6-4849-a3c6-f8117dccc7de', '2026-03-29 11:03:34.143308', '2026-03-29 12:12:45.762012'),
('61e73ddb-9f30-4f45-8950-729c769168a5', 'Economy Test','Honda',      'Civic',      20262022, 'ECO-123',  NULL,          NULL, NULL,  5100,   5000,  '961c590f-99f6-4849-a3c6-f8117dccc7de', '2026-03-29 11:11:59.309757', '2026-03-29 11:13:33.419352'),
('d896478e-a8d5-45ca-b232-6b064ee741bd', 'talha',       'Test Brand', 'Test Model', 2026,     'TAL-1234', NULL,          NULL, NULL,  105240, 105110,'961c590f-99f6-4849-a3c6-f8117dccc7de', '2026-03-29 11:32:44.085804', '2026-03-29 11:34:51.92553'),
('36d5320c-9494-41b5-a42a-b53cf36ea6f2', 'drive',       'toyota',     'camry',      2024,     'ABC-1234', 'white black', '$',  10000, 10000,  10000, '6d4055e6-0744-4752-90ae-33062604a7fa',  '2026-03-30 19:34:10.489643', '2026-03-31 09:15:17.351628');

-- -----------------------------------------------------------
-- vehicle_access
-- H2 column order: ID, ACCESS_STATUS, CREATED_AT, UPDATED_AT, USER_ID, VEHICLE_ID
-- -----------------------------------------------------------
INSERT IGNORE INTO `vehicle_access`
    (`id`, `user_id`, `vehicle_id`, `access_status`, `created_at`, `updated_at`)
VALUES
('507a382c-ca17-427b-ab92-5eb2ad4caa9f', '6d4055e6-0744-4752-90ae-33062604a7fa', '271e046f-a871-46f6-a5e9-f4d525fa8f69', 'APPROVED', '2026-03-26 19:54:44.511996', '2026-03-26 19:54:49.23401');

-- -----------------------------------------------------------
-- fuel_logs
-- H2 column order: ID, CREATED_AT, FUEL_AMOUNT, LOG_DATE, ODOMETER, TOTAL_COST,
--                  VEHICLE_ID, FUEL_ECONOMY
-- -----------------------------------------------------------
INSERT IGNORE INTO `fuel_logs`
    (`id`, `odometer`, `fuel_amount`, `total_cost`, `log_date`, `fuel_economy`,
     `vehicle_id`, `created_at`)
VALUES
('7a222524-83c6-4ad0-b6ef-d73da2323e0c', 10500,  40.0, 60.0,  '2026-03-26 00:00:00',       NULL,                  '271e046f-a871-46f6-a5e9-f4d525fa8f69', '2026-03-26 19:50:18.158084'),
('ecf210dd-a0f3-423c-bdb2-6ccdb4bd5ad1', 11000,  50.0, 100.0, '2026-03-26 00:00:00',       NULL,                  '271e046f-a871-46f6-a5e9-f4d525fa8f69', '2026-03-26 19:55:04.269584'),
('ecdd4543-db21-48b0-9377-198db35a3b1e', 5100,   10.0, 50.0,  '2026-03-29 00:00:00',       NULL,                  '61e73ddb-9f30-4f45-8950-729c769168a5', '2026-03-29 11:13:33.418966'),
('2148e7b2-fccf-4838-ab27-9fd64cca08f7', 105110, 10.0, 10.0,  '2026-03-29 00:00:00',       NULL,                  'd896478e-a8d5-45ca-b232-6b064ee741bd', '2026-03-29 11:34:01.765241'),
('cb1d48c5-1193-49c2-a918-27410cc39d24', 105240, 15.0, 15.0,  '2026-03-29 00:00:00',       NULL,                  'd896478e-a8d5-45ca-b232-6b064ee741bd', '2026-03-29 11:34:51.925132'),
('7152ed16-8133-4ab5-bd2f-550bab1d4b86', 1150,   10.0, 20.0,  '2026-03-29 00:00:00',       NULL,                  'eea9df2f-9fe9-4825-b7de-85e77c8eec42', '2026-03-29 11:46:35.255465'),
('7faa8f62-c644-4d0e-8bed-2e0d31d6b9cd', 11500,  30.0, 120.0, '2026-03-30 04:25:07.621',   33.333333333333336,    '271e046f-a871-46f6-a5e9-f4d525fa8f69', '2026-03-30 07:25:07.652376');

-- -----------------------------------------------------------
-- maintenance_logs
-- H2 column order: ID, COST, CREATED_AT, DESCRIPTION, ODOMETER, SERVICE_DATE,
--                  SERVICE_NAME, TYPE, VEHICLE_ID
-- -----------------------------------------------------------
INSERT IGNORE INTO `maintenance_logs`
    (`id`, `vehicle_id`, `service_name`, `description`, `type`, `odometer`,
     `cost`, `service_date`, `created_at`)
VALUES
('3bbc108d-ce07-4b8e-b50d-011ecd8c38b0', '271e046f-a871-46f6-a5e9-f4d525fa8f69', 'Oil Change', '', 'PREVENTIVE', 10500, 50.0,  '2026-03-26 14:51:00', '2026-03-26 19:51:54.164043'),
('59dbc9b6-54e3-4c0c-9033-83799b9dbe98', 'eea9df2f-9fe9-4825-b7de-85e77c8eec42', 'Oil Change', '', 'PREVENTIVE', 1100,  100.0, '2026-03-29 08:41:00', '2026-03-29 11:42:48.103857'),
('d33b4e86-4020-4e24-862f-085a3001184b', 'eea9df2f-9fe9-4825-b7de-85e77c8eec42', 'Oil Change', '', 'PREVENTIVE', 1200,  100.0, '2026-03-29 09:01:00', '2026-03-29 12:01:39.752048'),
('9e87fc11-e767-4dc2-b823-cc91e5967fbb', 'eea9df2f-9fe9-4825-b7de-85e77c8eec42', 'Oil Change', '', 'PREVENTIVE', 1300,  100.0, '2026-03-29 09:12:00', '2026-03-29 12:12:45.761337');

-- -----------------------------------------------------------
-- meters
-- H2 column order: ID, CREATED_AT, DESCRIPTION, IDENTIFIER, NAME, UPDATED_AT, OWNER_ID
-- -----------------------------------------------------------
INSERT IGNORE INTO `meters`
    (`id`, `owner_id`, `name`, `identifier`, `description`, `created_at`, `updated_at`)
VALUES
('29b92e78-c728-4fde-a8a2-d49ddbd4da93', '832fad90-a319-448a-8d01-06068d29c386', 'Home Meter',       'M-123',       NULL, '2026-03-27 10:03:44.943991', '2026-03-27 10:03:44.944012'),
('496614b6-a116-48a3-adb9-1422a75bce77', '6d4055e6-0744-4752-90ae-33062604a7fa', 'anwar',            '0001',        NULL, '2026-03-27 11:04:03.917479', '2026-03-27 11:04:03.917495'),
('ae72eef4-aa13-4b6b-9bb4-7f689a9d9343', '2e79e2c9-b45f-4f85-ba9d-4cae49aa8855', 'Test Meter 999',   'M-999',       NULL, '2026-03-28 16:40:28.515213', '2026-03-28 16:40:28.515225'),
('c671ed50-360f-45e9-a437-079f6c97bff6', '2e79e2c9-b45f-4f85-ba9d-4cae49aa8855', 'New Test Meter',   'M-123_NEW',   NULL, '2026-03-28 17:02:09.002798', '2026-03-28 17:02:09.002814'),
('584e7a6e-56a6-448e-a104-de54d25e8624', '6d4055e6-0744-4752-90ae-33062604a7fa', 'asif ',            '11',          NULL, '2026-03-29 10:34:13.131286', '2026-03-29 10:34:13.131305'),
('7ae62121-c732-4f9a-b01d-3219eda26691', '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c', 'Test Mobile Meter','TEST-MOB-001',NULL, '2026-03-29 19:05:18.210959', '2026-03-29 19:05:18.210972'),
('54357ed4-666c-4d84-adc9-a05bc0795c60', '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c', 'ASIF',             '002',         NULL, '2026-03-30 06:33:48.711318', '2026-03-30 06:33:48.71133'),
('62211432-87b1-4e22-8dd1-237b400ef3f9', '6d4055e6-0744-4752-90ae-33062604a7fa', 'abdur rehman',     '003',         NULL, '2026-03-30 06:53:30.509313', '2026-03-30 06:53:30.509324'),
('37a7a8cb-35b0-43b8-bff2-baa442a04610', '6d4055e6-0744-4752-90ae-33062604a7fa', 'anwar 2',          '0004',        NULL, '2026-03-30 19:33:43.416644', '2026-03-30 19:33:43.416668');

-- -----------------------------------------------------------
-- meter_readings
-- H2 column order: ID, CONSUMPTION, CREATED_AT, NOTES, READING_DATE, READING_VALUE,
--                  UPDATED_AT, METER_ID, RECORDED_BY, RECORDED_BY_MANUAL
-- -----------------------------------------------------------
INSERT IGNORE INTO `meter_readings`
    (`id`, `meter_id`, `reading_date`, `reading_value`, `consumption`,
     `notes`, `recorded_by`, `recorded_by_manual`, `created_at`, `updated_at`)
VALUES
('0d7adf0d-bd2e-4034-804b-516322c151be', '29b92e78-c728-4fde-a8a2-d49ddbd4da93', '2026-03-27', 1250.00,  0.00,    'Initial reading', '832fad90-a319-448a-8d01-06068d29c386', NULL,          '2026-03-27 10:04:36.029994', '2026-03-27 10:04:36.03001'),
('f650a41b-1ac3-40f7-8f3d-7e5116353415', '29b92e78-c728-4fde-a8a2-d49ddbd4da93', '2026-03-27', 1300.00,  50.00,   '',                '832fad90-a319-448a-8d01-06068d29c386', NULL,          '2026-03-27 10:09:03.572546', '2026-03-27 10:09:03.572554'),
('7ca0a40f-2528-4390-87be-bc7797d9b60f', '496614b6-a116-48a3-adb9-1422a75bce77', '2026-03-28', 1000.00,  0.00,    '',                '6d4055e6-0744-4752-90ae-33062604a7fa',  NULL,          '2026-03-28 21:12:12.10894',  '2026-03-28 21:12:12.108957'),
('3f7f6a84-4bde-45dc-b088-3b63cd8b9610', '496614b6-a116-48a3-adb9-1422a75bce77', '2026-03-29', 1020.00,  20.00,   '',                '6d4055e6-0744-4752-90ae-33062604a7fa',  NULL,          '2026-03-28 21:12:22.614433', '2026-03-28 21:12:22.614444'),
('5d064dbc-3e1d-4b74-96b3-90fdb0ed3dcf', '7ae62121-c732-4f9a-b01d-3219eda26691', '2026-03-29', 200.00,   0.00,    '',                '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c',  'Google User', '2026-03-29 19:09:55.148184', '2026-03-29 19:09:55.148197'),
('62ee86e2-46bb-4600-9b88-579462d8653e', '7ae62121-c732-4f9a-b01d-3219eda26691', '2026-03-29', 250.00,   50.00,   '',                '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c',  'Google User', '2026-03-29 19:10:13.676372', '2026-03-29 19:10:13.67638'),
('8104c628-9fa5-4cbb-a703-7beb0c2bb919', '7ae62121-c732-4f9a-b01d-3219eda26691', '2026-03-30', 1050.00,  850.00,  '',                '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c',  'Google User', '2026-03-30 06:32:44.570132', '2026-03-30 06:32:44.570141'),
('f8621e20-f407-4297-9242-91de6a166e3e', '7ae62121-c732-4f9a-b01d-3219eda26691', '2026-03-30', 1070.00,  20.00,   '',                '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c',  'Google User', '2026-03-30 06:32:49.786977', '2026-03-30 06:32:49.786986'),
('dc2d91ab-7400-4135-92e6-8e75a6464a96', '7ae62121-c732-4f9a-b01d-3219eda26691', '2026-03-30', 1100.00,  50.00,   '',                '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c',  'Google User', '2026-03-30 06:32:53.935632', '2026-03-30 06:32:53.93564'),
('36fddf40-7dca-43d8-bc75-d92e81655fd7', '496614b6-a116-48a3-adb9-1422a75bce77', '2026-03-30', 1030.00,  10.00,   NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-30 06:46:00.232973', '2026-03-30 06:46:00.232981'),
('51dace2c-0d57-4e42-8e5f-d9e1cad49207', '584e7a6e-56a6-448e-a104-de54d25e8624', '2026-03-30', 100.00,   0.00,    NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-30 06:46:00.244313', '2026-03-30 06:46:00.244321'),
('21ce7dae-86ba-4a53-a089-088a5fc20177', '7ae62121-c732-4f9a-b01d-3219eda26691', '2026-03-30', 1070.00,  20.00,   NULL,              '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c',  'Google User', '2026-03-30 16:09:57.935102', '2026-03-30 16:09:57.935118'),
('db644668-6325-4114-ba86-fc9f9a8ab96b', '54357ed4-666c-4d84-adc9-a05bc0795c60', '2026-03-30', 1000.00,  0.00,    NULL,              '87d6c2b0-adf2-4109-8c7c-dae5d3cb1f1c',  'Google User', '2026-03-30 16:09:57.96535',  '2026-03-30 16:09:57.965366'),
('61aee9a5-3ac4-46c7-ad31-9944e1e06b84', '496614b6-a116-48a3-adb9-1422a75bce77', '2026-03-30', 1100.00,  70.00,   NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-30 16:58:25.957357', '2026-03-30 16:58:25.95737'),
('2c9c0c60-708d-4eb9-bcd5-73c8e62db57b', '584e7a6e-56a6-448e-a104-de54d25e8624', '2026-03-30', 1090.00,  990.00,  NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-30 16:58:25.9686',   '2026-03-30 16:58:25.968612'),
('de0e75a8-1e82-4c9d-8f02-b6999a318bbd', '62211432-87b1-4e22-8dd1-237b400ef3f9', '2026-03-30', 1100.00,  0.00,    NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-30 16:58:25.978354', '2026-03-30 16:58:25.978364'),
('31127ecc-989d-4609-bc28-cfba6f32371c', '496614b6-a116-48a3-adb9-1422a75bce77', '2026-03-31', 1200.00,  170.00,  NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-31 10:39:57.630684', '2026-03-31 10:39:57.630747'),
('3ca2f606-e55b-49af-b1f4-d814e730da8c', '584e7a6e-56a6-448e-a104-de54d25e8624', '2026-03-31', 1136.00,  1036.00, NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-31 10:39:57.763447', '2026-03-31 10:39:57.763484'),
('aa4c714e-0358-4035-b83c-0bb9f98e6cc9', '62211432-87b1-4e22-8dd1-237b400ef3f9', '2026-03-31', 1200.00,  100.00,  NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-31 10:39:57.844126', '2026-03-31 10:39:57.844179'),
('a1aa2c69-90ff-46dc-9db8-e89a6ce0e1bd', '37a7a8cb-35b0-43b8-bff2-baa442a04610', '2026-03-31', 1000.00,  0.00,    NULL,              '6d4055e6-0744-4752-90ae-33062604a7fa',  'talha',       '2026-03-31 10:39:57.912432', '2026-03-31 10:39:57.912489');

-- -----------------------------------------------------------
-- meter_access
-- H2 column order: ID, ACCESS_STATUS, CREATED_AT, UPDATED_AT, METER_ID, USER_ID
-- -----------------------------------------------------------
INSERT IGNORE INTO `meter_access`
    (`id`, `user_id`, `meter_id`, `access_status`, `created_at`, `updated_at`)
VALUES
('1b4e9827-d23b-46f8-9918-1e547a38fc0f', 'e3e6040a-0859-48e2-a3bc-cce366ec06ce', 'ae72eef4-aa13-4b6b-9bb4-7f689a9d9343', 'PENDING', '2026-03-28 16:50:00.81691',  '2026-03-28 16:50:00.816925'),
('bc320ca4-4961-4916-b59f-e06e7709f815', 'e3e6040a-0859-48e2-a3bc-cce366ec06ce', 'c671ed50-360f-45e9-a437-079f6c97bff6', 'PENDING', '2026-03-28 17:05:25.270821', '2026-03-28 17:05:25.270844');

-- -----------------------------------------------------------
-- budgets
-- H2 column order: ID, CREATED_AT, DESCRIPTION, NAME, TYPE, UPDATED_AT, OWNER_ID
-- Note: H2 unicode escape U&'my bidget\000a' (0x000A = newline) replaced with 'my bidget\n'
-- -----------------------------------------------------------
INSERT IGNORE INTO `budgets`
    (`id`, `name`, `description`, `type`, `owner_id`, `created_at`, `updated_at`)
VALUES
('dc617128-2ca0-43c6-85cd-d6bd986b85e9', 'home ',         'my bidget\n',  'HOME', '236525df-91c9-43d9-9b9b-1364443f520d', '2026-03-28 19:23:41.139879', '2026-03-28 19:23:41.139898'),
('74b3a583-3c0f-4d37-b19d-3c8fd23a6a27', 'home',          NULL,           'HOME', '236525df-91c9-43d9-9b9b-1364443f520d', '2026-03-28 19:25:27.676448', '2026-03-28 19:25:27.676466'),
('d047bd1d-5fa2-417c-b076-f70e1776d5f4', 'home',          NULL,           'HOME', '6d4055e6-0744-4752-90ae-33062604a7fa',  '2026-03-28 19:29:05.745744', '2026-03-28 19:29:05.745795'),
('7d5768a9-87f5-4680-a4b0-e51d9954bc75', 'Home Budget 2026', NULL,        'HOME', '961c590f-99f6-4849-a3c6-f8117dccc7de', '2026-03-29 12:39:54.3738',   '2026-03-29 12:39:54.373815');

-- -----------------------------------------------------------
-- budget_categories
-- H2 column order: ID, COLOR, IS_SYSTEM, NAME, OWNER_ID
-- -----------------------------------------------------------
INSERT IGNORE INTO `budget_categories`
    (`id`, `name`, `color`, `is_system`, `owner_id`)
VALUES
('dbf8f25e-f129-4b32-a457-f02838c81dfa', 'Transport',       '#3b82f6', 1, NULL),
('58300471-9df3-4f58-b137-b432f698cefd', 'Entertainment',   '#f59e0b', 1, NULL),
('3ef51a3e-4670-48b6-977f-ead000810e29', 'Food & Dining',   '#f97316', 1, NULL),
('99548ff1-f62d-42bc-ab11-e2b6ed9df6d3', 'Other',           '#94a3b8', 1, NULL),
('4158a18a-65ec-4aae-b657-0294c0f73bcd', 'Bills & Utilities','#8b5cf6', 1, NULL),
('571df583-049b-43f6-ae70-4a767ab89246', 'Education',       '#6366f1', 1, NULL),
('9d08f981-aaae-486a-adde-fd54f4d9e858', 'Salary',          '#22c55e', 1, NULL),
('8aa0c678-a9f4-4fe3-b0ea-6c12ef9ed899', 'Health & Medical','#10b981', 1, NULL),
('89d632e0-f119-4b42-889a-8fb38aea064f', 'Shopping',        '#ec4899', 1, NULL),
('ed581049-d7b2-4c7d-b891-790b3ecee4ec', 'Business',        '#0ea5e9', 1, NULL);

-- -----------------------------------------------------------
-- budget_transactions
-- H2 column order: ID, AMOUNT, CATEGORY, CREATED_AT, DATE, DESCRIPTION, TYPE,
--                  ADDED_BY_ID, BUDGET_ID
-- -----------------------------------------------------------
INSERT IGNORE INTO `budget_transactions`
    (`id`, `budget_id`, `added_by_id`, `amount`, `type`, `category`,
     `description`, `date`, `created_at`)
VALUES
('78e8dcb8-ecfe-4e40-9feb-b380983dd668', 'd047bd1d-5fa2-417c-b076-f70e1776d5f4', '6d4055e6-0744-4752-90ae-33062604a7fa',  1000.00, 'INCOME',  'Food & Dining', NULL,    '2026-03-28', '2026-03-28 19:34:42.364935'),
('9ba0ddbd-45ed-4032-a305-d3b3d41ba964', 'd047bd1d-5fa2-417c-b076-f70e1776d5f4', '6d4055e6-0744-4752-90ae-33062604a7fa',  200.00,  'EXPENSE', 'Food & Dining', 'food',  '2026-03-28', '2026-03-28 21:20:37.06098'),
('a0661b14-18b8-4e05-ab0f-44ed9bba54ee', '7d5768a9-87f5-4680-a4b0-e51d9954bc75', '961c590f-99f6-4849-a3c6-f8117dccc7de', 5.00,    'EXPENSE', 'Transport',     'Coffee','2026-03-29', '2026-03-29 12:40:33.34915');

-- -----------------------------------------------------------
-- password_reset_tokens
-- H2 column order: ID, EXPIRY_DATE, TOKEN, USER_ID
-- -----------------------------------------------------------
INSERT IGNORE INTO `password_reset_tokens`
    (`id`, `token`, `user_id`, `expiry_date`)
VALUES
('edda7375-4fc4-4c8d-be1d-f1c878e90fcd', 'f4b9db52-1c10-474f-aa7f-38d2f776fce7', '6d4055e6-0744-4752-90ae-33062604a7fa', '2026-03-30 20:32:51.479029');

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- Done. Summary of migrated rows:
--   users                : 14
--   vehicles             : 5
--   vehicle_access       : 1
--   fuel_logs            : 7
--   maintenance_logs     : 4
--   maintenance_settings : 0 (none in H2)
--   meters               : 9
--   meter_readings       : 20
--   meter_access         : 2
--   budgets              : 4
--   budget_categories    : 10
--   budget_transactions  : 3
--   password_reset_tokens: 1
-- =============================================================
