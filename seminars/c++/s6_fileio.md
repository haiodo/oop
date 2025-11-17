---
marp: true
theme: default
paginate: true
---

# Ввод-вывод в C++

## Семинар 6

---

## Содержание

1. Потоки ввода-вывода
2. Стандартные потоки
3. Файловый ввод-вывод
4. Форматирование вывода
5. Обработка ошибок

---

## Потоки ввода-вывода

- **Поток** - абстракция для работы с данными
- Библиотека `<iostream>` для консольного ввода-вывода
- Библиотека `<fstream>` для файлового ввода-вывода

```cpp
#include <iostream>
#include <fstream>
```

---

## Стандартные потоки

```cpp
#include <iostream>

int main() {
  int number;
  std::cout << "Введите число: ";
  std::cin >> number;
  std::cout << "Вы ввели: " << number << std::endl;
  return 0;
}
```

- `std::cin` - стандартный ввод
- `std::cout` - стандартный вывод
- `std::cerr` - поток ошибок

---

## Операторы ввода-вывода

```cpp
// Вывод
std::cout << "Hello" << " " << "World" << std::endl;

// Ввод
std::string name;
int age;
std::cin >> name >> age;

// Ввод строки с пробелами
std::string line;
std::getline(std::cin, line);
```

---

## Файловый ввод-вывод

```cpp
#include <fstream>

// Запись в файл
std::ofstream outFile("output.txt");
outFile << "Hello, file!" << std::endl;
outFile.close();

// Чтение из файла
std::ifstream inFile("input.txt");
std::string data;
std::getline(inFile, data);
inFile.close();
```

---

## Режимы открытия файлов

```cpp
// Различные режимы
std::ofstream file1("file.txt", std::ios::out);        // запись
std::ifstream file2("file.txt", std::ios::in);         // чтение
std::ofstream file3("file.txt", std::ios::app);        // добавление
std::ofstream file4("file.txt", std::ios::trunc);      // перезапись
std::fstream file5("file.txt", std::ios::in | std::ios::out); // чтение+запись
```

---

## Проверка состояния потока

```cpp
std::ifstream file("data.txt");

if (!file.is_open()) {
  std::cerr << "Ошибка открытия файла!" << std::endl;
  return 1;
}

while (file.good()) {
  std::string line;
  std::getline(file, line);
  std::cout << line << std::endl;
}

file.close();
```

---

## Форматирование вывода

```cpp
#include <iomanip>

double pi = 3.14159;

std::cout << std::fixed << std::setprecision(2) << pi << std::endl;  // 3.14
std::cout << std::scientific << pi << std::endl;                     // 3.14e+00
std::cout << std::setw(10) << std::setfill('0') << 42 << std::endl;  // 0000000042
```

---

## Пример: работа с файлом

```cpp
#include <iostream>
#include <fstream>
#include <string>

int main() {
  // Запись данных
  std::ofstream outFile("students.txt");
  outFile << "Иван 20" << std::endl;
  outFile << "Мария 19" << std::endl;
  outFile.close();

  // Чтение данных
  std::ifstream inFile("students.txt");
  std::string name;
  int age;

  while (inFile >> name >> age) {
    std::cout << "Имя: " << name << ", Возраст: " << age << std::endl;
  }

  inFile.close();
  return 0;
}
```

---

## Обработка ошибок

```cpp
std::ifstream file("data.txt");

if (file.fail()) {
  std::cerr << "Не удалось открыть файл" << std::endl;
}

if (file.eof()) {
  std::cout << "Достигнут конец файла" << std::endl;
}

if (file.bad()) {
  std::cerr << "Критическая ошибка потока" << std::endl;
}
```

---

## Практические задания

1. Создать программу для записи и чтения списка студентов
2. Реализовать копирование содержимого одного файла в другой
3. Написать программу подсчета строк, слов и символов в файле

---

## Заключение

- Потоки - универсальный механизм ввода-вывода в C++
- Всегда проверяйте успешность операций с файлами
- Не забывайте закрывать файлы после работы
- Используйте форматирование для красивого вывода
