package app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * {@code GenerateInfoFiles} es la clase encargada de generar datos de ejemplo
 * en formato CSV para el sistema de reportes.
 * <p>
 * Archivos generados en la carpeta <b>./data</b>:
 * <ul>
 *   <li><b>productos.csv</b> → Catálogo de productos (ID;Nombre;Precio).</li>
 *   <li><b>vendedores.csv</b> → Lista de vendedores (TipoDocumento;NumeroDocumento;Nombres;Apellidos).</li>
 *   <li><b>ventas/ventas_{TipoDoc}_{Num}.csv</b> → Ventas asociadas a cada vendedor.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>IMPORTANTE:</b> Esta clase <i>solo</i> genera datos.
 * Los reportes se realizan mediante {@link ReportGenerator#runPipeline()}.
 * </p>
 */
public class GenerateInfoFiles {

    /**
     * Bandera de depuración.
     * Si {@code true}, muestra trazas completas de error.
     * Si {@code false}, muestra mensajes resumidos.
     */
    private static final boolean DEBUG = false;

    // === Rutas de trabajo ===
    private static final Path DATA_DIR      = Paths.get("data");
    private static final Path SALES_DIR     = DATA_DIR.resolve("ventas");
    private static final Path PRODUCTS_FILE = DATA_DIR.resolve("productos.csv");
    private static final Path SALESMEN_FILE = DATA_DIR.resolve("vendedores.csv");

    // Generador aleatorio con semilla fija (para reproducibilidad)
    private static final Random RNG = new Random(2025_0902);

    // Listas de ejemplo
    private static final String[] NOMBRES = {
            "Camila","Sofía","Valentina","Isabella","Mariana","Sebastián","Santiago","Mateo",
            "Samuel","Daniel","Juan","Andrés","María","Laura","Sara","Nicolás","David","Lucía"
    };
    private static final String[] APELLIDOS = {
            "González","Rodríguez","Gómez","Díaz","Martínez","Pérez","Sánchez","Ramírez",
            "Torres","Vargas","Rojas","Moreno","Romero","Jiménez","Reyes","Castaño","Álvarez"
    };
    private static final String[] TIPOS_DOC = {"CC","CE","TI"};
    private static final String PRODUCT_PREFIX = "P";

    /**
     * Método utilitario (no es un {@code public static void main} estándar).
     * <p>
     * Invocado desde {@code app.Main} para generar todos los archivos de ejemplo.
     * </p>
     *
     * @param args no utilizado.
     */
    public static void Main(String[] args) {
        try {
            ensureDirectories();

            // Parámetros por defecto
            int productsCount   = 50;
            int salesmanCount   = 25;
            int minSalesPerFile = 5;
            int maxSalesPerFile = 25;
            int minQtyPerSale   = 1;
            int maxQtyPerSale   = 10;

            // 1) Productos
            List<Product> catalog = createProductsFile(productsCount);

            // 2) Vendedores
            List<Salesman> salesmen = createSalesManInfoFile(salesmanCount);

            // 3) Ventas por cada vendedor
            for (Salesman s : salesmen) {
                int randomSalesCount = randomBetween(minSalesPerFile, maxSalesPerFile);
                createSalesMenFile(randomSalesCount, s.getFullName(), s.documentNumber, catalog, s.documentType, minQtyPerSale, maxQtyPerSale);
            }

            System.out.println("✅ Generación finalizada correctamente. Archivos en ./data");
        } catch (Exception ex) {
            System.err.println("❌ Error durante la generación de archivos: " + ex.getMessage());
            if (DEBUG) ex.printStackTrace();
            System.err.println("Sugerencia: Verifique permisos de escritura en la carpeta ./data");
        }
    }

    // =====================================================
    // MODELOS mínimos (solo para generación de archivos)
    // =====================================================

    /**
     * Modelo simple que representa un producto.
     */
    public static class Product {
        public final String id;
        public final String name;
        public final long unitPrice;

        public Product(String id, String name, long unitPrice) {
            this.id = Objects.requireNonNull(id);
            this.name = Objects.requireNonNull(name);
            this.unitPrice = unitPrice;
        }
    }

    /**
     * Modelo simple que representa un vendedor.
     */
    public static class Salesman {
        public final String documentType;
        public final long documentNumber;
        public final String firstNames;
        public final String lastNames;

        public Salesman(String documentType, long documentNumber, String firstNames, String lastNames) {
            this.documentType = documentType;
            this.documentNumber = documentNumber;
            this.firstNames = firstNames;
            this.lastNames = lastNames;
        }

        /** @return Nombre completo (nombres + apellidos). */
        public String getFullName() {
            return (firstNames + " " + lastNames).trim();
        }
    }

    // =====================================================
    // MÉTODOS DE GENERACIÓN
    // =====================================================

    /**
     * Crea un archivo de ventas para un vendedor con configuración por defecto.
     *
     * @param randomSalesCount número de ventas a generar.
     * @param name nombre del vendedor.
     * @param id número de documento.
     * @throws IOException si ocurre un error de escritura.
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        createSalesMenFile(randomSalesCount, name, id, null, "CC", 1, 10);
    }

    /**
     * Crea un archivo de ventas para un vendedor con parámetros completos.
     *
     * @param randomSalesCount número de ventas a generar.
     * @param name nombre del vendedor.
     * @param id número de documento.
     * @param catalog catálogo de productos (si es {@code null}, se carga de disco).
     * @param tipoDoc tipo de documento.
     * @param minQty cantidad mínima por venta.
     * @param maxQty cantidad máxima por venta.
     * @throws IOException si ocurre un error de escritura.
     */
    private static void createSalesMenFile(int randomSalesCount,
                                           String name,
                                           long id,
                                           List<Product> catalog,
                                           String tipoDoc,
                                           int minQty, int maxQty) throws IOException {
        if (randomSalesCount <= 0) randomSalesCount = 1;
        if (tipoDoc == null || tipoDoc.trim().isEmpty()) tipoDoc = "CC";

        if (catalog == null) {
            catalog = readCatalogFromFile(PRODUCTS_FILE);
        }

        Path file = SALES_DIR.resolve(String.format("ventas_%s_%d.csv", tipoDoc, id));

        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            bw.write(tipoDoc + ";" + id);
            bw.newLine();

            for (int i = 0; i < randomSalesCount; i++) {
                Product p = catalog.get(RNG.nextInt(catalog.size()));
                int qty = randomBetween(minQty, maxQty);
                bw.write(p.id + ";" + qty);
                bw.newLine();
            }
        }
    }

    /**
     * Crea el archivo de productos y devuelve el catálogo en memoria.
     *
     * @param productsCount número de productos a generar.
     * @return lista de productos.
     * @throws IOException si ocurre un error de escritura.
     */
    public static List<Product> createProductsFile(int productsCount) throws IOException {
        if (productsCount < 1) productsCount = 1;

        List<Product> catalog = new ArrayList<>(productsCount);
        DecimalFormat df = new DecimalFormat("0000");

        try (BufferedWriter bw = Files.newBufferedWriter(PRODUCTS_FILE, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            for (int i = 1; i <= productsCount; i++) {
                String id = PRODUCT_PREFIX + df.format(i);
                String name = generateProductName(i);
                long unitPrice = generatePositivePrice();
                catalog.add(new Product(id, name, unitPrice));
                bw.write(id + ";" + name + ";" + unitPrice);
                bw.newLine();
            }
        }
        return catalog;
    }

    /**
     * Crea el archivo de vendedores y devuelve la lista en memoria.
     *
     * @param salesmanCount número de vendedores a generar.
     * @return lista de vendedores.
     * @throws IOException si ocurre un error de escritura.
     */
    public static List<Salesman> createSalesManInfoFile(int salesmanCount) throws IOException {
        if (salesmanCount < 1) salesmanCount = 1;

        List<Salesman> salesmen = new ArrayList<>(salesmanCount);
        Set<Long> usedIds = new HashSet<>();

        try (BufferedWriter bw = Files.newBufferedWriter(SALESMEN_FILE, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            for (int i = 0; i < salesmanCount; i++) {
                String tipo = TIPOS_DOC[RNG.nextInt(TIPOS_DOC.length)];
                long numero = uniquePositiveId(usedIds);
                String nombres = randomFrom(NOMBRES) + (RNG.nextBoolean() ? " " + randomFrom(NOMBRES) : "");
                String apellidos = randomFrom(APELLIDOS) + (RNG.nextBoolean() ? " " + randomFrom(APELLIDOS) : "");

                Salesman s = new Salesman(tipo, numero, nombres, apellidos);
                salesmen.add(s);

                bw.write(tipo + ";" + numero + ";" + nombres + ";" + apellidos);
                bw.newLine();
            }
        }
        return salesmen;
    }

    // =====================================================
    // UTILIDADES
    // =====================================================

    /** Asegura que existan las carpetas ./data y ./data/ventas. */
    private static void ensureDirectories() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
            System.out.println("📂 Carpeta ./data creada automáticamente.");
        }
        if (!Files.exists(SALES_DIR)) {
            Files.createDirectories(SALES_DIR);
            System.out.println("📂 Carpeta ./data/ventas creada automáticamente.");
        }
    }

    private static int randomBetween(int a, int b) {
        if (a > b) { int t = a; a = b; b = t; }
        return a + RNG.nextInt(b - a + 1);
    }

    private static String randomFrom(String[] arr) {
        return arr[RNG.nextInt(arr.length)];
    }

    private static long uniquePositiveId(Set<Long> used) {
        long n;
        do {
            n = 10_000_000L + (Math.abs(RNG.nextLong()) % 900_000_000L);
        } while (used.contains(n));
        used.add(n);
        return n;
    }

    private static long generatePositivePrice() {
        return 1_000L + (Math.abs(RNG.nextLong()) % 499_000L);
    }

    private static String generateProductName(int index) {
        return "Producto " + String.format("%04d", index);
    }

    /**
     * Lee el catálogo desde {@code productos.csv}.
     *
     * @param productsCsv ruta del archivo de productos.
     * @return lista de productos cargados.
     * @throws IOException si ocurre un error de lectura.
     */
    private static List<Product> readCatalogFromFile(Path productsCsv) throws IOException {
        List<Product> list = new ArrayList<>();
        if (!Files.exists(productsCsv)) return list;

        for (String line : Files.readAllLines(productsCsv, StandardCharsets.UTF_8)) {
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String[] d = trimmed.split(";");
            if (d.length < 3) continue;
            try {
                String id = d[0].trim();
                String name = d[1].trim();
                long price = Long.parseLong(d[2].trim());
                list.add(new Product(id, name, price));
            } catch (Exception ignore) {
                System.err.println("⚠️ Línea inválida en productos.csv: " + trimmed);
            }
        }
        return list;
    }
}
