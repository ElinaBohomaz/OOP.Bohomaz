package task2;

import java.io.*;
import java.util.Scanner;

// Клас, що реалізує інтерфейс Serializable для можливості серіалізації об'єктів
class TextData implements Serializable {
    private static final long serialVersionUID = 1L; // Ідентифікатор версії для контролю сумісності
    private String text; // Звичайне поле для зберігання тексту
    private int year; // Звичайне поле для зберігання року
    private transient int favoriteNumber; // Поле transient, яке не буде серіалізоване

    // Конструктор класу
    public TextData(String text, int year, int favoriteNumber) {
        this.text = text;
        this.year = year;
        this.favoriteNumber = favoriteNumber;
    }

    // Методи доступу до полів класу
    public String getText() { return text; }
    public int getYear() { return year; }
    public int getFavoriteNumber() { return favoriteNumber; }

    // Спеціальний метод для управління процесом десеріалізації
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Викликаємо стандартне читання об'єкта
        this.favoriteNumber = 0; // Встановлюємо значення для transient поля, оскільки воно не серіалізується
    }
}

// Клас для управління даними та їх збереження/відновлення
class DataManager {
    private TextData data; // Зберігає об'єкт TextData

    // Конструктор класу
    public DataManager(String text, int year, int favoriteNumber) {
        this.data = new TextData(text, year, favoriteNumber);
    }

    // Метод для отримання даних
    public TextData getData() {
        return data;
    }

    // Метод для збереження даних у файл
    public void saveData(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Текст: " + data.getText());
            writer.newLine();
            writer.write("Рік: " + data.getYear());
            writer.newLine();
            writer.write("Улюблена цифра: " + data.getFavoriteNumber());
            System.out.println("Дані збережено " + filename);
        } catch (IOException e) {
            System.err.println("Помилка збереження: " + e.getMessage());
        }
    }

    // Метод для завантаження даних з файлу
    public void loadData(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String textLine = reader.readLine();
            String yearLine = reader.readLine();
            String favoriteLine = reader.readLine();

            this.data = new TextData(
                    textLine.substring(textLine.indexOf(": ") + 2),
                    Integer.parseInt(yearLine.substring(yearLine.indexOf(": ") + 2)),
                    0 // При завантаженні transient-поле встановлюється в 0
            );
        } catch (IOException e) {
            System.err.println("Помилка завантаження: " + e.getMessage());
        }
    }
}

// Головний клас програми
public class Main2 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Отримання даних від користувача
            System.out.println("Введіть текст:");
            String text = scanner.nextLine();

            System.out.println("Введіть рік:");
            int year = scanner.nextInt();

            System.out.println("Введіть улюблену цифру:");
            int favoriteNumber = scanner.nextInt();

            // Створення менеджера даних
            DataManager manager = new DataManager(text, year, favoriteNumber);
            TextData result = manager.getData();

            // Виведення початкових даних
            System.out.println("Введений текст: " + result.getText());
            System.out.println("Рік: " + result.getYear());
            System.out.println("Улюблена цифра: " + result.getFavoriteNumber());

            // Збереження та завантаження даних
            manager.saveData("C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt");
            manager.loadData("C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt");
            result = manager.getData();

            // Виведення даних після завантаження
            System.out.println("\nПісля серіалізації:");
            System.out.println("Введений текст: " + result.getText());
            System.out.println("Рік: " + result.getYear());
            System.out.println("Улюблена цифра: " + result.getFavoriteNumber()); // Буде 0, оскільки поле transient
        }
    }
}