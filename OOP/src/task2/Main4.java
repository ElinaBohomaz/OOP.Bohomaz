package task2;

import java.util.Scanner;

class Main4 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Введіть довжину сторони у двійковій системі числення:");
            String binarySide = scanner.nextLine();

            int side = Integer.parseInt(binarySide, 2);

            GeometryCalculator calculator = new GeometryCalculator(side);
            calculator.calculateAndPrintAreas();
        }
    }
}

class GeometryCalculator {
    private int side;

    public GeometryCalculator(int side) {
        this.side = side;
    }

    public void calculateAndPrintAreas() {
        double triangleArea = calculateTriangleArea();
        double rectangleArea = calculateRectangleArea();
        double totalArea = triangleArea + rectangleArea;

        System.out.println("\nДовжина сторони (десяткове): " + side);
        System.out.println("Площа рівностороннього трикутника: " + String.format("%.2f", triangleArea));
        System.out.println("Площа рівностороннього прямокутника: " + String.format("%.2f", rectangleArea));
        System.out.println("Сума площ: " + String.format("%.2f", totalArea));
    }

    private double calculateTriangleArea() {
        return (Math.sqrt(3) / 4) * side * side;
    }

    private double calculateRectangleArea() {
        return side * side;
    }
}