package task3;

import java.io.*;
import java.util.*;
/**
 * Інтерфейс для відображення результатів обчислень
 */
interface ResultDisplay {
    void displayBasic(Data data);
}

/**
 * Інтерфейс фабрики для створення об'єктів відображення
 */
interface DisplayFactory {
    ResultDisplay createDisplay();
}

/**
 * Абстрактний клас фабрики для створення об'єктів відображення
 * Реалізує інтерфейс DisplayFactory
 */
abstract class ResultDisplayFactory implements DisplayFactory {
    /**
     * Абстрактний метод для створення об'єкта відображення
     * @return об'єкт, що реалізує інтерфейс ResultDisplay
     */
    public abstract ResultDisplay createDisplay();
}

/**
 * Клас для відображення результатів у консоль
 * Реалізує інтерфейс ResultDisplay
 */
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

/**
 * Фабрика для створення об'єктів відображення у консоль
 * Розширює абстрактний клас ResultDisplayFactory
 */
class ConsoleResultDisplayFactory extends ResultDisplayFactory {
    /**
     * Створює об'єкт для відображення результатів у консоль
     * @return об'єкт ConsoleResultDisplay
     */
    @Override
    public ResultDisplay createDisplay() {
        return new ConsoleResultDisplay();
    }
}

/**
 * Клас для відображення результатів у файл
 * Реалізує інтерфейс ResultDisplay
 */
class FileResultDisplay implements ResultDisplay {
    /**
     * Відображає базову інформацію про дані у файл results.txt
     * @param data дані для запису у файл
     */
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

/**
 * Фабрика для створення об'єктів відображення у файл
 * Розширює абстрактний клас ResultDisplayFactory
 */
class FileResultDisplayFactory extends ResultDisplayFactory {
    @Override
    public ResultDisplay createDisplay() {
        return new FileResultDisplay();
    }
}

/**
 * Клас для зберігання та обробки даних
 * Реалізує інтерфейс Serializable для можливості серіалізації
 */
class Data implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Integer> numbers; // Список чисел
    private int result; // Результат обчислень (сума)
    public Data(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
        // Обчислення суми за допомогою Stream API
        this.result = numbers.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Повертає незмінний список чисел
     * @return незмінний список чисел
     */
    public List<Integer> getNumbers() { return Collections.unmodifiableList(numbers); }

    /**
     * Повертає результат обчислень (суму)
     * @return сума чисел
     */
    public int getResult() { return result; }

    /**
     * Виводить таблицю чисел у консоль
     * Розбиває числа на рядки по 3 числа в кожному
     */
    public void printTable() {
        for (int i = 0; i < numbers.size(); i++) {
            System.out.printf("%4d ", numbers.get(i));
            if ((i + 1) % 3 == 0) System.out.println();
        }
    }

    /**
     * Записує таблицю чисел у файл
     * Розбиває числа на рядки по 3 числа в кожному
     * @param writer об'єкт BufferedWriter для запису у файл
     * @throws IOException якщо виникає помилка вводу/виводу
     */
    public void printTable(BufferedWriter writer) throws IOException {
        for (int i = 0; i < numbers.size(); i++) {
            writer.write(String.format("%4d ", numbers.get(i)));
            if ((i + 1) % 3 == 0) writer.newLine();
        }
    }

    /**
     * Статичний метод для завантаження даних з файлу
     * @param filename ім'я файлу для завантаження
     * @return новий об'єкт Data з завантаженими даними
     */
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

    /**
     * Оновлює дані з файлу, замінюючи поточні дані
     * @param filename ім'я файлу для завантаження
     */
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

            this.numbers = newNumbers;

            // Перераховуємо нову суму після оновлення
            this.result = this.numbers.stream().mapToInt(Integer::intValue).sum();
        } catch (IOException e) {
            System.err.println("Помилка оновлення даних з файлу: " + e.getMessage());
        }
    }
}

/**
 * Клас для обробки даних і відображення результатів
 */
class Calculator {
    private Data data; // Дані для обробки
    private ResultDisplayFactory displayFactory; // Фабрика для створення об'єктів відображення

    /**
     * Конструктор для створення калькулятора
     * @param numbers список чисел для обробки
     * @param displayFactory фабрика для створення об'єктів відображення
     */
    public Calculator(List<Integer> numbers, ResultDisplayFactory displayFactory) {
        this.data = new Data(numbers);
        this.displayFactory = displayFactory;
    }

    /**
     * Відображає результати обчислень
     */
    public void displayResults() {
        ResultDisplay display = displayFactory.createDisplay();
        display.displayBasic(data);
    }

    /**
     * Зберігає дані у файл
     * @param filename ім'я файлу для збереження
     */
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

    /**
     * Завантажує дані з файлу
     * @param filename ім'я файлу для завантаження
     */
    public void loadData(String filename) {
        this.data = Data.loadDataFromFile(filename);
    }

    /**
     * Оновлює дані з файлу
     * @param filename ім'я файлу для оновлення
     */
    public void updateData(String filename) {
        this.data.updateDataFromFile(filename);
    }
}

/**
 * Головний клас програми
 */
public class Main {
    /**
     * Головний метод програми
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        List<Integer> numbers = new ArrayList<>();
        boolean running = true;

        // Головний цикл програми
        while (running) {
            System.out.println("Меню: 1 - Згенерувати числа, 2 - Показати результати, 3 - Зберегти, 4 - Оновити, 5 - Вийти");
            int action = scanner.nextInt();
            switch (action) {
                case 1 -> {
                    // Генерація випадкових чисел
                    numbers.clear();
                    for (int i = 0; i < 9; i++) {
                        numbers.add(random.nextInt(100));
                    }
                    System.out.print("Згенеровані числа: ");
                    System.out.println(numbers);
                }
                case 2 -> {
                    // Відображення результатів у консоль
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
                case 5 -> running = false; // Вихід з програми
                default -> System.out.println("Невідома команда");
            }
        }
        scanner.close();
    }
}