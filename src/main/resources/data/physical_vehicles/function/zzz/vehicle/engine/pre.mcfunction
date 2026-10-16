execute if entity @s[tag=phys_veh.engine_broken] run return run scoreboard players set @s phys_veh.engine_w 0
execute if score @s phys_veh.engine_w matches ..3 run scoreboard players set @s phys_veh.engine_w 0
function physical_vehicles:zzz/vehicle/engine/tick