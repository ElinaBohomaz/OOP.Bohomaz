package task2;

import java.io.*;
import java.util.Scanner;

class TextData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private int year;
    private transient int favoriteNumber;

    public TextData(String text, int year, int favoriteNumber) {
        this.text = text;
        this.year = year;
        this.favoriteNumber = favoriteNumber;
    }

    public String getText() { return text; }
    public int getYear() { return year; }
    public int getFavoriteNumber() { return favoriteNumber; }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.favoriteNumber = 0;
    }
}

class DataManager {
    private TextData data;

    public DataManager(String text, int year, int favoriteNumber) {
        this.data = new TextData(text, year, favoriteNumber);
    }

    public TextData getData() {
        return data;
    }

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

    public void loadData(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String textLine = reader.readLine();
            String yearLine = reader.readLine();
            String favoriteLine = reader.readLine();

            this.data = new TextData(
                    textLine.substring(textLine.indexOf(": ") + 2),
                    Integer.parseInt(yearLine.substring(yearLine.indexOf(": ") + 2)),
                    0
            );
        } catch (IOException e) {
            System.err.println("Помилка завантаження: " + e.getMessage());
        }
    }
}

public class Main2 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Введіть текст:");
            String text = scanner.nextLine();

            System.out.println("Введіть рік:");
            int year = scanner.nextInt();

            System.out.println("Введіть улюблену цифру:");
            int favoriteNumber = scanner.nextInt();

            DataManager manager = new DataManager(text, year, favoriteNumber);
            TextData result = manager.getData();

            System.out.println("Введений текст: " + result.getText());
            System.out.println("Рік: " + result.getYear());
            System.out.println("Улюблена цифра: " + result.getFavoriteNumber());

            manager.saveData("C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt");
            manager.loadData("C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt");
            result = manager.getData();

            System.out.println("\nПісля серіалізації:");
            System.out.println("Введений текст: " + result.getText());
            System.out.println("Рік: " + result.getYear());
            System.out.println("Улюблена цифра: " + result.getFavoriteNumber());
        }
    }
}