scoreboard players operation #ray main_score = @s phys_veh.velocity.z
execute if score #ray main_score matches 1.. run function physical_vehicles:zzz/shell/ray48
execute if score #ray main_score matches 1.. positioned ^ ^ ^48 run function physical_vehicles:zzz/shell/ray48
execute store result score #x main_score run data get entity @s Pos[0]
execute store result score #y main_score run data get entity @s Pos[1]
execute store result score #z main_score run data get entity @s Pos[2]
execute if function physical_vehicles:zzz/shell/in_pos_limits run return run kill

execute store result score #temp main_score run data get entity @s Rotation[1] 1000
execute if score @s phys_veh.velocity.z matches 1.. run function physical_vehicles:zzz/shell/gravity

execute store result storage midwut:main trigonometric.in int 1 run scoreboard players operation #temp main_score %= #360000 const
execute store result score #gravity main_score run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
function physical_vehicles:zzz/shell/gravity_ch
execute if score @s phys_veh.gravity.y matches 100.. run function physical_vehicles:zzz/shell/gravity_loop
execute if score @s phys_veh.gravity.y matches ..-100 run function physical_vehicles:zzz/shell/gravity_neg_loop

execute if entity @s[tag=phys_veh.shell.hit] run return run kill @s
execute if score #gametime main_score > @s phys_veh.afk_timer run return run kill @s

scoreboard players operation #vel main_score = @s phys_veh.velocity.z
scoreboard players operation #vel main_score *= #vel main_score
scoreboard players operation #vel main_score *= @s phys_veh.drag_force_magnitude
scoreboard players operation #vel main_score /= #10000000 const
scoreboard players operation @s phys_veh.velocity.z -= #vel main_score
execute if score @s phys_veh.velocity.z matches ..-1 run function physical_vehicles:zzz/shell/neg_vel
