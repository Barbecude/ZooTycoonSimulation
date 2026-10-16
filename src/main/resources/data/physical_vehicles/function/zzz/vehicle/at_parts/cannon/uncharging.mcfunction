execute store result storage midwut:main macros.time int 1 run scoreboard players add @s phys_veh.normalize_timer 1
scoreboard players operation #object_id main_score = @s phys_veh.id.storage
execute unless score @s phys_veh.sound_timer.cannon_reload matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/cannon/reload_sounds/main with storage midwut:main macros
execute if score @s phys_veh.normalize_timer >= @s phys_veh.charge_time run function physical_vehicles:zzz/vehicle/at_parts/cannon/uncharge
function physical_vehicles:zzz/vehicle/at_parts/cannon/actionbar_uncharge