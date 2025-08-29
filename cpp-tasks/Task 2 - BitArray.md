# ФИТ НГУ, курс ООП

## Задача 2а. Битовый массив (BitArray)

### Что нужно сделать

- Реализовать класс BitArray с заданным интерфейсом (см. ниже).
- Кратко задокументировать все публичные члены класса на техническом английском (в стиле Doxygen/комментариев к методам).
- Написать юнит‑тесты на все публичные методы с использованием GoogleTest. Достичь покрытия кода: каждая строка должна выполниться хотя бы одним тестом.

Полезные ссылки:

- Что такое битовый массив: [Bit array - Wikipedia](https://en.wikipedia.org/wiki/Bit_array)
- Пример промышленной реализации: [boost::dynamic_bitset](https://www.boost.org/doc/libs/1_51_0/libs/dynamic_bitset/dynamic_bitset.html)

---

### Важные уточнения (обязательно к соблюдению)

- Хранение:
  - Используйте компактное хранение на базе беззнаковых целых (например, std::vector<uint64_t> или uint32_t). Размер слова — на ваше усмотрение, но он должен быть документирован.
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
  - Неверный индекс (отрицательный или >= size()) в operator[], set, reset — std::out_of_range.
  - Отрицательные аргументы сдвига — std::invalid_argument.
  - resize с num_bits < 0 — std::invalid_argument.
- Поведение resize:
  - При увеличении длины новые биты инициализируются значением value.
  - При уменьшении длины биты с индексами >= нового размера отбрасываются.
- Сложность:
  - operator[], set, reset, push_back — амортизированно O(1).
  - Битовые операции и сдвиги — O(size / word_bits).
- Потокобезопасность:
  - Класс не является потокобезопасным.

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
  explicit BitArray(int num_bits, unsigned long value = 0);
  BitArray(const BitArray& b);

  // Swap contents with another BitArray. No-throw guarantee.
  void swap(BitArray& b);

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
a.push_back(true);      // size=6, бит с индексом 5 = 1
a.set(2, true);         // установить бит #2
auto s = a.to_string(); // вернет "101011" (MSB слева, LSB справа)
auto c = a.count();     // количество единиц
```

---

### Сборка и тестирование (CMake + GoogleTest)

1. Используйте шаблон проекта из папки cpp-template в репозитории.
2. Добавьте исходники BitArray в основной таргет.
3. Добавьте тесты в отдельный таргет с GoogleTest.
4. Пример команд:
   - cmake -S . -B build -DCMAKE_BUILD_TYPE=Debug
   - cmake --build build
   - ctest --test-dir build --output-on-failure

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
     resize, clear, push_back, set/reset (по индексу и глобально), operator&=, operator|=, operator^=, operator<<=, operator>>=, operator= (при присваивании из временного может принять владение без копии).
   - Константные методы (включая operator[], to_string, count, any/none, операторы сдвига/битовые без присваивания) не разрывают шаринг.
   - swap не детачит и остается noexcept.
   - Сильная гарантия исключений для модифицирующих операций: при сбое состояние объекта и шаринг не меняются.
   - Потокобезопасность не требуется (как в базовом задании). Допускается реализация через std::shared_ptr внутреннего буфера или собственный счетчик ссылок без синхронизации.
   - Сохранить асимптотику базового задания для всех операций.

   Подсказка по реализации:

   - Внутренний буфер: size_bits и vector<word_t> data.
   - Хранить shared_ptr на буфер; перед изменением вызывать ensure_unique().

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

   - Однопроходный (ForwardIterator достаточно).
   - Разыменование возвращает значение бита (не прокси).
   - Инвалидация при изменении размера.

Подсказка: для итератора храните указатель на BitArray и текущий индекс.

---
