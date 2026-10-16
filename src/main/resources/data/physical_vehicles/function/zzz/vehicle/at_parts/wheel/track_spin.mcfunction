#tellraw @a [{score:{name:"#fy_w",objective:"main_score"}}]
scoreboard players add @s phys_veh.count_wheels 1
scoreboard players operation @s phys_veh.add_spin.y += #fy_w main_score
scoreboard players operation @s phys_veh.add_spin.x += #radius main_score