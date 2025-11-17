---
marp: true
theme: default
paginate: true
---

# Множественное наследование и интерфейсы в C++

**Семинар 4**
Курс: Объектно-ориентированное программирование
2 курс

---

## План семинара

1. Множественное наследование
2. Проблема алмаза (Diamond Problem)
3. Виртуальное наследование
4. Интерфейсы в C++
5. Абстрактные классы
6. Практические примеры

---

## Множественное наследование

C++ поддерживает наследование от нескольких базовых классов:

```cpp
class A {
public:
  void methodA() { std::cout << "Method A" << std::endl; }
};

class B {
public:
  void methodB() { std::cout << "Method B" << std::endl; }
};

class C : public A, public B {
public:
  void methodC() { std::cout << "Method C" << std::endl; }
};
```

---

## Пример использования

```cpp
int main() {
  C obj;
  obj.methodA();  // Из класса A
  obj.methodB();  // Из класса B
  obj.methodC();  // Собственный метод
  return 0;
}
```

**Результат:**

```
Method A
Method B
Method C
```

---

## Проблема алмаза (Diamond Problem)

```cpp
class Animal {
public:
  void eat() { std::cout << "Animal eating" << std::endl; }
};

class Mammal : public Animal {
public:
  void breathe() { std::cout << "Mammal breathing" << std::endl; }
};

class Bird : public Animal {
public:
  void fly() { std::cout << "Bird flying" << std::endl; }
};

class Bat : public Mammal, public Bird {
  // Проблема: два экземпляра Animal!
};
```

---

## Проблема неоднозначности

```cpp
int main() {
  Bat bat;
  // bat.eat();  // Ошибка компиляции! Неоднозначность

  // Нужно явно указать путь:
  bat.Mammal::eat();  // OK
  bat.Bird::eat();    // OK

  return 0;
}
```

---

## Виртуальное наследование

**Решение проблемы алмаза:**

```cpp
class Animal {
public:
  void eat() { std::cout << "Animal eating" << std::endl; }
};

class Mammal : virtual public Animal {
public:
  void breathe() { std::cout << "Mammal breathing" << std::endl; }
};

class Bird : virtual public Animal {
public:
  void fly() { std::cout << "Bird flying" << std::endl; }
};

class Bat : public Mammal, public Bird {
  // Теперь только один экземпляр Animal
};
```

---

## Использование виртуального наследования

```cpp
int main() {
  Bat bat;
  bat.eat();     // OK! Нет неоднозначности
  bat.breathe(); // OK
  bat.fly();     // OK

  return 0;
}
```

**Результат:**

```
Animal eating
Mammal breathing
Bird flying
```

---

## Интерфейсы в C++

C++ не имеет ключевого слова `interface`, но интерфейсы создаются через **абстрактные классы**:

```cpp
class IDrawable {
public:
  virtual void draw() = 0;  // Чисто виртуальная функция
  virtual ~IDrawable() {}   // Виртуальный деструктор
};

class IPrintable {
public:
  virtual void print() = 0;
  virtual ~IPrintable() {}
};
```

---

## Реализация интерфейсов

```cpp
class Shape : public IDrawable, public IPrintable {
protected:
  string name;

public:
  Shape(const string& n) : name(n) {}

  void draw() override {
    std::cout << "Drawing " << name << std::endl;
  }

  void print() override {
    std::cout << "Printing " << name << std::endl;
  }
};
```

---

## Конкретные классы

```cpp
class Circle : public Shape {
private:
  double radius;

public:
  Circle(double r) : Shape("Circle"), radius(r) {}

  void draw() override {
    std::cout << "Drawing circle with radius " << radius << std::endl;
  }
};

class Rectangle : public Shape {
private:
  double width, height;

public:
  Rectangle(double w, double h)
    : Shape("Rectangle"), width(w), height(h) {}

  void draw() override {
    std::cout << "Drawing rectangle " << width << "x" << height << std::endl;
  }
};
```

---

## Полиморфное использование

```cpp
void processDrawable(IDrawable* drawable) {
  drawable->draw();
}

void processPrintable(IPrintable* printable) {
  printable->print();
}

int main() {
  Circle circle(5.0);
  Rectangle rect(10, 20);

  processDrawable(&circle);   // Полиморфизм
  processDrawable(&rect);

  processPrintable(&circle);
  processPrintable(&rect);

  return 0;
}
```

---

## Множественное наследование интерфейсов

```cpp
class IMovable {
public:
  virtual void move(int x, int y) = 0;
  virtual ~IMovable() {}
};

class GameObject : public IDrawable, public IPrintable, public IMovable {
protected:
  int x, y;

public:
  GameObject(int x = 0, int y = 0) : x(x), y(y) {}

  void move(int newX, int newY) override {
    x = newX;
    y = newY;
    std::cout << "Moved to (" << x << ", " << y << ")" << std::endl;
  }
};
```

---

## Практический пример: Система уведомлений

```cpp
class INotifier {
public:
  virtual void notify(const string& message) = 0;
  virtual ~INotifier() {}
};

class EmailNotifier : public INotifier {
public:
  void notify(const string& message) override {
    std::cout << "Email: " << message << std::endl;
  }
};

class SMSNotifier : public INotifier {
public:
  void notify(const string& message) override {
    std::cout << "SMS: " << message << std::endl;
  }
};
```

## Рекомендации

1. **Используйте виртуальное наследование** при проблеме алмаза
2. **Предпочитайте композицию** множественному наследованию
3. **Создавайте небольшие интерфейсы** (принцип ISP)
4. **Всегда добавляйте виртуальный деструктор**
5. **Документируйте** сложную иерархию наследования

---

## Задания для практики

1. Создайте иерархию классов транспортных средств с множественным наследованием
2. Реализуйте систему с интерфейсами для работы с файлами (чтение, запись, сжатие)
3. Решите проблему алмаза в иерархии классов животных
4. Создайте систему обработки событий с использованием интерфейсов

---

## Вопросы?

**Следующий семинар:** Исключения и обработка ошибок в C++
