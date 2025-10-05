package app;

import java.util.*;
import java.io.*;

import app.ReportGenerator.Producto;
import app.ReportGenerator.Vendedor;

public class Main {
    private static final boolean DEBUG = false; // Activa trazas detalladas solo en desarrollo

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

