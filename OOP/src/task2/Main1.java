package task2;

import java.io.*;
import java.util.*;

/**
 * Клас Data для зберігання списку чисел та результату їх обчислення.
 * Реалізує інтерфейс Serializable для можливості серіалізації.
 */
class Data implements Serializable {
    private static final long serialVersionUID = 1L; // Ідентифікатор версії для серіалізації
    private List<Double> numbers; // Список чисел
    private double result; // Результат обчислення (сума)

    /**
     * Конструктор класу Data.
     * @param numbers Список чисел для обробки
     */
    public Data(List<Double> numbers) {
        this.numbers = new ArrayList<>(numbers); // Створюємо копію для запобігання змін ззовні
        this.result = calculateSum(numbers); // Обчислюємо суму під час створення об'єкта
    }

    /**
     * Метод для обчислення суми чисел.
     * @param numbers Список чисел
     * @return Сума всіх чисел у списку
     */
    private double calculateSum(List<Double> numbers) {
        return numbers.stream().mapToDouble(Double::doubleValue).sum(); // Використовуємо Stream API для обчислення суми
    }

    /**
     * Повертає незмінний список чисел.
     * @return Незмінна копія списку чисел
     */
    public List<Double> getNumbers() { return Collections.unmodifiableList(numbers); }

    /**
     * Повертає результат обчислення.
     * @return Сума чисел
     */
    public double getResult() { return result; }

    /**
     * Перевизначений метод toString() для зручного виведення об'єкта.
     * @return Рядок з інформацією про числа та їх суму
     */
    @Override
    public String toString() {
        return "Числа- " + numbers + ", Сума- " + result;
    }
}

/**
 * Клас Calculator для роботи з даними.
 * Відповідає за обчислення, збереження та завантаження даних.
 */
class Calculator {
    private Data data; // Об'єкт для зберігання даних

    /**
     * Конструктор класу Calculator.
     * @param numbers Список чисел для обчислення
     */
    public Calculator(List<Double> numbers) {
        this.data = new Data(numbers); // Створюємо новий об'єкт Data з переданими числами
    }

    /**
     * Повертає об'єкт даних.
     * @return Об'єкт Data
     */
    public Data getData() {
        return data;
    }

    /**
     * Метод для збереження даних у текстовий та серіалізований файли.
     * @param filename Ім'я файлу для збереження
     */
    public void saveData(String filename) {
        // Зберігаємо дані у текстовий файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Числа: " + data.getNumbers());
            writer.newLine();
            writer.write("Сума: " + data.getResult());
            System.out.println("Дані успішно збережено до файлу: " + filename);
        } catch (IOException e) {
            System.err.println("Помилка збереження даних: " + e.getMessage());
        }

        // Серіалізуємо об'єкт у бінарний файл
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename + ".ser"))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Помилка серіалізації: " + e.getMessage());
        }
    }

    /**
     * Метод для завантаження даних із серіалізованого файлу.
     * @param filename Ім'я файлу для завантаження
     */
    public void loadData(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename + ".ser"))) {
            this.data = (Data) ois.readObject(); // Десеріалізуємо об'єкт
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Помилка завантаження даних: " + e.getMessage());
        }
    }
}

/**
 * Головний клас програми.
 * Взаємодіє з користувачем, отримує введення та демонструє результати.
 */
public class Main1 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) { // Використовуємо try-with-resources для автоматичного закриття Scanner
            List<Double> numbers = new ArrayList<>(); // Створюємо список для зберігання введених чисел

            System.out.println("Введіть числа для обчислення суми");
            System.out.println("Для завершення введення натисніть 0:");

            // Цикл для введення чисел користувачем
            while (true) {
                System.out.print("Введіть число: ");

                // Перевірка на коректність введення
                while (!scanner.hasNextDouble()) {
                    System.out.println("Некоректне введення");
                    scanner.next(); // Пропускаємо неправильний ввід
                }

                double number = scanner.nextDouble();

                if (number == 0) break; // Вихід з циклу при введенні 0
                numbers.add(number); // Додаємо число до списку
            }

            // Перевірка чи список не порожній
            if (numbers.isEmpty()) {
                System.out.println("Жодного числа не введено.");
                return;
            }

            // Створюємо калькулятор і отримуємо результат
            Calculator calculator = new Calculator(numbers);
            Data result = calculator.getData();

            System.out.println(result); // Виводимо результат

            // Шлях до файлу для збереження даних
            String filename = "C:\\Users\\Еля\\Desktop\\ООП\\OOP.Bohomaz.txt";
            calculator.saveData(filename); // Зберігаємо дані
            calculator.loadData(filename); // Завантажуємо дані

            // Отримуємо і виводимо дані після завантаження для перевірки
            result = calculator.getData();
            System.out.println("Дані після повторного завантаження: " + result);
        } catch (Exception e) {
            System.err.println("Сталася помилка: " + e.getMessage());
        }
    }
}
