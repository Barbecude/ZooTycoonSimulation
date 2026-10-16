scoreboard players operation #temp main_score = @s phys_veh.spin.y
$scoreboard players operation #temp main_score *= #phys_veh.input$(id).$(type) main_score
scoreboard players operation #temp main_score /= #100 const
scoreboard players operation @s phys_veh.add_angle.y += #temp main_score
scoreboard players operation @s phys_veh.add_angle.y %= #pi2_4 const
scoreboard players operation #temp main_score = @s phys_veh.add_angle.y
execute if score #temp main_score matches 31416.. run scoreboard players remove #temp main_score 62831
execute if score #temp main_score > @s phys_veh.add_spin.y run scoreboard players operation @s phys_veh.add_angle.y = @s phys_veh.add_spin.y