#> midwut:help/check/light_level

# §6 En
## Returns the light level to the scoreboard.
## §e Input Data:
### Function execution position
## §e Output Data:
### The function returns the number of light level (minimum 0, maximum 15)

# §6 Ru
## Возвращает в скорборд уровень света.
## §e Входные данные:
### Позиция исполнения функции
## §e Выходные данные:
### Функция возвращает число, уровень света (минимально 0, максимально 15)

# 
execute if predicate midwut:check/light_level/15 run return 15
execute if predicate midwut:check/light_level/14_15 run return 14
execute if predicate midwut:check/light_level/13_15 run return 13
execute if predicate midwut:check/light_level/12_15 run return 12
execute if predicate midwut:check/light_level/11_15 run return 11
execute if predicate midwut:check/light_level/10_15 run return 10
execute if predicate midwut:check/light_level/9_15 run return 9
execute if predicate midwut:check/light_level/8_15 run return 8
execute if predicate midwut:check/light_level/7_15 run return 7
execute if predicate midwut:check/light_level/6_15 run return 6
execute if predicate midwut:check/light_level/5_15 run return 5
execute if predicate midwut:check/light_level/4_15 run return 4
execute if predicate midwut:check/light_level/3_15 run return 3
execute if predicate midwut:check/light_level/2_15 run return 2
execute if predicate midwut:check/light_level/1_15 run return 1
return 0