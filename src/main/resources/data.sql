-- Tabla CAR
CREATE TABLE CAR (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    transmission VARCHAR(255),
    daily_rental_cost DECIMAL(10, 2),
    description VARCHAR(1000),
    main_photo_url VARCHAR(255)
);

-- Tabla CAR_PHOTO_GALLERY
CREATE TABLE CAR_PHOTO_GALLERY (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT,
    photo_gallery VARCHAR(255),
    FOREIGN KEY (car_id) REFERENCES CAR(id)
);

-- Datos en la tabla CAR
INSERT INTO CAR (model, category, transmission, daily_rental_cost, description, main_photo_url)
VALUES
(
    'Peugeot 208',
    'Sedán',
    'Manual',
    85,
    'El Peugeot 208 es un sedán versátil con diseño moderno y tecnología avanzada. Ofrece un interior cómodo, motor eficiente y gran maniobrabilidad. Su sistema de seguridad y conectividad lo hacen ideal para viajes urbanos y largos. Un coche confiable con excelente relación calidad-precio.', 'peugeot-208.png'
),
(
    'Peugeot 308',
    'Compacto',
    'Automático',
    110,
    'El Peugeot 308 combina diseño elegante con tecnología de vanguardia. Su conducción es precisa y eficiente, gracias a su motorización optimizada. Dispone de un habitáculo espacioso y bien equipado. Ideal para quienes buscan un coche compacto sin renunciar al confort y la seguridad avanzada.', 'peugeot-308.png'
),
(
    'Peugeot 2008',
    'SUV',
    'Manual',
    95,
    'El Peugeot 2008 es un SUV urbano con un diseño robusto y dinámico. Su habitáculo es espacioso y cuenta con tecnología avanzada. Equipado con sistemas de seguridad y asistencia, ofrece una conducción cómoda y segura. Ideal para quienes buscan un vehículo versátil y moderno.', 'peugeot-2008.png'
),
(
    'Audi A1 Sportback',
    'Compacto',
    'Manual',
    100,
    'El Audi A1 Sportback es un compacto premium con un diseño juvenil y deportivo. Ofrece un interior de alta calidad, tecnología avanzada y motores eficientes. Su conducción es ágil y segura, ideal para la ciudad. Destaca por su conectividad y sistemas de asistencia innovadores.', 'audi-a1-sportback.png'
),
(
    'Peugeot Rifter',
    'SUV',
    'Manual',
    90,
    'El Peugeot Rifter es un SUV espacioso y funcional, ideal para familias y aventureros. Su diseño robusto y su equipamiento tecnológico garantizan comodidad y seguridad. Ofrece gran capacidad de carga y versatilidad en el uso diario, con motores eficientes y asistencias avanzadas.', 'peugeot-rifter.png'
),
(
    'Peugeot 3008',
    'SUV',
    'Automático',
    130,
    'El Peugeot 3008 es un SUV sofisticado con un diseño innovador. Su interior tecnológico y espacioso ofrece confort y conectividad avanzada. Conducción ágil y eficiente con asistencia inteligente. Un vehículo que combina estilo, seguridad y rendimiento para experiencias de viaje inolvidables.', 'peugeot-3008.png'
),
(
    'Peugeot 408',
    'Sedán',
    'Automático',
    125,
    'El Peugeot 408 destaca por su diseño aerodinámico y elegante. Su interior amplio y tecnología avanzada lo convierten en un sedán de alto nivel. Con motores eficientes y sistemas de seguridad avanzados, es ideal para quienes buscan confort, dinamismo y eficiencia en la conducción.', 'peugeot-408.png'
),
(
    'BMW Serie 1',
    'Sedán',
    'Automático',
    120,
    'El BMW Serie 1 es un sedán premium con un diseño sofisticado y deportivo. Su motor potente y eficiente ofrece una experiencia de conducción dinámica. Cuenta con un interior refinado, tecnología de vanguardia y avanzados sistemas de seguridad. Ideal para quienes buscan confort, rendimiento y exclusividad.', 'bmw-serie-1.png'
),
(
    'Peugeot 308 SW',
    'Familiar',
    'Automático',
    105,
    'El Peugeot 308 SW es un familiar espacioso con un diseño moderno y funcional. Ofrece tecnología avanzada, eficiencia en el consumo y un maletero amplio. Ideal para viajes largos con la familia, gracias a su comodidad, seguridad y conectividad inteligente.', 'peugeot-308-sw.png'
),
(
    'Mini Countryman',
    'SUV',
    'Automático',
    140,
    'El Mini Countryman es un SUV compacto con un diseño icónico y detalles premium. Su conducción es ágil y divertida, con tecnología avanzada y seguridad optimizada. Espacioso y versátil, es perfecto para la ciudad y escapadas al aire libre, combinando estilo y funcionalidad.', 'mini-countryman.png'
),
(
    'BMW X1',
    'SUV',
    'Automático',
    135,
    'El BMW X1 es un SUV premium con un diseño robusto y sofisticado. Ofrece una conducción dinámica con motores eficientes y tecnología avanzada. Su interior es espacioso y cómodo, con sistemas de asistencia inteligentes. Un coche ideal para quienes buscan rendimiento y exclusividad.', 'bmw-x1.png'
),
(
    'BMW Serie 3',
    'Sedán',
    'Automático',
    150,
    'El BMW Serie 3 es un sedán de alto rendimiento con un diseño elegante y deportivo. Destaca por su tecnología avanzada, confort premium y motores potentes. Conducción precisa y seguridad de vanguardia lo convierten en una opción ideal para quienes buscan sofisticación y dinamismo.', 'bmw-serie-3.png'
),
(
    'BMW Serie 3 Touring',
    'Familiar',
    'Automático',
    145,
    'El BMW Serie 3 Touring combina deportividad con funcionalidad. Su diseño elegante y su amplio espacio interior lo hacen ideal para viajes en familia. Con tecnología de última generación y motores eficientes, ofrece confort, seguridad y una conducción excepcional en cualquier trayecto.', 'bmw-serie-3-touring.png'
),
(
    'Peugeot Rifter',
    'SUV',
    'Automático',
    100,
    'El Peugeot Rifter es un SUV práctico y espacioso, diseñado para la comodidad y la aventura. Ofrece un interior versátil, tecnología avanzada y seguridad optimizada. Su conducción es ágil y eficiente, con un diseño robusto ideal para la vida urbana y los viajes largos.', 'peugeot-rifter.png'
),
(
    'Audi A5 Sportback',
    'Coupé',
    'Automático',
    145,
    'El Audi A5 Sportback es un coupé elegante con un diseño aerodinámico y refinado. Su interior premium y tecnología avanzada garantizan una experiencia de conducción excepcional. Con un motor potente y eficiente, ofrece seguridad y dinamismo para quienes buscan exclusividad y confort.', 'audi-a5-sportback.png'
),
(
    'Audi Q5',
    'SUV',
    'Automático',
    135,
    'El Audi Q5 es un SUV premium con un diseño sofisticado y tecnología de vanguardia. Su interior espacioso y cómodo ofrece una experiencia de lujo. Conducción ágil y segura con asistencia avanzada, ideal para la ciudad y carretera. Eficiencia y rendimiento en cada detalle.', 'audi-q5.png'
),
(
    'VW Caravelle',
    'SUV',
    'Automático',
    110,
    'La VW Caravelle es un SUV funcional y espacioso, diseñado para el confort y la versatilidad. Ideal para familias o viajes largos, ofrece tecnología avanzada y seguridad optimizada. Su motor eficiente y conducción estable garantizan una experiencia placentera en cualquier trayecto.', 'vw-caravelle.png'
),
(
    'BMW X5',
    'SUV',
    'Automático',
    150,
    'El BMW X5 es un SUV de lujo con un diseño imponente y tecnología de última generación. Su motor potente y eficiencia avanzada ofrecen una conducción premium. Espacioso, cómodo y seguro, es perfecto para quienes buscan rendimiento, exclusividad y una experiencia de manejo excepcional.', 'bmw-x5.png'
);

-- Datos en la tabla CAR_PHOTO_GALLERY
INSERT INTO CAR_PHOTO_GALLERY (car_id, photo_gallery)
VALUES (1, 'audi-q5.png');

INSERT INTO CAR_PHOTO_GALLERY (car_id, photo_gallery)
VALUES (1, 'audi-q5.png');



