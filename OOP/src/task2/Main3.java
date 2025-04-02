package task2;

import java.io.*;
import java.util.Scanner;

public class Main3 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Отримання вхідних даних від користувача
            System.out.println("Введіть перше число:");
            int num1 = scanner.nextInt();

            System.out.println("Введіть друге число:");
            int num2 = scanner.nextInt();

            System.out.println("Введіть трете число:");
            int num3 = scanner.nextInt();

            // Створення об'єкта з введеними даними
            NumberData data = new NumberData(num1, num2, num3);

            // Виведення початкових даних
            System.out.println("\nПочаткові дані:");
            data.printInfo();

            // Шлях до файлу для серіалізації
            String filename = "C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt";

            // Викликаємо метод серіалізації для збереження об'єкта у файл
            serializeToFile(data, filename);

            // Викликаємо метод десеріалізації для відновлення об'єкта з файлу
            NumberData loadedData = deserializeFromFile(filename);

            // Виведення завантажених даних після десеріалізації
            System.out.println("\nПісля серіалізації:");
            loadedData.printInfo();
        }
    }

    private static void serializeToFile(NumberData data, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            // Записуємо об'єкт у потік виведення
            out.writeObject(data);
            System.out.println("Дані серіалізовано у " + filename);
        } catch (IOException e) {
            // Обробка помилок серіалізації
            System.err.println("Помилка серіалізації: " + e.getMessage());
        }
    }

    /**
     * Метод для десеріалізації об'єкта з файлу
     * @param filename шлях до файлу
     * @return відновлений об'єкт або null у випадку помилки
     */
    private static NumberData deserializeFromFile(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            // Читаємо об'єкт з потоку введення та приводимо його до потрібного типу
            return (NumberData) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Обробка помилок десеріалізації
            System.err.println("Помилка десеріалізації: " + e.getMessage());
            return null;
        }
    }
}

/**
 * Клас для зберігання та обробки числових даних
 * Реалізує інтерфейс Serializable для можливості серіалізації
 */
class NumberData implements Serializable {
    // Ідентифікатор версії для контролю сумісності при серіалізації
    private static final long serialVersionUID = 1L;

    // Поля для зберігання числових значень
    private int num1;
    private int num2;
    private int num3;

    /**
     * Конструктор класу
     * @param num1 перше число
     * @param num2 друге число
     * @param num3 третє число
     */
    public NumberData(int num1, int num2, int num3) {
        this.num1 = num1;
        this.num2 = num2;
        this.num3 = num3;
    }

    /**
     * Метод для виведення інформації про числа та їх математичні операції
     */
    public void printInfo() {
        System.out.println("Число 1: " + num1);
        System.out.println("Число 2: " + num2);
        System.out.println("Число 3: " + num3);
        System.out.println("Сума: " + (num1 + num2 + num3));
        System.out.println("Добуток: " + (num1 * num2 * num3));
    }
}
