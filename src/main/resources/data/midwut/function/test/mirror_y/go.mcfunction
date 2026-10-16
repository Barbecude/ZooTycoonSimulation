#function midwut:test/mirror_y/go

$scoreboard players set #x main_score $(x)
$scoreboard players set #y main_score $(y)
$scoreboard players set #z main_score $(z)
scoreboard players operation #y2 main_score = #y main_score

#scoreboard players remove #x main_score 1
#scoreboard players remove #y main_score 1
#scoreboard players remove #z main_score 1

scoreboard players operation #y2 main_score += #y main_score
#scoreboard players remove #y2 main_score 1

scoreboard players operation #ty main_score = #y main_score
function midwut:test/mirror_y/y
