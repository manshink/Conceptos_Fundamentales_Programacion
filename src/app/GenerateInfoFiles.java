package app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * GenerateInfoFiles
 * ------------------
 * Genera datos de ejemplo para el sistema:
 *  - ./data/productos.csv                    (ID;Nombre;Precio)
 *  - ./data/vendedores.csv                   (TipoDocumento;NumeroDocumento;Nombres;Apellidos)
 *  - ./data/ventas/ventas_{TipoDoc}_{Num}.csv
 *      * 1a línea: TipoDocumento;NumeroDocumento
 *      * siguientes: IDProducto;Cantidad
 *
 * IMPORTANTE: Este archivo solo genera datos. Los reportes se hacen con ReportGenerator.runPipeline().
 */
public class GenerateInfoFiles {

	private static final boolean DEBUG = false;

    // === Rutas ===
    private static final Path DATA_DIR      = Paths.get("data");
    private static final Path SALES_DIR     = DATA_DIR.resolve("ventas");
    private static final Path PRODUCTS_FILE = DATA_DIR.resolve("productos.csv");
    private static final Path SALESMEN_FILE = DATA_DIR.resolve("vendedores.csv");

    // RNG con semilla fija para reproducibilidad (modificar si se desean datos distintos cada ejecución)
    private static final Random RNG = new Random(2025_0902);

    // Listas rápidas de nombres para ejemplo
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
     * No es el "public static void main(String[])" estándar.
     * Es un método utilitario invocado desde app.Main (menú) como GenerateInfoFiles.Main(...).
     */
    public static void Main(String[] args) {
        try {
            // Crear carpetas
            ensureDirectories();

            // Parámetros por defecto (ajustables)
            int productsCount   = 50;   // cantidad de productos
            int salesmanCount   = 25;   // cantidad de vendedores
            int minSalesPerFile = 5;    // ventas mínimas por vendedor
            int maxSalesPerFile = 25;   // ventas máximas por vendedor
            int minQtyPerSale   = 1;    // cantidad mínima por línea de venta
            int maxQtyPerSale   = 10;   // cantidad máxima por línea de venta

            // 1) Productos y catálogo en memoria
            List<Product> catalog = createProductsFile(productsCount);

            // 2) Vendedores y lista en memoria
            List<Salesman> salesmen = createSalesManInfoFile(salesmanCount);

            // 3) Un archivo de ventas por vendedor
            for (Salesman s : salesmen) {
                int randomSalesCount = randomBetween(minSalesPerFile, maxSalesPerFile);
                createSalesMenFile(randomSalesCount, s.getFullName(), s.documentNumber, catalog, s.documentType, minQtyPerSale, maxQtyPerSale);
            }

            System.out.println("Generación finalizada correctamente. Archivos en carpeta ./data");
        } catch (Exception ex) {
            System.err.println("❌ Error durante la generación de archivos: " + ex.getMessage());
            if (DEBUG) ex.printStackTrace();
            System.err.println("Sugerencia: Verifique permisos de escritura en la carpeta ./data");
        }
    }

    // =====================================================
    // MODELOS mínimos (solo para generación de archivos)
    // =====================================================
    public static class Product {
        public final String id;
        public final String name;
        public final long unitPrice; // enteros para CSV, compatibles con parse double después

        public Product(String id, String name, long unitPrice) {
            this.id = Objects.requireNonNull(id);
            this.name = Objects.requireNonNull(name);
            this.unitPrice = unitPrice;
        }
    }

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

        public String getFullName() {
            return (firstNames + " " + lastNames).trim();
        }
    }

    // =====================================================
    // MÉTODOS REQUERIDOS / API de generación
    // =====================================================

    /**
     * Crea un archivo de ventas para un vendedor (firma solicitada).
     * Delegamos a la sobrecarga completa con tipoDoc="CC" y cantidades 1..10.
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        createSalesMenFile(randomSalesCount, name, id, null, "CC", 1, 10);
    }

    /**
     * Sobrecarga que permite pasar catálogo ya cargado, tipoDoc y rango de cantidades.
     */
    private static void createSalesMenFile(int randomSalesCount,
                                           String name,
                                           long id,
                                           List<Product> catalog,
                                           String tipoDoc,
                                           int minQty, int maxQty) throws IOException {
        if (randomSalesCount <= 0) randomSalesCount = 1;
        if (tipoDoc == null || tipoDoc.trim().isEmpty()) tipoDoc = "CC";

        // Si no pasaron catálogo, lo leemos desde disco (productos.csv)
        if (catalog == null) {
            catalog = readCatalogFromFile(PRODUCTS_FILE);
        }

        Path file = SALES_DIR.resolve(String.format("ventas_%s_%d.csv", tipoDoc, id));

        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // Cabecera: TipoDocumento;NumeroDocumento
            bw.write(tipoDoc + ";" + id);
            bw.newLine();

            // Líneas de ventas: IDProducto;Cantidad
            for (int i = 0; i < randomSalesCount; i++) {
                Product p = catalog.get(RNG.nextInt(catalog.size()));
                int qty = randomBetween(minQty, maxQty);
                bw.write(p.id + ";" + qty);
                bw.newLine();
            }
        }
    }

    /**
     * Crea el archivo de productos (ID;Nombre;Precio) y devuelve el catálogo en memoria.
     */
    public static List<Product> createProductsFile(int productsCount) throws IOException {
        if (productsCount < 1) productsCount = 1;

        List<Product> catalog = new ArrayList<>(productsCount);
        DecimalFormat df = new DecimalFormat("0000");

        try (BufferedWriter bw = Files.newBufferedWriter(PRODUCTS_FILE, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            for (int i = 1; i <= productsCount; i++) {
                String id = PRODUCT_PREFIX + df.format(i); // P0001, P0002, ...
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
     * Crea el archivo de vendedores (TipoDocumento;NumeroDocumento;Nombres;Apellidos)
     * y devuelve la lista en memoria.
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
            // 8-10 dígitos positivos
            n = 10_000_000L + (Math.abs(RNG.nextLong()) % 900_000_000L);
        } while (used.contains(n));
        used.add(n);
        return n;
    }

    private static long generatePositivePrice() {
        // Precio unitario entre 1_000 y 500_000 (enteros)
        return 1_000L + (Math.abs(RNG.nextLong()) % 499_000L);
    }

    private static String generateProductName(int index) {
        // Nombre simple pero legible
        return "Producto " + String.format("%04d", index);
    }

    /**
     * Lee el catálogo desde productos.csv para reusar en createSalesMenFile(...).
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
                System.err.println("Línea inválida en productos.csv: " + trimmed);
            }
        }
        return list;
    }
    

    
    
}
