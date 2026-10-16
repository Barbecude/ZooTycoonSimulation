data modify entity @s data.components set from storage midwut:main data.data.phys_veh
data modify storage midwut:main vehicle_name set from storage midwut:main data.data.phys_veh."minecraft:custom_name"
execute if data storage midwut:main data.CustomName run data modify entity @s data.components."minecraft:custom_name" set from storage midwut:main data.CustomName
execute if data storage midwut:main data.CustomName run data modify storage midwut:main vehicle_name set from storage midwut:main data.CustomName
data modify entity @s data.components."minecraft:entity_data".Tags set from storage midwut:main data.Tags

execute if score @s phys_veh.quaternion.x = @s phys_veh.quaternion.x run return 1
execute if score @s phys_veh.quaternion.y = @s phys_veh.quaternion.y run return 1
execute if score @s phys_veh.quaternion.z = @s phys_veh.quaternion.x run return 1
execute if score @s phys_veh.quaternion.w = @s phys_veh.quaternion.x run return 1

execute store result score #placement_angle main_score run data get entity @a[tag=midwut.used.dolphin_spawn_egg.selected_player,limit=1] Rotation[0] -1000
scoreboard players add #placement_angle main_score 90000
scoreboard players operation #placement_angle main_score %= #360000 const
scoreboard players operation #placement_directions main_score = @a[tag=midwut.used.dolphin_spawn_egg.selected_player,limit=1] phys_veh.placement_directions
scoreboard players set #placement_mul main_score 360000
scoreboard players operation #placement_mul main_score /= #placement_directions main_score
scoreboard players operation #placement_add main_score = #placement_mul main_score
scoreboard players operation #placement_add main_score /= #2 const
scoreboard players operation #placement_angle main_score += #placement_add main_score
scoreboard players operation #placement_angle main_score /= #placement_mul main_score
scoreboard players operation #placement_angle main_score %= #placement_directions main_score
execute store result storage midwut:main trigonometric.in int 0.5 run scoreboard players operation #placement_angle main_score *= #placement_mul main_score
execute store result score @s phys_veh.quaternion.y run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score @s phys_veh.quaternion.w run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players set @s phys_veh.quaternion.x 0
scoreboard players set @s phys_veh.quaternion.z 0
execute if score @s phys_veh.quaternion.w matches 0 if score @s phys_veh.quaternion.y matches 0 run scoreboard players set @s phys_veh.quaternion.w 1000
scoreboard players operation @s phys_veh.last_quaternion.x = @s phys_veh.quaternion.x
scoreboard players operation @s phys_veh.last_quaternion.y = @s phys_veh.quaternion.y
scoreboard players operation @s phys_veh.last_quaternion.z = @s phys_veh.quaternion.z
scoreboard players operation @s phys_veh.last_quaternion.w = @s phys_veh.quaternion.w

#scoreboard players operation #placement_angle main_score *= #pi_4 const
#scoreboard players operation #placement_angle main_score /= #18000 const
#scoreboard players operation @s phys_veh.add_angle.y += #placement_angle main_score

