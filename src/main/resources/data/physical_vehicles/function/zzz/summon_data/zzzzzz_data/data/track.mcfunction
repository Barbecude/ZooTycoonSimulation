execute if data storage midwut:main Data2.data.phys_veh.wheel_axle_id store result score @s phys_veh.id.wheel_axle run data get storage midwut:main Data2.data.phys_veh.wheel_axle_id 1
execute unless score @s phys_veh.id.wheel_axle = @s phys_veh.id.wheel_axle run scoreboard players set @s phys_veh.id.wheel_axle -1
execute store result score @s phys_veh.radius run data get storage midwut:main Data2.data.phys_veh.radius 1000
execute store result score @s phys_veh.max_delta run data get storage midwut:main Data2.data.phys_veh.delta 1000
execute store result score @s phys_veh.max_delta_neg run data get storage midwut:main Data2.data.phys_veh.delta -1000
execute store result score @s phys_veh.gear_num_max run data get storage midwut:main Data2.data.phys_veh.max_frame 1
execute store result score @s phys_veh.track_repair_time run data get storage midwut:main Data2.data.phys_veh.repair_time 100
scoreboard players operation @s phys_veh.track_repair_time *= #tick_rate main_score
scoreboard players operation @s phys_veh.track_repair_time /= #100 const
scoreboard players add @s phys_veh.gear_num_max 1
scoreboard players set @s phys_veh.gear_num 0
execute if data storage midwut:main Data2.data.phys_veh.init_frame store result score @s phys_veh.gear_num run data get storage midwut:main Data2.data.phys_veh.init_frame 1
execute store result storage midwut:main temp int 1 run scoreboard players get @s phys_veh.gear_num
data modify entity @s item.components."minecraft:custom_model_data".floats append from storage midwut:main temp

