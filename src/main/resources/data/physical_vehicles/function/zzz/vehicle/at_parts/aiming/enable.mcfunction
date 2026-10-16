tag @s add phys_veh.is_aiming
scoreboard players operation #x main_score = @s phys_veh.aiming_offset.x
scoreboard players operation #y main_score = @s phys_veh.aiming_offset.y
scoreboard players operation #z main_score = @s phys_veh.aiming_offset.z
data modify storage midwut:main macros.value set from entity @s data.phys_veh.aiming.reducing_fov
execute as @e[type=minecraft:text_display,tag=phys_veh.seat,tag=phys_veh.selected_part,limit=1,predicate=physical_vehicles:same_scores/object_id] run function physical_vehicles:zzz/vehicle/at_parts/aiming/enable_pos
function physical_vehicles:zzz/vehicle/at_parts/aiming/build_text