package app;

import java.util.*;
import java.io.*;

/**
 * Clase principal del sistema de generación de datos y reportes.
 * <p>
 * Muestra un menú en consola con las siguientes opciones:
 * <ul>
 *   <li><b>1</b> → Generar archivo de productos (productos.csv).</li>
 *   <li><b>2</b> → Generar archivo de vendedores (vendedores.csv).</li>
 *   <li><b>3</b> → Generar archivos de simulación de ventas (productos, vendedores y ventas).</li>
 *   <li><b>4</b> → Ejecutar el pipeline de reportes (procesa los archivos y genera reportes).</li>
 *   <li><b>0</b> → Salir del sistema.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Los datos generados se almacenan en la carpeta <b>./data</b>.
 * Los reportes generados se crean en la misma carpeta y se muestran también en consola.
 * </p>
 */
public class Main {

    /**
     * Bandera de depuración.
     * <p>
     * Si {@code true}, muestra trazas completas de errores (stacktrace).
     * Si {@code false}, solo muestra mensajes resumidos y entendibles.
     * </p>
     */
    private static final boolean DEBUG = false;

    /**
     * Método principal del programa.
     * <p>
     * Muestra un menú en consola que permite generar datos de ejemplo
     * y producir reportes de ventas.
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
            System.out.println("3. Generar archivos de simulación de ventas");
            System.out.println("4. Generación Reporte Ventas (productos, vendedores y ventas)");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = sc.nextInt();
            sc.nextLine();

            try {
                switch (opcion) {
                    /**
                     * Opción 1: Genera el archivo de productos.
                     */
                    case 1:
                        System.out.print("Cantidad de productos: ");
                        int p = sc.nextInt();
                        GenerateInfoFiles.createProductsFile(p);
                        System.out.println("✅ Archivo productos.csv generado.");
                        break;

                    /**
                     * Opción 2: Genera el archivo de vendedores.
                     */
                    case 2:
                        System.out.print("Cantidad de vendedores: ");
                        int v = sc.nextInt();
                        GenerateInfoFiles.createSalesManInfoFile(v);
                        System.out.println("✅ Archivo vendedores.csv generado.");
                        break;

                    /**
                     * Opción 3: Genera archivos de simulación completos (productos, vendedores y ventas).
                     */
                    case 3:
                        System.out.println("Generando archivos de simulación de ventas...");
                        GenerateInfoFiles.Main(new String[]{}); // usa parámetros por defecto de GenerateInfoFiles
                        break;

                    /**
                     * Opción 4: Ejecuta el pipeline de reportes.
                     */
                    case 4:
                        ReportGenerator.runPipeline();
                        break;

                    /**
                     * Opción 0: Termina la aplicación.
                     */
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
