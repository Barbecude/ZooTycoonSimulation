scoreboard players operation @s phys_veh.placement_directions > #1 const
scoreboard players operation @s phys_veh.placement_directions < #32 const
scoreboard players operation @s phys_veh.gear_changing_cooldown > #0 const
scoreboard players operation @s phys_veh.gear_changing_cooldown < #400 const
execute store result storage midwut:main macros.placement_directions int 1 run scoreboard players get @s phys_veh.placement_directions
execute store result storage midwut:main macros.gear_changing_cooldown float 0.05 run scoreboard players get @s phys_veh.gear_changing_cooldown
scoreboard players set #temp main_score 500
execute store result storage midwut:main macros.full_pedal_seconds float 0.01 run scoreboard players operation #temp main_score /= @s phys_veh.pedal_deltatime
data modify storage midwut:main macros.value set value "{full_pedal_seconds:$(full_pedal_seconds),gear_changing_cooldown:$(gear_changing_cooldown),placement_directions:$(placement_directions)}"
function physical_vehicles:zzz/player/triggers/user_settings/dialog with storage midwut:main macros