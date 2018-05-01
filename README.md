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
        tab(item("Полосатые"))
        tab(item("Синие"))
        tab(item("Красные"))
        tab(item("Зелёные"))
        tab(item("В сердечко"))
    item("Носки, " + (дни + 1) + " пар")
        tab(item("Чёрные"))
        tab(item("Серые"))
        tab(item("Белые"))

par градусы
header("Верхняя одежда")
    if (градусы < 0) enum("Теплая куртка")
    if (градусы < 10) enum("Свитер")
    enum("Легкая куртка")

header("Документы")
    enum("Паспорт")
    enum("Страховка")
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
fun tab(name) = "    " + name
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
    * Полосатые
    * Синие
    * Красные
    * Зелёные
    * В сердечко
* Носки, 12 пар
    * Чёрные
    * Серые
    * Белые
## Верхняя одежда
1. Свитер
1. Легкая куртка
## Документы
1. Паспорт
1. Страховка

```
