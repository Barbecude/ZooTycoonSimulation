function physical_vehicles:zzz/vehicle/math/velocity/decrease/z
execute if score @s phys_veh.velocity.z matches -39..39 run return run scoreboard players set @s phys_veh.velocity.z 0

scoreboard players operation #temp main_score = @s phys_veh.velocity.z
scoreboard players operation #temp main_score /= #tick_rate main_score
execute store result storage midwut:main Data.Pos[2] double 0.001 store result score @s phys_veh.pos.z run scoreboard players operation #main.pos.z main_score += #temp main_score
