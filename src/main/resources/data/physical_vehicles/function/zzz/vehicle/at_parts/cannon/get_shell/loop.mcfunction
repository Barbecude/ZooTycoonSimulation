execute store result score #caliber main_score run data get storage midwut:main Items[0].components."minecraft:custom_data".phys_veh.caliber 5.625
execute if score #caliber main_score = @s phys_veh.radius run return run function physical_vehicles:zzz/vehicle/at_parts/cannon/get_shell/get

data remove storage midwut:main Items[0]
execute if data storage midwut:main Items[0] run function physical_vehicles:zzz/vehicle/at_parts/cannon/get_shell/loop