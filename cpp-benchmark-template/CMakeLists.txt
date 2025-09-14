cmake_minimum_required(VERSION 3.14)

# задаем используемый стандарт С++
set(CMAKE_CXX_STANDARD 17)

# TODO: переименовать проект
project(benchmark-cpp-template)

# задаем имя исполняемого файла основной программы
set(exe_name ${PROJECT_NAME})

# задаем имя исполняемого файла программы для запуска тестов
set(test_name test)

# Включаем отслеживание всех предупреждений (warnings) и запрещаем компиляцию при наличии предупреждений.
# Считается хорошим тоном, когда программа собирается без предупреждений.
# Но вы можете убрать /WX и -Werror (разрешив компиляцию с предупреждениями), если сможете разумно объяснить своё решение.
if (MSVC)
  # warning level 4 and all warnings as errors
  add_compile_options(/W4 /WX)
else()
  # lots of warnings and all warnings as errors
  add_compile_options(-Wall -Wextra -pedantic -Werror)
endif()


# описываем исполняемый файл основной программы: это main.cpp + основная библиотека
add_executable(${exe_name} main.cpp)

# ========== все, что ниже, относится к тестированию при помощи Google Benchmark и Google Test ==========

# скачиваем библиотеку Google Benchmark
include(FetchContent)

# Disable Google Benchmark's internal Google Test to avoid conflicts
set(BENCHMARK_ENABLE_TESTING OFF CACHE BOOL "" FORCE)
set(BENCHMARK_ENABLE_GTEST_TESTS OFF CACHE BOOL "" FORCE)

FetchContent_Declare(
  benchmark
  # Specify the commit you depend on and update it regularly.
  URL https://github.com/google/benchmark/archive/refs/tags/v1.9.4.zip
)
# For Windows: Prevent overriding the parent project's compiler/linker settings
set(benchmark_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(benchmark)

# описываем исполняемый файл программы для запуска тестов: это cpp-файлы тестов + основная библиотека + библиотека benchmark
# наш собственный main.cpp здесь не нужен, т.к. функция main реализована в benchmark
add_executable(${test_name} benchmark_main.cpp)
target_link_libraries(${test_name} benchmark::benchmark)
