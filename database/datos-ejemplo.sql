-- =====================================================================
-- TiendaPC · Datos de ejemplo (opcional)
--
-- Uso:  psql -U david -d tiendaspring -f database/datos-ejemplo.sql
-- Ejecutar DESPUÉS de schema.sql, una sola vez.
-- Todos los productos y precios son ficticios. Los artículos no llevan
-- foto (se mostrará la imagen de relleno): se puede añadir desde el
-- panel de administración.
-- No incluye usuarios: se crean desde la propia aplicación (ver README).
-- =====================================================================

INSERT INTO categoria (nombre) VALUES
    ('Almacenamiento'), ('Componentes'), ('Monitores'), ('Periféricos'), ('Portátiles');

INSERT INTO subcategoria (id_categoria, nombre)
SELECT c.id_categoria, v.nombre
FROM (VALUES
    ('Almacenamiento', 'SSD'),
    ('Almacenamiento', 'Discos duros'),
    ('Componentes',    'Tarjetas gráficas'),
    ('Componentes',    'Procesadores'),
    ('Componentes',    'Placas base'),
    ('Componentes',    'Memoria RAM'),
    ('Periféricos',    'Ratones'),
    ('Periféricos',    'Teclados'),
    ('Periféricos',    'Auriculares')
) AS v(categoria, nombre)
JOIN categoria c ON c.nombre = v.categoria;

INSERT INTO marca (nombre) VALUES
    ('Samsung'), ('Intel'), ('Asus'), ('Kingston'),
    ('Logitech'), ('Corsair'), ('Lenovo'), ('MSI');

-- Los ids se buscan por nombre, así que no dependen de los valores de la identidad
INSERT INTO articulo (id_marca, id_categoria, id_subcategoria, nombre, descripcion, precio, stock)
SELECT m.id_marca, c.id_categoria, s.id_subcategoria, v.nombre, v.descripcion, v.precio, v.stock
FROM (VALUES
    ('Samsung',  'Almacenamiento', 'SSD',               '990 Pro 1TB',
        'SSD NVMe PCIe 4.0 de 1 TB con velocidades de lectura de hasta 7.450 MB/s.',        119.00, 25),
    ('Intel',    'Componentes',    'Procesadores',      'Core i5-14600K',
        'Procesador de 14 núcleos y socket LGA1700, con multiplicador desbloqueado.',        319.00, 12),
    ('Asus',     'Componentes',    'Tarjetas gráficas', 'Dual GeForce RTX 4070 Super',
        'Tarjeta gráfica con 12 GB GDDR6X y refrigeración de doble ventilador.',             629.00,  8),
    ('Kingston', 'Componentes',    'Memoria RAM',       'Fury Beast DDR5 16GB 5600',
        'Módulo de memoria DDR5 de 16 GB a 5600 MHz para equipos de sobremesa.',              64.90, 40),
    ('MSI',      'Componentes',    'Placas base',       'MAG B650 Tomahawk WiFi',
        'Placa base AM5 con chipset B650, WiFi 6E y soporte para memoria DDR5.',             219.00, 10),
    ('Logitech', 'Periféricos',    'Ratones',           'G502 Hero',
        'Ratón gaming con sensor de 25.600 DPI y 11 botones programables.',                   44.99, 30),
    ('Logitech', 'Periféricos',    'Teclados',          'G413 SE Mecánico',
        'Teclado mecánico con estructura de aluminio y retroiluminación.',                    69.90, 18),
    ('Corsair',  'Periféricos',    'Auriculares',       'HS80 RGB Wireless',
        'Auriculares inalámbricos con sonido envolvente y micrófono retráctil.',             119.90, 15),
    ('Samsung',  'Monitores',      NULL,                'Odyssey G5 27"',
        'Monitor curvo de 27 pulgadas, resolución QHD y 144 Hz.',                            259.00,  9),
    ('Lenovo',   'Portátiles',     NULL,                'LOQ 15 RTX 4050',
        'Portátil gaming de 15,6 pulgadas con 16 GB de RAM y gráfica RTX 4050.',             949.00,  6)
) AS v(marca, categoria, subcategoria, nombre, descripcion, precio, stock)
JOIN marca m     ON m.nombre = v.marca
JOIN categoria c ON c.nombre = v.categoria
LEFT JOIN subcategoria s ON s.id_categoria = c.id_categoria AND s.nombre = v.subcategoria;
