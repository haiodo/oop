---
marp: true
theme: default
paginate: true
---

# Классы в C++

Объектно-ориентированное программирование

---

## Что такое класс?

- **Класс** - это пользовательский тип данных
- Объединяет данные (поля) и функции (методы)
- Основа объектно-ориентированного программирования
- Позволяет создавать объекты

```cpp
class Rectangle {
private:
  double width, height;
public:
  void setDimensions(double w, double h);
  double getArea();
};
```

---

## Объявление класса

```cpp
class ClassName {
private:
  // Приватные члены класса
  int privateVar;

public:
  // Публичные члены класса
  int publicVar;
  void publicMethod();

protected:
  // Защищенные члены класса
  int protectedVar;
};
```

---

## Спецификаторы доступа

- **private** - доступ только внутри класса
- **public** - доступ из любого места программы
- **protected** - доступ внутри класса и наследников

```cpp
class Example {
private:
  int secret;      // Только внутри класса
public:
  int open;        // Везде
protected:
  int family;      // Класс + наследники
};
```

---

## Конструкторы

```cpp
class Point {
private:
  int x, y;

public:
  // Конструктор по умолчанию
  Point() : x(0), y(0) {}

  // Конструктор с параметрами
  Point(int x, int y) : x(x), y(y) {}

  // Копирующий конструктор
  Point(const Point& other) : x(other.x), y(other.y) {}
};
```

---

## Деструктор

```cpp
class MyClass {
private:
  int* data;

public:
  MyClass(int size) {
    data = new int[size];
  }

  // Деструктор
  ~MyClass() {
    delete[] data;
  }
};
```

---

## Методы класса

```cpp
class Calculator {
private:
  double result;

public:
  void add(double value) {
    result += value;
  }

  double getResult() const {
    return result;
  }

  void reset() {
    result = 0.0;
  }
};
```

---

## Создание и использование объектов

```cpp
int main() {
  // Создание объектов
  Point p1;              // Конструктор по умолчанию
  Point p2(10, 20);      // Конструктор с параметрами
  Point p3 = p2;         // Копирующий конструктор

  Calculator calc;
  calc.add(5.5);
  calc.add(2.3);

  std::cout << calc.getResult() << std::endl;  // 7.8

  return 0;
}
```

---

## Статические члены класса

```cpp
class Counter {
private:
  static int count;      // Статическое поле
  int id;

public:
  Counter() {
    id = ++count;
  }

  static int getCount() {    // Статический метод
    return count;
  }
};

int Counter::count = 0;        // Определение статического поля
```

---

## Перегрузка операторов

```cpp
class Complex {
private:
  double real, imag;

public:
  Complex(double r = 0, double i = 0) : real(r), imag(i) {}

  Complex operator+(const Complex& other) const {
    return Complex(real + other.real, imag + other.imag);
  }

  bool operator==(const Complex& other) const {
    return real == other.real && imag == other.imag;
  }
};
```

---

## Дружественные функции

```cpp
class Box {
private:
  double width, height;

public:
  Box(double w, double h) : width(w), height(h) {}

  // Дружественная функция
  friend double getArea(const Box& box);
};

double getArea(const Box& box) {
  return box.width * box.height;  // Доступ к приватным полям
}
```

---

## Пример: Класс "Студент"

```cpp
class Student {
private:
  string name;
  int age;
  vector<int> grades;

public:
  Student(const string& n, int a) : name(n), age(a) {}

  void addGrade(int grade) {
    grades.push_back(grade);
  }

  double getAverageGrade() const {
    if (grades.empty()) return 0.0;

    int sum = 0;
    for (int grade : grades) {
      sum += grade;
    }
    return static_cast<double>(sum) / grades.size();
  }

  void printInfo() const {
    cout << "Студент: " << name << ", возраст: " << age
       << ", средний балл: " << getAverageGrade() << endl;
  }
};
```

---

## Заключение

- Классы - основа ООП в C++
- Инкапсуляция данных и методов
- Конструкторы и деструкторы для управления ресурсами
- Спецификаторы доступа для контроля видимости
- Возможность перегрузки операторов
- Статические члены для общих данных/функций

---

## Практические задания

1. Создать класс `BankAccount` с методами пополнения и снятия средств
2. Реализовать класс `Matrix` с перегрузкой операторов сложения и умножения
3. Написать класс `Car` с полями марка, модель, год выпуска
4. Создать класс `Library` для управления книгами
