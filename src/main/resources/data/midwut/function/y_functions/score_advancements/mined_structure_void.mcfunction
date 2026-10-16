advancement revoke @s only midwut:scores/mined_structure_void

tag @s add midwut.mined.structure_void.selected_player
function #midwut:mined/structure_void
tag @s remove midwut.mined.structure_void.selected_player

scoreboard players reset @s midwut.mined.structure_void
