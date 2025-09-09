## Integrantes: 
- Jonathan Moreno
- Mateo Roldan
- Jader Monterrosa
- Alexander Vásquez Ossa

## Sistema Para Gestión de Ventas y Reportes

Este proyecto, implementa un sistema integral de procesamiento de datos comerciales, desarrollado en Java diseñado para simular un entorno empresarial real. Consta de dos módulos principales: un generador de datos de prueba (GenerateInfoFiles) que crea datasets pseudoaleatorios de productos, vendedores y transacciones comerciales en formato CSV; y un procesador analítico (Main) que transforma estos datos en reportes útiles para la toma de decisiones.

# Instalación y Ejecución del Proyecto

## Software Necesario:
- Java JDK 17 o superior
- Git para clonar el repositorio
- Terminal o línea de comandos

## Pasos de Instalación:
1. Clonar el repositorio
2. Compilar el proyecto
3. Generar datos de prueba
4. Procesar datos y generar reportes

## 📁 Estructura de Archivos
```plaintext
proyecto/
├── src/
│   └── app/
│       ├── GenerateInfoFiles.java    # Generador de datos
│       └── main.java                 # Procesador principal
├── data/
│   ├── productos.csv                 # Catálogo de productos
│   ├── vendedores.csv               # Lista de vendedores
│   └── ventas/
│       ├── ventas_CC_12345678.csv   # Ventas por vendedor
│       ├── ventas_CE_87654321.csv
│       └── ...
├── reporte_vendedores.csv           # Reporte generado
├── reporte_productos.csv            # Reporte generado
└── bin/                             # Archivos compilados
```
# Estructura del Código

## Clases principales

### GenerateInfoFiles:
-	Product: Representa un producto (id, nombre, precio)
-	Salesman: Representa un vendedor (documento, nombres, apellidos)

### Main:
-	Producto: Extiende con cantidad vendida
-	Vendedor: Extiende con ventas totales

## Métodos Principales

### GenerateInfoFiles:
-	createProductsFile(): Genera catálogo de productos
- 	createSalesManInfoFile(): Genera información de vendedores
-	createSalesMenFile(): Genera archivos de ventas

### Main:
-	cargarDatos(): Carga y mapea archivos CSV
-	procesarArchivoVenta(): Procesa ventas y actualiza estadísticas
-	generarReportes(): Genera reportes finales

# Consideraciones Importantes

## Limitaciones
-	Los archivos de ventas deben seguir la convención: vendedor_*.csv
-	El sistema sobrescribe reportes existentes en cada ejecución
-	Validación básica de datos de entrada

## Manejo de Errores
-	Advertencias para archivos con formato incorrecto
-	Continuación de procesamiento ante errores individuales
-	Logs informativos durante la ejecución

## Compatibilidad
-	Java 8+: Uso de Stream API y NIO.2
-	Codificación: UTF-8 para caracteres especiales
-	Separadores: Sistema compatible con comas (,) como separador CSV estándar

## Casos de Uso

1.	Simulación de Datos: Generar datasets de prueba para desarrollo
2.	Análisis de Rendimiento: Identificar top performers en ventas
3.	Gestión de Inventario: Analizar productos más demandados
4.	Reporting Automático: Generación periódica de informes de ventas
5.	Pruebas de Carga: Validar sistemas con volúmenes controlados de datos

## Contribución

## Estándares de Código

-	Mantener compatibilidad con Java 8
-	Conservar formato de archivos CSV existente
-	Documentar cambios en configuraciones
-	Incluir manejo de excepciones

## Licencia
Proyecto desarrollado para fines académicos y de aprendizaje. Código libre para uso educativo y comercial.












