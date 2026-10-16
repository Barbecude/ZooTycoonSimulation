scoreboard players remove #distance main_score 1
particle dust{color:[0f,1f,0f],scale:0.5f} ~ ~ ~ 0 0 0 0 1 normal

execute unless block ~ ~ ~ air align xyz run tp ~ ~ ~
execute unless block ~ ~ ~ air align xyz run function midwut:test/beam-plane/marker/end


execute if score #distance main_score matches 0 run kill
execute if score #distance main_score matches 1.. positioned ^ ^ ^0.1 run function midwut:test/beam-plane/marker/raycast

