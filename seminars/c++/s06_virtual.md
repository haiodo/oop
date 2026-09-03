---
marp: true
theme: default
paginate: true
style: |
  section { font-size: 22px; padding: 40px; line-height: 1.35; }
  section h1 { font-size: 1.8em; margin: 0 0 .5em; }
  section h2 { font-size: 1.45em; margin: 0 0 .6em; }
  section p, section ul, section ol { margin: .45em 0; }
  section li { margin: .15em 0; }
  section pre { font-size: .82em; line-height: 1.3; margin: .5em 0; padding: .6em .8em; }
  section table { font-size: .85em; }
---

# Виртуальные функции и полиморфизм в C++

**Семинар 6**
Курс: Объектно-ориентированное программирование
2 курс

---

## Полиморфизм

**Полиморфизм** - способность объектов разных типов отвечать на одинаковые
сообщения по-разному.

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
void report_sound(Animal& animal) {
  animal.sound();  // Which method is called?
}

Dog dog;
report_sound(dog);  // Prints "Some sound", not "Woof!"
```

**Проблема:** компилятор выбирает метод на этапе компиляции по типу
ссылки/указателя, а не по фактическому типу объекта.

---

## Виртуальные функции

`virtual` включает динамическое связывание: метод выбирается по фактическому
(динамическому) типу объекта, а не по типу ссылки.

```cpp
class Animal {
public:
  virtual void sound() { std::cout << "Some sound" << std::endl; }
  virtual ~Animal() = default;
};

class Dog : public Animal {
public:
  void sound() override { std::cout << "Woof!" << std::endl; }
};

class Cat : public Animal {
public:
  void sound() override { std::cout << "Meow!" << std::endl; }
};
```

Теперь `report_sound(dog)` печатает `"Woof!"` - вызов ушёл в переопределение.

---

## Срезка объектов (object slicing)

```cpp
// ПЛОХО: vector<Animal> stores objects of the exact type Animal.
std::vector<Animal> animals;
animals.push_back(Dog());
animals[0].sound();  // Prints "Some sound", never "Woof!"
```

```cpp
#include <memory>

// Correct: store pointers to the base, never the objects by value.
std::vector<std::unique_ptr<Animal>> animals;
animals.push_back(std::make_unique<Dog>());
animals[0]->sound();  // Prints "Woof!"
```

- `push_back(Dog())` копирует `Dog` в объект **точно** типа `Animal`:
  `Dog`-часть отбрасывается, копия ведёт себя как чистый `Animal`.
- Ссылка `Animal& a = dog;` не срезает `dog` - срезка происходит только там,
  где объект копируется по значению в переменную/поле/элемент типа `Animal`.
- Ровно то, что требует Задача 3: объекты хранятся по `std::unique_ptr`,
  а не по значению.

---

## Полиморфный контейнер и единообразная обработка

```cpp
struct Context {
  double delta_seconds;
};

class GameObject {
public:
  virtual ~GameObject() = default;
  virtual void act(const Context& context) = 0;
};

class Enemy : public GameObject {
public:
  void act(const Context&) override { /* move toward the player */ }
};

class Bullet : public GameObject {
public:
  void act(const Context&) override { /* move forward */ }
};
```

---

## Один цикл на все типы - и что в нём запрещено

```cpp
std::vector<std::unique_ptr<GameObject>> objects;
objects.push_back(std::make_unique<Enemy>());
objects.push_back(std::make_unique<Bullet>());

Context context{.delta_seconds = 0.016};
for (auto& object : objects) {
  object->act(context);  // Same call for every object, no type checks.
}
```

- Разное поведение - это переопределённый `act()` в каждом классе, а не
  ветвление в цикле.
- Запрещено в движке: `dynamic_cast<Enemy*>(...)`, `enum` с `switch` по
  типу объекта, методы вида `is_enemy()`, раздельные `vector<Enemy>` /
  `vector<Bullet>` вместо одного `vector<unique_ptr<GameObject>>`.

---

## Таблица виртуальных функций (vtable) и стоимость вызова

- Класс с виртуальными функциями обычно получает vtable - таблицу адресов
  виртуальных функций; каждый его объект хранит скрытый указатель на неё.
- Устройство vtable **не определено стандартом** - деталь реализации
  конкретного компилятора, общий только принцип.

```cpp
// A simplified illustration, not real generated code.
class Animal {
  void* vtable_ptr_;  // Added by the compiler, one per object.
public:
  virtual void sound();
  virtual ~Animal() = default;
};
```

- Каждый объект с виртуальными функциями становится на один указатель
  больше (`sizeof(void*)`).
- Вызов - косвенный (через vtable), инлайнинг обычно невозможен: тип
  объекта неизвестен на этапе компиляции.

---

## Не делайте объектом то, чего много

- Сотни однородных фоновых сущностей - клетки поля, звёзды, трава - не
  должны оборачиваться в `GameObject` с виртуальными функциями.
- Иерархия и виртуальные вызовы нужны там, где поведение **разное**
  (игрок, враг, пуля). Для однородного массива данных это только лишняя
  память (указатель на vtable в каждом элементе) и лишние косвенные вызовы.
- Практический совет из Задачи 3: держите такой фон отдельной структурой
  данных (например, `std::vector<CellState>`), а `GameObject` делайте
  только из того, у чего есть собственное поведение.

---

## Чисто виртуальные функции

```cpp
#include <numbers>

class Shape {
public:
  virtual double area() const = 0;  // Pure virtual function.
  virtual void draw() const = 0;
  virtual ~Shape() = default;
};

class Circle : public Shape {
public:
  explicit Circle(double radius) : radius_(radius) {}

  double area() const override { return std::numbers::pi * radius_ * radius_; }
  void draw() const override { /* draw a circle */ }

private:
  double radius_;
};
```

`Shape` уже содержит `virtual ~Shape() = default;` - почему это обязательно,
разберём через два слайда.

---

## Абстрактные классы

- Класс с хотя бы одной чисто виртуальной функцией - **абстрактный**.
- Нельзя создать объект абстрактного класса, но можно указатели и ссылки.

```cpp
// Shape shape;         // Compile error: Shape is abstract.
Shape* ptr = nullptr;   // OK: pointer to an abstract type.

Circle circle(5.0);     // OK: Circle overrides every pure virtual function.
ptr = &circle;          // OK: Shape* may point to any derived object.
```

---

## Виртуальный деструктор: undefined behavior

```cpp
class Base {
public:
  virtual void act() {}
  ~Base() {}  // ПЛОХО: Base has a virtual function, ~Base() is not virtual.
};
```

```cpp
class Base {
public:
  virtual void act() {}
  virtual ~Base() = default;  // Correct: matches the virtual act().
};
```

- `delete` объекта `Derived` через `Base*` с невиртуальным `~Base()` -
  **неопределённое поведение целиком**, а не "не вызовется `~Derived()`".
- Правило: есть хотя бы одна `virtual`-функция в классе - деструктор тоже
  обязан быть `virtual`.
- Компилятор часто предупреждает об этом уже на сборке (`-Wnon-virtual-dtor`),
  но это предупреждение, а не гарантия - без `virtual` ошибка остаётся UB.

---

## Виртуальный деструктор: unique_ptr и когда он не нужен

```cpp
class Derived : public Base {
public:
  ~Derived() override { /* release a resource */ }
};

std::unique_ptr<Base> ptr = std::make_unique<Derived>();
// ptr's destructor calls ~Base() first; it must be virtual
// to reach ~Derived() before that.
```

- `std::unique_ptr<Base>` при удалении вызывает деструктор `Base` - без
  `virtual` это тот же UB, просто без явного `delete` в вашем коде.
- Класс никогда не удаляется через указатель на себя (нет виртуальных
  функций вовсе) - деструктор можно сделать `protected` и невиртуальным:
  тогда `delete base_ptr;` не скомпилируется, ошибка видна сразу.

---

## `override`: ловит ошибку переопределения

```cpp
class Base {
public:
  virtual void update(int) const {}
  virtual ~Base() = default;
};

class Derived : public Base {
public:
  void update(int) {}  // ПЛОХО: missing const -> new method, not override.
};

Derived derived;
Base& ref = derived;
ref.update(1);  // Calls Base::update: no override happened, no warning.
```

- `override` требует, чтобы метод переопределял виртуальный метод базового
  класса с **точно такой же** сигнатурой (имя, параметры, `const`).
- Без `override` компилятор молча создаёт новый, не связанный с базовым,
  метод. С `override` та же ошибка (пропущенный `const`) - ошибка сборки.

---

## `final`: запрет дальнейшего переопределения

```cpp
class Base {
public:
  virtual void act() {}
  virtual ~Base() = default;
};

class Locked : public Base {
public:
  void act() final {}  // No further override of act() is allowed.
};

// class Broken : public Locked {
//   void act() override {}  // Compile error: act() is final in Locked.
// };
```

`final` на классе (`class Locked final : public Base`) запрещает
наследоваться от него вообще.

---

## Виртуальный вызов в конструкторе и деструкторе

```cpp
class Base {
public:
  Base() { log(); }  // Calls Base::log(): the object is still just a Base.
  virtual void log() const { std::cout << "Base" << std::endl; }
  virtual ~Base() = default;
};

class Derived : public Base {
public:
  void log() const override { std::cout << "Derived" << std::endl; }
};

Derived derived;  // Prints "Base", not "Derived".
```

- Пока выполняется конструктор `Base`, объект ещё не стал `Derived` - его
  динамический тип на этот момент `Base`.
- Для чисто виртуальной функции без определения вызов из конструктора или
  деструктора базового класса - undefined behavior, а не "вызовется базовая".

---

## Аргументы по умолчанию у виртуальных функций

```cpp
class Base {
public:
  virtual void greet(std::string name = "Base") const {
    std::cout << "Hello, " << name << std::endl;
  }
  virtual ~Base() = default;
};

class Derived : public Base {
public:
  void greet(std::string name = "Derived") const override {
    std::cout << "Hi, " << name << std::endl;
  }
};

Derived derived;
Base& ref = derived;
ref.greet();  // Prints "Hi, Base": nobody expects this combination.
```

Значение по умолчанию подставляется по **статическому** типу (`Base&`), тело
вызывается по **динамическому** (`Derived`). Правило: не давать виртуальным
функциям аргументы по умолчанию.

---

## Абстрактный класс - это почти интерфейс

- Класс с только чисто виртуальными функциями и виртуальным деструктором
  работает как интерфейс: задаёт контракт, но не реализацию и не состояние.
- В C++ нет ключевого слова `interface` - вместо него используется
  абстрактный класс.
- Следующий семинар: множественное наследование позволяет классу
  реализовать сразу несколько таких контрактов, и разбирает связанные с
  этим тонкости - алмаз наследования, RTTI, приведение типов.

---

## Практический пример: графические фигуры

```cpp
class Figure {
public:
  virtual double area() const = 0;
  virtual double perimeter() const = 0;
  virtual void print() const = 0;
  virtual ~Figure() = default;
};

class Rectangle : public Figure {
public:
  Rectangle(double width, double height) : width_(width), height_(height) {}

  double area() const override { return width_ * height_; }
  double perimeter() const override { return 2 * (width_ + height_); }
  void print() const override {
    std::cout << "Rectangle " << width_ << "x" << height_ << std::endl;
  }

private:
  double width_;
  double height_;
};
```

---

## Типичные ошибки

- `std::vector<GameObject>` вместо `std::vector<std::unique_ptr<GameObject>>` -
  срезка объектов, полиморфизм молча перестаёт работать.
- Деструктор базового класса не `virtual` - удаление наследника через
  указатель на базу - undefined behavior, а не "не вызвался деструктор".
- Переопределение без `override` - опечатка в сигнатуре (забытый `const`,
  другой тип параметра) создаёт новый метод вместо переопределения, и это
  не ловится компилятором.
- Виртуальные функции с аргументами по умолчанию - подстановка по
  статическому типу даёт неожиданную комбинацию поведения и аргумента.
- `dynamic_cast`, `switch` по `enum`-типу объекта или методы `is_enemy()`
  в игровом движке - прямое нарушение пункта зачёта Задачи 3.
- Вызов виртуальной функции из конструктора базового класса в расчёте на
  переопределённую версию - вызывается базовая, для чисто виртуальной - UB.

---

## Вопросы для самопроверки

- Что сломается, если убрать `virtual` у деструктора вашего корневого
  `GameObject`, и почему это UB, а не "просто не вызовется деструктор"?
- Почему объекты в вашем движке хранятся как
  `std::vector<std::unique_ptr<GameObject>>`, а не по значению?
- Покажите место в движке, где вызывается `act()` - почему там нет ни
  одной проверки конкретного типа объекта?
- Что делает `override` в вашем коде и какую ошибку он поймал бы, если бы
  вы забыли `const` в сигнатуре переопределения?
- Почему в вашей иерархии нет виртуальных функций с аргументами по
  умолчанию?

---

## Практика на паре

1. Сделайте иерархию: базовый класс с виртуальным методом и виртуальным
   деструктором, два наследника. Сложите их в
   `std::vector<std::unique_ptr<Base>>`, вызовите общий метод в цикле.
2. Уберите `virtual` у деструктора базового класса и запустите тот же код
   под ASan (`cmake --preset asan`). Прочитайте отчёт санитайзера.
3. Сломайте сигнатуру одного переопределения (уберите `const` или
   поменяйте тип параметра) и убедитесь, что `override` ловит это на
   этапе компиляции, а без него компилятор молчит.
