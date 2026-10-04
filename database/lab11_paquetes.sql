IF OBJECT_ID('dbo.PAQUETES', 'U') IS NULL
BEGIN
    CREATE TABLE PAQUETES (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        envio_id BIGINT NOT NULL,
        descripcion VARCHAR(255) NOT NULL,
        peso_kg DECIMAL(5,2) NOT NULL,
        CONSTRAINT FK_Paquetes_Envios FOREIGN KEY (envio_id) REFERENCES Envio(envio_id) ON DELETE CASCADE
    );
END
GO

IF COL_LENGTH('dbo.Envio', 'fecha_despacho') IS NULL
    ALTER TABLE dbo.Envio ADD fecha_despacho DATE NULL;
GO

IF COL_LENGTH('dbo.Envio', 'fecha_entrega_estimada') IS NULL
    ALTER TABLE dbo.Envio ADD fecha_entrega_estimada DATE NULL;
GO

DECLARE @sql NVARCHAR(MAX);

SELECT @sql = 'ALTER TABLE dbo.Envio ALTER COLUMN vehiculo_id ' + t.name + ' NULL'
FROM sys.columns c
JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('dbo.Envio') AND c.name = 'vehiculo_id' AND c.is_nullable = 0;
IF @sql IS NOT NULL EXEC sp_executesql @sql;

SET @sql = NULL;
SELECT @sql = 'ALTER TABLE dbo.Envio ALTER COLUMN conductor_id ' + t.name + ' NULL'
FROM sys.columns c
JOIN sys.types t ON c.user_type_id = t.user_type_id
WHERE c.object_id = OBJECT_ID('dbo.Envio') AND c.name = 'conductor_id' AND c.is_nullable = 0;
IF @sql IS NOT NULL EXEC sp_executesql @sql;
GO
