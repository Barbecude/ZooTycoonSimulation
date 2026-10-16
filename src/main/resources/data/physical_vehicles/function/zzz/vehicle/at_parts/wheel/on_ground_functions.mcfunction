execute store result score @s phys_veh.coeff_friction run function midwut:help/check/friction_coeff_irl
execute if block ~ ~ ~ #physical_vehicles:sound_material/hard run return run scoreboard players set @s phys_veh.ground_material_type 1
execute if block ~ ~ ~ #physical_vehicles:sound_material/rough run return run scoreboard players set @s phys_veh.ground_material_type 2
execute if block ~ ~ ~ #physical_vehicles:sound_material/loose run return run scoreboard players set @s phys_veh.ground_material_type 3
execute if block ~ ~ ~ #physical_vehicles:sound_material/friable run return run scoreboard players set @s phys_veh.ground_material_type 4

