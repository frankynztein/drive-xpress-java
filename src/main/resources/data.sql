-- Eliminar tablas existentes en orden correcto
--DROP TABLE IF EXISTS car_features;
--DROP TABLE IF EXISTS car_categories;
--DROP TABLE IF EXISTS rental;
--DROP TABLE IF EXISTS car_photo_gallery;
--DROP TABLE IF EXISTS car;
--DROP TABLE IF EXISTS feature;
--DROP TABLE IF EXISTS category;
--DROP TABLE IF EXISTS users;

-- Tabla USERS
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_email CHECK (email LIKE '%@%.%')
);

-- Tabla CATEGORY
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla FEATURE
CREATE TABLE feature (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla CAR
CREATE TABLE car (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model VARCHAR(255) NOT NULL,
    transmission VARCHAR(50) NOT NULL CHECK (transmission IN ('Manual', 'Automático')),
    daily_rental_cost DECIMAL(10,2) NOT NULL CHECK (daily_rental_cost > 0),
    description VARCHAR(1000),
    main_photo_url VARCHAR(255) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla CAR_PHOTO_GALLERY
CREATE TABLE car_photo_gallery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    photo_url VARCHAR(255) NOT NULL,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES car(id) ON DELETE CASCADE
);

-- Tablas car_features
CREATE TABLE car_features (
    car_id BIGINT NOT NULL,
    feature_id BIGINT NOT NULL,
    PRIMARY KEY (car_id, feature_id),
    FOREIGN KEY (car_id) REFERENCES car(id) ON DELETE CASCADE,
    FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE
);

CREATE TABLE car_categories (
    car_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (car_id, category_id),
    FOREIGN KEY (car_id) REFERENCES car(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE
);

-- Tabla RENTAL
CREATE TABLE rental (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'REFUNDED', 'PARTIALLY_REFUNDED')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES car(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_dates CHECK (end_date > start_date)
);

-- Tabla FAVORITES
CREATE TABLE favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    car_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (car_id) REFERENCES car(id) ON DELETE CASCADE,
    CONSTRAINT unique_favorite UNIQUE (user_id, car_id)  -- Sintaxis correcta para H2
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_rental_user ON rental(user_id);
CREATE INDEX idx_rental_car ON rental(car_id);
CREATE INDEX idx_rental_dates ON rental(start_date, end_date);
CREATE INDEX idx_rental_status ON rental(status);

-- Datos tabla category
INSERT INTO category (title, description, image_url) VALUES
('Sedán', 'Vehículo de cuatro puertas con maletero separado', 'audi-a1-sportback.png'),
('Hatchback', 'Vehículo compacto con portón trasero', 'audi-q5.png'),
('SUV', 'Vehículo utilitario deportivo', 'bmw-x1.png'),
('Coupé', 'Vehículo de dos puertas y diseño deportivo', 'bmw-x5.png'),
('Pickup', 'Vehículo con área de carga descubierta', 'audi-a1-sportback.png'),
('Familiar', 'Vehículo espacioso para familias', 'audi-q5.png'),
('Convertible', 'Vehículo con techo retráctil', 'bmw-x1.png'),
('Minivan', 'Vehículo familiar de gran capacidad', 'bmw-x5.png'),
('Eléctricos', 'Vehículo totalmente eléctrico', 'audi-a1-sportback.png'),
('Híbridos', 'Vehículo con motor híbrido', 'audi-q5.png'),
('Deportivos', 'Vehículos de alto rendimiento', 'bmw-x1.png'),
('Lujo', 'Vehículos premium y de alta gama', 'bmw-x5.png');

-- Datos tabla feature
INSERT INTO feature (name, icon) VALUES
('Aire acondicionado', 'fa fa-snowflake-o'),
('Sensores de estacionamiento', 'fa fa-exclamation'),
('Cámara de reversa', 'fa fa-camera'),
('Control crucero', 'fa fa-dot-circle-o'),
('Bluetooth', 'fa fa-bluetooth'),
('Navegación GPS', 'fa fa-map-o'),
('Control de temperatura', 'fa fa-thermometer-empty'),
('Techo solar', 'fa fa-sun-o'),
('Sistema de sonido premium', 'fa fa-volume-up'),
('CarPlay/Android Auto', 'fa fa-mobile');

-- Datos tabla car
INSERT INTO car (model, transmission, daily_rental_cost, description, main_photo_url, is_available) VALUES
('Peugeot 208', 'Manual', 85.00, 'El Peugeot 208 es un sedán versátil con diseño moderno y tecnología avanzada. Ofrece un interior cómodo, motor eficiente y gran maniobrabilidad. Su sistema de seguridad y conectividad lo hacen ideal para viajes urbanos y largos. Un coche confiable con excelente relación calidad-precio.', 'peugeot-208.png', true),
('Peugeot 308', 'Automático', 110.00, 'El Peugeot 308 combina diseño elegante con tecnología de vanguardia. Su conducción es precisa y eficiente, gracias a su motorización optimizada. Dispone de un habitáculo espacioso y bien equipado. Ideal para quienes buscan un coche compacto sin renunciar al confort y la seguridad avanzada.', 'peugeot-308.png', true),
('Peugeot 2008', 'Manual', 95.00, 'El Peugeot 2008 es un SUV urbano con un diseño robusto y dinámico. Su habitáculo es espacioso y cuenta con tecnología avanzada. Equipado con sistemas de seguridad y asistencia, ofrece una conducción cómoda y segura. Ideal para quienes buscan un vehículo versátil y moderno.', 'peugeot-2008.png', true),
('Audi A1 Sportback', 'Manual', 100.00, 'El Audi A1 Sportback es un compacto premium con un diseño juvenil y deportivo. Ofrece un interior de alta calidad, tecnología avanzada y motores eficientes. Su conducción es ágil y segura, ideal para la ciudad. Destaca por su conectividad y sistemas de asistencia innovadores.', 'audi-a1-sportback.png', true),
('Peugeot Rifter', 'Manual', 90.00, 'El Peugeot Rifter es un SUV espacioso y funcional, ideal para familias y aventureros. Su diseño robusto y su equipamiento tecnológico garantizan comodidad y seguridad. Ofrece gran capacidad de carga y versatilidad en el uso diario, con motores eficientes y asistencias avanzadas.', 'peugeot-rifter.png', true),
('Peugeot 3008', 'Automático', 130.00, 'El Peugeot 3008 es un SUV sofisticado con un diseño innovador. Su interior tecnológico y espacioso ofrece confort y conectividad avanzada. Conducción ágil y eficiente con asistencia inteligente. Un vehículo que combina estilo, seguridad y rendimiento para experiencias de viaje inolvidables.', 'peugeot-3008.png', true),
('Peugeot 208-II', 'Manual', 85.00, 'El Peugeot 208 es un sedán versátil con diseño moderno y tecnología avanzada. Ofrece un interior cómodo, motor eficiente y gran maniobrabilidad. Su sistema de seguridad y conectividad lo hacen ideal para viajes urbanos y largos. Un coche confiable con excelente relación calidad-precio.', 'peugeot-208.png', true),
('Peugeot 308-II', 'Automático', 110.00, 'El Peugeot 308 combina diseño elegante con tecnología de vanguardia. Su conducción es precisa y eficiente, gracias a su motorización optimizada. Dispone de un habitáculo espacioso y bien equipado. Ideal para quienes buscan un coche compacto sin renunciar al confort y la seguridad avanzada.', 'peugeot-308.png', true),
('Peugeot 2008-II', 'Manual', 95.00, 'El Peugeot 2008 es un SUV urbano con un diseño robusto y dinámico. Su habitáculo es espacioso y cuenta con tecnología avanzada. Equipado con sistemas de seguridad y asistencia, ofrece una conducción cómoda y segura. Ideal para quienes buscan un vehículo versátil y moderno.', 'peugeot-2008.png', true),
('Audi A1 Sportback-II', 'Manual', 100.00, 'El Audi A1 Sportback es un compacto premium con un diseño juvenil y deportivo. Ofrece un interior de alta calidad, tecnología avanzada y motores eficientes. Su conducción es ágil y segura, ideal para la ciudad. Destaca por su conectividad y sistemas de asistencia innovadores.', 'audi-a1-sportback.png', true),
('Peugeot Rifter-II', 'Manual', 90.00, 'El Peugeot Rifter es un SUV espacioso y funcional, ideal para familias y aventureros. Su diseño robusto y su equipamiento tecnológico garantizan comodidad y seguridad. Ofrece gran capacidad de carga y versatilidad en el uso diario, con motores eficientes y asistencias avanzadas.', 'peugeot-rifter.png', true),
('Peugeot 3008-II', 'Automático', 130.00, 'El Peugeot 3008 es un SUV sofisticado con un diseño innovador. Su interior tecnológico y espacioso ofrece confort y conectividad avanzada. Conducción ágil y eficiente con asistencia inteligente. Un vehículo que combina estilo, seguridad y rendimiento para experiencias de viaje inolvidables.', 'peugeot-3008.png', false);

-- Datos tabla car_photo_gallery
INSERT INTO car_photo_gallery (car_id, photo_url) VALUES
(2, 'audi-q5.png'),
(2, 'bmw-x1.png'),
(2, 'bmw-x5.png'),
(2, 'bmw-serie-1.png'),
(2, 'bmw-serie-3.png'),
(2, 'audi-a5-sportback.png'),
(2, 'audi-a1-sportback.png'),
(2, 'bmx-serie-3-touring.png'),
(2, 'peugeot-rifter.png');


-- Datos tabla car_categories
INSERT INTO car_categories (car_id, category_id) VALUES
(1, 1), (1, 9),
(2, 2), (2, 10),
(3, 3), (3, 11),
(4, 4), (4, 12),
(5, 5), (5, 1),
(6, 6), (6, 2),
(7, 7), (7, 3),
(8, 8), (8, 4),
(9, 9), (9, 5),
(10, 10), (10, 6),
(11, 11), (11, 7),
(12, 12), (12, 8);


-- Datos tabla car_features
INSERT INTO car_features (car_id, feature_id) VALUES
(1, 1), (1, 3), (1, 5),
(2, 1), (2, 2), (2, 4), (2, 6),
(3, 1), (3, 3), (3, 7), (3, 9),
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 8), (4, 9), (4, 10),
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10);

-- Datos tabla users
INSERT INTO users (first_name, last_name, email, password, is_admin) VALUES
('Emma', 'Arellan', 'emma@emma.com', '$2a$10$oA5/s9vEVceELsryS3xx0eBWVWKj297fm0yqQUnpREoFq9jEndNoC', FALSE),
('Harry', 'Arellan', 'harry@harry.com', '$2a$10$oA5/s9vEVceELsryS3xx0eBWVWKj297fm0yqQUnpREoFq9jEndNoC', FALSE),
('admin', 'user', 'admin@admin.com', '$2a$10$oA5/s9vEVceELsryS3xx0eBWVWKj297fm0yqQUnpREoFq9jEndNoC', TRUE);

-- Datos tabla rental
INSERT INTO rental (car_id, user_id, start_date, end_date, total_price, status) VALUES
(1, 1, DATEADD('DAY', 1, CURRENT_DATE), DATEADD('DAY', 3, CURRENT_DATE), 170.00, 'CANCELLED'),
(2, 2, DATEADD('DAY', 5, CURRENT_DATE), DATEADD('DAY', 7, CURRENT_DATE), 220.00, 'CANCELLED'),
(3, 1, CURRENT_DATE, DATEADD('DAY', 2, CURRENT_DATE), 190.00, 'CANCELLED'),
(4, 2, DATEADD('DAY', -10, CURRENT_DATE), DATEADD('DAY', -7, CURRENT_DATE), 300.00, 'CANCELLED'),
(5, 1, DATEADD('DAY', -15, CURRENT_DATE), DATEADD('DAY', -10, CURRENT_DATE), 450.00, 'CANCELLED'),
(6, 2, DATEADD('DAY', -5, CURRENT_DATE), DATEADD('DAY', -3, CURRENT_DATE), 260.00, 'CANCELLED'),
(7, 1, DATEADD('DAY', 10, CURRENT_DATE), DATEADD('DAY', 12, CURRENT_DATE), 170.00, 'CANCELLED'),
(8, 2, DATEADD('DAY', 15, CURRENT_DATE), DATEADD('DAY', 18, CURRENT_DATE), 330.00, 'CANCELLED'),
(9, 1, DATEADD('DAY', -3, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), 190.00, 'CANCELLED'),
(10, 2, DATEADD('DAY', -20, CURRENT_DATE), DATEADD('DAY', -15, CURRENT_DATE), 500.00, 'CANCELLED'),
(11, 1, DATEADD('DAY', 20, CURRENT_DATE), DATEADD('DAY', 25, CURRENT_DATE), 450.00, 'CANCELLED'),
(12, 2, CURRENT_DATE, DATEADD('DAY', 5, CURRENT_DATE), 650.00, 'CONFIRMED');

-- Data favorite
INSERT INTO favorite (user_id, car_id, created_at) VALUES
(1, 3, CURRENT_TIMESTAMP),  -- Usuario 1 marca como favorito el coche 3
(1, 5, CURRENT_TIMESTAMP),  -- Usuario 1 marca como favorito el coche 5
(2, 1, CURRENT_TIMESTAMP),-- Usuario 2 marca como favorito el coche 1
(2, 3,CURRENT_TIMESTAMP),
(3, 10, CURRENT_TIMESTAMP),
(3, 4, CURRENT_TIMESTAMP);  -- Admin marca como favorito el coche 4