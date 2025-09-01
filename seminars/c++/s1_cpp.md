---
marp: true
theme: default
paginate: true
---

# Введение в C++

---

## Что такое C++?

- Язык программирования общего назначения
- Расширение языка C с поддержкой ОПП
- Создан Бьярном Страуструпом в 1985 году
- Компилируемый язык
- Статическая типизация

---

## Особенности C++

- **Производительность** - близко к "железу"
- **Объектно-ориентированное программирование**
- **Шаблоны** (templates)
- **Управление памятью** вручную
- **Множественное наследование**
- **Перегрузка операторов**

---

## Структура программы

```cpp
#include <iostream>

int main() {
  std::cout << "Hello, World!" << std::endl;
  return 0;
}
```

- `#include` - директива препроцессора
- `main()` - точка входа в программу
- `std::cout` - вывод в консоль

---

## Типы данных

```cpp
int number = 42;           // целое число
double price = 19.99;      // число с плавающей точкой
char letter = 'A';         // символ
bool isTrue = true;        // логический тип
string name = "John";      // строка
```

---

## Переменные и константы

```cpp
// Переменные
int age = 20;
double height = 1.75;

// Константы
const int MAX_SIZE = 100;
const double PI = 3.14159;

// Автоматический вывод типа (C++11)
auto value = 42;  // int
auto pi = 3.14;   // double
```

---

## Ввод и вывод

```cpp
#include <iostream>
#include <string>

int main() {
  std::string name;
  int age;

  std::cout << "Введите имя: ";
  std::cin >> name;

  std::cout << "Введите возраст: ";
  std::cin >> age;

  std::cout << "Привет, " << name << "! Вам " << age << " лет." << std::endl;

  return 0;
}
```

---

## Условные операторы

```cpp
int score = 85;

if (score >= 90) {
  cout << "Отлично!" << endl;
} else if (score >= 70) {
  cout << "Хорошо!" << endl;
} else if (score >= 50) {
  cout << "Удовлетворительно" << endl;
} else {
  cout << "Неудовлетворительно" << endl;
}
```

---

## Циклы

```cpp
// Цикл for
for (int i = 0; i < 10; i++) {
  cout << i << " ";
}

// Цикл while
int count = 0;
while (count < 5) {
  cout << count << endl;
  count++;
}

// Цикл do-while
int num;
do {
  cout << "Введите число (0 для выхода): ";
  cin >> num;
} while (num != 0);
```

---

## Функции

```cpp
// Объявление функции
int add(int a, int b);

// Определение функции
int add(int a, int b) {
  return a + b;
}

// Функция без возвращаемого значения
void printMessage(string message) {
  cout << message << endl;
}

int main() {
  int result = add(5, 3);
  printMessage("Результат: " + to_string(result));
  return 0;
}
```

---

## Массивы

```cpp
// Статический массив
int numbers[5] = {1, 2, 3, 4, 5};

// Обход массива
for (int i = 0; i < 5; i++) {
  cout << numbers[i] << " ";
}

// Range-based for (C++11)
for (int num : numbers) {
  cout << num << " ";
}
```

---

## Указатели и ссылки

```cpp
int value = 42;

// Указатель
int* ptr = &value;
std::cout << "Значение: " << *ptr << std::endl;
std::cout << "Адрес: " << ptr << std::endl;

// Ссылка
int& ref = value;
ref = 100;  // изменяет исходную переменную
std::cout << "Новое значение: " << value << std::endl;
```

---

## Компиляция

```bash
# Компиляция одного файла
g++ -o program main.cpp
clang++ -o program main.cpp

# Компиляция с флагами
g++ -std=c++17 -Wall -Wextra -o program main.cpp
clang++ -std=c++17 -Wall -Wextra -o program main.cpp

# Запуск программы
./program
```

---

## Практическое задание

Напишите программу, которая:

1. Запрашивает у пользователя 5 чисел
2. Сохраняет их в массив
3. Находит максимальное и минимальное значения
4. Вычисляет среднее арифметическое
5. Выводит результаты

---

## Вопросы?

**Спасибо за внимание!**
