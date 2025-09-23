// Archivo: models/Producto.java
package app.models;

/**
 * Clase que representa un producto en el sistema de ventas.
 * Almacena información básica del producto y mantiene un contador
 * de la cantidad total vendida.
 * 
 * @author Tu nombre
 * @version 1.1
 */
public class Producto {
    // Atributos privados para encapsular los datos del producto
    private String id;              // Identificador único del producto
    private String nombre;          // Nombre descriptivo del producto
    private double precio;          // Precio unitario del producto
    private int cantidadVendida;    // Contador de unidades vendidas (inicializado en 0)

    /**
     * Constructor principal para crear un nuevo producto.
     * 
     * @param id Identificador único del producto (no puede ser null o vacío)
     * @param nombre Nombre del producto (no puede ser null o vacío)
     * @param precio Precio del producto (debe ser mayor o igual a 0)
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public Producto(String id, String nombre, double precio) {
        // Validación de entrada para asegurar datos consistentes
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del producto no puede estar vacío");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        
        // Asignación de valores, eliminando espacios en blanco adicionales
        this.id = id.trim();
        this.nombre = nombre.trim();
        this.precio = precio;
        this.cantidadVendida = 0; // Inicialización explícita para claridad
    }

    /**
     * Método de fábrica (Factory Method) para crear un Producto desde una línea CSV.
     * Formato esperado: "id;nombre;precio"
     * 
     * @param linea String con los datos del producto separados por punto y coma
     * @return Producto creado a partir de los datos, o null si hay errores
     */
    public static Producto fromCsv(String linea) {
        // Validación de entrada
        if (linea == null || linea.trim().isEmpty()) {
            System.err.println("Error: Línea CSV vacía o nula");
            return null;
        }
        
        // División de la línea por el separador punto y coma
        String[] datos = linea.split(";");
        
        // Verificación de que tengamos al menos los 3 campos requeridos
        if (datos.length < 3) {
            System.err.println("Error: Datos insuficientes en línea CSV: " + linea);
            return null;
        }
        
        try {
            // Extracción y limpieza de datos
            String id = datos[0].trim();
            String nombre = datos[1].trim();
            
            // Conversión del precio con manejo de errores
            double precio = Double.parseDouble(datos[2].trim());
            
            // Creación del producto usando el constructor (que incluye validaciones)
            return new Producto(id, nombre, precio);
            
        } catch (NumberFormatException e) {
            // Manejo específico de errores de conversión numérica
            System.err.println("Error al parsear precio en línea: " + linea);
            System.err.println("Detalle del error: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            // Manejo de errores de validación del constructor
            System.err.println("Error de validación en línea: " + linea);
            System.err.println("Detalle: " + e.getMessage());
            return null;
        }
    }

    // ==================== MÉTODOS GETTER ====================
    // Proporcionan acceso de solo lectura a los atributos privados
    
    /**
     * @return ID único del producto
     */
    public String getId() { 
        return id; 
    }
    
    /**
     * @return Nombre del producto
     */
    public String getNombre() { 
        return nombre; 
    }
    
    /**
     * @return Precio unitario del producto
     */
    public double getPrecio() { 
        return precio; 
    }
    
    /**
     * @return Cantidad total de unidades vendidas
     */
    public int getCantidadVendida() { 
        return cantidadVendida; 
    }

    // ==================== MÉTODOS DE NEGOCIO ====================
    
    /**
     * Actualiza la cantidad vendida del producto.
     * Solo acepta cantidades positivas para mantener la integridad de los datos.
     * 
     * @param cantidad Número de unidades a agregar (debe ser mayor que 0)
     * @return true si la operación fue exitosa, false si la cantidad es inválida
     */
    public boolean agregarCantidadVendida(int cantidad) {
        // Validación para asegurar que solo se agreguen cantidades positivas
        if (cantidad > 0) {
            this.cantidadVendida += cantidad;
            return true; // Operación exitosa
        } else {
            // Log de advertencia para cantidades inválidas
            System.err.println("Advertencia: Intento de agregar cantidad inválida: " + cantidad);
            return false; // Operación fallida
        }
    }
    
    /**
     * Calcula el total de ingresos generados por este producto.
     * 
     * @return Ingresos totales (precio * cantidadVendida)
     */
    public double calcularIngresosTotales() {
        return precio * cantidadVendida;
    }
    
    /**
     * Método toString para facilitar la depuración y logging.
     * 
     * @return Representación en string del producto con todos sus datos
     */
    @Override
    public String toString() {
        return String.format("Producto{id='%s', nombre='%s', precio=%.2f, cantidadVendida=%d, ingresos=%.2f}",
                           id, nombre, precio, cantidadVendida, calcularIngresosTotales());
    }
    
    /**
     * Método equals para comparar productos por su ID único.
     * 
     * @param obj Objeto a comparar
     * @return true si los productos tienen el mismo ID
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return id.equals(producto.id);
    }
    
    /**
     * Método hashCode basado en el ID del producto.
     * 
     * @return Hash code del producto
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}