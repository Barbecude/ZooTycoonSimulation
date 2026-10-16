#> midwut:help/check/air_only_collision

# §6 En
## This function checks for the presence of air/water/lava at the execution position of the function, considering only the collision
## §c To use this function, you need to have the "function midwut:marker" command at the very beginning of your tick function
## §e Input Data:
### function execution position
## §e Output Data:
### /scoreboard players get #check.block main_score — (0 — if a solid block or a block collision is found, 1 — if air is found, 2 — if water is found (for example, stairs filled with water), 3 — if lava is found (for example, a lava cauldron), 4 — if snow is found (for example, the snow levels of the cauldron))

# §6 Ru
## Эта функция проверяет наличие воздуха/воды/лавы на позиции исполнения функции, учитывая только коллизию
## §c Для использования этой функции вам нужно иметь команду "function midwut:marker" в самом начале вашей tick функции
## §e Входные данные:
### Позиция исполнения функции
## §e Выходные данные:
### /scoreboard players get #check.block main_score — (0 — если найден целый блок или коллизия блока, 1 — если найден воздух, 2 — если найдена вода (например ступеньки залитые водой), 3 — если найдена лава (например котёл с лавой), 4 — если найден снег (например уровни снега котла))

# 
execute unless loaded ~ ~ ~ run return 0
execute if block ~ ~ ~ #midwut:run/snow run return 4
execute if block ~ ~ ~ #midwut:run unless block ~ ~ ~ #midwut:pixels/all run return run function midwut:y_functions/air_at_run_function/no_pixels
execute unless block ~ ~ ~ #midwut:all/1.21.11 run return 0
execute unless function midwut:y_functions/air_at_run_function/void_check run return 5
execute unless block ~ ~ ~ #midwut:pixels/all run return 0
tp 0000051d-0000-0fb7-0000-001500000014 ~ ~ ~
execute as 0000051d-0000-0fb7-0000-001500000014 run return run function midwut:y_functions/air_at_run_function/main_only_collision
execute if score #check.block main_score matches 1 if block ~ ~ ~ #midwut:pixels/all[waterlogged=true] run return run scoreboard players set #check.block main_score 2
return run scoreboard players get #check.block main_score
