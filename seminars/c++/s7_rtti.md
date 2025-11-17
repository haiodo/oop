---
marp: true
theme: default
class: lead
paginate: true
backgroundColor: #fff
---

# Runtime Type Identification (RTTI) в C++

**Семинар 7**  
Программирование на C++  
2 курс

---

## Что такое RTTI?

**Runtime Type Identification** - механизм определения типа объекта во время выполнения программы.

### Основные компоненты RTTI:

- `typeid` оператор
- `type_info` класс
- `dynamic_cast` оператор

---

## Оператор typeid

Возвращает объект типа `type_info`, содержащий информацию о типе.

```cpp
#include <typeinfo>
#include <iostream>

int main() {
  int x = 42;
  double y = 3.14;

  std::cout << typeid(x).name() << std::endl;  // i
  std::cout << typeid(y).name() << std::endl;  // d
  std::cout << typeid(int).name() << std::endl; // i
}
```

---

## Класс type_info

Предоставляет информацию о типе:

```cpp
#include <typeinfo>

class Base { public: virtual ~Base() {} };
class Derived : public Base { ~Derived() {}};

int main() {
  Base* ptr = new Derived();

  const std::type_info& ti = typeid(*ptr);
  std::cout << ti.name() << std::endl;

  // Сравнение типов
  if (typeid(*ptr) == typeid(Derived)) {
    std::cout << "Это объект Derived" << std::endl;
  }
}
```

---

## dynamic_cast

Безопасное приведение типов для полиморфных классов:

```cpp
class Base {
public:
  virtual ~Base() {}
};

class Derived : public Base {
public:
  void derivedMethod() {}
};

Base* base = new Derived();

// Безопасное приведение
Derived* derived = dynamic_cast<Derived*>(base);
if (derived != nullptr) {
  derived->derivedMethod();
}
```

---

## dynamic_cast с ссылками

При неудачном приведении выбрасывает исключение `std::bad_cast`:

```cpp
try {
  Base& baseRef = *base;
  Derived& derivedRef = dynamic_cast<Derived&>(baseRef);
  derivedRef.derivedMethod();
}
catch (std::bad_cast& e) {
  std::cout << "Приведение неудачно: " << e.what() << std::endl;
}
```

---

## Требования для RTTI

1. **Полиморфные классы**: класс должен иметь хотя бы одну виртуальную функцию
2. **Компиляция с RTTI**: флаг `-frtti` (включен по умолчанию)

```cpp
// Не работает - класс не полиморфный
class NonPolymorphic {
  int data;
};

// Работает - есть виртуальная функция
class Polymorphic {
  virtual ~Polymorphic() {}
  int data;
};
```

---

## Пример: иерархия фигур

```cpp
class Shape {
public:
  virtual ~Shape() {}
  virtual double area() const = 0;
};

class Circle : public Shape {
  double radius;
public:
  Circle(double r) : radius(r) {}
  double area() const override { return 3.14 * radius * radius; }
  double getRadius() const { return radius; }
};

class Rectangle : public Shape {
  double width, height;
public:
  Rectangle(double w, double h) : width(w), height(h) {}
  double area() const override { return width * height; }
};
```

---

## Использование RTTI с фигурами

```cpp
void processShape(Shape* shape) {
  std::cout << "Тип: " << typeid(*shape).name() << std::endl;
  std::cout << "Площадь: " << shape->area() << std::endl;

  // Специальная обработка для круга
  if (Circle* circle = dynamic_cast<Circle*>(shape)) {
    std::cout << "Радиус: " << circle->getRadius() << std::endl;
  }

  // Проверка типа через typeid
  if (typeid(*shape) == typeid(Rectangle)) {
    std::cout << "Это прямоугольник!" << std::endl;
  }
}
```

---

## Накладные расходы RTTI

**Плюсы:**

- Безопасность приведений типов
- Удобство работы с полиморфными иерархиями

**Минусы:**

- Дополнительная память для хранения type_info
- Накладные расходы на проверки типов
- Может нарушать принцип открытости/закрытости

---

## Альтернативы RTTI

```cpp
// Visitor pattern
class ShapeVisitor {
public:
  virtual void visit(Circle& circle) = 0;
  virtual void visit(Rectangle& rectangle) = 0;
};

class Shape {
public:
  virtual void accept(ShapeVisitor& visitor) = 0;
};

class Circle : public Shape {
public:
  void accept(ShapeVisitor& visitor) override {
    visitor.visit(*this);
  }
};
```

---

## Хорошие практики

1. **Избегайте частого использования** RTTI в критичном по производительности коде
2. **Предпочитайте виртуальные функции** проверкам типов
3. **Используйте RTTI для отладки** и логирования
4. **dynamic_cast предпочтительнее** static_cast для полиморфных типов

```cpp
// Плохо
if (typeid(*shape) == typeid(Circle)) {
  static_cast<Circle*>(shape)->specificMethod();
}

// Хорошо
if (Circle* circle = dynamic_cast<Circle*>(shape)) {
  circle->specificMethod();
}
```

---

## Заключение

RTTI - полезный инструмент для:

- Безопасного приведения типов
- Отладки и логирования
- Работы с неизвестными типами

Используйте осторожно и по назначению!

**Вопросы?**
