package task2;

import java.io.*;
import java.util.Scanner;

public class Main3 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Введіть перше число:");
            int num1 = scanner.nextInt();

            System.out.println("Введіть друге число:");
            int num2 = scanner.nextInt();

            System.out.println("Введіть трете число:");
            int num3 = scanner.nextInt();

            NumberData data = new NumberData(num1, num2, num3);

            // Виведення початкових даних
            System.out.println("\nПочаткові дані:");
            data.printInfo();

            // Серіалізація
            String filename = "C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt";
            serializeToFile(data, filename);

            // Десеріалізація
            NumberData loadedData = deserializeFromFile(filename);

            // Виведення завантажених даних
            System.out.println("\nПісля серіалізації:");
            loadedData.printInfo();
        }
    }

    // Метод серіалізації у файл
    private static void serializeToFile(NumberData data, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(data);
            System.out.println("Дані серіалізовано у " + filename);
        } catch (IOException e) {
            System.err.println("Помилка серіалізації: " + e.getMessage());
        }
    }

    // Метод десеріалізації з файлу
    private static NumberData deserializeFromFile(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (NumberData) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Помилка десеріалізації: " + e.getMessage());
            return null;
        }
    }
}

class NumberData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int num1;
    private int num2;
    private int num3;

    public NumberData(int num1, int num2, int num3) {
        this.num1 = num1;
        this.num2 = num2;
        this.num3 = num3;
    }

    // Метод для виведення інформації про числа
    public void printInfo() {
        System.out.println("Число 1: " + num1);
        System.out.println("Число 2: " + num2);
        System.out.println("Число 3: " + num3);
        System.out.println("Сума: " + (num1 + num2 + num3));
        System.out.println("Добуток: " + (num1 * num2 * num3));
    }
}