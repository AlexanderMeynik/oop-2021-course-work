# Моделирование работы порта

## Описание проекта

В рамках  проекта предлагалось создать модель обслуживания потока заявок на разгрузку,
поступающих от грузовых судов, прибывающих в морской порт.
Изначально модель должна состоять из трёх сервисов:
1. Генерирует расписание судов для порта
2. Получает данные из сервиса 1 и сервиса 3 и сохраняет их в json-файл.
3. В соответствие с расписанием из с.1. производит симуляцию осблуживания всех кораблей в порту и выводит результаты.


В ходе реализации сервисов было принято решение объеденить 1 и 2 сервисы в 1
ввиду того, что данные на них всё равно поступают в виде json.
Основные работы производились с марта по начало мая 2021 года.

## Стек технологий
1. Java 11
2. apache maven
2. spring boot
2. h2 database
3. google json

## Описание провдимых мероприятий

1. Были закреплены основе создания REST API на базе spring.
2. Для увеличения скорости моделирования краны для рзагрузки судов запускались в параллельных потоках.
2. Были применены примитивы синхронизации в многопоточных приложения.
3. Были получены навыки работы с системой apache maven.
4. Создана архитектура на базе http микросервисов для запроса необходимых данных.
