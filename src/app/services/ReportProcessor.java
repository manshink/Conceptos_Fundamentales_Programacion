// Archivo: services/ReportProcessor.java
package app.services;

import app.models.Producto;
import app.models.Vendedor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Clase utilitaria para procesar archivos de reportes y datos de ventas.
 * Maneja la carga de productos y vendedores desde archivos CSV, así como
 * el procesamiento de archivos de ventas individuales.
 * 
 * @author Tu nombre
 * @version 1.1
 */
public class ReportProcessor {
    
    // Logger para manejo profesional de logs
    private static final Logger logger = Logger.getLogger(ReportProcessor.class.getName());
    
    // Constantes para configuración
    private static final String SEPARADOR_CSV = ";";
    private static final int LINEAS_ENCABEZADO_A_SALTAR = 1;

    /**
     * Carga productos desde un archivo CSV y los retorna como un Map indexado por ID.
     * 
     * Formato esperado del archivo CSV:
     * - Primera línea: encabezado (se ignora)
     * - Líneas siguientes: id;nombre;precio
     * 
     * @param archivo Ruta del archivo CSV de productos
     * @return Map donde la clave es el ID del producto y el valor es el objeto Producto
     * @throws IOException si ocurre un error al leer el archivo
     * @throws IllegalArgumentException si el archivo es null o no existe
     */
    public static Map<String, Producto> cargarProductos(String archivo) throws IOException {
        // Validación de entrada
        if (archivo == null || archivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo de productos no puede estar vacía");
        }
        
        Path rutaArchivo = Paths.get(archivo);
        if (!Files.exists(rutaArchivo)) {
            throw new IOException("El archivo de productos no existe: " + archivo);
        }
        
        logger.info("Iniciando carga de productos desde: " + archivo);
        
        try (Stream<String> lines = Files.lines(rutaArchivo)) {
            Map<String, Producto> productos = lines
                    .skip(LINEAS_ENCABEZADO_A_SALTAR) // Ignorar el encabezado
                    .map(linea -> {
                        Producto producto = Producto.fromCsv(linea);
                        if (producto == null) {
                            logger.warning("No se pudo procesar la línea de producto: " + linea);
                        }
                        return producto;
                    })
                    .filter(Objects::nonNull) // Filtrar productos nulos (errores de parsing)
                    .collect(Collectors.toMap(
                        Producto::getId, 
                        producto -> producto,
                        // En caso de IDs duplicados, mantener el primero y logear advertencia
                        (existente, duplicado) -> {
                            logger.warning("ID de producto duplicado encontrado: " + existente.getId() + 
                                         ". Manteniendo el primer registro.");
                            return existente;
                        }
                    ));
            
            logger.info("Carga completada. Total de productos cargados: " + productos.size());
            return productos;
            
        } catch (IOException e) {
            logger.severe("Error al leer el archivo de productos: " + archivo);
            throw e;
        }
    }

    /**
     * Carga vendedores desde un archivo CSV y los retorna como un Map indexado por número de documento.
     * 
     * Formato esperado del archivo CSV:
     * - Primera línea: encabezado (se ignora)
     * - Líneas siguientes: tipoDoc;numDoc;nombres;apellidos
     * 
     * @param archivo Ruta del archivo CSV de vendedores
     * @return Map donde la clave es el número de documento y el valor es el objeto Vendedor
     * @throws IOException si ocurre un error al leer el archivo
     * @throws IllegalArgumentException si el archivo es null o no existe
     */
    public static Map<String, Vendedor> cargarVendedores(String archivo) throws IOException {
        // Validación de entrada
        if (archivo == null || archivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo de vendedores no puede estar vacía");
        }
        
        Path rutaArchivo = Paths.get(archivo);
        if (!Files.exists(rutaArchivo)) {
            throw new IOException("El archivo de vendedores no existe: " + archivo);
        }
        
        logger.info("Iniciando carga de vendedores desde: " + archivo);
        
        try (Stream<String> lines = Files.lines(rutaArchivo)) {
            Map<String, Vendedor> vendedores = lines
                    .skip(LINEAS_ENCABEZADO_A_SALTAR) // Ignorar el encabezado
                    .map(linea -> {
                        Vendedor vendedor = Vendedor.fromCsv(linea);
                        if (vendedor == null) {
                            logger.warning("No se pudo procesar la línea de vendedor: " + linea);
                        }
                        return vendedor;
                    })
                    .filter(Objects::nonNull) // Filtrar vendedores nulos (errores de parsing)
                    .collect(Collectors.toMap(
                        Vendedor::getNumDoc, 
                        vendedor -> vendedor,
                        // En caso de números de documento duplicados, mantener el primero y logear advertencia
                        (existente, duplicado) -> {
                            logger.warning("Número de documento duplicado encontrado: " + existente.getNumDoc() + 
                                         ". Manteniendo el primer registro.");
                            return existente;
                        }
                    ));
            
            logger.info("Carga completada. Total de vendedores cargados: " + vendedores.size());
            return vendedores;
            
        } catch (IOException e) {
            logger.severe("Error al leer el archivo de vendedores: " + archivo);
            throw e;
        }
    }

    /**
     * Procesa un archivo de ventas individual y actualiza los totales correspondientes
     * en los mapas de productos y vendedores.
     * 
     * Formato esperado del archivo de ventas:
     * - Primera línea: CC;numeroDocumentoVendedor (información del vendedor)
     * - Líneas siguientes: idProducto;cantidad (registros de ventas)
     * 
     * @param archivo Path del archivo de ventas a procesar
     * @param productos Map de productos previamente cargado
     * @param vendedores Map de vendedores previamente cargado
     * @return ResumenVenta objeto con estadísticas del procesamiento
     */
    public static ResumenVenta procesarArchivoVenta(Path archivo, 
                                                   Map<String, Producto> productos, 
                                                   Map<String, Vendedor> vendedores) {
        
        // Validación de parámetros
        if (archivo == null) {
            logger.severe("El archivo de ventas no puede ser null");
            return new ResumenVenta(false, "Archivo null proporcionado");
        }
        
        if (productos == null || vendedores == null) {
            logger.severe("Los mapas de productos y vendedores no pueden ser null");
            return new ResumenVenta(false, "Mapas de datos null");
        }
        
        // Verificar que el archivo existe
        if (!Files.exists(archivo)) {
            String mensaje = "Archivo no encontrado: " + archivo.getFileName();
            logger.warning(mensaje);
            return new ResumenVenta(false, mensaje);
        }
        
        logger.info("Procesando archivo de ventas: " + archivo.getFileName());
        
        try (Stream<String> lines = Files.lines(archivo)) {
            List<String> lineas = lines.collect(Collectors.toList());
            
            // Verificar que el archivo no esté vacío
            if (lineas.isEmpty()) {
                String mensaje = "Archivo vacío: " + archivo.getFileName();
                logger.warning(mensaje);
                return new ResumenVenta(false, mensaje);
            }

            // Procesar la primera línea para obtener el ID del vendedor
            String primeraLinea = lineas.get(0);
            String[] datosVendedor = primeraLinea.split(SEPARADOR_CSV);
            
            if (datosVendedor.length < 2) {
                String mensaje = "Formato inválido en la primera línea del archivo: " + archivo.getFileName();
                logger.severe(mensaje);
                return new ResumenVenta(false, mensaje);
            }

            String idVendedor = datosVendedor[1].trim();
            Vendedor vendedor = vendedores.get(idVendedor);

            if (vendedor == null) {
                String mensaje = "Vendedor con ID " + idVendedor + " no encontrado en archivo: " + archivo.getFileName();
                logger.warning(mensaje);
                return new ResumenVenta(false, mensaje);
            }

            // Contadores para estadísticas
            int ventasProcesadas = 0;
            int ventasConError = 0;
            double totalVentasArchivo = 0.0;

            // Procesar las ventas de cada línea (desde la línea 2 en adelante)
            for (int i = 1; i < lineas.size(); i++) {
                String lineaActual = lineas.get(i);
                String[] datos = lineaActual.split(SEPARADOR_CSV);
                
                // Validar formato de la línea
                if (datos.length < 2) {
                    logger.warning("Línea mal formada en archivo " + archivo.getFileName() + 
                                 ", línea " + (i + 1) + ": " + lineaActual);
                    ventasConError++;
                    continue;
                }

                try {
                    // Extraer datos de la venta
                    String idProducto = datos[0].trim();
                    int cantidad = Integer.parseInt(datos[1].trim());
                    
                    // Validar cantidad
                    if (cantidad <= 0) {
                        logger.warning("Cantidad inválida en archivo " + archivo.getFileName() + 
                                     ", línea " + (i + 1) + ": " + cantidad);
                        ventasConError++;
                        continue;
                    }
                    
                    // Buscar el producto
                    Producto producto = productos.get(idProducto);
                    
                    if (producto != null) {
                        // Calcular el total de la venta
                        double totalVenta = producto.getPrecio() * cantidad;
                        
                        // Actualizar totales
                        boolean ventaAgregada = vendedor.agregarVentasTotales(totalVenta);
                        boolean cantidadAgregada = producto.agregarCantidadVendida(cantidad);
                        
                        if (ventaAgregada && cantidadAgregada) {
                            ventasProcesadas++;
                            totalVentasArchivo += totalVenta;
                            
                            logger.fine(String.format("Venta procesada: Producto %s, Cantidad %d, Total: %.2f", 
                                                     idProducto, cantidad, totalVenta));
                        } else {
                            ventasConError++;
                            logger.warning("Error al actualizar totales para producto: " + idProducto);
                        }
                        
                    } else {
                        logger.warning("Producto con ID " + idProducto + " no encontrado en archivo: " + 
                                     archivo.getFileName() + ", línea " + (i + 1));
                        ventasConError++;
                    }
                    
                } catch (NumberFormatException e) {
                    logger.warning("Error al parsear cantidad en archivo " + archivo.getFileName() + 
                                 ", línea " + (i + 1) + ": " + lineaActual + " - " + e.getMessage());
                    ventasConError++;
                }
            }
            
            // Crear resumen del procesamiento
            String mensaje = String.format("Archivo %s procesado exitosamente. Ventas: %d, Errores: %d, Total: %.2f",
                                          archivo.getFileName(), ventasProcesadas, ventasConError, totalVentasArchivo);
            logger.info(mensaje);
            
            return new ResumenVenta(true, mensaje, ventasProcesadas, ventasConError, totalVentasArchivo, vendedor);

        } catch (IOException e) {
            String mensaje = "Error procesando el archivo: " + archivo.getFileName() + " - " + e.getMessage();
            logger.severe(mensaje);
            return new ResumenVenta(false, mensaje);
        }
    }
    
    /**
     * Procesa múltiples archivos de ventas en un directorio.
     * 
     * @param directorioVentas Path del directorio que contiene los archivos de ventas
     * @param productos Map de productos previamente cargado
     * @param vendedores Map de vendedores previamente cargado
     * @return List de ResumenVenta con los resultados de cada archivo procesado
     */
    public static List<ResumenVenta> procesarDirectorioVentas(Path directorioVentas,
                                                              Map<String, Producto> productos,
                                                              Map<String, Vendedor> vendedores) {
        List<ResumenVenta> resumenes = new ArrayList<>();
        
        if (!Files.exists(directorioVentas) || !Files.isDirectory(directorioVentas)) {
            logger.severe("El directorio de ventas no existe o no es un directorio: " + directorioVentas);
            return resumenes;
        }
        
        try (Stream<Path> archivos = Files.list(directorioVentas)) {
            archivos.filter(Files::isRegularFile)
                   .filter(archivo -> archivo.toString().toLowerCase().endsWith(".csv"))
                   .forEach(archivo -> {
                       ResumenVenta resumen = procesarArchivoVenta(archivo, productos, vendedores);
                       resumenes.add(resumen);
                   });
                   
        } catch (IOException e) {
            logger.severe("Error al listar archivos del directorio: " + directorioVentas);
        }
        
        return resumenes;
    }

    /**
     * Clase interna para encapsular el resultado del procesamiento de un archivo de ventas.
     */
    public static class ResumenVenta {
        private final boolean exito;
        private final String mensaje;
        private final int ventasProcesadas;
        private final int ventasConError;
        private final double totalVentas;
        private final Vendedor vendedor;
        
        // Constructor para casos de error
        public ResumenVenta(boolean exito, String mensaje) {
            this(exito, mensaje, 0, 0, 0.0, null);
        }
        
        // Constructor completo
        public ResumenVenta(boolean exito, String mensaje, int ventasProcesadas, 
                           int ventasConError, double totalVentas, Vendedor vendedor) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.ventasProcesadas = ventasProcesadas;
            this.ventasConError = ventasConError;
            this.totalVentas = totalVentas;
            this.vendedor = vendedor;
        }
        
        // Getters
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public int getVentasProcesadas() { return ventasProcesadas; }
        public int getVentasConError() { return ventasConError; }
        public double getTotalVentas() { return totalVentas; }
        public Vendedor getVendedor() { return vendedor; }
        
        @Override
        public String toString() {
            return String.format("ResumenVenta{exito=%s, ventasProcesadas=%d, errores=%d, total=%.2f}",
                               exito, ventasProcesadas, ventasConError, totalVentas);
        }
    }
}