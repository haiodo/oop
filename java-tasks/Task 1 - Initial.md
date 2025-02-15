# ФИТ НГУ, курс ООП

Задача познакомиться с инструментарием и решить пару простых задач.

## Задача 1

Реализовать консольный вариант игры "Быки и Коровы" на языке Java с вводом и выводом из консоли.

### Описание

"Быки и Коровы" — это логическая игра, в которой один игрок должен угадать задуманную последовательность другого игрока за несколько попыток. Варианты игры могут включать числа, цвета, пиктограммы или слова. После каждой попытки игрок, задумавший последовательность, выставляет оценку, указывая количество угаданных элементов без совпадения с их позициями (количество «коров») и полных совпадений (количество «быков»).

Роли участников игры не равнозначны: угадывающий анализирует сделанные попытки и полученные оценки, его роль активна. Партнёр сравнивает очередной вариант с задуманным и выставляет оценку по формальным правилам, его роль пассивна. Для уравновешивания ролей одновременно играют две встречные партии.

Первоначально игра была задумана для двух игроков, но с появлением компьютерных версий стал популярен вариант, когда игрок отгадывает число, задуманное программой, то есть играет в одиночку. В классическом варианте игра рассчитана на двух игроков. Каждый из игроков задумывает и записывает тайное 4-значное число с неповторяющимися цифрами.

Игрок, который начинает игру по жребию, делает первую попытку отгадать число. Попытка — это 4-значное число с неповторяющимися цифрами, сообщаемое противнику. Противник сообщает в ответ, сколько цифр угадано без совпадения с их позициями в тайном числе (количество коров) и сколько угадано вплоть до позиции в тайном числе (количество быков). Например:
Задумано тайное число «3219».

Попытка: «2310».

Результат: две «коровы» (две цифры: «2» и «3» — угаданы на неверных позициях) и один «бык» (одна цифра «1» угадана вплоть до позиции).

Игрок вводит комбинации одну за другой, пока не отгадает всю последовательность.

### Основные требования

1. Разделить логику игры на интерфейсы/классы для повышения гибкости кода.
2. Проект должен быть выполнен на основе maven системы сборки.
3. По результату `mvn verify` должны выполняться тесты и получаться выходной jar файл, содержащий программу.
4. Форматирование и имена всех классов и переменных должны соответствовать требованиям.
5. Требуется написание Junit тестов.

### Расширенные требования (на автомат)

1. Реализовать режим игры с ограничением по времени на каждую попытку.
2. Реализовать огнаничение на число попыток отгадывания, при исчерпании завершить игру с проигрышем.
3. Добавить возможность выбора длины последовательности (например, от 3 до 6 цифр).
4. Реализовать логирование всех действий в файл (например, game.log), чтобы можно было отслеживать ход игры и возможные ошибки.
5. Создать javadoc документацию для всех классов и методов.
6. Написать readme-файл с инструкцией по установке, запуску и игре.

### Ссылки

- Для реализации модульных тестов обычно используют готовые библиотеки:
  - TestNG (http://testng.org/).
  - JUnit (http://www.junit.org/).
- Для реализации журналирования обычно используют одну из библиотек:
- Java Logging API (http://download.oracle.com/javase/1.4.2/docs/guide/util/logging/overview.html).
- Log4j (http://logging.apache.org/log4j/1.2/).
