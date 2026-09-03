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

# Наследование, интерфейсы и RTTI

**Семинар 7**
Курс: Объектно-ориентированное программирование
2 курс

---

## Множественное наследование

C++ позволяет классу иметь несколько прямых базовых классов:

```cpp
class Swimmer {
public:
  void swim() { std::cout << "Swimming" << std::endl; }
};

class Flyer {
public:
  void fly() { std::cout << "Flying" << std::endl; }
};

class Duck : public Swimmer, public Flyer {
public:
  void quack() { std::cout << "Quack!" << std::endl; }
};

int main() {
  Duck duck;
  duck.swim();   // from Swimmer
  duck.fly();    // from Flyer
  duck.quack();  // own method
  return 0;
}
```

---

## Неоднозначность имён

Если два базовых класса объявляют метод с одинаковым именем, вызов без
уточнения не компилируется:

```cpp
class Logger {
public:
  void report() { std::cout << "Logger::report" << std::endl; }
};

class Sensor {
public:
  void report() { std::cout << "Sensor::report" << std::endl; }
};

class Device : public Logger, public Sensor {};

int main() {
  Device device;
  // device.report();        // Error: ambiguous call
  device.Logger::report();   // OK: qualified call
  device.Sensor::report();   // OK
  return 0;
}
```

---

## Проблема ромба (Diamond Problem)

Особый случай: оба базовых класса сами унаследованы от одного класса.

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
  // Bat contains two separate Animal subobjects.
};
```

`bat.eat()` не компилируется - неоднозначность та же, что и на предыдущем
слайде, но замаскированная: в объекте физически лежат **две копии** `Animal`.
Нужно `bat.Mammal::eat()` или `bat.Bird::eat()`, и это две разные функции eat.

---

## Виртуальное наследование

**Решение:** унаследовать `Animal` как `virtual` - тогда на весь `Bat`
будет одна общая копия базы.

```cpp
class Animal {
public:
  void eat() { std::cout << "Animal eating" << std::endl; }
};

class Mammal : public virtual Animal {
public:
  void breathe() { std::cout << "Mammal breathing" << std::endl; }
};

class Bird : public virtual Animal {
public:
  void fly() { std::cout << "Bird flying" << std::endl; }
};

class Bat : public Mammal, public Bird {};

int main() {
  Bat bat;
  bat.eat();      // OK now: only one Animal subobject
  bat.breathe();
  bat.fly();
  return 0;
}
```

---

## Порядок конструирования: базовые классы

Виртуальную базу всегда инициализирует **самый производный класс**,
а не промежуточные - их попытки инициализировать её игнорируются.

```cpp
class Animal {
public:
  explicit Animal(std::string name) : name_(std::move(name)) {
    std::cout << "Animal(" << name_ << ")" << std::endl;
  }

protected:
  std::string name_;
};

class Mammal : public virtual Animal {
public:
  Mammal() : Animal("mammal-default") {
    std::cout << "Mammal()" << std::endl;
  }
};

class Bird : public virtual Animal {
public:
  Bird() : Animal("bird-default") {
    std::cout << "Bird()" << std::endl;
  }
};
```

---

## Порядок конструирования: самый производный класс

```cpp
class Bat : public Mammal, public Bird {
public:
  Bat() : Animal("Bat"), Mammal(), Bird() {
    std::cout << "Bat()" << std::endl;
  }
};

int main() {
  Bat bat;
  return 0;
}
```

Вывод:

```
Animal(Bat)
Mammal()
Bird()
Bat()
```

Инициализаторы `Animal("mammal-default")` и `Animal("bird-default")`
из `Mammal()`/`Bird()` ни разу не сработали - `Bat` как самый производный
класс уже вызвал `Animal("Bat")` напрямую, и только этот вызов учитывается.

---

## Виртуальное наследование не бесплатно

- Смещение виртуальной базы внутри объекта не фиксировано - оно зависит
  от конечного (самого производного) типа. Доступ к её полям идёт не по
  постоянному смещению, а косвенно, через служебный указатель/таблицу.
- Каждый уровень `virtual`-наследования - это лишнее косвенное обращение
  при доступе к полю базы и увеличение размера объекта на служебный указатель.
- Поэтому виртуальное наследование стоит применять только там, где
  diamond problem реально возникает, а не заводить его "на всякий случай".

---

## Интерфейсы через абстрактные классы

В C++ нет ключевого слова `interface` - интерфейс это абстрактный класс
с чисто виртуальными методами:

```cpp
class Drawable {
public:
  virtual void draw() const = 0;
  virtual ~Drawable() = default;
};

class Printable {
public:
  virtual void print() const = 0;
  virtual ~Printable() = default;
};
```

Виртуальный деструктор и `override` разбирались в семинаре 6 - здесь важно,
что деструктор интерфейса `virtual ... = default`, а не пустое тело `{}`.

Префикс `I` (`IDrawable`) - соглашение C#/COM. В C++ и его стандартной
библиотеке отдельной языковой сущности "интерфейс" нет, это обычный
абстрактный класс, поэтому префикс не нужен: `std::exception`, а не
`IException`.

---

## Реализация интерфейса

Один класс может реализовать сразу несколько интерфейсов:

```cpp
class Shape : public Drawable, public Printable {
public:
  explicit Shape(std::string name) : name_(std::move(name)) {}

  void draw() const override {
    std::cout << "Drawing " << name_ << std::endl;
  }

  void print() const override {
    std::cout << "Printing " << name_ << std::endl;
  }

protected:
  std::string name_;
};
```

---

## Конкретный класс

```cpp
class Circle : public Shape {
public:
  explicit Circle(double radius) : Shape("Circle"), radius_(radius) {}

  void draw() const override {
    std::cout << "Circle, radius " << radius_ << std::endl;
  }

private:
  double radius_;
};
```

`Rectangle`, `Triangle` и так далее устроены так же - переопределяют
`draw()`/`print()`, не трогая остальную иерархию.

---

## Полиморфное использование

```cpp
void process_drawable(const Drawable& drawable) {
  drawable.draw();
}

void process_printable(const Printable& printable) {
  printable.print();
}

int main() {
  Circle circle(5.0);
  process_drawable(circle);
  process_printable(circle);
  return 0;
}
```

---

## Множественное наследование интерфейсов и ISP

Интерфейсы, в отличие от классов с данными, почти всегда безопасно
наследовать вместе - у них нет полей, значит нет ромба.

```cpp
class Movable {
public:
  virtual void move(int new_x, int new_y) = 0;
  virtual ~Movable() = default;
};

class GameObject : public Drawable, public Movable {
public:
  void move(int new_x, int new_y) override {
    x_ = new_x;
    y_ = new_y;
  }

private:
  int x_ = 0;
  int y_ = 0;
};
```

**Принцип разделения интерфейсов (ISP):** несколько маленьких интерфейсов
(`Drawable`, `Movable`, `Printable`) лучше одного большого. Стена реализует
только `Drawable` - ей незачем писать пустую заглушку `move()`.

---

## Композиция против наследования

- **Наследование** - это is-a: `Circle` это `Shape`, `Enemy` это `GameObject`.
- **Композиция** - это has-a: объект хранит другой объект как поле, а не
  наследует его.
- В Задаче 3 иерархия игровых объектов (`Enemy`, `Bullet`, `Wall` -
  `GameObject`) - это is-a. А вот "у игрока есть оружие" - это has-a:
  `Player` хранит `Weapon` как поле, а не наследуется от него.
- Наследование от класса с большим публичным интерфейсом (в том числе
  от контейнера) - частая ошибка: клиент получает доступ ко всем методам
  базы, даже к тем, что ломают инварианты производного класса.

---

## Пример: наследование вместо композиции

```cpp
// BAD: public inheritance exposes the whole vector interface, including
// operations that break the "this is a stack" invariant.
class Stack : public std::vector<int> {
public:
  void push(int value) { push_back(value); }

  int pop() {
    int value = back();
    pop_back();
    return value;
  }
};
```

Ничто не мешает вызвать `stack.erase(stack.begin() + 1)` или
`stack.insert(...)` - публичное наследование делает **весь** интерфейс
`std::vector` частью интерфейса `Stack`.

---

## Исправление: композиция

```cpp
class Stack {
public:
  void push(int value) { data_.push_back(value); }

  int pop() {
    int value = data_.back();
    data_.pop_back();
    return value;
  }

  bool empty() const { return data_.empty(); }

private:
  std::vector<int> data_;
};
```

`Stack` хранит `std::vector<int>` как поле (has-a) - наружу торчат только
`push`/`pop`/`empty`, инвариант "это стек" защищён.

---

## Публичное, защищённое и приватное наследование

```cpp
class Base {};

class PublicChild : public Base {};       // is-a, весь API базы наружу
class ProtectedChild : protected Base {}; // виден только дальнейшим потомкам
class PrivateChild : private Base {};     // "реализовано через", не is-a
```

- `public` - почти всегда то, что нужно: is-a, публичный интерфейс базы
  доступен снаружи.
- `protected` - редкость, имеет смысл только если сама иерархия будет
  расширяться дальше.
- `private` - "реализовано через" (implemented-in-terms-of): интерфейс базы
  становится приватным. По возможностям похоже на композицию, но
  композиция обычно яснее и почти всегда предпочтительнее (см. `Stack` выше).

---

## typeid и type_info

`typeid` возвращает объект `std::type_info` - информацию о типе времени
выполнения:

```cpp
#include <typeinfo>

class Base {
public:
  virtual ~Base() = default;
};

class Derived : public Base {
public:
  void hello() const { std::cout << "Hello from Derived" << std::endl; }
};

void print_type(const Base& obj) {
  if (typeid(obj) == typeid(Derived)) {
    std::cout << "This is a Derived" << std::endl;
  }
}
```

`std::type_index` (`<typeindex>`) оборачивает `type_info`, чтобы его можно
было использовать как ключ `std::map`/`std::unordered_map` - у самого
`type_info` нет `operator<` и хэша.

---

## dynamic_cast для указателей

```cpp
void handle(Base* base) {
  if (Derived* derived = dynamic_cast<Derived*>(base)) {
    derived->hello();
  } else {
    std::cout << "Not a Derived" << std::endl;
  }
}
```

- При неудаче `dynamic_cast` для указателя возвращает `nullptr` - отсюда
  проверка через `if`.
- `static_cast<Derived*>(base)` вниз по иерархии ничего не стоит и не
  проверяет тип - это корректно только когда тип гарантированно известен,
  иначе неопределённое поведение. `dynamic_cast` стоит дороже (обходит
  информацию об иерархии классов во время выполнения), зато безопасен.

---

## dynamic_cast для ссылок

```cpp
void handle(Base& base) {
  try {
    Derived& derived = dynamic_cast<Derived&>(base);
    derived.hello();
  } catch (const std::bad_cast& e) {
    std::cout << "Cast failed: " << e.what() << std::endl;
  }
}
```

- У ссылки нет состояния "пусто", поэтому при неудаче `dynamic_cast`
  бросает `std::bad_cast`, а не возвращает что-то похожее на `nullptr`.
- `typeid(*p)` при `p == nullptr` бросает `std::bad_typeid` - это отдельно
  оговорено стандартом именно для `typeid` от разыменования нулевого
  указателя.

---

## Требования, флаги, стоимость

- `dynamic_cast` требует полиморфный класс-источник (хотя бы одна
  `virtual`-функция) - иначе это ошибка компиляции, а не времени выполнения.
- `typeid` компилируется и для неполиморфных типов, но тогда возвращает
  статический тип выражения, известный уже на этапе компиляции, - операнд
  в рантайме даже не вычисляется. Настоящую проверку типа во время
  выполнения `typeid` даёт только для полиморфных классов.
- RTTI включён по умолчанию. Флаг отключения: gcc/clang `-fno-rtti`,
  MSVC `/GR-`. Без RTTI `dynamic_cast` не соберётся - на курсе не отключайте.
- `typeid(x).name()` - implementation-defined: gcc/clang (Itanium ABI)
  дают манглированное имя, MSVC - человекочитаемое. Для читаемого вида
  на gcc/clang нужен demangling (`abi::__cxa_demangle` из `<cxxabi.h>`).
  `name()` годится для отладки и логов, не для логики - типы сравнивайте
  через `typeid(a) == typeid(b)`, а не строки `name()`.
- Стоимость: `dynamic_cast` обходит информацию об иерархии классов во
  время выполнения - дороже `static_cast` и обычного виртуального вызова,
  особенно при множественном/виртуальном наследовании.

---

## Почему проверка типа - это запах

Задача 3 запрещает это в движке напрямую:

```cpp
class GameObject { public: virtual ~GameObject() = default; };
class Enemy : public GameObject { public: void chase_player() {} };
class Bullet : public GameObject { public: void fly() {} };
class Wall : public GameObject { public: void block() {} };

// BAD: engine has to know about every object type; new type = engine edit.
void update(std::vector<std::unique_ptr<GameObject>>& objects) {
  for (auto& object : objects) {
    if (auto* enemy = dynamic_cast<Enemy*>(object.get())) {
      enemy->chase_player();
    } else if (auto* bullet = dynamic_cast<Bullet*>(object.get())) {
      bullet->fly();
    } else if (auto* wall = dynamic_cast<Wall*>(object.get())) {
      wall->block();
    }
  }
}
```

Поведение объекта вынесено из его класса в движок. Добавление третьего,
четвёртого типа - это правка `update()`, а не только новый класс.

---

## Новый тип объекта без правок в движке

```cpp
class GameContext {};

class GameObject {
public:
  virtual ~GameObject() = default;
  virtual void act(GameContext& context) = 0;
};

class Enemy : public GameObject {
public:
  void act(GameContext&) override { /* chase player */ }
};

void update(std::vector<std::unique_ptr<GameObject>>& objects,
            GameContext& context) {
  for (auto& object : objects) {
    object->act(context);  // engine does not care which subclass this is
  }
}
```

Новый тип (`Wall`, `PowerUp`, ...) - это новый класс, переопределяющий
`act()`. `update()` не меняется ни на строку - это и есть "живая
модификация" из `GRADING.md`.

---

## Когда RTTI всё-таки уместен

- **Отладка и логирование:** `typeid(*obj).name()` в сообщении об ошибке -
  на логику программы это не влияет.
- **Десериализация:** в файле/сети лежит имя типа, по нему нужно решить,
  какой конкретный класс собрать - тип объекта заранее неизвестен коду.
- **Границы с чужим кодом:** библиотека отдаёт указатель на базовый класс,
  а её контракт не даёт виртуального метода для того, что вам нужно.
- **Паттерн Visitor** - структурированная альтернатива ручным
  `dynamic_cast`: набор типов фиксирован заранее, поведение вынесено наружу.

---

## Паттерн Visitor

```cpp
class Circle;
class Rectangle;

class ShapeVisitor {
public:
  virtual void visit(Circle& circle) = 0;
  virtual void visit(Rectangle& rectangle) = 0;
  virtual ~ShapeVisitor() = default;
};

class Shape {
public:
  virtual void accept(ShapeVisitor& visitor) = 0;
  virtual ~Shape() = default;
};

class Circle : public Shape {
public:
  void accept(ShapeVisitor& visitor) override { visitor.visit(*this); }
};
```

Честно: `Visitor` фиксирует набор типов в интерфейсе `ShapeVisitor`.
Новый наследник `Shape` требует добавить `visit()` во все визиторы - это
правка интерфейса, ровно то, чего мы избегаем в движке Задачи 3.

---

## Столкновения без проверок типа

Два объекта разных типов столкнулись - как обработать это без `dynamic_cast`?

```cpp
class GameContext;

class GameObject {
public:
  virtual ~GameObject() = default;
  virtual void act(GameContext& context) = 0;
  virtual void on_touch(GameObject&) {}
};

class Bullet : public GameObject {
public:
  void act(GameContext&) override {}
  void on_touch(GameObject&) override { hit_ = true; }

private:
  bool hit_ = false;
};
```

Каждый объект сам решает, что для него значит касание - виртуальный метод,
а не движок с деревом `if`. Упрощение уровня курса: полная двойная
диспетчеризация (разное поведение для каждой конкретной пары типов)
сложнее и на курсе не требуется.

---

## Типичные ошибки

- Публичное наследование там, где на самом деле has-a (`Stack : public
  std::vector`) - наружу торчит весь чужой интерфейс.
- Ромб без `virtual` - две независимые копии базы, неожиданные значения
  полей, неоднозначные вызовы.
- `virtual ~Interface() {}` вместо `= default` - лишнее пустое тело,
  ничего не даёт.
- Каскад `dynamic_cast`/`if` в игровом цикле - именно то, что Задача 3
  запрещает явно.
- `typeid(x).name()` используется как часть логики (сравнение строк,
  `switch` по строке) - implementation-defined, сломается при смене
  компилятора.
- `dynamic_cast` для указателя используется без проверки результата
  на `nullptr`.

---

## Вопросы для самопроверки

- Почему в вашем движке нет `dynamic_cast`/`switch` по типу объекта -
  покажите, как добавляется новый тип без правок в `update()`/`act()`.
- Что сломается, если убрать `virtual` в одном из промежуточных классов
  ромбовидной иерархии?
- Почему деструктор интерфейса `virtual ... = default`, а не просто
  виртуальный с пустым телом?
- Если в вашем коде есть RTTI - почему это не могло быть виртуальным
  методом?
- В чём разница между `dynamic_cast<T*>` и `dynamic_cast<T&>` при
  неудачном приведении?

---

## Практика на паре

1. Возьмите каскад `if (dynamic_cast<Enemy*>...) else if
   (dynamic_cast<Bullet*>...)` со слайда "До" и перепишите на виртуальный
   `act()`. Добавьте третий тип (`Wall`) и убедитесь, что `update()` не
   изменился ни на строку.
2. Воспроизведите ромб (`Animal -> Mammal, Bird -> Bat`) без `virtual`,
   получите два экземпляра `Animal`: сравните адреса `&bat.Mammal::name_`
   и `&bat.Bird::name_` - они разные. Почините виртуальным наследованием
   и убедитесь, что адрес стал один.
3. Добавьте вывод в конструкторы `Animal`/`Mammal`/`Bird`/`Bat` из примера
   с порядком конструирования и напечатайте порядок вызова при создании
   `Bat`. Объясните, почему `Animal("mammal-default")` из `Mammal()`
   не печатается.
