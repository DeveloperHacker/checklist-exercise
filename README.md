# checklist-exercise

Build:  
```mvn package```

Usage:
* Linux ```./checklist <storage> <checklist>```
* Other ```java -jar target/checklist-jar-with-dependencies.jar <storage> <checklist>```

Example:
###### markup.cl
```
title("Список в поездку")

fun переходник(страна) = "Переходник на " + страна + " розетки"

par страна
if (страна == "США") {
    item(переходник("американские"))
    item("Доллары")
    item("Долларавая карточка")
} else if (страна == "Великобритания") {
    item(переходник("британские"))
    item("Фунты")
    item("Любая карточка")
}

par дни
header("Нижняя одежда")
    item("Трусы, " + (дни + 1) + " пар")
    item("Носки, " + (дни + 1) + " пар")

par градусы
header("Верхняя одежда")
    if (градусы < 0) item("Теплая куртка")
    if (градусы < 10) item("Свитер")
    item("Легкая куртка")

header("Документы")
    item("Паспорт")
    item("Страховка")
```
###### config.cl
```
par страна = "США"
par дни = 11
par градусы = 3
```
###### stdlib.cl
```
fun title(name) = header(1, name)
fun header(name) = header(2, name)
fun header(type, name) = "#" * type + " " + name
fun item(name) = "* " + name
fun enum(name) = "1. " + name
```
Run checklist example ```./example/do >> output.md```
###### output.md
```
# Список в поездку
* Переходник на американские розетки
* Доллары
* Долларавая карточка
## Нижняя одежда
* Трусы, 12 пар
* Носки, 12 пар
## Верхняя одежда
* Свитер
* Легкая куртка
## Документы
* Паспорт
* Страховка
```
