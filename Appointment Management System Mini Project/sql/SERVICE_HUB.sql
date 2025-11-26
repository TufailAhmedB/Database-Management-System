CREATE DATABASE service_hub;

-- Set the current database context (change 'service_hub' if your database name is different)
USE service_hub;

-- 1. Drop tables in reverse dependency order
DROP TABLE IF EXISTS time_slots;
DROP TABLE IF EXISTS service_days;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS company_profiles;


-- 2. Create tables in dependency order

-- Table 1: companies (PK: company_id, FK: company_id)
CREATE TABLE companies (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE
);
 -- Table 2: company_profiles (FK: company_id)
-- This table retains ALL columns from your original definition
CREATE TABLE company_profiles (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    owner_name VARCHAR(100) NOT NULL,
    company_name VARCHAR(100) NOT NULL UNIQUE,
    business_type VARCHAR(50),
    tagline VARCHAR(255),
    description TEXT,
    logo_path VARCHAR(255),
    start_time VARCHAR(10),
    end_time VARCHAR(10),
    off_days VARCHAR(50), 
    phone VARCHAR(20),
    email VARCHAR(100),
    website VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 3: staff (PK: staff_id, FK: company_id)
-- This table holds the individual provider details
CREATE TABLE staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(50),
    FOREIGN KEY (company_id) REFERENCES company_profiles(company_id) ON DELETE CASCADE
);

-- Table 4: services (PK: service_id, FKs: company_id, staff_id)
-- This table is normalized: 'provider_name' is replaced by FK 'staff_id'
CREATE TABLE services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    num_customers INT NOT NULL, -- Retained from original
    description TEXT,           -- Retained from original
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Retained from original
    -- --- NEW FOREIGN KEYS ---
    company_id INT NOT NULL,
    staff_id INT NOT NULL,
    -- --- FOREIGN KEY CONSTRAINTS ---
    FOREIGN KEY (company_id) REFERENCES company_profiles(company_id) ON DELETE RESTRICT,
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id) ON DELETE RESTRICT
);

-- Table 5: service_days (PK: service_day_id, FK: service_id)
CREATE TABLE service_days (
    service_day_id INT AUTO_INCREMENT PRIMARY KEY,
    service_id INT NOT NULL,
    day_abbr VARCHAR(10) NOT NULL,
    UNIQUE KEY uk_service_day (service_id, day_abbr),
    FOREIGN KEY (service_id) REFERENCES services(service_id) ON DELETE CASCADE
);

-- Table 6: time_slots (PK: slot_id, FK: service_day_id)
CREATE TABLE time_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    service_day_id INT NOT NULL,
    start_time VARCHAR(20),
    end_time VARCHAR(20),
    UNIQUE KEY uk_slot (service_day_id, start_time, end_time),
    FOREIGN KEY (service_day_id) REFERENCES service_days(service_day_id) ON DELETE CASCADE
);

CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT,
    company_name VARCHAR(255),
    service_name VARCHAR(255),
    day VARCHAR(20),
    slot_time VARCHAR(50),
    customer_name VARCHAR(255),
    phone VARCHAR(30),
    email VARCHAR(255),
    notes TEXT,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE ON UPDATE CASCADE
);
