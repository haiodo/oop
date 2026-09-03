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

# Инструменты разработки C++

**Семинар 0**
Курс: Объектно-ориентированное программирование
2 курс

---

## Что поставить: без этого не собрать ничего

| | Windows | Linux (Ubuntu) | macOS |
|---|---|---|---|
| Компилятор | Visual Studio Community | `sudo apt install build-essential` | `xcode-select --install` |
| CMake 3.21+ | идёт с Visual Studio | `sudo apt install cmake` | `brew install cmake` |
| Git | `winget install Git.Git` | `sudo apt install git` | идёт с Xcode CLT |

Нужен компилятор с поддержкой C++20: GCC 12+, Clang 15+, MSVC 2022.
В установщике Visual Studio отметьте компонент "Разработка классических
приложений на C++" - без него компилятора не будет.

`brew` ставится с [brew.sh](https://brew.sh), `winget` уже есть в Windows 10/11.

---

## Что поставить: линтер и форматирование

`clang-tidy` и `clang-format` идут одним пакетом LLVM. Без них не пройдут
две проверки из пяти.

| Windows | Linux (Ubuntu) | macOS |
|---|---|---|
| компонент "C++ Clang tools for Windows" в установщике Visual Studio | `sudo apt install clang-tidy clang-format` | `brew install llvm` |

На macOS Homebrew не кладёт LLVM в `PATH` намеренно, чтобы не подменять
системный компилятор. Путь надо добавить самому:

```bash
echo 'export PATH="$(brew --prefix llvm)/bin:$PATH"' >> ~/.zshrc
```

---

## Что поставить: под конкретные задачи

Ставится не сразу, а когда дойдёте до задачи.

| Зачем | Windows | Linux (Ubuntu) | macOS |
|---|---|---|---|
| Покрытие: `lcov` | не поддерживается, смотрите артефакт CI | `sudo apt install lcov` | `brew install lcov` |
| Терминальная графика | скачается сама (PDCurses) | `sudo apt install libncurses-dev` | `brew install ncurses` |
| Запись геймплея | [asciinema.org](https://asciinema.org) | `sudo apt install asciinema` | `brew install asciinema` |
| Подготовка WAV | `winget install ffmpeg` | `sudo apt install ffmpeg` | `brew install ffmpeg` |
| Просмотр звука | [audacityteam.org](https://www.audacityteam.org) | `sudo apt install audacity` | `brew install --cask audacity` |

---

## Проверка, что всё встало

```bash
cmake --version         # 3.21 или новее
git --version
clang-format --version
clang-tidy --version
```

Если команда не находится - дело в `PATH`, а не в том, что "не установилось".
На Windows эти команды надо запускать из "Developer Command Prompt for VS 2022":
обычный `cmd` не видит компилятор.

Дальше из корня репозитория:

```bash
cmake --preset dev && cmake --build --preset dev && ctest --preset dev
```

Должно собраться и пройти два теста из `tasks/example`.

---

## Редактор

Пресеты из `CMakePresets.json` читают сами, настраивать сборку руками не нужно:

- **Visual Studio 2022** - открыть папку репозитория (не `.sln`).
- **CLion** - пресеты появятся в списке конфигураций.
- **VS Code** - расширения "CMake Tools" и "clangd" или "C/C++".

`clangd` берёт настройки из `build/dev/compile_commands.json` - он создаётся
при первом `cmake --preset dev`, до этого подсказки в редакторе работать не будут.

---

## Git: ветка и Pull Request

- Каждая задача - отдельная ветка и отдельный Pull Request.
- Преподаватель добавляется в ревьюеры PR - это и есть способ подать задачу на проверку.
- История коммитов - основной материал для разговора: преподаватель выбирает из неё произвольные фрагменты.
- Краткий курс по git (ветки, merge, rebase): https://learngitbranching.js.org - пройти один раз, до Задачи 1.

---

## Git: коммиты

- Коммиты содержательные и распределены по времени, а не "начал и сразу всё запушил".
- Один коммит на 2000 строк - задача возвращается без проверки: обсуждать в таком коммите нечего.
- Каждый коммит обязан собираться сам по себе, не только финальный:

```bash
git rebase --exec 'cmake --preset dev && cmake --build --preset dev' origin/main
```

- Команда проигрывает историю от `origin/main` коммит за коммитом и останавливается на первом, где сборка упала.

---

## Шаблон курса

- https://github.com/haiodo/oop-cpp-template - открыть, нажать "Use this template".
- Репозиторий создаётся приватным, доступ преподавателю даётся через ревьюеров PR.
- В шаблоне уже настроены CMake, GoogleTest, все пять проверок и CI (GitHub Actions) - включать ничего не нужно.
- Каждая подзадача - папка `tasks/<имя>/`: скопировать `tasks/example`, переименовать, описать в своём `CMakeLists.txt`.

---

## oop_task: сборка одной задачи

```cmake
oop_task(NAME task1b SOURCES word_count.cpp MAIN main.cpp TESTS word_count_test.cpp)
```

- `NAME` - имя задачи, совпадает с именем папки в `tasks/`.
- `SOURCES` - `.cpp` с реализацией (без `main`), собираются в библиотеку; список не может быть пустым.
- `MAIN` - файл с точкой входа; не нужен, если задача - библиотека, покрытая тестами, а не консольная программа.
- `TESTS` - файлы с тестами GoogleTest, линкуются с `SOURCES`.
- Есть ещё `CURSES` - для задач с текстовым интерфейсом (пример - `tasks/example_terminal`), в Задаче 1 не понадобится.

---

## CMake: пресеты

- Пресет - именованный набор настроек сборки: компилятор, флаги, тип сборки. Описан в `CMakePresets.json` шаблона, писать `CMakeLists.txt` руками не нужно.
- В шаблоне несколько пресетов (`dev`, `asan`, `tidy`, `coverage`) - у каждого своя цель, поэтому одной сборки недостаточно.
- `cmake --preset dev` - **configure**: генерирует файлы сборки под конкретный пресет.
- `cmake --build --preset dev` - **build**: компиляция по уже сгенерированным файлам.
- Одна и та же команда работает одинаково на Linux, macOS и Windows.

---

## Пять проверок

До разговора с преподавателем все пять обязаны быть зелёными (GRADING.md §2):

| Проверка | Что означает |
|---|---|
| `dev` | Сборка без единого предупреждения, все тесты проходят |
| `asan` | Под санитайзерами нет ошибок памяти и неопределённого поведения |
| `tidy` | `clang-tidy` не нашёл проблем, включая имена |
| `coverage` | Покрытие тестами не ниже порога, заданного в задаче |
| `format-check` | Код отформатирован по `.clang-format` |

Каждая проверка - одна команда, одинаковая на всех трёх платформах.

---

## dev: сборка и тесты

```bash
cmake --preset dev && cmake --build --preset dev && ctest --preset dev
```

- Ловит: любое предупреждение компилятора (флаги уже настроены в шаблоне) и любой упавший тест.
- Типичный провал: скопировали код из другого источника - там неиспользуемая переменная или сравнение знакового с беззнаковым, компилятор предупреждает, сборка падает.
- Чинится: убрать причину предупреждения (не глушить его прагмами), пересобрать.

---

## asan: зачем нужен

```bash
cmake --preset asan && cmake --build --preset asan && ctest --preset asan
```

- AddressSanitizer (ASan) - ловит ошибки работы с памятью: use-after-free, выход за границы массива/вектора, двойной `delete`, утечки.
- UndefinedBehaviorSanitizer (UBSan) - ловит неопределённое поведение: переполнение знакового числа, разыменование `nullptr`, некорректный сдвиг.
- Это главный инструмент отладки на курсе: сегфолт без ASan говорит "что-то сломалось", ASan говорит - где именно и как.
- Такие ошибки часто "случайно" работают на `dev`, поэтому `asan` - отдельный обязательный пресет.

---

## asan: пример - выход за границы массива

```cpp
#include <vector>

// BAD: index 5 is past the end of a 3-element array.
void write_out_of_bounds() {
  int* data = new int[3]{1, 2, 3};
  data[5] = 42;  // heap-buffer-overflow, caught by the asan preset
  delete[] data;
}

// Correct: bounds-checked access, throws instead of corrupting memory.
void write_safely() {
  std::vector<int> data = {1, 2, 3};
  data.at(2) = 42;
}
```

---

## asan: как читать вывод

```
==12345==ERROR: AddressSanitizer: heap-buffer-overflow
WRITE of size 4 at 0x602000000014 thread T0
    #0 write_out_of_bounds() asan_demo.cpp:6
    #1 main asan_demo.cpp:12

0x602000000014 is located 0 bytes after a 12-byte region
allocated by thread T0 here:
    #0 operator new[](unsigned long)
    #1 write_out_of_bounds() asan_demo.cpp:5

SUMMARY: AddressSanitizer: heap-buffer-overflow asan_demo.cpp:6
```
--- 

## asan: как читать вывод - 2 
- Первая строка - тип ошибки; `WRITE`/`READ of size N` - какой доступ упал.
- Первый стек (`#0`, `#1`, ...) - читать сверху вниз, `#0` - место самой ошибки.
- "allocated by ... here" - где была выделена память (при use-after-free рядом будет ещё и "freed by ... here").
- `SUMMARY` - однострочная сводка, удобно искать в логе CI.

---

## tidy: имена и статический анализ

```bash
cmake --preset tidy && cmake --build --preset tidy
```

Ловит нарушения именования (таблица дальше) и типовые проблемы: неиспользуемые переменные, опасные неявные преобразования.

```cpp
// BAD: PascalCase function and missing trailing underscore.
void GetWordCount();
class BadCounter { int total = 0; };

// Correct:
void get_word_count();
class WordCounter { int total_ = 0; };
```

Типичный провал: скопировали пример с другого курса или из книги - там свои соглашения об именах.

---

## coverage: покрытие строк

```bash
cmake --preset coverage && cmake --build --preset coverage && ctest --preset coverage
```

- Ловит: доля выполненных тестами строк ниже порога, заданного в файле конкретной задачи (для Задачи 1 порог не задан - покрытие не измеряется).
- Типичный провал: тесты гоняют только "счастливый путь", обработка ошибок (пустой файл, неверные аргументы) не вызывается ни разу.
- Чинится: добавить тест на непокрытую ветку. HTML-отчёт с подсветкой непокрытых строк лежит в артефактах CI - ссылку на него кладите в `REPORT.md`.

---

## format-check: автоформатирование

```bash
cmake --build --preset dev --target format-check
```

Ловит: код не соответствует `.clang-format` из шаблона.

```cpp
// BAD: brace on its own line, 4-space indent.
void increment(int& count, bool bit)
{
    if (bit)
    {
        ++count;
    }
}
```

Чинится одной командой, форматирование руками не нужно:

```bash
cmake --build --preset dev --target format
```

---

## GoogleTest: минимальный тест

Предположим, в `word_count.h` есть функция, возвращающая частоты слов:

```cpp
#include <gtest/gtest.h>
#include "word_count.h"

// count_words returns std::map<std::string, std::size_t>
TEST(WordCountTest, CountsSingleWord) {
  auto result = count_words("hello");
  EXPECT_EQ(result["hello"], 1u);
}
```

- `TEST(SuiteName, TestName)` - один тест: `SuiteName` группирует связанные тесты, `TestName` - что именно проверяется.
- Файл теста подключается через `TESTS` в `oop_task` и собирается в отдельный исполняемый файл.

---

## EXPECT_EQ и ASSERT_EQ

- `EXPECT_EQ` - тест продолжает выполняться после неудачи; видно все упавшие проверки за один прогон.
- `ASSERT_EQ` - тест останавливается сразу; нужен, когда без этого условия следующие строки бессмысленны или упадут сами.

```cpp
TEST(WordCountTest, SplitsOnPunctuation) {
  auto words = split_words("Hello, world!");

  // words[1] below is UB if size is wrong, so stop here first.
  ASSERT_EQ(words.size(), 2u);
  EXPECT_EQ(words[0], "hello");
  EXPECT_EQ(words[1], "world");
}
```

---

## EXPECT_THROW

```cpp
#include <stdexcept>

TEST(WordCountTest, RejectsMissingFile) {
  EXPECT_THROW(read_file("does_not_exist.txt"), std::runtime_error);
}
```

- Проверяет, что выражение бросает исключение указанного типа.
- Тест падает и если исключения не было, и если тип не совпал.
- Для проверки отсутствия исключения есть `EXPECT_NO_THROW`.

---

## Антипаттерн: тест без ожидаемого значения

GRADING.md §7 прямо это запрещает:

```cpp
// BAD: compares the function to itself, catches nothing.
TEST(WordCountTest, CountIsConsistent) {
  EXPECT_EQ(count_words("a a b"), count_words("a a b"));
}

// Correct: expected values computed by hand.
TEST(WordCountTest, CountsRepeatedWord) {
  auto result = count_words("a a b");
  EXPECT_EQ(result["a"], 2u); EXPECT_EQ(result["b"], 1u);
}
```

- Если в `count_words` есть баг, обе стороны `EXPECT_EQ` одинаково неправильные - тест зелёный, ошибка не поймана.
- Такие проверки тестами не считаются: нужны значения, посчитанные вручную.

---

## Стиль курса: имена

| Что | Правило | Пример |
|---|---|---|
| Классы, структуры, перечисления | `CamelCase` | `BitArray`, `WordCount`, `CSVParser` |
| Функции и методы | `lower_case` | `push_back`, `to_string`, `split_words` |
| Переменные и параметры | `lower_case` | `word_count`, `num_bits` |
| Поля класса | `lower_case_` | `size_`, `data_` |
| Константы `constexpr` | `kCamelCase` | `kWordBits` |
| Пространства имён | `lower_case` | `word_count` |
| Макросы | `UPPER_CASE` | |

- Официального стандарта имён в C++ нет - курс следует стилю самой стандартной библиотеки.
- Функций с большой буквы не бывает: `GetArea()`, `SetValue()`, `PushBack()` - неверно.
- Проверяется автоматически: `cmake --preset tidy && cmake --build --preset tidy`.

---

## Стиль курса: язык

- Комментарии, имена и строковые литералы в `.h`/`.cpp` - только на английском: кириллица ломается на чужой локали и в чужом редакторе, а собирать код будут не только на вашей машине.
- `README.md` и `REPORT.md` - по-русски.

```cpp
// BAD: comment in Russian, breaks in other locale/editor.
// Обрезает старшие биты, которые не входят в size_.
void trim_high_bits();

// Correct: comment in English, same meaning.
// Bits above size_ must stay zero.
void discard_high_bits();
```

---

## REPORT.md

К каждому Pull Request прикладывается `REPORT.md` (GRADING.md §5):

- Команды сборки и запуска - копируются и работают как есть.
- Число покрытия и ссылка на HTML-отчёт из артефактов CI.
- Артефакты, которые требует конкретная задача (записи терминала, скриншоты, замеры времени).
- Раздел "Известные ограничения" - что не реализовано и почему.
- Раздел "Что было сложно" - одна-две честных строки; не оценивается, но задаёт тему разговора.
- Артефакты - только со своей машины, и это видно: пути, версия компилятора, размер терминала.

---

## Типичные ошибки

- Один коммит на всю задачу вместо истории, распределённой по времени.
- Забыли добавить преподавателя в ревьюеры Pull Request.
- Функция названа с большой буквы (`GetWordCount`) - `tidy` возвращает задачу.
- Проигнорировано предупреждение компилятора вместо исправления причины - `dev` не проходит.
- Код закоммичен незаформатированным в расчёте "потом поправлю" - `format-check` ловит это в CI.
- Тест вида `EXPECT_EQ(f(x), f(x))` - увеличивает покрытие, не ловит ни одной ошибки.

---

## Вопросы для самопроверки

- Почему в шаблоне несколько пресетов CMake, а не один набор флагов?
- Что выведет ASan, если в коде есть use-after-free, и как прочитать его стек?
- Почему функции и методы - `lower_case`, а не `CamelCase`, и что об этом скажет `tidy`?
- Чем `EXPECT_EQ` отличается от `ASSERT_EQ` и почему тест `EXPECT_EQ(f(x), f(x))` ничего не проверяет?
- Что случится с Pull Request, если он состоит из одного коммита на 2000 строк?

---

## Практика на паре

1. Создать репозиторий из шаблона ("Use this template"), склонировать локально.
2. Собрать `tasks/example`, запустить, прогнать все пять проверок на нетронутом шаблоне - все должны быть зелёными.
3. Намеренно сломать форматирование в одном файле, увидеть провал `format-check`, починить `--target format`.
4. Намеренно написать выход за границы массива, собрать с пресетом `asan`, прогнать тесты, прочитать отчёт ASan.
