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

# C++ std::string и работа с кодировками

## UTF-8 и другие кодировки

---

## Статус материала

- Факультатив. Задачи курса этот материал не требуют.
- `cpp-tasks/Task 1 - Initial.md`, Задача 1b: кодировка входного файла -
  **однобайтовая** (ASCII, latin-1, cp1251, cp866), один байт = один символ.
  UTF-8 в задаче намеренно не рассматривается, таблиц кодировок писать не нужно.
- Зачем всё-таки прочитать: понять, почему `size()` строки не равен числу
  символов, и не удивляться при работе с реальными текстами за пределами курса.

---

## Проблемы std::string с кодировками

- `std::string` работает с байтами, не с символами
- Не понимает UTF-8 и другие многобайтовые кодировки
- Операции `size()`, `substr()`, `[]` работают на уровне байтов

```cpp
#include <iostream>
#include <string>

int main() {
  std::string utf8_text = "Привет";  // UTF-8, six Cyrillic letters
  std::cout << utf8_text.size() << std::endl;  // bytes, not characters
  return 0;
}
```

---

## UTF-8 в std::string

```cpp
#include <iostream>
#include <string>

int main() {
  std::string text = "Hello мир 🌍";

  // Size in bytes (not characters)
  std::cout << "Bytes: " << text.size() << std::endl;

  // Unsafe indexed access: char is signed, cast before printing as a number
  for (std::size_t i = 0; i < text.size(); ++i) {
    std::cout << static_cast<int>(static_cast<unsigned char>(text[i])) << " ";
  }
}
```

---

## Что такое кодовая точка, символ и графема

- Байт - единица памяти. Кодовая точка Unicode (code point) - число,
  обозначающее один "элемент" (например, `U+043F` - буква "п").
- То, что человек видит как один символ (графема), может состоять из
  нескольких кодовых точек: буква с диакритикой, эмодзи с модификатором
  тона кожи, семейный эмодзи, склеенный из нескольких кодовых точек.
- Поэтому "число символов в строке" - плохо определённый вопрос: на уровне
  байт, кодовых точек и графем ответ разный.
- Дальше везде считаются кодовые точки UTF-8, а не графемы - это проще
  реализовать, но не совпадает с бытовым понятием "символ".

---

## Подсчет кодовых точек UTF-8

```cpp
#include <string>
#include <iostream>

std::size_t utf8_code_point_count(const std::string& str) {
  std::size_t count = 0;
  for (std::size_t i = 0; i < str.size(); ++i) {
    // char is signed on x86: cast to unsigned char before bitwise ops
    if ((static_cast<unsigned char>(str[i]) & 0xC0) != 0x80) {
      ++count;
    }
  }
  return count;
}

int main() {
  std::string text = "Привет";
  std::cout << utf8_code_point_count(text) << " code points" << std::endl;
  return 0;
}
```

Приведение к `unsigned char` обязательно: `str[i]` для байта `>= 0x80`
отрицателен на x86, и побитовые операции с таким значением - зона риска.

---

## Итерация по UTF-8 кодовым точкам

```cpp
#include <string>
#include <iostream>

void print_utf8_chars(const std::string& str) {
  for (std::size_t i = 0; i < str.size();) {
    unsigned char byte = static_cast<unsigned char>(str[i]);
    std::size_t char_len = 1;

    if ((byte & 0x80) == 0) {
      char_len = 1;  // ASCII
    } else if ((byte & 0xE0) == 0xC0) {
      char_len = 2;
    } else if ((byte & 0xF0) == 0xE0) {
      char_len = 3;
    } else if ((byte & 0xF8) == 0xF0) {
      char_len = 4;
    }

    std::cout << str.substr(i, char_len) << std::endl;
    i += char_len;
  }
}
```

---

## std::wstring для широких символов

```cpp
#include <string>
#include <iostream>
#include <locale>

int main() {
  std::wstring wide_text = L"Привет мир 🌍";
  std::wcout.imbue(std::locale(""));
  std::wcout << wide_text << std::endl;

  // Answer depends on the platform - see notes below
  std::wcout << L"Length: " << wide_text.length() << std::endl;
  return 0;
}
```

`wchar_t` имеет разный размер на разных платформах: 2 байта на Windows
(UTF-16), 4 байта на Linux и macOS (UTF-32). Этот пример даёт `Length: 12`
на Linux/macOS, но `Length: 13` на Windows - эмодзи `U+1F30D` не помещается
в один `wchar_t` и кодируется суррогатной парой. `std::wstring` непереносим,
и `length()` даже для него не равен числу символов.

---

## Конверсия между кодировками - устарело

**Устарело в C++17, удаляется в C++26. Не использовать в новом коде.**

```cpp
#include <string>
#include <locale>
#include <codecvt>

// deprecated, see notes below for the replacement
std::wstring utf8_to_utf16(const std::string& utf8) {
  std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
  return converter.from_bytes(utf8);
}

std::string utf16_to_utf8(const std::wstring& utf16) {
  std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
  return converter.to_bytes(utf16);
}
```

В сборке курса (`-Werror`) этот код не соберётся: `-Wdeprecated-declarations`
превращается в ошибку.

---

## Почему устарело и чем заменить

- Причина: интерфейс `wstring_convert`/`codecvt_utf8_utf16` не позволял
  нормально сообщать об ошибках конверсии (некорректный байт бросал
  исключение без деталей, либо молча портил данные).
- Стандартной замены в языке нет - комитет убрал старое, новое не предложил.
- Практические варианты:
  - платформенные функции: `MultiByteToWideChar`/`WideCharToMultiByte`
    на Windows, `iconv` на POSIX (Linux, macOS);
  - библиотеки: ICU, simdutf, utfcpp.

---

## C++20: std::u8string

```cpp
#include <string>
#include <iostream>

int main() {
  // Distinct type since C++20, not implicitly convertible to char
  std::u8string utf8_text = u8"Привет мир 🌍";

  auto size_bytes = utf8_text.size();

  // std::cout has no operator<< for char8_t: convert explicitly
  std::string regular_text(
      reinterpret_cast<const char*>(utf8_text.data()), utf8_text.size());
  std::cout << "Text: " << regular_text << std::endl;
  std::cout << "Size in bytes: " << size_bytes << std::endl;  // 24
  return 0;
}
```

`char8_t` - отдельный тип, не `char` и не `unsigned char`. Неявных
преобразований между ними в C++20 нет, а перегрузки `operator<<` для
`char8_t` в `std::cout` убраны - выводить `std::u8string` напрямую нельзя.

---

## Использование ICU библиотеки

**ICU не подключена в шаблоне курса.** Код ниже не соберётся без отдельной
установки библиотеки (vcpkg, conan, apt, brew) и правки `CMakeLists.txt`.

```cpp
#include <unicode/unistr.h>
#include <unicode/ustream.h>

int main() {
  icu::UnicodeString text = icu::UnicodeString::fromUTF8("Привет мир 🌍");

  // Character count, not byte count
  std::cout << "Characters: " << text.length() << std::endl;

  for (int32_t i = 0; i < text.length(); ++i) {
    UChar32 ch = text.char32At(i);
    // work with the Unicode code point
  }
  return 0;
}
```

---

## Практические рекомендации

- Однобайтовая кодировка (Задача 1b: ASCII, latin-1, cp1251, cp866) -
  `std::string` как есть, байт = символ.
- UTF-8, нужно только считать/резать по кодовым точкам - ручной разбор
  байтов (как на слайдах выше) или готовая библиотека.
- Конверсия между кодировками - платформенные функции или ICU/simdutf/utfcpp,
  не `std::wstring_convert`.
- Широкие строки - только внутри одной платформы, помня про размер `wchar_t`.
- Настоящая сегментация на "символы для человека" (графемы) - это ICU
  или аналогичная библиотека, а не самописный код.

---

## Ссылки

- https://en.cppreference.com/w/cpp/string/basic_string
- https://en.cppreference.com/w/cpp/string/basic_string/basic_string (u8string)
- https://en.cppreference.com/w/cpp/locale/codecvt_utf8_utf16 (депрекация)
- https://unicode.org/reports/tr29/ (UAX #29, границы графем)
- https://icu.unicode.org/
- https://github.com/simdutf/simdutf
- https://github.com/nemtrif/utfcpp
- https://learn.microsoft.com/windows/win32/api/stringapiset/nf-stringapiset-multibytetowidechar
- https://man7.org/linux/man-pages/man3/iconv.3.html
