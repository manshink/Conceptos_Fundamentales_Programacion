package app;

import java.util.*;
import java.nio.file.*;
import java.io.*;

import app.ReportGenerator.Producto;
import app.ReportGenerator.Vendedor;

public class Main {
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
                        System.out.println("Archivo productos.csv generado.");
                        break;
                    case 2:
                        System.out.print("Cantidad de vendedores: ");
                        int v = sc.nextInt();
                        GenerateInfoFiles.createSalesManInfoFile(v);
                        System.out.println("Archivo vendedores.csv generado.");
                        break;

                    case 3:
                        GenerateInfoFiles.Main(new String[]{});
                        break;
                    case 0:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } while (opcion != 0);

        sc.close();
    }
}
