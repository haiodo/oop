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

# Строки и текстовый ввод-вывод в C++

## std::string, потоки и файлы

### Семинар 2

### Курс: Объектно-ориентированное программирование

---

## Что такое std::string?

- **Класс** для работы со строками в C++
- Часть стандартной библиотеки `<string>`
- Заменяет C-style строки (`char*`)
- **RAII** - автоматическое управление памятью
- Проверка границ доступна через `at()`

```cpp
#include <string>
std::string str = "Hello, World!";
```

---

## Создание строк

```cpp
#include <string>

// Different ways to create a string
std::string s1;                    // empty string
std::string s2("Hello");           // from a literal
std::string s3 = "World";          // copy-initialization
std::string s4(s2);                // copy
std::string s5(10, 'A');           // 10 copies of 'A'
std::string s6{'H', 'i'};          // from an initializer list
```

---

## Основные операции

### Конкатенация

```cpp
std::string a = "Hello";
std::string b = "World";
std::string c = a + " " + b;       // "Hello World"
a += " there";                     // "Hello there"
```

### Сравнение

```cpp
std::string x = "Hello";
std::string y = "World";

if (x == y) {
  std::cout << "equal" << std::endl;
}
if (x < y) {
  std::cout << "x is lexicographically less" << std::endl;
}
```

---

## Доступ к символам

```cpp
std::string str = "Hello";

// Bounds-checked access
char c1 = str.at(0);               // 'H', checks bounds
char c2 = str[1];                  // 'e', no bounds check

// Iterators
for (auto it = str.begin(); it != str.end(); ++it) {
  std::cout << *it;
}

// Range-based for
for (char c : str) {
  std::cout << c;
}
```

---

## Размер и ёмкость

```cpp
std::string str = "Hello";

std::cout << str.size() << std::endl;      // 5
std::cout << str.length() << std::endl;    // 5, same as size()
std::cout << str.capacity() << std::endl;  // >= 5
std::cout << str.max_size() << std::endl;  // implementation-defined limit

str.reserve(100);                          // reserve capacity
str.shrink_to_fit();                       // release unused capacity
```

---

## Модификация строк

```cpp
std::string str = "Hello";

str.push_back('!');                // "Hello!"
str.pop_back();                    // "Hello"
str.append(" World");              // "Hello World"
str.insert(5, ",");                // "Hello, World"
str.erase(5, 1);                   // "Hello World"
str.clear();                       // ""
```

---

## Подстроки и поиск

### Подстроки

```cpp
std::string str = "Hello World";
std::string sub = str.substr(6, 5);        // "World"
std::string sub2 = str.substr(6);          // "World"
```

### Поиск

```cpp
std::size_t pos = str.find("World");       // 6
std::size_t pos2 = str.find('o');          // 4
std::size_t pos3 = str.rfind('o');         // 7, last occurrence

if (pos != std::string::npos) {
  // found
}
```

---

## Внутреннее устройство

### Small String Optimization (SSO)

```
Короткие строки (примерно до 15-23 символов):
+-------------------------------+
| символы прямо в объекте       |
+-------------------------------+

Длинные строки:
+---------+---------+---------+
| pointer |  size   |capacity |
+---------+---------+---------+
   |
   v
+-------------------------------+
|        данные в куче          |
+-------------------------------+
```

Короткие строки не требуют аллокации в куче - объект `std::string` хранит
их прямо в себе. Порог зависит от реализации стандартной библиотеки.

---

## Производительность

```cpp
// BAD: a new temporary string is allocated on every iteration,
// total complexity is O(n^2) in the amount of data
std::string join_bad(const std::vector<std::string>& parts) {
  std::string result;
  for (const auto& s : parts) {
    result = result + s;
  }
  return result;
}

// GOOD: += appends into the existing buffer, complexity is O(n)
std::string join_good(const std::vector<std::string>& parts) {
  std::string result;
  std::size_t total = 0;
  for (const auto& s : parts) {
    total += s.size();
  }
  result.reserve(total);  // final size is known ahead of time
  for (const auto& s : parts) {
    result += s;
  }
  return result;
}
```

`reserve` окупается, только когда итоговый размер известен заранее и цикл
большой. Микрооптимизации без замера (профилировщик, бенчмарк) чаще всего
не окупаются - сначала пишите понятный код.

---

## Параметры-строки: const&, string_view, по значению

```cpp
// string_view: только читаем, ничего не сохраняем
std::size_t count_spaces(std::string_view sv) {
  return std::count(sv.begin(), sv.end(), ' ');
}

// const string&: нужен нуль-терминатор (например, для C API)
void log_message(const std::string& msg) {
  std::puts(msg.c_str());
}

// по значению: функция и так делает копию (сохраняет поле)
struct Logger {
  explicit Logger(std::string text) : prefix(std::move(text)) {}
  std::string prefix;
};
```

Правило: `string_view` - для чтения без сохранения; `const std::string&` -
если нужен `c_str()` или совместимость со старым кодом; по значению - если
функция всё равно копирует.

---

## Осторожно: висячий string_view

```cpp
// ПЛОХО: string_view смотрит на копию text, а копия - параметр по значению
std::string_view first_word(std::string text) {
  auto pos = text.find(' ');
  return std::string_view(text).substr(0, pos);
}  // text is destroyed when the function returns - the view now dangles
```

`std::string_view` не владеет данными - он только ссылается на чужой буфер.
Здесь буфер - параметр `text` (копия, живёт только внутри функции); он
разрушается при выходе из функции, а `string_view` продолжает на него
указывать. Никогда не возвращайте и не храните `string_view` дольше, чем
живёт то, на что он указывает.

---

## Преобразования: строка и число

```cpp
// int to string
int num = 42;
std::string str = std::to_string(num);      // "42"

// string to int
std::string s = "123";
int n = std::stoi(s);                       // 123
double d = std::stod("3.14");               // 3.14

// with error checking
try {
  int result = std::stoi("abc");
  std::cout << result << std::endl;
} catch (const std::invalid_argument& e) {
  std::cerr << "invalid number: " << e.what() << std::endl;
}
```

---

## Строки в C++20 и что дальше

```cpp
#include <string>

std::string path = "report.csv";

path.starts_with("report");              // true, C++20
path.ends_with(".csv");                  // true, C++20
path.find(".csv") != std::string::npos;  // C++20-способ проверить "contains"
```

- `contains()` у `std::string` появился только в **C++23** - в C++20 его нет.
  У `std::map`/`std::unordered_map` `contains()` - как раз C++20, см. дальше.
- `std::format` (C++20, `<format>`) - типобезопасная замена `printf`.
  Полная поддержка в libstdc++ - только с GCC 13, проверьте компилятор.

---

## Знаковость char и \<cctype\>

```cpp
#include <cctype>

// ПЛОХО: char может быть отрицательным, isalnum(c) - undefined behavior
bool is_word_char_bad(char c) {
  return std::isalnum(c) || static_cast<unsigned char>(c) >= 0x80;
}

// Хорошо: приводим к unsigned char перед вызовом
bool is_word_char_good(char c) {
  unsigned char uc = static_cast<unsigned char>(c);
  return std::isalnum(uc) || uc >= 0x80;
}
```

Аргумент функций `<cctype>` (`isalnum`, `isalpha`, `isdigit`, ...) обязан
быть представим как `unsigned char` либо равен `EOF`. `char` на x86
(gcc/clang/MSVC) - знаковый: байт `>= 0x80` даёт отрицательное значение, и
вызов - undefined behavior по стандарту. На практике glibc и libc++ на
такой вызов обычно не падают: таблица классификации символов у них с
запасом на отрицательные индексы, поэтому программа молча "работает" -
но результат классификации байта не гарантирован. Полагаться на "не упало,
значит верно" нельзя: это не ловится тестами надёжно, это ищут при
код-ревью.

---

## Файлы: ifstream, ofstream, режимы

```cpp
#include <fstream>

std::ifstream in("input.txt");                    // read
std::ofstream out("output.csv");                  // write, truncates
std::ofstream log_file("log.txt", std::ios::app);  // append instead of truncate

if (!in) {  // equivalent to checking in.fail() right after opening
  std::cerr << "cannot open input.txt" << std::endl;
  return 2;
}
```

- `<fstream>`: `std::ifstream` - чтение, `std::ofstream` - запись,
  `std::fstream` - оба направления сразу.
- По умолчанию `ofstream` очищает файл (`std::ios::trunc`);
  `std::ios::app` дописывает в конец, не удаляя старое содержимое.

---

## Чтение построчно: getline и антипаттерн

```cpp
std::ifstream file("input.txt");
std::string line;

// ПЛОХО: good() не знает, что чтение ниже провалится
while (file.good()) {
  std::getline(file, line);
  std::cout << line << std::endl;
}

// Хорошо: поток сам сообщает результат последней операции
while (std::getline(file, line)) {
  std::cout << line << std::endl;
}
```

`getline` возвращает сам поток, а поток конвертируется в `bool` по
результату **последней** операции. После настоящей последней строки
`good()` ещё `true` - тело `while (file.good())` выполнится ещё раз,
`getline` не найдёт данных и очистит `line` - лишняя пустая итерация
в конце файла. `while (std::getline(...))` этой проблемы не имеет.

---

## fail(), bad(), eof(): в чём разница

```cpp
std::ifstream file("data.txt");
std::string line;
while (std::getline(file, line)) {
  // eof() is still false here - the read just succeeded
}
// loop exited: getline could not extract another line
```

- `eof()` - поток дошёл до конца входных данных.
- `fail()` - последняя операция не удалась (в том числе просто из-за `eof`).
- `bad()` - серьёзная ошибка ввода-вывода (сбойный диск, оборванный поток);
  после неё доверять потоку нельзя.
- После обычного выхода из `while (std::getline(...))`: `eof()` и `fail()`
  - `true`, `bad()` - `false`. Это штатный конец файла, а не ошибка.

---

## RAII: файл и деструктор

```cpp
void write_report(const std::vector<std::string>& lines) {
  std::ofstream out("report.csv");
  for (const auto& line : lines) {
    out << line << "\n";
  }
}  // out is destroyed here, its destructor closes the file
```

Ручной `close()` в конце функции не нужен - деструктор `ofstream`/`ifstream`
уже закрывает файл. `close()` вызывают явно, когда важно проверить ошибку
записи ДО выхода из области видимости (например, диск переполнился):

```cpp
out.close();
if (!out) {
  std::cerr << "failed to write report.csv" << std::endl;
  return 2;
}
```

---

## std::istringstream: разбор строки на токены

```cpp
#include <sstream>

std::istringstream stream("42 hello 3.14");
int a;
std::string word;
double d;
stream >> a >> word >> d;   // a == 42, word == "hello", d == 3.14
```

`std::ostringstream` решает обратную задачу: собрать строку из кусков через
`<<`, результат забирается через `stream.str()`. Оба типа нужны для разбора
поля CSV в задаче 1b и для форматированной строки в задаче 5.

---

## std::istringstream: поле прочитано не полностью

```cpp
std::istringstream stream("12abc");
int value;
stream >> value;  // value == 12, БЕЗ ошибки: failbit не выставлен
// "abc" тихо остаётся непрочитанным в потоке

bool fully_read = !stream.fail() && (stream.eof() || stream.peek() == EOF);
```

`operator>>` останавливается на первом символе, не подходящем числу, и не
считает это ошибкой - хвост строки молча теряется, если его не проверить.
Чтобы убедиться, что поле прочитано целиком, нужно проверить И `fail()`
(само извлечение прошло), И конец данных (`eof()` либо `peek() == EOF`).

---

## Форматирование вывода: iomanip

```cpp
#include <iomanip>

double percent = 12.3456;
std::cout << std::fixed << std::setprecision(2) << percent << std::endl;
// 12.35

std::cout << std::setw(10) << std::setfill('0') << 42 << std::endl;
// 0000000042
```

- Для CSV из задачи 1b: `std::fixed` + `std::setprecision(2)` дают ровно
  два знака после запятой, десятичный разделитель - точка (`.`).
- `setprecision` без `fixed` считает значащие цифры, а не знаки после
  запятой - легко перепутать.
- Локаль (`std::locale`) может поменять разделитель на запятую. На курсе
  локаль не трогаем: формат обязан быть одинаковым на любой машине.

---

## Детерминированный вывод: тай-брейк и stable_sort

```cpp
// ПЛОХО: сравнение только по частоте, порядок равных не определён
std::sort(entries.begin(), entries.end(),
          [](const auto& a, const auto& b) {
            return a.second > b.second;
          });

// Хорошо: полный порядок - при равенстве сравниваем по слову
std::stable_sort(entries.begin(), entries.end(),
                  [](const auto& a, const auto& b) {
                    if (a.second != b.second) {
                      return a.second > b.second;
                    }
                    return a.first < b.first;
                  });
```

Без тай-брейка при равной частоте `std::sort` не гарантирует порядок -
два запуска (или две разные реализации `libstdc++`/`libc++`) могут дать
разный CSV. Полный компаратор (частота, потом слово) убирает саму
возможность "равных" элементов - `stable_sort` здесь не строго обязателен,
но это безопасная привычка на случай, если компаратор в будущем ослабят.
Для задачи 1b: два запуска на одном входе обязаны совпасть побайтово.

---

## Счётчики слов: map и unordered_map

```cpp
#include <map>
#include <string>

std::map<std::string, std::size_t> counts;

for (const auto& word : words) {
  ++counts[word];  // if word is new, operator[] inserts it as 0 first
}
```

`operator[]` у `map`/`unordered_map`: если ключа нет - **вставляет** его со
значением по умолчанию (`0` для `std::size_t`), потом возвращает ссылку.
Для счётчика это ровно то, что нужно: `++counts[word]` заводит запись и
сразу увеличивает её. `std::unordered_map<std::string, std::size_t>`
используется точно так же.

---

## Ловушка operator[] и выбор контейнера

```cpp
std::map<std::string, std::size_t> counts;

// ПЛОХО: просто чтение counts["x"] вставляет запись "x" -> 0
if (counts["x"] == 0) {
  // ...
}

// Хорошо: проверка без вставки
if (!counts.contains("x")) {                  // C++20
  // ...
}
if (counts.find("x") == counts.end()) {       // альтернатива, любой стандарт
  // ...
}
```

`std::map` хранит ключи упорядоченными, вставка/поиск - `O(log n)`.
`std::unordered_map` порядка не хранит, вставка/поиск - `O(1)` в среднем,
но с большей константой на хеширование. Для задачи 1b порядок обхода
контейнера не важен (вывод сортируется явно), но время работы `map` и
`unordered_map` на больших файлах различается - это пункт автомата задачи.

---

## Типичные ошибки

- `std::isalnum(c)` с `char c >= 0x80` без приведения к `unsigned char` -
  undefined behavior; на glibc/libc++ обычно не падает и не ловится
  санитайзерами, программа просто может неверно классифицировать байт.
- `while (file.good()) { getline(...); }` - лишняя пустая итерация в конце
  файла; правильно - `while (std::getline(file, line))`.
- `if (counts[word] == 0)` для проверки наличия ключа - тихо создаёт
  запись; нужен `contains()` или `find()`.
- `istringstream("12abc") >> value` не считается ошибкой сама по себе -
  забытая проверка остатка молча теряет данные.
- Сортировка без тай-брейка по слову при равной частоте - результат
  недетерминирован, два запуска дают разные файлы.
- Ручной `close()` в каждой функции "для порядка" - лишний код; закрывает
  деструктор, `close()` нужен только чтобы проверить ошибку записи до
  конца области видимости.

---

## Вопросы для самопроверки

- Что сломается, если передать `char` со значением `>= 0x80` напрямую в
  `std::isalnum` без приведения к `unsigned char`?
- Почему `while (file.good())` даёт лишнюю обработку в конце файла, а
  `while (std::getline(file, line))` - нет?
- Что произойдёт при `if (counts[word] == 0)`, если слова `word` в
  `counts` ещё не было?
- Как проверить, что `istringstream` прочитал число целиком, а не только
  префикс строки?
- Почему для тай-брейка при равной частоте достаточно сравнения по слову,
  и что случится без него?

---

## Практика на паре

1. Написать `std::vector<std::string> split_words(const std::string& line)`
   по правилу задачи 1b: байт - часть слова, если это ASCII-буква,
   ASCII-цифра или `>= 0x80`; иначе - разделитель.
2. Покрыть функцию тестами: пустая строка, слово из одного байта, строка
   из одних разделителей, байты `>= 0x80` внутри слова (например, cp1251).
3. Сознательно передать `char` напрямую в `std::isalnum` для байта `>= 0x80`
   (без приведения), собрать и прогнать `cmake --preset asan && cmake
   --build --preset asan && ctest --preset asan` - убедиться, что программа,
   скорее всего, не упадёт (в этом и опасность UB: код работает "случайно",
   а не по гарантии стандарта), затем исправить приведением к
   `unsigned char`.
4. Написать компаратор для сортировки пар (слово, частота) с тай-брейком
   и проверить его на входе с двумя словами одинаковой частоты.
