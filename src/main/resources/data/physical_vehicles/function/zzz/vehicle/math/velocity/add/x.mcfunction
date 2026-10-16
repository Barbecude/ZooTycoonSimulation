function physical_vehicles:zzz/vehicle/math/velocity/decrease/x
execute if score @s phys_veh.velocity.x matches -39..39 run return run scoreboard players set @s phys_veh.velocity.x 0

scoreboard players operation #temp main_score = @s phys_veh.velocity.x
scoreboard players operation #temp main_score /= #tick_rate main_score
execute store result storage midwut:main Data.Pos[0] double 0.001 store result score @s phys_veh.pos.x run scoreboard players operation #main.pos.x main_score += #temp main_score
