#> midwut:help/random/macros

# §6 En
## §e Input data:
### scoreboard players get #random_min main_score - Minimum (including)
### scoreboard players get #random_max main_score - Maximum (including)
## §e Output data:
### The function returns a number using the /return command. This means that before running the random function, you can write§7 execute store result <...>§r and the random number will be written wherever you specify
## §e Example:
### execute store result score #random main_score run function midwut:help/random/macro

# §6 Ru
## §e Входные данные:
### scoreboard players get #random_min main_score - Минимум (включая)
### scoreboard players get #random_max main_score - Максимум (включая)
## §e Выходные данные:
### Функция возвращает число используя команду /return. Это означает, что перед запуском функцией рандома вы можете написать§7 execute store result <...>§r и рандомное число запишется туда, куда вы укажите
## §e Пример:
### execute store result score #random main_score run function midwut:help/random/macro

# 
$return run random value $(min)..$(max)
