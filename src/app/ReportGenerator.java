package app;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@code ReportGenerator} es la clase encargada de:
 * <ul>
 *     <li>Cargar datos desde archivos CSV (productos, vendedores, ventas).</li>
 *     <li>Procesar las ventas por vendedor.</li>
 *     <li>Generar reportes en formato CSV y mostrarlos en consola.</li>
 * </ul>
 *
 * Los archivos de entrada esperados se encuentran en la carpeta <b>./data</b>:
 * <ul>
 *     <li><b>productos.csv</b></li>
 *     <li><b>vendedores.csv</b></li>
 *     <li>Archivos en ./data/ventas/ con ventas por vendedor</li>
 * </ul>
 *
 * Los reportes generados se crean en:
 * <ul>
 *     <li><b>reporte_vendedores.csv</b></li>
 *     <li><b>reporte_productos.csv</b></li>
 * </ul>
 */
public class ReportGenerator {

    /**
     * Bandera de depuración.
     * <p>
     * Si está en {@code true}, muestra trazas completas de excepciones
     * (stacktrace) en consola. Si está en {@code false}, solo muestra mensajes
     * claros y resumidos de error.
     * </p>
     *
     * <p>Recomendación: mantener en {@code false} en producción.</p>
     */
    private static final boolean DEBUG = false;

    // === MODELOS ===

    /**
     * Representa un producto disponible en el sistema.
     */
    public static class Producto {
        public String id, nombre;
        public double precio;
        public int cantidadVendida = 0;

        /**
         * Constructor de Producto.
         *
         * @param id     Identificador único (ej. P0001).
         * @param n      Nombre del producto.
         * @param p      Precio unitario.
         */
        public Producto(String id, String n, double p) {
            this.id = id;
            this.nombre = n;
            this.precio = p;
        }

        public String getId() { return id; }
    }

    /**
     * Representa un vendedor dentro del sistema.
     */
    public static class Vendedor {
        public String tipoDoc, numDoc, nombres, apellidos;
        public double ventasTotales = 0.0;

        /**
         * Constructor de Vendedor.
         *
         * @param td Tipo de documento (CC, CE, TI).
         * @param nd Número de documento.
         * @param n  Nombres.
         * @param a  Apellidos.
         */
        public Vendedor(String td, String nd, String n, String a) {
            this.tipoDoc = td;
            this.numDoc = nd;
            this.nombres = n;
            this.apellidos = a;
        }

        public String getNumDoc() { return numDoc; }
    }

    // === Cargar datos desde CSV ===

    /**
     * Carga datos desde un archivo CSV y los transforma en un mapa.
     *
     * @param archivo    Ruta del archivo CSV.
     * @param constructor Función que convierte una línea CSV en un objeto.
     * @param getKey      Función que extrae la clave única del objeto.
     * @param <T>         Tipo de objeto a mapear.
     * @return Un {@code Map<String, T>} con los objetos cargados.
     * @throws IOException Si ocurre un error de lectura del archivo.
     */
    public static <T> Map<String, T> cargarDatos(
            String archivo,
            Function<String, T> constructor,
            Function<T, String> getKey
    ) throws IOException {
        return Files.lines(Paths.get(archivo))
                .filter(l -> !l.trim().isEmpty())
                .map(constructor)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(getKey, item -> item));
    }

    // === Procesar archivo de ventas ===

    /**
     * Procesa un archivo de ventas y actualiza los productos y vendedores.
     *
     * @param archivo Ruta del archivo de ventas a procesar.
     * @param prods   Mapa de productos cargados.
     * @param vends   Mapa de vendedores cargados.
     */
    public static void procesarArchivoVenta(Path archivo, Map<String, Producto> prods, Map<String, Vendedor> vends) {
        try {
            List<String> lineas = Files.readAllLines(archivo);
            if (lineas.isEmpty()) return;

            String[] cabecera = lineas.get(0).split(";");
            if (cabecera.length < 2) return;
            String idVendedor = cabecera[1].trim();

            Vendedor vendedor = vends.get(idVendedor);
            if (vendedor == null) return;

            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = lineas.get(i).split(";");
                if (datos.length < 2) continue;

                String idProd = datos[0].trim();
                String qtyStr = datos[1].trim();
                if (idProd.isEmpty() || qtyStr.isEmpty()) continue;

                Producto producto = prods.get(idProd);
                if (producto == null) continue;

                int cantidad = Integer.parseInt(qtyStr);
                vendedor.ventasTotales += producto.precio * cantidad;
                producto.cantidadVendida += cantidad;
            }
        } catch (Exception e) {
            System.err.println("⚠️ ADVERTENCIA procesando archivo: " + archivo.getFileName());
            if (DEBUG) e.printStackTrace();
        }
    }

    // === Generar reportes ===

    /**
     * Genera los reportes de vendedores y productos.
     * <ul>
     *     <li>reporte_vendedores.csv</li>
     *     <li>reporte_productos.csv</li>
     * </ul>
     *
     * @param mapaVendedores Mapa de vendedores con datos procesados.
     * @param mapaProductos  Mapa de productos con datos procesados.
     * @throws IOException Si ocurre un error de escritura en disco.
     */
    public static void generarReportes(Map<String, Vendedor> mapaVendedores, Map<String, Producto> mapaProductos) throws IOException {
        Path dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        // --- Reporte de vendedores ---
        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
                .sorted(Comparator.comparingDouble(v -> -v.ventasTotales))
                .toList();

        Path vendedoresFile = dataDir.resolve("reporte_vendedores.csv");
        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(vendedoresFile, StandardCharsets.UTF_8))) {
            writer.println("Vendedor;TipoDoc;Documento;VentasTotales");
            for (Vendedor v : vendedoresOrdenados) {
                String nombreCompleto = (v.nombres + " " + v.apellidos).trim();
                writer.printf("%s;%s;%s;%.2f%n", nombreCompleto, v.tipoDoc, v.numDoc, v.ventasTotales);
            }
        }

        // --- Consola ---
        System.out.println("\n📊 === REPORTE DE VENDEDORES ===");
        System.out.printf("%-25s %-8s %-15s %12s%n", "Nombre", "TipoDoc", "Documento", "Ventas Totales");
        System.out.println("--------------------------------------------------------------------------");
        for (Vendedor v : vendedoresOrdenados) {
            String nombreCompleto = (v.nombres + " " + v.apellidos).trim();
            System.out.printf("%-25s %-8s %-15s %12.2f%n",
                    nombreCompleto, v.tipoDoc, v.numDoc, v.ventasTotales);
        }

        // --- Reporte de productos ---
        List<Producto> productosOrdenados = mapaProductos.values().stream()
                .sorted(Comparator.comparingInt(p -> -p.cantidadVendida))
                .toList();

        Path productosFile = dataDir.resolve("reporte_productos.csv");
        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(productosFile, StandardCharsets.UTF_8))) {
            writer.println("Producto;Precio;CantidadVendida");
            for (Producto p : productosOrdenados) {
                writer.printf("%s;%.2f;%d%n", p.nombre, p.precio, p.cantidadVendida);
            }
        }

        // --- Consola ---
        System.out.println("\n📦 === REPORTE DE PRODUCTOS ===");
        System.out.printf("%-20s %10s %15s%n", "Producto", "Precio", "Cantidad Vendida");
        System.out.println("---------------------------------------------------------------");
        for (Producto p : productosOrdenados) {
            System.out.printf("%-20s %10.2f %15d%n",
                    p.nombre, p.precio, p.cantidadVendida);
        }

        System.out.println("\n✅ ¡Reportes generados en ./data y mostrados en pantalla!");
    }

    // === Parsers ===

    /**
     * Convierte una línea CSV en un objeto {@link Producto}.
     *
     * @param linea Línea en formato CSV.
     * @return Objeto Producto o {@code null} si hay error de formato.
     */
    public static Producto parseProducto(String linea) {
        if (linea == null || linea.trim().isEmpty()) return null;
        String[] d = linea.split(";");
        if (d.length < 3) return null;
        try {
            return new Producto(d[0].trim(), d[1].trim(), Double.parseDouble(d[2].trim()));
        } catch (Exception e) {
            System.err.println("Error parseando producto: " + linea + " -> " + e.getMessage());
            return null;
        }
    }

    /**
     * Convierte una línea CSV en un objeto {@link Vendedor}.
     *
     * @param linea Línea en formato CSV.
     * @return Objeto Vendedor o {@code null} si hay error de formato.
     */
    public static Vendedor parseVendedor(String linea) {
        if (linea == null || linea.trim().isEmpty()) return null;
        String[] d = linea.split(";");
        if (d.length < 4) return null;
        try {
            return new Vendedor(d[0].trim(), d[1].trim(), d[2].trim(), d[3].trim());
        } catch (Exception e) {
            System.err.println("Error parseando vendedor: " + linea + " -> " + e.getMessage());
            return null;
        }
    }

    /**
     * Ejecuta todo el pipeline de generación de reportes:
     * <ol>
     *     <li>Valida la existencia de los archivos base.</li>
     *     <li>Carga productos y vendedores.</li>
     *     <li>Procesa los archivos de ventas en ./data/ventas.</li>
     *     <li>Genera reportes en CSV y en consola.</li>
     * </ol>
     *
     * @throws IOException Si ocurre un error de lectura/escritura en disco.
     */
    public static void runPipeline() throws IOException {

        Path dataDir = Paths.get("data");
        Path salesDir = dataDir.resolve("ventas");

        // Validaciones iniciales
        if (!Files.exists(dataDir)) {
            System.err.println("❌ No existe la carpeta ./data. Ejecute primero la generación de archivos.");
            return;
        }
        if (!Files.exists(dataDir.resolve("productos.csv"))) {
            System.err.println("❌ Archivo productos.csv no encontrado en ./data");
            return;
        }
        if (!Files.exists(dataDir.resolve("vendedores.csv"))) {
            System.err.println("❌ Archivo vendedores.csv no encontrado en ./data");
            return;
        }

        Map<String, Producto> productos = cargarDatos(
                dataDir.resolve("productos.csv").toString(),
                ReportGenerator::parseProducto,
                p -> p.id
        );

        Map<String, Vendedor> vendedores = cargarDatos(
                dataDir.resolve("vendedores.csv").toString(),
                ReportGenerator::parseVendedor,
                v -> v.numDoc
        );

        // Procesamiento de ventas
        int procesados = 0;
        int errores = 0;

        if (Files.exists(salesDir)) {
            try (var stream = Files.list(salesDir)) {
                for (Path p : stream.filter(Files::isRegularFile).filter(f -> f.toString().endsWith(".csv")).toList()) {
                    try {
                        procesarArchivoVenta(p, productos, vendedores);
                        procesados++;
                    } catch (Exception e) {
                        errores++;
                        System.err.printf("⚠️ Error procesando archivo %s: %s%n", p.getFileName(), e.getMessage());
                        if (DEBUG) e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("⚠️ No existe el directorio de ventas: " + salesDir.toString());
        }

        // Resumen
        System.out.printf("📊 Resumen: %d archivos procesados correctamente, %d con errores.%n", procesados, errores);

        generarReportes(vendedores, productos);
    }

}
