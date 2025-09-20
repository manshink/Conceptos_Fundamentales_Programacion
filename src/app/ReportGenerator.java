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
                .collect(Collectors.toMap(getKey, item -> item));
    }

    // === Procesar archivo de ventas ===
    public static void procesarArchivoVenta(Path archivo, Map<String, Producto> prods, Map<String, Vendedor> vends) {
        try {
            List<String> lineas = Files.readAllLines(archivo);
            if (lineas.isEmpty()) return;

            String idVendedor = lineas.get(0).split(";")[1];
            Vendedor vendedor = vends.get(idVendedor);
            if (vendedor == null) return;

            for (int i = 1; i < lineas.size(); i++) {
                String[] datos = lineas.get(i).split(";");
                if (datos.length < 2) continue;

                Producto producto = prods.get(datos[0]);
                int cantidad = Integer.parseInt(datos[1]);

                if (producto != null) {
                    vendedor.ventasTotales += producto.precio * cantidad;
                    producto.cantidadVendida += cantidad;
                }
            }
        } catch (Exception e) {
            System.err.println("ADVERTENCIA procesando archivo: " + archivo.getFileName());
            e.printStackTrace();
        }
    }

    // === Generar reportes ===
    public static void generarReportes(Map<String, Vendedor> mapaVendedores, Map<String, Producto> mapaProductos) throws IOException {
        // Reporte de vendedores
        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
                .sorted(Comparator.comparingDouble(v -> -v.ventasTotales))
                .toList();

        try (PrintWriter writer = new PrintWriter("reporte_vendedores.csv")) {
            writer.println("Vendedor;VentasTotales");
            for (Vendedor v : vendedoresOrdenados) {
                writer.printf("%s %s (%s);%.2f%n", v.nombres, v.apellidos, v.numDoc, v.ventasTotales);
            }
        }

        // Reporte de productos
        List<Producto> productosOrdenados = mapaProductos.values().stream()
                .sorted(Comparator.comparingInt(p -> -p.cantidadVendida))
                .toList();

        try (PrintWriter writer = new PrintWriter("reporte_productos.csv")) {
            writer.println("Producto;Precio;CantidadVendida");
            for (Producto p : productosOrdenados) {
                writer.printf("%s;%.2f;%d%n", p.nombre, p.precio, p.cantidadVendida);
            }
        }

        System.out.println("¡Reportes generados en la carpeta raíz!");
    }
}