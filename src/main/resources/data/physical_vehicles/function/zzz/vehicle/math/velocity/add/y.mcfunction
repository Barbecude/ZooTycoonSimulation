function physical_vehicles:zzz/vehicle/math/velocity/decrease/y
execute if score @s phys_veh.velocity.y matches -39..39 run return run scoreboard players set @s phys_veh.velocity.y 0

scoreboard players operation #temp main_score = @s phys_veh.velocity.y
scoreboard players operation #temp main_score /= #tick_rate main_score
execute store result storage midwut:main Data.Pos[1] double 0.001 store result score @s phys_veh.pos.y run scoreboard players operation #main.pos.y main_score += #temp main_score
