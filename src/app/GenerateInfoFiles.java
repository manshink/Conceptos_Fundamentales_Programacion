package app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Clase para la ENTREGa 1: genera archivos planos pseudoaleatorios
 * que servirán como entrada para el programa principal del proyecto.
 *
 * Archivos generados:
 *  - data/productos.csv
 *  - data/vendedores.csv
 *  - data/ventas/ventas_{TipoDoc}_{NumeroDoc}csv(uno por vendedor)
 *
 * Requisitos de formato:
 *  productos.csv: ID;Nombre;Precio
 *  vendedorescsvTipoDocumento;NumeroDocumento;Nombres;Apellidos
 *  ventas por vendedor:
 *      1a línea: TipoDocumentoVendedor;NumeroDocumentoVendedor
 *      siguientes líneas: IDProducto;Cantidad
 *
 * No solicita datos al usuario. Java 8. Rutas relativas al proyecto.
 */
public class GenerateInfoFiles {

    // === Configuración general ===
    private static final Path DATA_DIR   = Paths.get("data");
    private static final Path SALES_DIR  = DATA_DIR.resolve("ventas");
    private static final Path PRODUCTS_FILE   = DATA_DIR.resolve("productos.csv");
    private static final Path SALESMEN_FILE   = DATA_DIR.resolve("vendedores.csv");

    // Semilla fija para reproducibilidad (cambia si quieres otra corrida)
    private static final Random RNG = new Random(2025_0902);

    // Listas de nombres/apellidos reales (recortadas para el ejemplo)
    private static final String[] NOMBRES = {
        "Camila","Sofía","Valentina","Isabella","Mariana","Sebastián","Santiago","Mateo",
        "Samuel","Daniel","Juan","Andrés","María","Laura","Sara","Nicolás","David","Lucía"
    };
    private static final String[] APELLIDOS = {
        "González","Rodríguez","Gómez","Díaz","Martínez","Pérez","Sánchez","Ramírez",
        "Torres","Vargas","Rojas","Moreno","Romero","Jiménez","Reyes","Castaño","Álvarez"
    };
    private static final String[] TIPOS_DOC = {"CC","CE","TI"};

    // Prefijo para IDs de producto (P0001, P0002, ...)
    private static final String PRODUCT_PREFIX = "P";

    /**
     * Punto de entrada para ENTREGa 1.
     * Genera:
     *  - N productos
     *  - M vendedores
     *  - 1 archivo de ventas por vendedor con K ventas (K aleatorio en rango)
     */
    public static void Main(String[] args) {
        try {
            // 1) Crear carpetas si no existen
            ensureDirectories();

            // 2) Parámetros de ejemplo (ajusta a gusto o déjalos así)
            int productsCount   = 50;   // cantidad de productos
            int salesmanCount   = 25;   // cantidad de vendedores
            int minSalesPerFile = 5;    // ventas mínimas por vendedor
            int maxSalesPerFile = 25;   // ventas máximas por vendedor
            int minQtyPerSale   = 1;    // cantidad mínima por línea de venta
            int maxQtyPerSale   = 10;   // cantidad máxima por línea de venta

            // 3) Generar archivo de productos y mantener catálogo en memoria
            List<Product> catalog = createProductsFile(productsCount);

            // 4) Generar archivo de información de vendedores y mantener lista en memoria
            List<Salesman> salesmen = createSalesManInfoFile(salesmanCount);

            // 5) Generar un archivo de ventas por cada vendedor
            for (Salesman s : salesmen) {
                int randomSalesCount = randomBetween(minSalesPerFile, maxSalesPerFile);
                createSalesMenFile(randomSalesCount, s.getFullName(), s.documentNumber, catalog, s.documentType, minQtyPerSale, maxQtyPerSale);
            }

            System.out.println("Generación finalizada correctamente. Archivos en carpeta ./data");
        } catch (Exception ex) {
            System.err.println("Error durante la generación de archivos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==============================
    // Métodos requeridos por el enunciado
    // ==============================

    /**
     * Crea un archivo de ventas para un vendedor.
     *
     * Formato:
     *   Línea 1: TipoDocumentoVendedor;NumeroDocumentoVendedor
     *   Siguientes líneas: IDProducto;Cantidad
     *
     * NOTA: El enunciado da la firma (count, name, id). Para cumplir formato,
     * asumimos TipoDocumento="CC" si no se provee. Aquí añadimos una sobrecarga
     * interna con tipoDoc explícito para mantener coherencia; la firma original
     * delega a esta.
     *
     * @param randomSalesCount número de líneas de venta a generar
     * @param name nombre completo del vendedor (no se escribe en el archivo, pero lo usamos para nombrar el archivo si se desea)
     * @param id número de documento del vendedor
     * @throws IOException si hay problemas de E/S
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
        // Firma exacta solicitada: por defecto tipoDoc = "CC" y cantidades 1..10
        createSalesMenFile(randomSalesCount, name, id, null, "CC", 1, 10);
    }

    // Sobrecarga que además recibe catálogo, tipoDoc y rangos de cantidad (útil para coherencia)
    private static void createSalesMenFile(int randomSalesCount,
                                           String name,
                                           long id,
                                           List<Product> catalog,
                                           String tipoDoc,
                                           int minQty, int maxQty) throws IOException {
        if (randomSalesCount <= 0) randomSalesCount = 1;
        if (tipoDoc == null || tipoDoc.trim().isEmpty()) tipoDoc = "CC";

        // Si no nos pasaron catálogo (cuando se llama la firma exacta), lo recreamos a partir del archivo de productos
        if (catalog == null) {
            catalog = readCatalogFromFile(PRODUCTS_FILE);
        }

        // Nombre de archivo: ventas_{TipoDoc}_{NumeroDoc}.csv
        String safeName = name == null ? "" : name.replaceAll("[^\\p{L}\\p{N}\\s_-]", "").trim();
        Path file = SALES_DIR.resolve(String.format("ventas_%s_%d.csv", tipoDoc, id));

        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // Primera línea: tipoDoc;numeroDoc
            bw.write(tipoDoc + ";" + id);
            bw.newLine();

            // Conjunto para evitar repetir el mismo producto demasiadas veces si no se desea
            // (no obligatorio; permitir duplicados también es válido)
            for (int i = 0; i < randomSalesCount; i++) {
                Product p = catalog.get(RNG.nextInt(catalog.size()));
                int qty = randomBetween(minQty, maxQty);
                // Línea: IDProducto;Cantidad
                bw.write(p.id + ";" + qty + ";");
                bw.newLine();
            }
        }
    }

    /**
     * Crea el archivo de productos (ID;Nombre;Precio) y devuelve el catálogo en memoria.
     *
     * @param productsCount cantidad de productos a generar
     * @return lista de productos generados (en memoria)
     * @throws IOException si hay problemas de escritura
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
     * y devuelve la lista de vendedores en memoria.
     *
     * @param salesmanCount cantidad de vendedores a generar
     * @return lista de vendedores generados (en memoria)
     * @throws IOException si hay problemas de escritura
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

    // ==============================
    // Utilidades
    // ==============================

    private static void ensureDirectories() throws IOException {
        if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
        if (!Files.exists(SALES_DIR)) Files.createDirectories(SALES_DIR);
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
            n = 1_000_0000L + (Math.abs(RNG.nextLong()) % 9_000_00000L);
        } while (used.contains(n));
        used.add(n);
        return n;
    }

    private static long generatePositivePrice() {
        // Precio unitario entre 1_000 y 500_000
        return 1_000L + (Math.abs(RNG.nextLong()) % 499_000L);
    }

    private static String generateProductName(int i) {
        // Nombres sencillos: "Producto X" o algo más variado
        String[] bases = {"Café", "Azúcar", "Arroz", "Aceite", "Leche", "Pan", "Galletas", "Chocolate",
                          "Jabón", "Shampoo", "Cepillo", "Cuaderno", "Lápiz", "Borrador", "Detergente", "Queso"};
        return bases[i % bases.length] + " " + (100 + i);
    }

    private static List<Product> readCatalogFromFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Product> list = new ArrayList<>(lines.size());
        for (String ln : lines) {
            if (ln.trim().isEmpty()) continue;
            String[] parts = ln.split(";");
            if (parts.length < 3) continue; // tolerante a líneas defectuosas (opcional)
            String id = parts[0].trim();
            String name = parts[1].trim();
            long price = Long.parseLong(parts[2].trim());
            if (price <= 0) continue; // evita precios no válidos
            list.add(new Product(id, name, price));
        }
        if (list.isEmpty()) {
            throw new IOException("El catálogo de productos está vacío o mal formado: " + path.toString());
        }
        return list;
    }

    // ==============================
    // Tipos simples de apoyo (sin main)
    // ==============================

    /** Representa un producto del catálogo. */
    private static class Product {
        final String id;
        final String name;
        final long unitPrice;

        Product(String id, String name, long unitPrice) {
            this.id = id;
            this.name = name;
            this.unitPrice = unitPrice;
        }
    }

    /** Representa un vendedor. */
    private static class Salesman {
        final String documentType;
        final long documentNumber;
        final String firstNames;
        final String lastNames;

        Salesman(String documentType, long documentNumber, String firstNames, String lastNames) {
            this.documentType = documentType;
            this.documentNumber = documentNumber;
            this.firstNames = firstNames;
            this.lastNames = lastNames;
        }

        String getFullName() {
            return (firstNames + " " + lastNames).trim();
        }
    }
}
