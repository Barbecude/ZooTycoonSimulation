execute store result storage midwut:main macros.time int -1 run scoreboard players remove @s phys_veh.normalize_timer 1
execute unless score @s phys_veh.sound_timer.cannon_reload matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/cannon/reload_sounds/main with storage midwut:main macros
function physical_vehicles:zzz/vehicle/at_parts/cannon/actionbar_charge
