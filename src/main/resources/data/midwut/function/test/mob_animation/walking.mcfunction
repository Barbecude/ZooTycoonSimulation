scoreboard players add @s timer 1
execute if score @s timer matches 20.. run scoreboard players set @s timer 0

execute if score #walking main_score matches -100..100 if score @s timer2 matches -1 run function midwut:test/mob_animation/stop_pre
#tellraw @a [{"score":{"name": "@s","objective": "timer"}},"   ",{"score":{"name": "@s","objective": "timer2"}},"   ",{"score":{"name": "#walking","objective": "main_score"}}]
execute if score @s timer = @s timer2 run function midwut:test/mob_animation/stop