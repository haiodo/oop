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

# Бинарный ввод-вывод

## Семинар 9

### Курс: Объектно-ориентированное программирование, ФИТ НГУ

---

## Текстовый режим против бинарного

```cpp
// BAD: default (text) mode.
// On Windows the runtime rewrites bytes on every read/write:
// 0x0A -> 0x0D 0x0A on write, 0x0D 0x0A -> 0x0A on read.
// A 0x0A byte that is audio data, not a newline, gets silently corrupted.
std::ifstream in("input.wav");
std::ofstream out("output.wav");

// GOOD: std::ios::binary disables the translation on every platform.
std::ifstream in_bin("input.wav", std::ios::binary);
std::ofstream out_bin("output.wav", std::ios::binary);
```

- На Linux и macOS текстовый и бинарный режим ничем не отличаются -
  трансляции `\n` в `\r\n` там никогда не было, поэтому ошибка не проявится
  на вашей машине.
- Курс собирается и на Windows (MSVC): без `std::ios::binary` Задача 4
  ломается именно там - классическое "у меня работает".

---

## read и write: сигнатуры

```cpp
std::ifstream in("data.bin", std::ios::binary);

char buffer[16];
in.read(buffer, sizeof(buffer));    // read(char*, std::streamsize)

std::ofstream out("copy.bin", std::ios::binary);
out.write(buffer, in.gcount());     // write(const char*, std::streamsize)

// For non-char types, cast the address, not the type.
std::uint32_t value = 0;
in.read(reinterpret_cast<char*>(&value), sizeof(value));
```

- `read`/`write` работают побайтово, без знания о типах и без
  форматирования - в отличие от `<<`/`>>`.
- `reinterpret_cast<char*>` - единственный переносимый способ передать
  адрес не-`char` переменной в `read`/`write`.

---

## gcount(): короткое чтение - это не ошибка

```cpp
char buffer[64];
in.read(buffer, sizeof(buffer));
std::streamsize got = in.gcount();

if (got < static_cast<std::streamsize>(sizeof(buffer))) {
  // Not an error by itself: this may simply be the last, shorter
  // block at the end of the file. Check in.eof() / in.bad() to know why.
}
```

- `read` не гарантирует, что прочитает ровно столько байт, сколько
  запрошено. Гарантия одна - `gcount()` вернёт, сколько реально прочитано.
- Предположение "раз запросил N байт, значит прочитал N" - источник
  ошибок ровно на последнем блоке файла.

---

## seekg/tellg/seekp

```cpp
std::ifstream in("input.wav", std::ios::binary);

in.seekg(0, std::ios::end);
std::streamoff file_size = in.tellg();
in.seekg(0, std::ios::beg);

// Skip a chunk of known size without reading its bytes.
std::uint32_t chunk_size = 40;
in.seekg(chunk_size, std::ios::cur);
```

- `seekg`/`tellg` - для чтения (`g` = get), `seekp` - для записи (`p` = put).
- `std::ios::beg`/`cur`/`end` - точка отсчёта: начало, текущая позиция, конец.
- Пропуск чанка через `seekg(size, std::ios::cur)` не тратит время на
  чтение данных, которые всё равно не нужны.

---

## Порядок байт (endianness)

- **Little-endian**: младший байт числа хранится по меньшему адресу.
  **Big-endian**: наоборот, первым идёт старший байт.
- RIFF/WAV - формат little-endian (унаследован от x86).
- x86 и ARM в обычном режиме - little-endian, поэтому "прочитать 4 байта в
  `std::uint32_t` через `reinterpret_cast`" часто *работает* на вашей машине.
- Это не гарантия: порядок байт хоста не оговорён стандартом C++, и такой
  код неявно завязан на архитектуру, а не на формат файла.

```cpp
// Relies on the host being little-endian. No compile error, no runtime
// error - just a silently wrong result on a big-endian host.
std::uint32_t size;
in.read(reinterpret_cast<char*>(&size), sizeof(size));
```

---

## Переносимая сборка числа из байт

```cpp
std::uint32_t read_u32_le(std::istream& in) {
  unsigned char bytes[4];
  in.read(reinterpret_cast<char*>(bytes), sizeof(bytes));
  return static_cast<std::uint32_t>(bytes[0]) |
         (static_cast<std::uint32_t>(bytes[1]) << 8) |
         (static_cast<std::uint32_t>(bytes[2]) << 16) |
         (static_cast<std::uint32_t>(bytes[3]) << 24);
}

std::uint16_t read_u16_le(std::istream& in) {
  unsigned char bytes[2];
  in.read(reinterpret_cast<char*>(bytes), sizeof(bytes));
  return static_cast<std::uint16_t>(bytes[0] | (bytes[1] << 8));
}
```

- Сдвиги и `|` дают одинаковый результат на любой архитектуре - он не
  зависит от порядка байт хоста.
- Ровно эти функции нужны для чтения размера чанка и полей `fmt ` в
  Задаче 4.

---

## Выравнивание структур

```cpp
struct Example {
  char id[4];           // offset 0, 4 bytes
  std::uint16_t flag;   // offset 4, 2 bytes
  std::uint32_t value;  // needs 4-byte alignment -> 2 padding bytes here
};

// sizeof(Example) is typically 12, not 10: the compiler inserts padding
// so that "value" starts at an address divisible by alignof(std::uint32_t).
```

- Читать заголовок файла в `struct` одним `read()` предполагает, что в
  структуре нет padding-байт - это implementation-defined, компилятор
  ничего не обязан.
- `#pragma pack(1)` уберёт padding, но это расширение компилятора, а не
  стандарт: ведёт себя по-разному на MSVC/GCC/Clang и никак не решает
  вопрос порядка байт.
- Правильный путь - читать поля по одному, как на предыдущем слайде.

---

## Целочисленные типы фиксированной ширины

```cpp
#include <cstdint>

std::uint32_t chunk_size = 0;    // exactly 32 bits, always
std::uint16_t num_channels = 0;  // exactly 16 bits, always
std::int16_t sample = 0;         // exactly 16 bits, signed, always
```

- `int` - стандарт гарантирует лишь "не меньше 16 бит". На практике почти
  везде 32, но формат файла не должен зависеть от "почти".
- `long` - 32 бита на Windows (MSVC) и 64 бита на Linux/macOS в тех же
  64-битных сборках. Прочитать 4 байта файла в `long` - разное поведение
  на разных платформах, на которых собирается курс.
- Формат файла описывает поля в битах. Типы из `<cstdint>` - единственный
  способ сказать это в коде однозначно.

---

## RIFF: устройство чанка

```
[4 bytes: chunk id, ASCII, e.g. "fmt " or "data"]
[4 bytes: chunk size, uint32 little-endian - size of the data that follows]
[chunk size bytes: data]
[1 padding byte, only if chunk size is odd - not counted in the size]
```

- Любой чанк RIFF устроен одинаково: идентификатор, размер, данные.
- Размер описывает **только данные**, не идентификатор и не себя.
- Без размера текущего чанка нельзя найти начало следующего - разбор
  файла останавливается.

---

## RIFF: вложенность

```
RIFF (id="RIFF", size, id="WAVE")
  fmt  chunk   - audio parameters (channels, sample rate, bits per sample)
  LIST chunk   - metadata, optional, written by ffmpeg without -bitexact
  fact chunk   - optional, sample count for non-PCM formats
  data chunk   - the actual samples
```

- WAV - это RIFF-контейнер с типом формы `WAVE`: первые 12 байт файла -
  `"RIFF"`, размер, `"WAVE"`.
- Дальше идёт последовательность чанков. `fmt ` обычно стоит раньше
  `data`, но это обычай, а не гарантия формата.
- Состав и число чанков между `fmt ` и `data` не фиксированы.

---

## RIFF: пропуск посторонних чанков

- Чанк с неизвестным идентификатором - **не ошибка** и **не данные**.
  Его нужно пропустить на `chunk_size` байт вперёд через `seekg`.
- Падать с ошибкой на `LIST`/`fact`/`JUNK`/`bext` нельзя: их пишет ffmpeg
  и большинство редакторов, это нормальная часть формата.
- Если `chunk_size` нечётный, сразу после данных стоит один
  байт-заполнитель, который **не входит** в размер чанка. Забыть его -
  значит съехать на 1 байт при чтении следующего чанка и сломать разбор
  всего оставшегося файла.

---

## RIFF: чтение fmt

```cpp
struct WavFormat {
  std::uint16_t audio_format = 0;
  std::uint16_t num_channels = 0;
  std::uint32_t sample_rate = 0;
  std::uint32_t byte_rate = 0;
  std::uint16_t block_align = 0;
  std::uint16_t bits_per_sample = 0;
};
struct WavInfo {
  WavFormat fmt;
  std::streamoff data_offset = 0;
  std::uint32_t data_size = 0;
};

WavFormat read_fmt_chunk(std::istream& in) {
  WavFormat fmt;
  fmt.audio_format = read_u16_le(in);
  fmt.num_channels = read_u16_le(in);
  fmt.sample_rate = read_u32_le(in);
  fmt.byte_rate = read_u32_le(in);
  fmt.block_align = read_u16_le(in);
  fmt.bits_per_sample = read_u16_le(in);
  return fmt;
}
```

---

## Скелет цикла чтения чанков

```cpp
WavInfo parse_riff(std::istream& in) {
  WavInfo info;
  while (in) {
    char chunk_id[4];
    in.read(chunk_id, sizeof(chunk_id));
    if (in.gcount() < static_cast<std::streamsize>(sizeof(chunk_id))) {
      break;  // no more chunks
    }
    std::uint32_t chunk_size = read_u32_le(in);
    std::string id(chunk_id, sizeof(chunk_id));
    if (id == "fmt ") {
      info.fmt = read_fmt_chunk(in);
    } else if (id == "data") {
      info.data_offset = in.tellg();
      info.data_size = chunk_size;          // size from THIS header
      in.seekg(chunk_size, std::ios::cur);
    } else {
      in.seekg(chunk_size, std::ios::cur);  // unknown chunk: skip
    }
    if (chunk_size % 2 == 1) {
      in.seekg(1, std::ios::cur);  // odd-size padding byte
    }
  }
  return info;
}
```

- `info.data_size` - из заголовка чанка `data`, а не "всё, что осталось
  в файле". `info.fmt.sample_rate`/`bits_per_sample` - из разобранных
  полей `fmt `, не константы `44100`/`16`.

---

## Почему 44 байта - это неправильно

```cpp
// BAD: assumes RIFF(12) + fmt (24) + data header(8) = 44 bytes exactly,
// and that "data" starts right after. True only if nothing else sits
// between fmt and data.
std::ifstream in("input.wav", std::ios::binary);
in.seekg(44, std::ios::beg);
// Everything read from here on is treated as samples - including any
// chunk id/size bytes that do not belong to "data" at all.
```

- Файл, сделанный `ffmpeg ... -bitexact`, действительно укладывается
  ровно в 44 байта заголовка - решение "работает", автор уверен в коде.
- Обычный `ffmpeg` (без `-bitexact`) добавляет служебный чанк (например,
  `LIST`/`INFO` с именем кодировщика) между `fmt ` и `data`. Байты после
  44-го тогда - id/size чужого чанка, а не сэмплы: на выходе шум или тишина.
- Это ровно тот случай, из-за которого Задачу 4 не принимают: код
  работает на одном файле и ломается на файле того же формата.

---

## Потоковая обработка

```cpp
// info = parse_riff(in) and out (std::ios::binary) come from earlier steps.
constexpr std::size_t kBlockBytes = 4096;
std::vector<char> block(kBlockBytes);
std::uint32_t bytes_left = info.data_size;

while (bytes_left > 0) {
  std::size_t to_read = std::min<std::size_t>(kBlockBytes, bytes_left);
  in.read(block.data(), static_cast<std::streamsize>(to_read));
  std::size_t got = static_cast<std::size_t>(in.gcount());
  // process(block.data(), got) - convert this one block, then write it.
  out.write(block.data(), static_cast<std::streamsize>(got));
  bytes_left -= static_cast<std::uint32_t>(got);
}
```

- `block` выделен один раз фиксированного размера, а не растёт вместе с
  файлом - пиковая память не зависит от длины файла.
- Плохо: `std::vector<char> all_data(info.data_size)` и один `read()` на
  весь файл - работает на маленьком файле, не проходит замер памяти
  на большом (требование Задачи 4).

---

## Проверка ошибок

```cpp
std::ifstream in("input.wav", std::ios::binary);
if (!in.is_open()) {
  throw std::runtime_error("cannot open input.wav");
}
if (!in) {  // same as: if (in.fail())
  throw std::runtime_error("stream is not usable");
}
```

- `is_open()` - файл вообще открылся.
- `if (!file)` - идиома, эквивалентна `file.fail()`: сработавшая проверка
  ошибки любого рода после последней операции.
- `eof()` - конец потока достигнут, это не ошибка сама по себе.
- `bad()` - неустранимая ошибка ввода-вывода (например, сбой диска).
- `fail()` - логическая ошибка операции, включая `eof()`, наступивший до
  того, как запрошенные данные пришли полностью.
- Обрезанный файл: если `data_size` из заголовка больше, чем осталось
  байт в файле, - это `UnsupportedFormatError` из иерархии Задачи 4,
  а не молчаливое чтение меньшего числа сэмплов.

---

## Типичные ошибки

- Открыть файл без `std::ios::binary` - работает на Linux/macOS, ломается
  на Windows в CI.
- Читать фиксированные 44 байта заголовка вместо разбора чанков - падает
  на файле без `-bitexact`.
- Забыть байт выравнивания после чанка нечётного размера - весь
  дальнейший разбор файла съезжает на 1 байт.
- Считать неизвестный чанк ошибкой или частью `data`, вместо того чтобы
  пропустить его по размеру.
- Читать заголовок одним `read()` в `struct` - результат зависит от
  padding и порядка байт компилятора и платформы.
- Прочитать весь файл в `std::vector` - нарушает требование "память не
  зависит от длины потока".

---

## Вопросы для самопроверки

- Что сломается, если открыть входной WAV без `std::ios::binary`?
- Почему нельзя прочитать заголовок чанка одним `read()` в `struct`?
- Что произойдёт, если не пропустить байт выравнивания после чанка
  нечётного размера?
- Как отличить "read прочитал меньше байт, потому что это конец файла"
  от настоящей ошибки чтения?
- Почему размер `data` берётся из заголовка чанка, а не вычисляется как
  "всё, что осталось до конца файла"?

---

## Практика на паре

1. Написать функцию, печатающую список чанков WAV-файла: идентификатор и
   размер каждого чанка, без чтения его данных.
2. Проверить эту функцию на файле, сконвертированном
   `ffmpeg -i in.mp3 -f wav out.wav` (без `-bitexact`), и убедиться, что
   в списке есть чанки помимо `fmt ` и `data`.
3. Прогнать её же на файле с `-bitexact` - список должен свестись только
   к `fmt ` и `data`.
4. Добавить в функцию вывод `sample_rate` и `bits_per_sample` из
   разобранного `fmt `.
