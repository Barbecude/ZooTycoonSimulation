#> midwut:help/check/sky

# §6 En
## §c To use this function, you need to have the "function midwut:marker" command at the very beginning of your tick function
## §e Input Data:
### Function execution position
## §e Output Data:
### The function returns 1 (true) if the block is found, otherwise 0 (false)

# §6 Ru
## §c Для использования этой функции вам нужно иметь команду "function midwut:marker" в самом начале вашей tick функции
## §e Входные данные:
### Позиция исполнения функции
## §e Выходные данные:
### Функция возвращает 1 (правда), если найден блок, иначе 0 (ложь)

# 

execute positioned over motion_blocking positioned ~-0.0001 ~-0.0001 ~-0.0001 run return run execute if entity @s[dy=2000]

#tp 0000051d-0000-0fb7-0000-001500000014 ~ ~ ~
#return run execute as 0000051d-0000-0fb7-0000-001500000014 run function midwut:y_functions/check/sky






