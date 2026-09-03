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

# Фабрики, реестры и конвейеры обработки

Семинар 10. Курс ООП (C++), ФИТ НГУ.

---

## Проблема: `if (name == ...)`

Первое, что напрашивается, - завести один большой разбор по имени:

```cpp
std::unique_ptr<Converter> make_converter(const std::string& name,
                                           const std::vector<std::string>& args) {
  if (name == "mute") {
    return std::make_unique<MuteConverter>(args);
  } else if (name == "mix") {
    return std::make_unique<MixConverter>(args);
  } else if (name == "gain") {
    return std::make_unique<GainConverter>(args);
  }
  throw ConfigError("unknown converter: " + name);
}
```

Работает. Проблема - не здесь.

---

## Проблема: что ломается

Три места должны остаться синхронными, и ничего в компиляторе это не
проверяет:

1. **Парсер** - список `if/else if` внутри `make_converter`.
2. **Справка** - отдельная функция `print_help()`, где кто-то вручную
   перечислил имена и подписи текстом.
3. **Исполнитель** - если `mix` требует дополнительный входной файл, а
   остальные нет, где-то есть ещё один список "кому нужен доп. поток".

Добавили новый конвертер, поправили `if/else` - собралось, тесты прошли.
Про `print_help()` вспоминают не всегда: она не участвует в компиляции и
не участвует в тестах конвертации, поэтому забытая правка не ловится
ничем, кроме внимательного чтения вывода `-h` глазами.

---

## Фабричный метод: интерфейс и конвертер

```cpp
class Converter {
public:
  virtual ~Converter() = default;
  virtual std::vector<int16_t> process(const std::vector<int16_t>& in) = 0;
};

class MuteConverter : public Converter {
public:
  explicit MuteConverter(const std::vector<std::string>& args) {
    if (args.size() != 2) {
      throw ConfigError("mute expects 2 arguments: start end");
    }
    start_ = std::stod(args[0]);
    end_ = std::stod(args[1]);
  }

  std::vector<int16_t> process(const std::vector<int16_t>& in) override;

private:
  double start_;
  double end_;
};
```

- Виртуальный деструктор в базовом классе обязателен: конвертеры удаляются
  через `std::unique_ptr<Converter>`, без него вызовется `~Converter()`, а не
  `~MuteConverter()`.
- Конструктор сразу превращает строки из конфига в типизированные поля.
- `std::stod` здесь не обёрнут в `try/catch` ради краткости слайда - в
  реальном коде обязательно (слайд про разбор конфигурации).

---

## Фабричный метод: функция-фабрика

```cpp
std::unique_ptr<Converter> make_mute(const std::vector<std::string>& args) {
  return std::make_unique<MuteConverter>(args);
}
```

- "Фабричный метод" - функция (или метод), которая создаёт объект и
  возвращает его через указатель на **базовый** тип, а не через конкретный.
- Вызывающий код работает с `Converter`, не зная и не заботясь, какой
  именно наследник создан.
- Владение сразу уходит в `std::unique_ptr` - ни одного `new` без
  соответствующего удаления, ни одной утечки при исключении в конструкторе.

---

## `std::function`: контейнер для фабрик

`make_mute`, `make_mix`, `make_gain` - разные функции с одинаковой
**сигнатурой**. Чтобы сложить их в один контейнер (реестр), нужен один
общий тип для "что угодно вызываемое с такой сигнатурой".

```cpp
#include <functional>

using ConverterFactory =
    std::function<std::unique_ptr<Converter>(const std::vector<std::string>&)>;

ConverterFactory factory = [](const std::vector<std::string>& args) {
  return std::make_unique<MuteConverter>(args);
};

std::unique_ptr<Converter> converter = factory({"0", "30"});
```

- `std::function<R(Args...)>` - обёртка, стирающая конкретный тип
  вызываемого объекта (функция, лямбда, функтор), пока сигнатура совпадает.
- У каждой лямбды свой уникальный тип - без `std::function` их нельзя
  положить в один `std::map`.

---

## Реестр: `Registration` и `ConverterRegistry`

```cpp
struct Registration {
  std::string name;
  std::string description;
  std::string signature;
  ConverterFactory factory;
};

class ConverterRegistry {
public:
  void register_converter(Registration reg) {
    registry_[reg.name] = std::move(reg);
  }

  const Registration& find(const std::string& name) const {
    auto it = registry_.find(name);
    if (it == registry_.end()) {
      throw ConfigError("unknown converter: " + name);
    }
    return it->second;
  }
  const std::map<std::string, Registration>& all() const { return registry_; }

private:
  std::map<std::string, Registration> registry_;
};
```

- `description` и `signature` лежат рядом с `factory`: справка и парсер
  читают одну и ту же запись реестра, им негде разойтись.
- `find` явно бросает `ConfigError`. `registry_.at(name)` бросил бы
  `std::out_of_range` - не то исключение, что ждёт `main`.

---

## Регистрация: `register_all()`

```cpp
void register_all(ConverterRegistry& registry) {
  registry.register_converter({"mute", "Mute a time interval [start, end)",
                                "mute start end",
                                [](const std::vector<std::string>& args) {
                                  return std::make_unique<MuteConverter>(args);
                                }});
  registry.register_converter({"mix", "Mix in a secondary stream", "mix $n [offset]",
                                [](const std::vector<std::string>& args) {
                                  return std::make_unique<MixConverter>(args);
                                }});
  registry.register_converter({"gain", "Scale amplitude by a factor", "gain factor",
                                [](const std::vector<std::string>& args) {
                                  return std::make_unique<GainConverter>(args);
                                }});
}
```

- Новый конвертер - один новый класс (`FooConverter`) и один вызов
  `register_converter(...)` здесь. `wav_io`, `pipeline`, `print_help` не
  меняются: они читают реестр, а не список имён.
- `register_all` вызывается один раз, явно, из `main`, до разбора
  аргументов командной строки.

---

## Проблема самостоятельной регистрации

Хочется, чтобы каждый конвертер регистрировал себя сам, без общего списка:

```cpp
// ПЛОХО: each converter registers itself via a static object; works only
// by accident of link order and translation unit selection.
namespace {

struct MuteRegistrar {
  MuteRegistrar() {
    global_registry().register_converter({"mute", "...", "...", make_mute});
  }
};

const MuteRegistrar mute_registrar;

}  // namespace
```

Два независимых способа получить тихо неработающую программу:

- **Порядок статической инициализации.** `global_registry()` из другого
  `.cpp` может быть ещё не построен, когда уже выполняется конструктор
  `mute_registrar` - порядок инициализации статических объектов из разных
  единиц трансляции не определён стандартом (static initialization order
  fiasco).
- **Мёртвый код в статической библиотеке.** Если ничто в остальной
  программе не ссылается на символы из `mute.cpp` напрямую, линкер вправе
  не включить этот `.o` из статической библиотеки в бинарник вообще -
  `mute_registrar` никогда не сконструируется, без единой ошибки сборки.

Надёжный вариант проще: одна функция `register_all()` (слайд выше),
вызванная явно в начале `main`. Это осознанное упрощение с понятным
потолком: если конвертеры станут плагинами, загружаемыми во время
выполнения (`.so`/`.dll`), самостоятельная регистрация станет неизбежной -
но это отдельная тема, не требуемая Задачей 4.

---

## Справка из реестра

```cpp
void print_help(const ConverterRegistry& registry) {
  std::cout << "sound_processor -c <config> <out.wav> <in1.wav> [...]\n";
  std::cout << "sound_processor -h | --help\n\n";
  std::cout << "Converters:\n";
  for (const auto& [name, reg] : registry.all()) {
    std::cout << "  " << reg.signature << " - " << reg.description << "\n";
  }
}
```

- Восемь строк, и в них нет ни одного захардкоженного имени конвертера -
  весь список приходит из `registry.all()`.
- Добавили конвертер в `register_all()` - он в тот же момент появился
  в `-h`, без единой правки этой функции.

---

## Разделение на модули

```
wav_io          reads/writes WAV chunks; nothing here knows about converters
converters      Converter interface + concrete converters; nothing here knows about WAV
config_parser   turns config.txt into validated operations, via the registry
pipeline        runs operations block by block; depends on wav_io + converters
main            owns the registry, calls register_all(), maps exceptions to exit codes

main --> config_parser --> converters (through the registry)
main --> pipeline --> wav_io
main --> pipeline --> converters (through the operations)
```

- `wav_io` не включает ни одного заголовка из `converters` - его можно
  тестировать на round-trip коротких буферов, даже если `converters` ещё
  пуст.
- `converters` не включает ни одного заголовка из `wav_io` - тесты
  конвертеров работают на `std::vector<int16_t>`, без единого файла на
  диске: ровно то, что требует Задача 4 - "тесты конвертеров на коротких
  синтетических буферах".
- Направление зависимости - от конкретного к общему: `main` знает про всё,
  `wav_io` не знает ни про что.

---

## Двухфазность: сначала проверить всё, потом обрабатывать

> "Ошибка в последней строке конфига из 10000 команд обнаруживается до
> того, как прочитан первый семпл... Программа обязана не создать
> выходной файл вообще, а если он уже существовал - не изменить его."

Это требование Автомата, но следует прямо из архитектуры, а не из
дополнительного кода:

- **Фаза 1.** `parse_config` читает файл целиком, для каждой строки
  находит конвертер в реестре и конструирует его - конструктор бросает
  `ConfigError` при неверных аргументах. Функция возвращает готовый
  `std::vector<Operation>` или не возвращает ничего вовсе.
- **Фаза 2.** `pipeline` получает уже проверенный вектор операций, выходной
  файл открывается только после успешного завершения фазы 1.

Если фаза 1 бросает исключение на строке 10000, `main` попадает в `catch`
раньше, чем где-либо в программе появится код, открывающий `output.wav`
на запись.

---

## `Operation`: результат фазы 1

```cpp
struct Operation {
  std::unique_ptr<Converter> converter;
  std::vector<int> extra_stream_indices;  // e.g. mix's $n, already validated
};

std::vector<Operation> parse_config(std::istream& config,
                                     const ConverterRegistry& registry,
                                     int num_inputs);
```

- К моменту, когда `parse_config` возвращает результат, каждая операция
  уже содержит типизированные аргументы - не строки из файла, а поля
  `double`/`int` внутри самого `Converter` (`MuteConverter::start_` и т.д.).
- `pipeline` (следующие слайды) больше не видит сырую строку и не может
  встретить ошибку разбора - она уже случилась бы на фазе 1.

---

## Конвейер: рекурсия против цикла

Соединить конвертеры можно рекурсией или циклом - разница проявляется
только на большом числе операций.

```cpp
// ПЛОХО: recursion depth equals the number of operations - a config with
// 10000 lines overflows the call stack.
std::vector<int16_t> apply_chain(const std::vector<Operation>& ops, std::size_t i,
                                  std::vector<int16_t> buffer) {
  if (i == ops.size()) {
    return buffer;
  }
  return apply_chain(ops, i + 1, ops[i].converter->process(buffer));
}
```

Итеративный вариант глубины вызовов не имеет:

```cpp
std::vector<int16_t> apply_chain(const std::vector<Operation>& ops,
                                  std::vector<int16_t> buffer) {
  for (const auto& op : ops) {
    buffer = op.converter->process(buffer);
  }
  return buffer;
}
```

Стек в порядке при любом числе операций. Но что именно лежит в `buffer`?

---

## Интерфейс конвертера: чего требует память

> "Спроектируйте интерфейс до того, как писать конвертеры, иначе
> переделывать придётся всё."

Сигнатура `process(const std::vector<int16_t>& in)` не говорит, сколько
семплов в `in`. Если конвейер вызывает её один раз на конвертер, передавая
целиком входной файл:

```cpp
// ПЛОХО: one call per converter with the ENTIRE stream in "buffer" - peak
// memory equals stream length, exactly what Task 4 forbids.
std::vector<int16_t> buffer = read_whole_file(main_in);
for (const auto& op : ops) {
  buffer = op.converter->process(buffer);
}
write_whole_file(out, buffer);
```

- Тот же самый цикл, что на прошлом слайде избавил нас от рекурсии - стек
  здесь ни при чём, проблема в размере `buffer`.
- На файле в 10 минут это гигабайты памяти на каждом шаге конвейера;
  требование Задачи 4 - пиковая память не должна зависеть от длины потока.
- По той же причине в задаче запрещён `reverse`: чтобы развернуть поток,
  нужно сначала увидеть его целиком.

---

## Интерфейс конвертера: блочный конвейер

Правильный вызов - много раз, с ограниченным окном входа:

```cpp
constexpr std::size_t kBlockSamples = 4096;

void run_pipeline(const std::vector<Operation>& ops, WavReader& main_in, WavWriter& out) {
  std::vector<int16_t> block(kBlockSamples);
  while (true) {
    std::size_t read = main_in.read_block(block);
    if (read == 0) {
      break;
    }
    std::vector<int16_t> chunk(block.begin(), block.begin() + read);
    for (const auto& op : ops) {
      chunk = op.converter->process(chunk);
    }
    out.write_block(chunk);
  }
}
```

- Сигнатура `process` не изменилась - изменилась дисциплина вызова: блок
  фиксированного размера, много раз. Тип это не гарантирует, гарантирует
  только `pipeline`.
- Конвертеру с памятью (например, echo с линией задержки) состояние
  негде хранить, кроме собственных полей объекта - он создан один раз в
  фазе 1 и живёт все вызовы `process` этой операции.
- Ровно здесь продолжается семинар 9: `block`/`process(...)` - тот же
  фиксированный буфер, что читал байты чанка `data`, только на уровне
  семплов, а не байт.

---

## Коды возврата

| Код | Значение |
|---|---|
| 0 | успех |
| 1 | ошибка аргументов командной строки |
| 2 | ошибка ввода-вывода (открытие/чтение/запись) |
| 3 | неподдерживаемый формат входного файла |
| 4 | ошибка в файле конфигурации (парсинг/валидация) |
| 5 | ошибка обработки (во время конвертации) |

- Коды - прямое отображение иерархии исключений семинара 4:
  `CmdlineError -> 1`, `IoError -> 2`, `UnsupportedFormatError -> 3`,
  `ConfigError -> 4`, `ProcessingError -> 5`.
- Сообщение всегда идёт в `std::cerr`, никогда в `std::cout` - `stdout`
  занят выводом `-h` и не должен смешиваться с диагностикой.

---

## `main` целиком

```cpp
int report_error(const std::exception& e, int code) {
  std::cerr << e.what() << "\n";
  return code;
}
```

```cpp
int main(int argc, char** argv) {
  ConverterRegistry registry;
  register_all(registry);
  try {
    CmdlineArgs args = parse_cmdline(argc, argv);
    if (args.show_help) {
      print_help(registry);
      return 0;
    }
    run_sound_processor(args, registry);
    return 0;
  } catch (const CmdlineError& e) {
    return report_error(e, 1);
  } catch (const IoError& e) {
    return report_error(e, 2);
  } catch (const UnsupportedFormatError& e) {
    return report_error(e, 3);
  } catch (const ConfigError& e) {
    return report_error(e, 4);
  } catch (const ProcessingError& e) {
    return report_error(e, 5);
  }
}
```

- `report_error` вынесен один раз: тело каждого `catch` одинаковое -
  напечатать `what()`, вернуть код. Пять одинаковых тел - повод для одной
  вспомогательной функции, а не для копирования.
- `registry` строится и заполняется `register_all()` один раз, до `try`:
  если сама регистрация некорректна, это баг программы, а не ошибка
  пользователя, и коду возврата 0-5 здесь взяться не из чего.

---

## Разбор аргументов командной строки

```cpp
struct CmdlineArgs {
  bool show_help = false;
  std::string config_path;
  std::string output_path;
  std::vector<std::string> input_paths;
};

CmdlineArgs parse_cmdline(int argc, char** argv) {
  std::vector<std::string> tokens(argv + 1, argv + argc);
  auto has = [&](const std::string& flag) {
    return std::find(tokens.begin(), tokens.end(), flag) != tokens.end();
  };
  bool show_help = has("-h") || has("--help");
  if (show_help && has("-c")) {
    throw CmdlineError("-h is incompatible with -c");
  }
  if (show_help) {
    return {true, "", "", {}};
  }
  if (!has("-c") || tokens.size() < 4) {
    throw CmdlineError("usage: sound_processor -c <config> <out.wav> <in1.wav> [...]");
  }
  return {false, tokens[1], tokens[2], {tokens.begin() + 3, tokens.end()}};
}
```

- Упрощённый разбор предполагает порядок аргументов как в `usage`
  (`-c config out in1 [in2 ...]`) - этого достаточно для синтаксиса,
  который задаёт сама Задача 4.

---

## Разбор конфигурационного файла

```cpp
std::vector<Operation> parse_config(std::istream& config, const ConverterRegistry& registry,
                                     int num_inputs) {
  std::vector<Operation> ops;
  std::string line;
  int line_number = 0;
  while (std::getline(config, line)) {
    ++line_number;
    if (line.empty() || line[0] == '#') {
      continue;
    }
    std::istringstream tokens(line);
    std::string name;
    tokens >> name;
    std::vector<std::string> args{std::istream_iterator<std::string>(tokens), {}};
    ops.push_back(parse_operation(registry, name, args, num_inputs, line_number));
  }
  return ops;
}
```

- `std::getline` даёт номер строки бесплатно - счётчик растёт на каждой
  итерации, включая пустые строки и комментарии.
- `std::istringstream` сама делит строку по пробелам и табам через `>>` -
  отдельный токенизатор не нужен (нужны заголовки `<sstream>`, `<iterator>`).
- `parse_operation` (не показан) находит конвертер в реестре, вызывает его
  `factory`, и всё, что она бросит, ловит здесь и дополняет номером строки.

---

## Ссылки `$n` и номер строки в ошибке

```cpp
int parse_stream_ref(const std::string& token, int num_inputs, int line_number) {
  if (token.empty() || token[0] != '$') {
    throw ConfigError("line " + std::to_string(line_number) + ": expected $n reference");
  }
  int n = 0;
  try {
    n = std::stoi(token.substr(1));
  } catch (const std::exception&) {
    throw ConfigError("line " + std::to_string(line_number) + ": bad stream reference '" +
                       token + "'");
  }
  if (n < 1 || n > num_inputs) {
    throw ConfigError("line " + std::to_string(line_number) + ": $" + std::to_string(n) +
                       " is out of range [1, " + std::to_string(num_inputs) + "]");
  }
  return n;
}
```

- Номер строки попадает в сообщение здесь же, где обнаружена ошибка.
- `std::stoi` без `try/catch` бросает `std::invalid_argument` мимо
  `ConfigError` прямо в `main`, где такое исключение никто не ловит - код
  возврата будет не 4, а `terminate`.

---

## Типичные ошибки

- Справка написана текстом в `print_help()`, а не собрана обходом реестра -
  при добавлении конвертера про неё забывают, `-h` расходится со списком.
- Самостоятельная регистрация через статические объекты в разных `.cpp`
  файлах статической библиотеки - линкер может выбросить неиспользуемый
  `.o`, конвертер тихо пропадает без ошибки сборки.
- `registry.at(name)` вместо явной проверки - на неизвестном имени в
  `main` прилетает `std::out_of_range`, а не `ConfigError`, код возврата
  не 4.
- Конвейер реализован рекурсивно (функция вызывает себя на каждую
  операцию) - на конфиге из 10000 команд переполняется стек.
- Обработка начинается до полной проверки конфигурации - на ошибке в
  последней из 10000 строк уже создан или испорчен `output.wav`.
- `std::stod`/`std::stoi` в конструкторе конвертера не обёрнуты в
  `try/catch` - `gain abc` роняет программу необработанным исключением
  вместо кода 4.

---

## Вопросы для самопроверки

- Что сломается, если конвертер вместо строки в `register_all()` начнёт
  регистрироваться через статический объект в отдельном `.cpp` своей
  статической библиотеки?
- Почему разбор конфигурации не может просто пропустить ошибочную строку
  и обработать остальные - что изменится на конфиге из 10000 команд с
  ошибкой в последней строке?
- Почему конвейер реализован циклом по вектору операций, а не рекурсивным
  вызовом? Покажите, где в вашем коде это гарантировано.
- Почему интерфейс `Converter` не принимает `std::vector<int16_t>` на весь
  файл? Где в вашем коде ограничен размер блока?
- Живая модификация: добавьте конвертер из банка Задачи 4 (например,
  `fadein`) - через сколько файлов и строк прошла правка?

---

## Практика на паре

1. Сделайте мини-реестр из двух "конвертеров" над `std::vector<int>`
   (например, `add_one`, `negate`): интерфейс с виртуальным `process`,
   `ConverterRegistry`, функция `register_all()`. Справка печатается
   единственным циклом по `registry.all()`, без единого захардкоженного
   имени.
2. Добавьте третий конвертер (`scale`, умножает на константу из аргумента).
   Посчитайте правки: должно получиться ровно две - новый класс и одна
   строка `register_converter(...)`. Если правок больше - найдите, где
   нарушилась независимость модулей.
3. Напишите `parse_line(const std::string& line, int line_number)`: по
   строке вида `add_one` или `scale 3` возвращает готовый объект из
   реестра или бросает исключение с номером строки внутри сообщения.
   Проверьте на строке с неизвестным именем и на пустой строке.
