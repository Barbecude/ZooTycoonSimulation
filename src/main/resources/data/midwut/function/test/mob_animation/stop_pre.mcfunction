
execute store result score #test main_score if score @s timer matches 11..
execute if score @s timer matches 11.. run scoreboard players remove @s timer 10

scoreboard players set @s timer2 10
execute if score #test main_score matches 1 run scoreboard players set @s timer2 0

scoreboard players set #test2 main_score 11
execute if score @s timer matches 1..4 store result score @s timer run scoreboard players operation #test2 main_score -= @s timer

execute if score #test main_score matches 1 run scoreboard players add @s timer 10

## (((([/time query gametime] % 24000) - starting frame) + 1) % total duration)

execute store result score #time main_score run time query gametime
scoreboard players operation #time main_score %= #24000 const
scoreboard players operation #time main_score -= @s timer
scoreboard players add #time main_score 1
execute store result entity @s item.tag.CustomPotionColor int 1 run scoreboard players operation #time main_score %= #20 const


