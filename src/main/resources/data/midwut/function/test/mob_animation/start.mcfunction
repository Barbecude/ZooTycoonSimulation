tag @s add walking
scoreboard players set @s timer 0
scoreboard players set @s timer2 -1

## (([/time query gametime] + 1) % total duration)

execute store result score #time main_score run time query gametime
scoreboard players add #time main_score 1
data modify entity @s item.tag.CustomModelData set value 1
execute store result entity @s item.tag.CustomPotionColor int 1 run scoreboard players operation #time main_score %= #20 const


