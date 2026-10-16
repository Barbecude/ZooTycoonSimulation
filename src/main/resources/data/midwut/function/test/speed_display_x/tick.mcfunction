#> midwut:test/speed_display_x/tick
execute store result score #speed main_score run data get entity @n[type=minecraft:player] Pos[0] 10
execute store result score #motion main_score run data get entity @n[type=minecraft:player] Motion[0] 10
scoreboard players operation #temp main_score = #speed main_score
scoreboard players operation #speed main_score -= #speed_save main_score
scoreboard players operation #speed_save main_score = #temp main_score 

#tellraw @p [{"score":{"name":"#temp",objective:"main_score"}}]
tellraw @p ["bpt: ",{"score":{"name":"#speed",objective:"main_score"}}," m: ",{"score":{"name":"#motion",objective:"main_score"}}]