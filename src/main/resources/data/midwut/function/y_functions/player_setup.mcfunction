#> midwut:y_functions/player_id
## function midwut:y_functions/player_id

execute store result score @s midwut.player_id run scoreboard players add #midwut.player_id midwut.player_id 1
advancement revoke @s only midwut:performed/player_setup

data modify storage midwut:main UUID set from entity @s UUID
execute store result score @s midwut.uuid.0 run data get storage midwut:main UUID[0]
execute store result score @s midwut.uuid.1 run data get storage midwut:main UUID[1]
execute store result score @s midwut.uuid.2 run data get storage midwut:main UUID[2]
execute store result score @s midwut.uuid.3 run data get storage midwut:main UUID[3]

tag @s add midwut.player_setup.selected_player
function #midwut:player_setup
tag @s remove midwut.player_setup.selected_player
