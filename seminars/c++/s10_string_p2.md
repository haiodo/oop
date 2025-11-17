---
marp: true
theme: default
paginate: true
---

# C++ std::string и работа с кодировками

## UTF-8 и другие кодировки

---

## Проблемы std::string с кодировками

- `std::string` работает с байтами, не с символами
- Не понимает UTF-8 и другие многобайтовые кодировки
- Операции `size()`, `substr()`, `[]` работают на уровне байтов

```cpp
std::string utf8_text = "Привет"; // UTF-8
std::cout << utf8_text.size(); // Размер в байтах, не символах
```

---

## UTF-8 в std::string

```cpp
#include <iostream>
#include <string>

int main() {
  std::string text = "Hello мир 🌍";

  // Размер в байтах (не символах)
  std::cout << "Bytes: " << text.size() << std::endl;

  // Небезопасное обращение по индексу
  for (size_t i = 0; i < text.size(); ++i) {
    std::cout << static_cast<unsigned char>(text[i]) << " ";
  }
}
```

---

## Подсчет UTF-8 символов

```cpp
#include <string>

size_t utf8_length(const std::string& str) {
  size_t length = 0;
  for (size_t i = 0; i < str.size(); ++i) {
    // Проверяем начальный байт UTF-8 символа
    if ((str[i] & 0xC0) != 0x80) {
      ++length;
    }
  }
  return length;
}

// Использование
std::string text = "Привет";
std::cout << utf8_length(text) << " symbols" << std::endl;
```

---

## Итерация по UTF-8 символам

```cpp
#include <string>
#include <iostream>

void print_utf8_chars(const std::string& str) {
  for (size_t i = 0; i < str.size();) {
    unsigned char byte = str[i];
    size_t char_len = 1;

    // Определяем длину UTF-8 символа
    if ((byte & 0x80) == 0) char_len = 1;      // ASCII
    else if ((byte & 0xE0) == 0xC0) char_len = 2;
    else if ((byte & 0xF0) == 0xE0) char_len = 3;
    else if ((byte & 0xF8) == 0xF0) char_len = 4;

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
#include <codecvt>

int main() {
  // Широкая строка
  std::wstring wide_text = L"Привет мир 🌍";
  std::wcout.imbue(std::locale(""));
  std::wcout << wide_text << std::endl;

  // Размер в символах (зависит от платформы)
  std::wcout << L"Length: " << wide_text.length() << std::endl;

  return 0;
}
```

---

## Конверсия между кодировками

```cpp
#include <string>
#include <locale>
#include <codecvt>

// UTF-8 в UTF-16
std::wstring utf8_to_utf16(const std::string& utf8) {
  std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
  return converter.from_bytes(utf8);
}

// UTF-16 в UTF-8
std::string utf16_to_utf8(const std::wstring& utf16) {
  std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
  return converter.to_bytes(utf16);
}
```

---

## C++20: std::u8string

```cpp
#include <string>

int main() {
  // Явно UTF-8 строка (C++20)
  std::u8string utf8_text = u8"Привет мир 🌍";

  // Все те же проблемы с индексацией
  auto size_bytes = utf8_text.size();

  // Нужны специальные функции для работы с символами

  std::string regular_text(utf8_text.begin(), utf8_text.end());
  std::cout << "Text: " << regular_text << std::endl;
  std::cout << "Size in bytes: " << size_bytes << std::endl; // 24
  return 0;
}
```

---

## Использование ICU библиотеки

```cpp
#include <unicode/unistr.h>
#include <unicode/ustream.h>

int main() {
  // Создание Unicode строки из UTF-8
  icu::UnicodeString text = icu::UnicodeString::fromUTF8("Привет мир 🌍");

  // Правильная длина в символах
  std::cout << "Characters: " << text.length() << std::endl;

  // Безопасная работа с символами
  for (int32_t i = 0; i < text.length(); ++i) {
    UChar32 ch = text.char32At(i);
    // Работа с Unicode code point
  }

  return 0;
}
```

---

## Практические рекомендации

1. **Для ASCII**: используйте `std::string`
2. **Для UTF-8**:
   - `std::string` + специальные функции
   - Или библиотеки типа ICU
3. **Для широких символов**: `std::wstring` (осторожно с портируемостью)
4. **Современный подход**: рассмотрите библиотеки типа ICU или ranges

---

## Заключение

- `std::string` не идеален для Unicode
- Всегда учитывайте разницу между байтами и символами
- Используйте специализированные библиотеки для серьезной работы с текстом
- Тестируйте с реальными многобайтовыми данными
