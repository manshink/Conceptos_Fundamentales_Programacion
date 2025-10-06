package app;

import java.util.*;
import java.io.*;

import app.ReportGenerator.Producto;
import app.ReportGenerator.Vendedor;

/**
 * Clase principal del sistema.
 * <p>
 * Proporciona un menú en consola para:
 * <ul>
 *   <li>Generar archivo de productos.</li>
 *   <li>Generar archivo de vendedores.</li>
 *   <li>Generar reportes a partir de los archivos creados.</li>
 * </ul>
 *
 * Los datos se almacenan en la carpeta <b>./data</b>.
 */
public class Main {

    /**
     * Bandera de depuración.
     * <p>
     * Si está en {@code true}, muestra trazas completas de errores (stacktrace).
     * Si está en {@code false}, muestra mensajes resumidos.
     * </p>
     */
    private static final boolean DEBUG = false;

    /**
     * Método principal del programa.
     * <p>
     * Muestra un menú en consola y permite al usuario elegir acciones
     * relacionadas con la generación de archivos y reportes.
     * </p>
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n=== MENÚ DE GENERACIÓN Y REPORTES ===");
            System.out.println("1. Generar archivo de productos");
            System.out.println("2. Generar archivo de vendedores");
            System.out.println("3. Generación Reporte Ventas (productos, vendedores y ventas)");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            try {
                switch (opcion) {
                    case 1:
                        System.out.print("Cantidad de productos: ");
                        int p = sc.nextInt();
                        GenerateInfoFiles.createProductsFile(p);
                        System.out.println("✅ Archivo productos.csv generado.");
                        break;
                    case 2:
                        System.out.print("Cantidad de vendedores: ");
                        int v = sc.nextInt();
                        GenerateInfoFiles.createSalesManInfoFile(v);
                        System.out.println("✅ Archivo vendedores.csv generado.");
                        break;
                    case 3:
                        ReportGenerator.runPipeline();
                        break;
                    case 0:
                        System.out.println("Finalización exitosa...");
                        break;
                    default:
                        System.out.println("⚠️ Opción inválida.");
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                if (DEBUG) e.printStackTrace();
                System.err.println("Sugerencia: verifique que los archivos requeridos existan en ./data y tengan permisos de acceso.");
            }
        } while (opcion != 0);

        sc.close();
    }
}


