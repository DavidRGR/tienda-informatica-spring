-- =====================================================================
-- TiendaPC · Esquema completo de la base de datos (PostgreSQL)
-- Base de datos: tiendaspring
--
-- Uso:  psql -U david -d tiendaspring -f database/schema.sql
-- Es para una base de datos vacía. La aplicación usa ddl-auto=validate,
-- así que no crea ni modifica tablas: tienen que existir antes de arrancar.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Catálogo
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
    id_categoria BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE subcategoria (
    id_subcategoria BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_categoria    BIGINT NOT NULL REFERENCES categoria (id_categoria),
    nombre          VARCHAR(100) NOT NULL,
    UNIQUE (id_categoria, nombre),
    -- Necesaria para la clave foránea compuesta de articulo
    UNIQUE (id_categoria, id_subcategoria)
);

CREATE TABLE marca (
    id_marca BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre   VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE articulo (
    id_articulo        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_marca           BIGINT        NOT NULL REFERENCES marca (id_marca),
    id_categoria       BIGINT        NOT NULL REFERENCES categoria (id_categoria),
    id_subcategoria    BIGINT,
    nombre             VARCHAR(200)  NOT NULL,
    descripcion        TEXT,
    imagen_url         VARCHAR(500),
    precio             NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    stock              INTEGER       NOT NULL DEFAULT 0 CHECK (stock >= 0),
    activo             BOOLEAN       NOT NULL DEFAULT TRUE,
    -- Auditoría: la rellena Spring Data JPA con el correo del empleado
    creado_por         VARCHAR(150),
    modificado_por     VARCHAR(150),
    fecha_creacion     TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP     NOT NULL DEFAULT NOW(),
    -- La subcategoría, si existe, debe pertenecer a la categoría del artículo.
    -- Con id_subcategoria NULL la clave no se comprueba: la subcategoría es opcional.
    FOREIGN KEY (id_categoria, id_subcategoria)
        REFERENCES subcategoria (id_categoria, id_subcategoria)
);

-- ---------------------------------------------------------------------
-- Usuarios y roles
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    correo        VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE rol (
    id_rol BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario_rol (
    id_usuario BIGINT NOT NULL REFERENCES usuario (id_usuario),
    id_rol     BIGINT NOT NULL REFERENCES rol (id_rol),
    PRIMARY KEY (id_usuario, id_rol)
);

-- ---------------------------------------------------------------------
-- Compras (el carrito es una compra en estado PENDIENTE)
-- ---------------------------------------------------------------------
CREATE TABLE compra (
    id_compra  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario BIGINT        NOT NULL REFERENCES usuario (id_usuario),
    fecha      TIMESTAMP     NOT NULL DEFAULT NOW(),
    estado     VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE'
               CHECK (estado IN ('PENDIENTE', 'PAGADA', 'CANCELADA')),
    total      NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (total >= 0)
);

CREATE TABLE detalle_compra (
    id_detalle      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_compra       BIGINT        NOT NULL REFERENCES compra (id_compra),
    id_articulo     BIGINT        NOT NULL REFERENCES articulo (id_articulo),
    cantidad        INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0)
);

-- ---------------------------------------------------------------------
-- Comentarios y valoraciones
-- ---------------------------------------------------------------------
CREATE TABLE comentario (
    id_comentario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_articulo   BIGINT    NOT NULL REFERENCES articulo (id_articulo),
    id_usuario    BIGINT    NOT NULL REFERENCES usuario (id_usuario),
    valoracion    INTEGER   NOT NULL CHECK (valoracion BETWEEN 1 AND 5),
    texto         TEXT      NOT NULL,
    fecha         TIMESTAMP NOT NULL DEFAULT NOW(),
    visible       BOOLEAN   NOT NULL DEFAULT TRUE,
    -- Cada usuario puede opinar una sola vez sobre cada artículo
    CONSTRAINT uq_comentario_articulo_usuario UNIQUE (id_articulo, id_usuario)
);

-- ---------------------------------------------------------------------
-- Registro de actividad de los empleados (sin claves foráneas a propósito:
-- el historial debe sobrevivir aunque se borre una cuenta o un registro)
-- ---------------------------------------------------------------------
CREATE TABLE auditoria (
    id_auditoria BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha        TIMESTAMP    NOT NULL DEFAULT NOW(),
    usuario      VARCHAR(150) NOT NULL,
    accion       VARCHAR(50)  NOT NULL,
    entidad      VARCHAR(50)  NOT NULL,
    id_entidad   BIGINT,
    detalle      VARCHAR(500)
);

-- ---------------------------------------------------------------------
-- Índices
-- ---------------------------------------------------------------------
CREATE INDEX idx_articulo_marca        ON articulo (id_marca);
CREATE INDEX idx_articulo_categoria    ON articulo (id_categoria);
CREATE INDEX idx_articulo_subcategoria ON articulo (id_subcategoria);
CREATE INDEX idx_compra_usuario        ON compra (id_usuario);
CREATE INDEX idx_detalle_compra        ON detalle_compra (id_compra);
CREATE INDEX idx_detalle_articulo      ON detalle_compra (id_articulo);
CREATE INDEX idx_comentario_articulo   ON comentario (id_articulo);
CREATE INDEX idx_auditoria_fecha       ON auditoria (fecha DESC);

-- ---------------------------------------------------------------------
-- Datos imprescindibles: sin estos roles la aplicación no puede
-- registrar usuarios
-- ---------------------------------------------------------------------
INSERT INTO rol (nombre) VALUES ('COMPRADOR'), ('ADMIN');
