#execute if entity @s[tag=phys_veh.wheel.driving] run tellraw @a [{"score":{name:"#fy",objective:"main_score"}}]
scoreboard players operation #fy main_score *= #temp main_score
scoreboard players operation #fy main_score /= #100 const
#execute if entity @s[tag=phys_veh.wheel.driving] run tellraw @a [{"score":{name:"#fy",objective:"main_score"}}]