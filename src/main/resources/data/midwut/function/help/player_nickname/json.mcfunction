#> midwut:help/player_nickname/get

# §6 En
## §e Input data:
### The player from whom the function is executed
## §e Output data:
### data get storage midwut:main PlayerName.json - (in json text format)

# §6 Ru
## §e Входные данные:
### Игрок, от которого исполняется функция
## §e Выходные данные:
### data get storage midwut:main PlayerName.json - (в json text формате)

# 
execute if entity @s[type=player] in midwut:main run function midwut:y_functions/player_nickname/json

