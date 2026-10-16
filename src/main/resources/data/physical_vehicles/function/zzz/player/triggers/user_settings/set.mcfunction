$data modify storage midwut:main macros.full_pedal_seconds set value $(full_pedal_seconds)
$data modify storage midwut:main macros.gear_changing_cooldown set value $(gear_changing_cooldown)
$scoreboard players set @s phys_veh.placement_directions $(placement_directions)
execute store result score @s phys_veh.gear_changing_cooldown run data get storage midwut:main macros.gear_changing_cooldown 20
execute store result score #full_pedal_seconds main_score run data get storage midwut:main macros.full_pedal_seconds 100
scoreboard players set @s phys_veh.pedal_deltatime 500
scoreboard players operation @s phys_veh.pedal_deltatime /= #full_pedal_seconds main_score
