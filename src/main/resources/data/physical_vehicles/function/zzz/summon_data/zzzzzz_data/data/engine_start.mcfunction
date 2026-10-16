tag @s add phys_veh.ignition
tag @s add phys_veh.has_key

scoreboard players operation @s phys_veh.engine_w = @s phys_veh.engine_idle_w
execute store result score @s phys_veh.engine_rpm run data get storage midwut:main Data.data.phys_veh.engine.idle_rpm 1