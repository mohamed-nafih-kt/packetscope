-- Create Database
CREATE DATABASE IF NOT EXISTS packetscope;
USE packetscope;

-- -----------------------------------------------------
-- Table: packets
-- -----------------------------------------------------
-- Stores raw telemetry for every captured packet.
-- Indexed for Keyset Pagination and Time-series aggregation.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS packets (
    packet_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL DEFAULT 1,
    captured_at      DATETIME(6) NOT NULL, -- Microsecond precision for high-speed traffic
    ip_version       TINYINT NOT NULL,
    protocol         INT NOT NULL,         -- IANA Protocol numbers (6=TCP, 17=UDP)
    source_ip        VARBINARY(16) NOT NULL, -- Supports both IPv4 (4 bytes) and IPv6 (16 bytes)
    destination_ip   VARBINARY(16) NOT NULL,
    source_port      INT DEFAULT NULL,     -- Nullable for non-TCP/UDP traffic (ICMP)
    destination_port INT DEFAULT NULL,
    packet_size      INT NOT NULL,
    interface_name   VARCHAR(100),
    direction        TINYINT NOT NULL,     -- 1=OUTBOUND, 2=INBOUND
    
    -- Optimized index for Keyset Pagination (fetchPacketsAfter)
    INDEX idx_pagination (captured_at, packet_id),
    
    -- Index for Timeline and Flow analytics
    INDEX idx_analytics (captured_at, protocol)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Table: transaction_logs
-- -----------------------------------------------------
-- Stores HTTP Replay data.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS transaction_logs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    method            VARCHAR(10) NOT NULL,
    url               TEXT NOT NULL,
    request_headers   JSON,                 -- Native JSON support for DTO mapping
    request_body      LONGTEXT,
    response_status   INT,
    response_headers  JSON,
    response_body     LONGTEXT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_created (created_at)
) ENGINE=InnoDB;