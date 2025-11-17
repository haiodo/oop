---
marp: true
theme: default
paginate: true
---

# Виртуальные функции в классах C++

**Семинар 3**
_Объектно-ориентированное программирование_

---

## Содержание

1. Полиморфизм и наследование
2. Проблема статического связывания
3. Виртуальные функции
4. Таблица виртуальных функций (vtable)
5. Абстрактные классы
6. Практические примеры

---

## Полиморфизм

**Полиморфизм** - способность объектов разных типов отвечать на одинаковые сообщения по-разному.

```cpp
class Animal {
public:
  void sound() { std::cout << "Some sound" << std::endl; }
};

class Dog : public Animal {
public:
  void sound() { std::cout << "Woof!" << std::endl; }
};
```

---

## Проблема статического связывания

```cpp
void makeSound(Animal& animal) {
  animal.sound(); // Какой метод вызовется?
}

Dog dog;
makeSound(dog); // Выведет "Some sound", а не "Woof!"
```

**Проблема:** компилятор выбирает метод на этапе компиляции по типу указателя/ссылки.

---

## Виртуальные функции

Ключевое слово `virtual` обеспечивает динамическое связывание:

```cpp
class Animal {
public:
  virtual void sound() { std::cout << "Some sound" << std::endl; }
};

class Dog : public Animal {
public:
  void sound() override { std::cout << "Woof!" << std::endl; }
};
```

---

## Пример работы виртуальных функций

```cpp
void makeSound(Animal& animal) {
  animal.sound(); // Динамическое связывание
}

Animal* animals[] = {
  new Dog(),
  new Cat(),
  new Bird()
};

for (int i = 0; i < 3; i++) {
  animals[i]->sound(); // Вызовется правильный метод
}
```

---

## Таблица виртуальных функций (vtable)

- Каждый класс с виртуальными функциями имеет vtable
- Объект содержит указатель на vtable своего класса
- vtable содержит адреса виртуальных функций

```cpp
// Упрощенное представление
class Animal {
  void* vtable_ptr; // Добавляется компилятором
public:
  virtual void sound();
};
```

---

## Чисто виртуальные функции

```cpp
class Shape {
public:
  virtual double area() = 0; // Чисто виртуальная функция
  virtual void draw() = 0;
};

class Circle : public Shape {
public:
  double area() override { return 3.14 * r * r; }
  void draw() override { /* рисуем круг */ }
private:
  double r;
};
```

---

## Абстрактные классы

- Класс с хотя бы одной чисто виртуальной функцией
- Нельзя создать объект абстрактного класса
- Можно создавать указатели и ссылки

```cpp
Shape shape;        // Ошибка компиляции!
Shape* ptr;         // OK
Circle circle;      // OK, если все функции реализованы
Shape* ptr = &circle; // OK
```

---

## Виртуальный деструктор

**Важно:** всегда делайте деструктор виртуальным в базовых классах!

```cpp
class Base {
public:
  virtual ~Base() { std::cout << "Base destructor" << std::endl; }
};

class Derived : public Base {
public:
  ~Derived() { std::cout << "Derived destructor" << std::endl; }
};

Base* ptr = new Derived();
delete ptr; // Вызовутся оба деструктора
```

---

## Практический пример: Графические фигуры

```cpp
class Figure {
public:
  virtual double area() = 0;
  virtual double perimeter() = 0;
  virtual void print() = 0;
  virtual ~Figure() = default;
};

class Rectangle : public Figure {
private:
  double width, height;
public:
  Rectangle(double w, double h) : width(w), height(h) {}
  double area() override { return width * height; }
  double perimeter() override { return 2 * (width + height); }
  void print() override { std::cout << "Rectangle " << width << "x" << height; }
};
```

---

## Ключевые моменты

1. `virtual` - включает динамическое связывание
2. `override` - проверяет корректность переопределения
3. `= 0` - делает функцию чисто виртуальной
4. Виртуальный деструктор обязателен в базовых классах
5. Небольшие накладные расходы на вызов виртуальных функций

---

## Домашнее задание

Реализуйте иерархию классов для системы управления транспортом:

- Базовый класс `Vehicle` с виртуальными методами
- Производные классы `Car`, `Bicycle`, `Motorcycle`
- Методы: `start()`, `stop()`, `getMaxSpeed()`
- Демонстрация полиморфизма через массив указателей

---

## Вопросы?

**Следующий семинар:** Множественное наследование и интерфейсы
