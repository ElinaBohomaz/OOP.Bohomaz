package task2;

import java.util.Scanner;
class Main4 {
    public static void main(String[] args) {
        // Використання try-with-resources для автоматичного закриття Scanner
        try (Scanner scanner = new Scanner(System.in)) {
            // Запит користувача ввести довжину сторони у двійковій системі
            System.out.println("Введіть довжину сторони у двійковій системі числення:");
            String binarySide = scanner.nextLine();
            // Перетворення двійкового рядка в десяткове число
            int side = Integer.parseInt(binarySide, 2);
            // Створення об'єкта калькулятора з отриманою довжиною сторони
            GeometryCalculator calculator = new GeometryCalculator(side);
            // Виклик методу для обчислення та виведення площ
            calculator.calculateAndPrintAreas();
        }
    }
}
/**
 * Клас для обчислення площ геометричних фігур
 */
class GeometryCalculator {
    // Поле для зберігання довжини сторони
    private int side;
    public GeometryCalculator(int side) {
        this.side = side;
    }
    /**
     * Метод для обчислення та виведення площ фігур
     */
    public void calculateAndPrintAreas() {
        // Обчислення площі трикутника
        double triangleArea = calculateTriangleArea();
        // Обчислення площі прямокутника
        double rectangleArea = calculateRectangleArea();
        // Обчислення суми площ
        double totalArea = triangleArea + rectangleArea;
        // Виведення результатів обчислень
        System.out.println("\nДовжина сторони (десяткове): " + side);
        System.out.println("Площа рівностороннього трикутника: " + String.format("%.2f", triangleArea));
        System.out.println("Площа рівностороннього прямокутника: " + String.format("%.2f", rectangleArea));
        System.out.println("Сума площ: " + String.format("%.2f", totalArea));
    }
    private double calculateTriangleArea() {
        // Формула для обчислення площі рівностороннього трикутника
        return (Math.sqrt(3) / 4) * side * side;
    }
    private double calculateRectangleArea() {
        // Формула для обчислення площі квадрата
        return side * side;
    }
}