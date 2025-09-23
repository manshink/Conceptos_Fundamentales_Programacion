package app;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReportGenerator {

    // === MODELOS ===
    public static class Producto {
        public String id, nombre;
        public double precio;
        public int cantidadVendida = 0;

        public Producto(String id, String n, double p) {
            this.id = id;
            this.nombre = n;
            this.precio = p;
        }

        public String getId() { return id; }
    }

    public static class Vendedor {
        public String tipoDoc, numDoc, nombres, apellidos;
        public double ventasTotales = 0.0;

        public Vendedor(String td, String nd, String n, String a) {
            this.tipoDoc = td;
            this.numDoc = nd;
            this.nombres = n;
            this.apellidos = a;
        }

        public String getNumDoc() { return numDoc; }
    }

    // === Cargar datos desde CSV ===
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
    public static void procesarArchivoVenta(Path archivo, Map<String, Producto> prods, Map<String, Vendedor> vends) {
        try {
            List<String> lineas = Files.readAllLines(archivo);
            if (lineas.isEmpty()) return;

            String idVendedor = lineas.get(0).split(";")[1].trim();
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
            System.err.println("ADVERTENCIA procesando archivo: " + archivo.getFileName());
            e.printStackTrace();
        }
    }

    // === Generar reportes (CSV en ./data + Consola) ===
    public static void generarReportes(Map<String, Vendedor> mapaVendedores, Map<String, Producto> mapaProductos) throws IOException {
        Path dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        // Reporte de vendedores
        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
                .sorted(Comparator.comparingDouble(v -> -v.ventasTotales))
                .toList();

        Path vendedoresFile = dataDir.resolve("reporte_vendedores.csv");
        try (PrintWriter writer = new PrintWriter(vendedoresFile.toFile())) {
            writer.println("Vendedor;VentasTotales");
            for (Vendedor v : vendedoresOrdenados) {
                writer.printf("%s %s (%s);%.2f%n", v.nombres, v.apellidos, v.numDoc, v.ventasTotales);
            }
        }

        // === Consola ===
        System.out.println("\n📊 === REPORTE DE VENDEDORES ===");
        System.out.printf("%-25s %-15s %12s%n", "Nombre", "Documento", "Ventas Totales");
        System.out.println("---------------------------------------------------------------");
        for (Vendedor v : vendedoresOrdenados) {
            System.out.printf("%-25s %-15s %12.2f%n",
                    v.nombres + " " + v.apellidos, v.numDoc, v.ventasTotales);
        }

        // Reporte de productos
        List<Producto> productosOrdenados = mapaProductos.values().stream()
                .sorted(Comparator.comparingInt(p -> -p.cantidadVendida))
                .toList();

        Path productosFile = dataDir.resolve("reporte_productos.csv");
        try (PrintWriter writer = new PrintWriter(productosFile.toFile())) {
            writer.println("Producto;Precio;CantidadVendida");
            for (Producto p : productosOrdenados) {
                writer.printf("%s;%.2f;%d%n", p.nombre, p.precio, p.cantidadVendida);
            }
        }

        // === Consola ===
        System.out.println("\n📦 === REPORTE DE PRODUCTOS ===");
        System.out.printf("%-20s %10s %15s%n", "Producto", "Precio", "Cantidad Vendida");
        System.out.println("---------------------------------------------------------------");
        for (Producto p : productosOrdenados) {
            System.out.printf("%-20s %10.2f %15d%n",
                    p.nombre, p.precio, p.cantidadVendida);
        }

        System.out.println("\n¡Reportes generados en ./data y mostrados en pantalla!");
    }

    // === Parsers y orquestación ===
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

    public static void runPipeline() throws IOException {
        Path dataDir = Paths.get("data");
        Path salesDir = dataDir.resolve("ventas");

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

        if (Files.exists(salesDir)) {
            try (var stream = Files.list(salesDir)) {
                stream.filter(Files::isRegularFile)
                      .filter(p -> p.toString().endsWith(".csv"))
                      .forEach(p -> procesarArchivoVenta(p, productos, vendedores));
            }
        } else {
            System.err.println("No existe el directorio de ventas: " + salesDir.toString());
        }

        generarReportes(vendedores, productos);
    }
}
