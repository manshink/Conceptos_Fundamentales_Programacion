// Archivo: models/Vendedor.java
package app.models;

import java.util.Objects;

/**
 * Clase que representa un vendedor en el sistema de ventas.
 * Almacena la información personal del vendedor y mantiene un registro
 * del total de ventas acumuladas.
 * 
 * @author Tu nombre
 * @version 1.1
 */
public class Vendedor {
    // Atributos privados para encapsular los datos del vendedor
    private String tipoDoc;         // Tipo de documento (CC, CE, TI, etc.)
    private String numDoc;          // Número de documento de identidad (único)
    private String nombres;         // Nombres del vendedor
    private String apellidos;       // Apellidos del vendedor
    private double ventasTotales;   // Total acumulado de ventas realizadas

    /**
     * Constructor principal para crear un nuevo vendedor.
     * 
     * @param tipoDoc Tipo de documento de identidad (no puede ser null o vacío)
     * @param numDoc Número de documento de identidad (no puede ser null o vacío)
     * @param nombres Nombres del vendedor (no puede ser null o vacío)
     * @param apellidos Apellidos del vendedor (no puede ser null o vacío)
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public Vendedor(String tipoDoc, String numDoc, String nombres, String apellidos) {
        // Validación exhaustiva de todos los parámetros de entrada
        if (tipoDoc == null || tipoDoc.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento no puede estar vacío");
        }
        if (numDoc == null || numDoc.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }
        if (nombres == null || nombres.trim().isEmpty()) {
            throw new IllegalArgumentException("Los nombres no pueden estar vacíos");
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos");
        }
        
        // Asignación de valores con limpieza de espacios en blanco
        this.tipoDoc = tipoDoc.trim().toUpperCase(); // Normalizar tipo de documento
        this.numDoc = numDoc.trim();
        this.nombres = nombres.trim();
        this.apellidos = apellidos.trim();
        this.ventasTotales = 0.0; // Inicialización explícita para claridad
    }

    /**
     * Método de fábrica (Factory Method) para crear un Vendedor desde una línea CSV.
     * Formato esperado: "tipoDoc;numDoc;nombres;apellidos"
     * 
     * @param linea String con los datos del vendedor separados por punto y coma
     * @return Vendedor creado a partir de los datos, o null si hay errores
     */
    public static Vendedor fromCsv(String linea) {
        // Validación de entrada
        if (linea == null || linea.trim().isEmpty()) {
            System.err.println("Error: Línea CSV vacía o nula para Vendedor");
            return null;
        }
        
        // División de la línea por el separador punto y coma
        String[] datos = linea.split(";");
        
        // Verificación de que tengamos al menos los 4 campos requeridos
        if (datos.length < 4) {
            System.err.println("Error: Datos insuficientes en línea CSV de Vendedor: " + linea);
            return null;
        }
        
        try {
            // Creación del vendedor usando el constructor (que incluye validaciones)
            return new Vendedor(
                datos[0].trim(), // tipoDoc
                datos[1].trim(), // numDoc
                datos[2].trim(), // nombres
                datos[3].trim()  // apellidos
            );
            
        } catch (IllegalArgumentException e) {
            // Manejo de errores de validación del constructor
            System.err.println("Error de validación en línea CSV de Vendedor: " + linea);
            System.err.println("Detalle: " + e.getMessage());
            return null;
        }
    }

    // ==================== MÉTODOS GETTER ====================
    // Proporcionan acceso de solo lectura a los atributos privados
    
    /**
     * @return Tipo de documento del vendedor
     */
    public String getTipoDoc() { 
        return tipoDoc; 
    }
    
    /**
     * @return Número de documento del vendedor (identificador único)
     */
    public String getNumDoc() { 
        return numDoc; 
    }
    
    /**
     * @return Nombres del vendedor
     */
    public String getNombres() { 
        return nombres; 
    }
    
    /**
     * @return Apellidos del vendedor
     */
    public String getApellidos() { 
        return apellidos; 
    }
    
    /**
     * @return Total acumulado de ventas del vendedor
     */
    public double getVentasTotales() { 
        return ventasTotales; 
    }

    // ==================== MÉTODOS DE NEGOCIO ====================
    
    /**
     * Actualiza el total de ventas del vendedor.
     * Solo acepta montos positivos para mantener la integridad de los datos.
     * 
     * @param ventas Monto de ventas a agregar (debe ser mayor que 0)
     * @return true si la operación fue exitosa, false si el monto es inválido
     */
    public boolean agregarVentasTotales(double ventas) {
        // Validación para asegurar que solo se agreguen montos positivos
        if (ventas > 0) {
            this.ventasTotales += ventas;
            return true; // Operación exitosa
        } else {
            // Log de advertencia para montos inválidos
            System.err.println("Advertencia: Intento de agregar venta inválida: " + ventas + 
                              " al vendedor " + getNombreCompleto());
            return false; // Operación fallida
        }
    }
    
    /**
     * Obtiene el nombre completo del vendedor.
     * 
     * @return Nombres y apellidos concatenados
     */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
    
    /**
     * Obtiene la identificación completa del vendedor.
     * 
     * @return Tipo y número de documento concatenados
     */
    public String getIdentificacionCompleta() {
        return tipoDoc + " " + numDoc;
    }
    
    /**
     * Verifica si el vendedor ha realizado ventas.
     * 
     * @return true si tiene ventas registradas, false en caso contrario
     */
    public boolean tieneVentas() {
        return ventasTotales > 0;
    }
    
    /**
     * Reinicia el contador de ventas del vendedor.
     * Útil para períodos de reporte o al inicio de nuevos ciclos de ventas.
     */
    public void reiniciarVentas() {
        this.ventasTotales = 0.0;
    }
    
    /**
     * Método toString para facilitar la depuración y logging.
     * 
     * @return Representación en string del vendedor con todos sus datos
     */
    @Override
    public String toString() {
        return String.format("Vendedor{%s %s, %s, ventasTotales=%.2f}",
                           tipoDoc, numDoc, getNombreCompleto(), ventasTotales);
    }
    
    /**
     * Método equals para comparar vendedores por su número de documento.
     * Se asume que el número de documento es único para cada vendedor.
     * 
     * @param obj Objeto a comparar
     * @return true si los vendedores tienen el mismo número de documento
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vendedor vendedor = (Vendedor) obj;
        return Objects.equals(numDoc, vendedor.numDoc);
    }
    
    /**
     * Método hashCode basado en el número de documento del vendedor.
     * 
     * @return Hash code del vendedor
     */
    @Override
    public int hashCode() {
        return Objects.hash(numDoc);
    }
}