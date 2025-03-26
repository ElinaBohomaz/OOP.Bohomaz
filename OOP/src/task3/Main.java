package task3;

import java.io.*;
import java.util.*;

interface ResultDisplay {
    void displayBasic(Data data);
}

interface DisplayFactory {
    ResultDisplay createDisplay();
}

abstract class ResultDisplayFactory implements DisplayFactory {
    public abstract ResultDisplay createDisplay();
}

class ConsoleResultDisplay implements ResultDisplay {
    @Override
    public void displayBasic(Data data) {
        System.out.println("Базове відображення🎉:");
        System.out.println("Таблиця чисел:");
        data.printTable();
        System.out.println("Сума: " + data.getResult());
        System.out.println("Кількість чисел: " + data.getNumbers().size());

    }
}

class ConsoleResultDisplayFactory extends ResultDisplayFactory {
    @Override
    public ResultDisplay createDisplay() {
        return new ConsoleResultDisplay();
    }
}

class FileResultDisplay implements ResultDisplay {
    @Override
    public void displayBasic(Data data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"))) {
            writer.write("Результати обчислень:\n");
            writer.write("Таблиця чисел:\n");
            data.printTable(writer);
            writer.write("Сума: " + data.getResult());
            writer.newLine();
            writer.write("Кількість чисел: " + data.getNumbers().size());
        } catch (IOException e) {
            System.err.println("Помилка збереження у файл: " + e.getMessage());
        }
    }
}

class FileResultDisplayFactory extends ResultDisplayFactory {
    @Override
    public ResultDisplay createDisplay() {
        return new FileResultDisplay();
    }
}

class Data implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Integer> numbers;
    private int result;

    public Data(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
        this.result = numbers.stream().mapToInt(Integer::intValue).sum();
    }

    public List<Integer> getNumbers() { return Collections.unmodifiableList(numbers); }
    public int getResult() { return result; }

    public void printTable() {
        for (int i = 0; i < numbers.size(); i++) {
            System.out.printf("%4d ", numbers.get(i));
            if ((i + 1) % 3 == 0) System.out.println();
        }
    }

    public void printTable(BufferedWriter writer) throws IOException {
        for (int i = 0; i < numbers.size(); i++) {
            writer.write(String.format("%4d ", numbers.get(i)));
            if ((i + 1) % 3 == 0) writer.newLine();
        }
    }

    public static Data loadDataFromFile(String filename) {
        List<Integer> numbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Перевіряємо, чи не є рядок рядком з підсумком
                if (!line.startsWith("Сума:")) {
                    String[] nums = line.split(" ");
                    for (String num : nums) {
                        if (!num.trim().isEmpty()) {
                            numbers.add(Integer.parseInt(num.trim()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка завантаження даних з файлу: " + e.getMessage());
        }
        return new Data(numbers);
    }

    public void updateDataFromFile(String filename) {
        List<Integer> newNumbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Перевіряємо, чи не є рядок рядком з підсумком
                if (!line.startsWith("Сума:")) {
                    String[] nums = line.split(" ");
                    for (String num : nums) {
                        if (!num.trim().isEmpty()) {
                            newNumbers.add(Integer.parseInt(num.trim()));
                        }
                    }
                }
            }

            // Замість додавання, повністю замінюємо список
            this.numbers = newNumbers;

            // Перераховуємо нову суму після оновлення
            this.result = this.numbers.stream().mapToInt(Integer::intValue).sum();
        } catch (IOException e) {
            System.err.println("Помилка оновлення даних з файлу: " + e.getMessage());
        }
    }
}

class Calculator {
    private Data data;
    private ResultDisplayFactory displayFactory;

    public Calculator(List<Integer> numbers, ResultDisplayFactory displayFactory) {
        this.data = new Data(numbers);
        this.displayFactory = displayFactory;
    }

    public void displayResults() {
        ResultDisplay display = displayFactory.createDisplay();
        display.displayBasic(data);
    }

    public void saveData(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Integer num : data.getNumbers()) {
                writer.write(num + " ");
            }
            writer.newLine();
            writer.write("Сума: " + data.getResult());
            writer.newLine();
            System.out.println("Дані успішно збережено.");
        } catch (IOException e) {
            System.err.println("Помилка збереження: " + e.getMessage());
        }
    }

    public void loadData(String filename) {
        this.data = Data.loadDataFromFile(filename);
    }

    public void updateData(String filename) {
        this.data.updateDataFromFile(filename);
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        List<Integer> numbers = new ArrayList<>();
        boolean running = true;

        while (running) {
            System.out.println("Меню: 1 - Згенерувати числа, 2 - Показати результати, 3 - Зберегти, 4 - Оновити, 5 - Вийти");
            int action = scanner.nextInt();
            switch (action) {
                case 1 -> {
                    numbers.clear();
                    for (int i = 0; i < 9; i++) {
                        numbers.add(random.nextInt(100));
                    }
                    System.out.print("Згенеровані числа: ");
                    System.out.println(numbers);
                }
                case 2 -> {
                    Calculator calculator = new Calculator(numbers, new ConsoleResultDisplayFactory());
                    calculator.displayResults();
                }
                case 3 -> {
                    // Автоматично зберігаємо дані в файл
                    Calculator calculator = new Calculator(numbers, new ConsoleResultDisplayFactory());
                    calculator.saveData("C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt");
                }
                case 4 -> {
                    // Оновлення даних з файлу
                    Calculator calculator = new Calculator(numbers, new ConsoleResultDisplayFactory());
                    calculator.updateData("C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt");
                    System.out.println("Дані успішно оновлено.");
                }
                case 5 -> running = false;
                default -> System.out.println("Невідома команда");
            }
        }
        scanner.close();
    }
}