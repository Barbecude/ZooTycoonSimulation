scoreboard players operation #temp main_score = @s phys_veh.spin.x
$scoreboard players operation #temp main_score *= #phys_veh.input$(id).$(type) main_score
scoreboard players operation #temp main_score /= #100 const
scoreboard players operation @s phys_veh.add_angle.x -= #temp main_score
scoreboard players operation @s phys_veh.add_angle.x %= #pi2_4 const
scoreboard players operation #temp main_score = @s phys_veh.add_angle.x
execute if score #temp main_score matches 31416.. run scoreboard players remove #temp main_score 62831
execute if score #temp main_score < @s phys_veh.coll_spin.x run scoreboard players operation @s phys_veh.add_angle.x = @s phys_veh.coll_spin.x