---
marp: true
theme: default
paginate: false
---

# Паттерны проектирования в ООП

## Введение

- Типовые решения часто встречающихся проблем
- Преимущества: проверенная практика, общий словарь, поддерживаемость кода
- Классификация GoF:
  - Порождающие: создание объектов
  - Структурные: компоновка объектов
  - Поведенческие: взаимодействие объектов

## Порождающие паттерны

### Singleton

- Гарантирует единственный экземпляр класса
- Применение: логгеры, соединения с БД

```java
public class Singleton {
    private static Singleton instance;
    private Singleton() {}
    public static synchronized Singleton getInstance() {
        if (instance == null) instance = new Singleton();
        return instance;
    }
}

public class DoubleLockingSingleton {
    private static volatile DoubleLockingSingleton instance;
    private DoubleLockingSingleton() {}

    public static DoubleLockingSingleton getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (DoubleLockingSingleton.class) {
            if (instance == null) {
                instance = new DoubleLockingSingleton();
            }
        }
        return instance;
    }
}

```

### Factory Method

- Делегирует создание объектов подклассам
- Применение: когда тип создаваемого объекта заранее неизвестен

```java
interface Product { void operation(); }

abstract class Creator {
    public abstract Product createProduct();
    public void someOperation() {
        Product product = createProduct();
        product.operation();
    }
}

class ConcreteCreator extends Creator {
    @Override
    public Product createProduct() {
        return () -> System.out.println("ConcreteProduct");
    }
}
```

### Builder

- Пошаговое создание сложных объектов
- Разделяет конструирование и представление

```java
// Использование
Product product = new ProductBuilder()
    .buildPartA("A")
    .buildPartB("B")
    .buildPartC("C")
    .getResult();
```

## Структурные паттерны

### Adapter

- Преобразует интерфейс класса к ожидаемому клиентом
- Интеграция с внешними библиотеками

```java
class Adapter implements Target {
    private Adaptee adaptee;
    public Adapter(Adaptee adaptee) { this.adaptee = adaptee; }
    @Override
    public void request() { adaptee.specificRequest(); }
}
```

### Decorator

- Динамически добавляет функциональность объекту
- Альтернатива наследованию

```java
abstract class Decorator implements Component {
    protected Component component;
    public Decorator(Component component) { this.component = component; }
    @Override
    public void operation() { component.operation(); }
}
```

## Поведенческие паттерны

### Observer

- Определяет зависимость "один-ко-многим"
- Уведомление объектов об изменениях

```java
class Subject {
    private List<Observer> observers = new ArrayList<>();
    public void attach(Observer o) { observers.add(o); }
    public void detach(Observer o) { observers.remove(o); }
    public void notifyObservers(String msg) {
        for (Observer o : observers) o.update(msg);
    }
}
```

### Strategy

- Семейство взаимозаменяемых алгоритмов
- Выбор алгоритма во время выполнения

```java
interface SortStrategy { void sort(int[] array); }
class SortContext {
    private SortStrategy strategy;
    public void setStrategy(SortStrategy strategy) { this.strategy = strategy; }
    public void executeStrategy(int[] array) { strategy.sort(array); }
}
```

### Command

- Инкапсулирует запрос как объект
- Параметризация клиентов, очереди, отмена операций

```java
interface Command { void execute(); }
class Invoker {
    private Command command;
    public void setCommand(Command c) { command = c; }
    public void executeCommand() { command.execute(); }
}
```

## MVC Архитектура

### Компоненты

- **Model**: данные и бизнес-логика
- **View**: представление данных пользователю
- **Controller**: связующее звено, обработка ввода

### Преимущества

- Разделение ответственности
- Повторное использование компонентов
- Параллельная разработка
- Упрощение тестирования

## Советы по использованию паттернов

- Не применять паттерны ради паттернов
- Использовать, когда они действительно решают проблему
- Изучать примеры в реальных проектах
- Понимать компромиссы при использовании паттернов
