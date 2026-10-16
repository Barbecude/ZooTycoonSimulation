scoreboard players remove @s phys_veh.gear_num 1
scoreboard players operation @s phys_veh.velocity.z += @s phys_veh.max_delta
execute if score @s phys_veh.velocity.z <= @s phys_veh.max_delta_neg run function physical_vehicles:zzz/vehicle/at_parts/track/vel_norm_inv
scoreboard players operation @s phys_veh.gear_num %= @s phys_veh.gear_num_max
# item modify entity @s contents {function:"minecraft:set_custom_model_data",floats:{mode:"replace_section",offset:0,size:1,values:[{type:"minecraft:score",target:{type:"minecraft:context",target:"this"},score:"phys_veh.gear_num"}]}}
