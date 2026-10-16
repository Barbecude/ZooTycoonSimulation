advancement revoke @s only midwut:scores/dropped_poisonous_potato

tag @s add midwut.dropped.poisonous_potato.selected_player
function #midwut:dropped/poisonous_potato
tag @s remove midwut.dropped.poisonous_potato.selected_player

scoreboard players reset @s midwut.dropped.poisonous_potato
