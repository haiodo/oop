# ФИТ НГУ, курс ООП

## Задача 2а. Битовый массив (BitArray)

### Что нужно сделать

- Реализовать класс BitArray с заданным интерфейсом (см. ниже).
- Кратко задокументировать все публичные члены класса на техническом английском (в стиле Doxygen/комментариев к методам).
- Написать юнит-тесты на все публичные методы с использованием GoogleTest.
- Достичь покрытия: **не ниже 90% строк** по коду `BitArray` (тесты и `main.cpp` из подсчёта
  исключаются). Меряется пресетом `coverage` из шаблона курса, HTML-отчёт прикладывается к PR:
  ```bash
  cmake --preset coverage && cmake --build --preset coverage && ctest --preset coverage
  ```

Полезные ссылки:

- Что такое битовый массив: [Bit array - Wikipedia](https://en.wikipedia.org/wiki/Bit_array)
- Пример промышленной реализации: [boost::dynamic_bitset](https://www.boost.org/doc/libs/1_51_0/libs/dynamic_bitset/dynamic_bitset.html)

---

### Важные уточнения (обязательно к соблюдению)

- Хранение:
  - Используйте компактное хранение на базе без-знаковых целых (например, std::vector<uint64_t> или uint32_t). Размер слова — на ваше усмотрение, но он должен быть документирован.
- Индексация:
  - Индексы битов: 0..size()-1.
  - Бит с индексом 0 — младший бит (least significant bit, LSB) массива.
- Печать и строки:
  - to_string() возвращает строку длины size(), состоящую из '0' и '1'.
  - Порядок: самый старший бит (индекс size()-1) слева, младший бит (индекс 0) — справа.
- Сдвиги:
  - operator<< и operator<<= сдвигают биты к более старшим индексам (влево), заполняя освободившиеся биты нулями.
  - operator>> и operator>>= сдвигают биты к более младшим индексам (вправо), заполняя нулями.
  - Сдвиг на n >= size() обнуляет массив.
- Операции над массивами:
  - Битовые операции (&=, |=, ^=, также свободные операторы &, |, ^) допустимы только для массивов одинакового размера.
  - При несовпадении размеров выбрасывайте std::invalid_argument.
- Ошибки и исключения:
  - Неверный индекс (отрицательный или >= size()) в operator[], set, reset - std::out_of_range.
  - Отрицательные аргументы сдвига - std::invalid_argument.
  - resize с num_bits < 0 - std::invalid_argument.
  - Конструктор `BitArray(int num_bits, ...)` с num_bits < 0 - std::invalid_argument
    (то же правило, что и у resize).
- Поведение resize:
  - При увеличении длины новые биты инициализируются значением value.
  - При уменьшении длины биты с индексами >= нового размера отбрасываются.
- Сложность:
  - operator[], set, reset, push_back — амортизированно O(1).
  - Битовые операции и сдвиги — O(size / word_bits).
- Потоко-безопасность:
  - Класс не является потоко-безопасным.
- Пустой массив (`size() == 0`) - **валидное состояние**, а не ошибка. Определено так:
  - `to_string()` возвращает пустую строку `""`.
  - `any()` возвращает `false`, `none()` возвращает `true`, `count()` возвращает `0`.
  - `set()` и `reset()` без аргументов - разрешены и ничего не делают.
  - `operator~()` возвращает пустой массив.
  - Сдвиг пустого массива на любое неотрицательное `n` - разрешён, результат пустой.
  - Битовые операции двух пустых массивов - разрешены (размеры совпадают).
  - `operator==` для двух пустых массивов - `true`.
- Неиспользуемые старшие биты последнего слова обязаны быть **всегда нулевыми**. Это инвариант класса.
  Его нарушение не видно через публичный интерфейс до первого `count()`, `any()` или `operator==`,
  сравнивающего слова целиком - поэтому его надо поддерживать после каждой операции,
  а не чинить в момент чтения. Отдельно проверьте `operator~`, `resize` с уменьшением и `push_back`.
- Неконстантного `operator[]` в интерфейсе **нет намеренно**. Не добавляйте прокси-класс
  для записи вида `a[0] = true` - для записи есть `set(n, val)`.
- Тип `int` в интерфейсе (`size()`, `count()`, индексы, аргументы сдвига) - осознанное упрощение,
  унаследованное от `boost::dynamic_bitset` тех лет. Следствие: отрицательные аргументы возможны
  и обязаны диагностироваться, а не превращаться в огромные `size_t` при неявном приведении.
  Сборка с `-Wsign-conversion` не должна давать предупреждений - продумайте, где ставить приведения.

---

### Интерфейс (реализуйте ровно так; комментарии уточняют семантику)

```c++
// В этой задаче для простоты не требуется делать контейнер шаблонным.
// По желанию можно выбрать базовый тип хранения: 8/16/32/64-битные беззнаковые.

class BitArray
{
public:
  BitArray();
  ~BitArray();

  // Construct an array of num_bits bits.
  // The lowest sizeof(unsigned long)*8 bits can be initialized from 'value'.
  // Throws std::invalid_argument if num_bits < 0.
  explicit BitArray(int num_bits, unsigned long value = 0);
  BitArray(const BitArray& b);

  // Swap contents with another BitArray. No-throw guarantee.
  void swap(BitArray& b) noexcept;

  BitArray& operator=(const BitArray& b);

  // Change the size to num_bits.
  // On growth, new bits are initialized with 'value'.
  void resize(int num_bits, bool value = false);

  // Remove all bits; size becomes 0. Capacity may be released.
  void clear();

  // Append one bit at the end; reallocate if needed.
  void push_back(bool bit);

  // Bitwise ops (sizes must match; otherwise throw std::invalid_argument).
  BitArray& operator&=(const BitArray& b);
  BitArray& operator|=(const BitArray& b);
  BitArray& operator^=(const BitArray& b);

  // Logical shifts with zero fill.
  // Shift left: towards higher indices. Shift right: towards lower indices.
  BitArray& operator<<=(int n);
  BitArray& operator>>=(int n);
  BitArray operator<<(int n) const;
  BitArray operator>>(int n) const;

  // Set bit at index n to 'val'. Throws std::out_of_range on bad index.
  BitArray& set(int n, bool val = true);
  // Set all bits to 1 (true).
  BitArray& set();

  // Reset bit at index n to 0 (false). Throws std::out_of_range on bad index.
  BitArray& reset(int n);
  // Set all bits to 0 (false).
  BitArray& reset();

  // Returns true if there is at least one bit set to 1.
  bool any() const;
  // Returns true if all bits are 0 (or size()==0).
  bool none() const;

  // Bitwise NOT (inversion), size-preserving.
  BitArray operator~() const;

  // Count number of 1-bits. Returns 0..size().
  int count() const;

  // Read-only access to bit at index i. Throws std::out_of_range on bad index.
  bool operator[](int i) const;

  // Number of bits currently stored.
  int size() const;

  // True if size()==0.
  bool empty() const;

  // Return string representation: MSB (index size()-1) first, LSB (index 0) last.
  std::string to_string() const;
};

// Equality: sizes must match and all bits equal.
bool operator==(const BitArray & a, const BitArray & b);
bool operator!=(const BitArray & a, const BitArray & b);

// Free bitwise operators. Throw std::invalid_argument on size mismatch.
BitArray operator&(const BitArray& b1, const BitArray& b2);
BitArray operator|(const BitArray& b1, const BitArray& b2);
BitArray operator^(const BitArray& b1, const BitArray& b2);
```

---

### Методические указания

- Начните с внутреннего представления:
  - Выберите word_t (например, uint64_t). Определите константы: kWordBits = 64, маски, вычисления индексов: word = i / kWordBits, bit = i % kWordBits.
  - Храните размер (int size*) и вектор слов (std::vector<word_t> data*).
- Полезные вспомогательные функции:
  - ensure_capacity_for(size), trim_high_unused_bits() — обнулять неиспользуемые биты в последнем слове.
  - check_index(n) — бросает std::out_of_range.
  - same_size_or_throw(b) — для битовых операций.
- Реализация сдвигов:
  - Обрабатывайте отдельно сдвиг на целые слова и остаток по битам.
  - Учтите n >= size(): просто обнулите массив.

---

### Пример использования (для самопроверки)

```c++
// Размер 5, value=0b1011 инициализирует младшие биты: [1,1,0,1,0] (индексы 0..4)
BitArray a(5, 0b1011ul);
// to_string() здесь == "01011": idx4=0, idx3=1, idx2=0, idx1=1, idx0=1

a.push_back(true);      // size=6, бит с индексом 5 = 1 -> "101011"
a.set(2, true);         // установить бит #2 -> биты idx0..5 = 1,1,1,1,0,1
auto s = a.to_string(); // вернет "101111" (MSB слева, LSB справа)
auto c = a.count();     // 5
```

Проверьте эту цепочку вручную, прежде чем писать код. Если ваш `to_string()` даёт другой результат -
вы перепутали порядок битов, а не пример.

---

### Сборка и тестирование (CMake + GoogleTest)

1. Используйте шаблон курса: [oop-cpp-template](https://github.com/haiodo/oop-cpp-template).
2. Скопируйте `tasks/example` в `tasks/task2` и опишите задачу:
   ```cmake
   oop_task(NAME task2 SOURCES bit_array.cpp TESTS bit_array_test.cpp)
   ```
   Здесь `MAIN` не нужен: задача - библиотека с тестами, отдельной программы нет.
4. Сборка и тесты:
   - `cmake --preset dev`
   - `cmake --build --preset dev`
   - `ctest --preset dev`

Обязательно:

- Протестируйте все публичные методы, включая граничные случаи и исключения.
- Проверьте все ветви: несоответствие размеров, сдвиги на 0, на >= size(), resize уменьшение/увеличение, пустой массив, один элемент и т.д.
- Проверьте порядок битов в to_string() и семантику operator[].

Чек-лист для тестов:

- Конструкторы/деструктор/копирование/присваивание/swap.
- resize (расширение/сжатие, заполнение значением).
- clear, empty, size.
- set/reset по индексу и глобально.
- operator[], any, none, count, operator~.
- Сдвиги <<, >> и их присваивающие версии.
- Побитовые операции для равных размеров и исключения для разных.
- Свободные операторы ==, !=, &, |, ^.

---

## Дополнительно (для автомата)

1. Реализовать Copy-on-Write (COW) без изменения публичного интерфейса.

   - Семантика не меняется для пользователя; производительность копирования — O(1) по времени и памяти.
   - Все модифицирующие операции выполняют detach (создают уникальную копию буфера), если он разделяется:
     resize, clear, push_back, set/reset (по индексу и глобально), operator&=, operator|=, operator^=,
     operator<<=, operator>>=.
   - Копирующее присваивание `operator=(const BitArray&)` не копирует буфер, а разделяет его
     (detach произойдёт позже, при первой модификации).
   - Перемещающее присваивание `operator=(BitArray&&)` (пункт 2 ниже) забирает владение буфером
     без копирования и без detach.
   - Константные методы (включая operator[], to_string, count, any/none, операторы сдвига/битовые без присваивания) не разрывают шаринг.
   - swap не детачит и остается noexcept.
   - Сильная гарантия исключений для модифицирующих операций: при сбое состояние объекта и шаринг не меняются.
   - Потоко-безопасность не требуется (как в базовом задании). Допускается реализация через std::shared_ptr внутреннего буфера или собственный счетчик ссылок без синхронизации.
   - Сохранить асимптотику базового задания для всех операций.

   Тесты (минимум):

   - Копирование BitArray — O(1): изменение копии не влияет на оригинал; константные операции не разрывают шаринг.
   - После detachment изменяется только модифицируемый объект; оригинал бит-в-бит неизменен.
   - swap не вызывает detachment.

2. Реализовать перемещающие операции:

   ```c++
   BitArray(BitArray&& x) noexcept;
   BitArray& operator=(BitArray&& x) noexcept;
   ```

3. Итераторы:

   ```c++
   // Iterator over bits [0..size()); value_type = bool (by value)
   iterator begin();
   iterator end();
   ```

   Требования к итератору:

   - Категория - **ForwardIterator** (многопроходный: два независимых прохода по одному диапазону
     дают одинаковый результат). Термин "однопроходный" относится к `InputIterator` - это не то же самое.
   - Разыменование возвращает **значение** бита (`bool` по значению), а не прокси-ссылку.
     Следствие: `*it = true` не компилируется, и это правильно.
   - Итераторы инвалидируются любой операцией, меняющей размер (`resize`, `clear`, `push_back`),
     а при COW - ещё и любой модифицирующей операцией, вызвавшей detach.

Подсказка: для итератора храните указатель на BitArray и текущий индекс.

## Приём задачи

Общие правила - в [GRADING.md](GRADING.md).

### Автоматические проверки

```bash
cmake --preset dev      && cmake --build --preset dev      && ctest --preset dev
cmake --preset asan     && cmake --build --preset asan     && ctest --preset asan
cmake --preset tidy     && cmake --build --preset tidy
cmake --preset coverage && cmake --build --preset coverage && ctest --preset coverage
```

Покрытие кода `BitArray` - не ниже 90% строк.

Из включённых предупреждений в этой задаче основное - `-Wsign-conversion`: интерфейс работает
с `int`, а вектор слов индексируется `size_t`. Расставить приведения "чтобы замолчало"
недостаточно: на разговоре надо объяснить, какие из них безопасны и почему.

### Зачёт

- [ ] Реализованы все методы интерфейса с заданной семантикой.
- [ ] Пустой массив ведёт себя как описано в разделе уточнений: `to_string()` пустая,
      `none()` истинно, `set()`/`reset()` без аргументов допустимы, сдвиги разрешены.
- [ ] Исключения: `std::out_of_range` на неверный индекс, `std::invalid_argument`
      на отрицательный сдвиг и на несовпадение размеров.
- [ ] `resize` работает в обе стороны, новые биты заполняются значением `value`.
- [ ] Неиспользуемые старшие биты последнего слова всегда нулевые. Проверено после
      `operator~`, уменьшающего `resize`, `push_back` и сдвигов.
- [ ] `to_string()` печатает старший бит слева; результат совпадает с примером самопроверки.
- [ ] Сдвиги и битовые операции работают пословно, а не побитовым циклом.
- [ ] Тесты покрывают все публичные методы и все ветки с исключениями.
      Ожидаемые значения посчитаны вручную.
- [ ] Публичный интерфейс задокументирован на английском в стиле Doxygen:
      описана семантика, а не пересказано имя метода.
- [ ] Прокси-класс для записи через `operator[]` не добавлен.

### Автомат

Дополнительно к зачёту, полностью и с тестами (подробности в разделе "Дополнительно" выше):

- [ ] Copy-on-Write без изменения публичного интерфейса: копирование за O(1),
      константные операции не разрывают шаринг, `swap` не вызывает detach,
      сильная гарантия исключений на модифицирующих операциях.
- [ ] Перемещающие конструктор и присваивание, оба `noexcept`.
- [ ] Итераторы `begin()`/`end()` категории ForwardIterator, разыменование возвращает значение.
- [ ] Тесты на COW: изменение копии не влияет на оригинал; после detach оригинал
      бит-в-бит неизменен; `swap` не детачит.

### Разговор: живая модификация

Одна на выбор преподавателя. Реализовать, собрать без предупреждений, покрыть тестом.

1. `int find_first() const` - индекс младшего установленного бита, `-1` если единиц нет.
2. `int count(int from, int to) const` - число единиц в полуинтервале `[from, to)`.
3. `BitArray& rotate_left(int n)` - циклический сдвиг.
4. Сменить `word_t` с `uint64_t` на `uint8_t` или наоборот. Все тесты обязаны пройти
   без правок: если тесты пришлось менять, они были завязаны на внутреннее представление.
5. `BitArray slice(int from, int to) const` - подмассив как новый `BitArray`.
6. `operator<` - лексикографическое сравнение от старшего бита к младшему,
   при разных размерах короче считается меньше.

### Артефакты в REPORT.md

- Покрытие: число и ссылка на HTML-отчёт из CI.
- Выбранный `word_t` и описание внутреннего представления, 5-10 строк.
- Для автомата: как устроен подсчёт ссылок и в каких операциях происходит detach.
