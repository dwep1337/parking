CREATE TABLE garage_sector (
    sector VARCHAR(10) NOT NULL PRIMARY KEY,
    base_price DECIMAL(10, 2) NOT NULL,
    max_capacity INT NOT NULL
);

CREATE TABLE parking_spot (
    id BIGINT NOT NULL PRIMARY KEY,
    sector VARCHAR(10) NOT NULL,
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_parking_spot_sector FOREIGN KEY (sector) REFERENCES garage_sector (sector)
);

CREATE TABLE vehicle (
    license_plate VARCHAR(20) NOT NULL PRIMARY KEY,
    entry_time TIMESTAMP(3) NOT NULL,
    exit_time TIMESTAMP(3) NULL,
    sector VARCHAR(10) NOT NULL,
    spot_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    hourly_rate DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_vehicle_sector FOREIGN KEY (sector) REFERENCES garage_sector (sector)
);

CREATE TABLE revenue (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sector VARCHAR(10) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL,
    CONSTRAINT fk_revenue_sector FOREIGN KEY (sector) REFERENCES garage_sector (sector)
);

CREATE INDEX idx_revenue_sector_created_at ON revenue (sector, created_at);
