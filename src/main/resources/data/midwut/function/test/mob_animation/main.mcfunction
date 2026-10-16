data modify storage midwut:main Motion set from entity @e[limit=1,tag=body] Motion
execute store result score #walking main_score run data get storage midwut:main Motion[0] 10000
execute store result score #test main_score run data get storage midwut:main Motion[2] 10000
execute if score #walking main_score matches ..-1 run scoreboard players operation #walking main_score *= #-1 const
execute if score #test main_score matches ..-1 run scoreboard players operation #test main_score *= #-1 const
scoreboard players operation #walking main_score > #test main_score

execute if entity @s[tag=walking] run function midwut:test/mob_animation/walking
execute unless score #walking main_score matches -100..100 if entity @s[tag=!walking] run function midwut:test/mob_animation/start
#function midwut:test/mob_animation/start

execute at @e[tag=body,limit=1] run tp @s ~ ~0.5 ~ ~ 0
execute unless entity @e[tag=body,limit=1] run kill