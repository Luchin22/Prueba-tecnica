-- Base de Datos completa


-- conectar a la base de datos
\c db_clientes;

-- tabla: clientes (hereda campos de persona)
CREATE TABLE IF NOT EXISTS clientes (
    cliente_id VARCHAR(12) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(10) NOT NULL CHECK (genero IN ('M', 'F', 'OTRO')),
    edad INTEGER NOT NULL CHECK (edad >= 18 AND edad <= 120),
    identificacion VARCHAR(13) UNIQUE NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

-- indices para optimizacion de consultas
CREATE INDEX IF NOT EXISTS idx_cliente_identificacion ON clientes(identificacion);
CREATE INDEX IF NOT EXISTS idx_cliente_estado ON clientes(estado);
CREATE INDEX IF NOT EXISTS idx_cliente_fecha_creacion ON clientes(fecha_creacion);



-- Base de datos : db_cuentas


-- conectar a la segunda base de datos
\c db_cuentas;

-- tabla: cuentas
CREATE TABLE IF NOT EXISTS cuentas (
    numero_cuenta VARCHAR(12) PRIMARY KEY,
    tipo_cuenta VARCHAR(10) NOT NULL CHECK (tipo_cuenta IN ('AHORRO', 'CORRIENTE')),
    saldo_inicial DECIMAL(15, 2) NOT NULL DEFAULT 0.00 CHECK (saldo_inicial >= 0),
    saldo_actual DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    estado BOOLEAN NOT NULL DEFAULT true,
    cliente_id VARCHAR(12) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- indices para cuentas
CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_id ON cuentas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_cuenta_estado ON cuentas(estado);
CREATE INDEX IF NOT EXISTS idx_cuenta_tipo ON cuentas(tipo_cuenta);
CREATE INDEX IF NOT EXISTS idx_cuenta_cliente_estado ON cuentas(cliente_id, estado);


-- tabla: movimientos
CREATE TABLE IF NOT EXISTS movimientos (
    movimiento_id VARCHAR(16) PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(10) NOT NULL CHECK (tipo_movimiento IN ('DEPOSITO', 'RETIRO')),
    valor DECIMAL(15, 2) NOT NULL,
    saldo_anterior DECIMAL(15, 2) NOT NULL,
    saldo_despues DECIMAL(15, 2) NOT NULL,
    numero_cuenta VARCHAR(12) NOT NULL,
    descripcion VARCHAR(500),
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (numero_cuenta)
        REFERENCES cuentas(numero_cuenta) ON DELETE CASCADE
);

-- indices para movimientos
CREATE INDEX IF NOT EXISTS idx_mov_numero_cuenta ON movimientos(numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_mov_fecha ON movimientos(fecha);
CREATE INDEX IF NOT EXISTS idx_mov_tipo ON movimientos(tipo_movimiento);
-- indice compuesto por rango de fechas
CREATE INDEX IF NOT EXISTS idx_mov_cuenta_fecha ON movimientos(numero_cuenta, fecha DESC);
CREATE INDEX IF NOT EXISTS idx_mov_fecha_tipo ON movimientos(fecha, tipo_movimiento);


-- datos de prueba segun casos de uso

-- creacion de clientes
\c db_clientes;

INSERT INTO clientes (cliente_id, nombre, genero, edad, identificacion, direccion, telefono, contrasena, estado)
VALUES
    ('CLI-00000001', 'Jose Lema', 'M', 35, '1234567890', 'Otavalo sn y principal', '098254785', '$2a$10$dummyhash1', true),
    ('CLI-00000002', 'Marianela Montalvo', 'F', 28, '0987654321', 'Amazonas y NNUU', '097548965', '$2a$10$dummyhash2', true),
    ('CLI-00000003', 'Juan Osorio', 'M', 42, '1122334455', '13 junio y Equinoccial', '098874587', '$2a$10$dummyhash3', true)
ON CONFLICT (cliente_id) DO NOTHING;

-- creacion de cuatro cuentas
\c db_cuentas;

INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, saldo_actual, estado, cliente_id)
VALUES
    ('CTA-00000001', 'AHORRO', 2000.00, 2000.00, true, 'CLI-00000001'),
    ('CTA-00000002', 'CORRIENTE', 100.00, 100.00, true, 'CLI-00000002'),
    ('CTA-00000003', 'AHORRO', 0.00, 0.00, true, 'CLI-00000003'),
    ('CTA-00000004', 'AHORRO', 540.00, 540.00, true, 'CLI-00000002')
ON CONFLICT (numero_cuenta) DO NOTHING;

-- funciones y triggers


\c db_cuentas;

-- funcion para actualizar fecha actualziacion
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- trigger para cuentas
CREATE TRIGGER update_cuenta_updated_at BEFORE UPDATE ON cuentas
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

