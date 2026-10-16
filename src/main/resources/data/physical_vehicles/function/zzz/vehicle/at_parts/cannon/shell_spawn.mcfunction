tag @s add phys_veh.shell

execute store result score @s phys_veh.afk_timer run time query gametime
scoreboard players add @s phys_veh.afk_timer 1200
scoreboard players operation @s phys_veh.id.object = #player_id main_score
scoreboard players operation @s phys_veh.id.vehicle = #vehicle_id main_score

function physical_vehicles:zzz/vehicle/math/quaternions/macro_facing_pre

#execute rotated as @s positioned ^0.0 ^ ^60 facing ^ ^ ^-1 run tp @s ~ ~ ~ ~ ~
data modify entity @s data.phys_veh.shell set from storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh
execute if data storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh{tracer:true} run tag @s add phys_veh.shell.have_tracer
execute if data storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh{explode_on_impact:true} run tag @s add phys_veh.shell.explode_on_impact
execute if data storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh{explode_on_entity_impact:true} run tag @s add phys_veh.shell.explode_on_entity_impact
execute if data storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh.hit_sound run tag @s add phys_veh.shell.hit_sound

scoreboard players operation @s phys_veh.mass = #mass main_score
scoreboard players operation @s phys_veh.velocity.z = #proj_velocity main_score
#scoreboard players set @s phys_veh.velocity.z 200
scoreboard players operation @s phys_veh.velocity.z /= #tick_rate main_score

execute store result score @s phys_veh.drag_force_magnitude run data get storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh.drag_coefficient 1000
#scoreboard players set @s phys_veh.drag_force_magnitude 720
#tellraw @a [{score:{name:"@s",objective:"phys_veh.drag_force_magnitude"}}]
scoreboard players operation @s phys_veh.drag_force_magnitude *= #phys_veh.air_density main_score
scoreboard players operation @s phys_veh.drag_force_magnitude /= #1000 const
execute store result score #area main_score run data get storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh.caliber 5
execute store result score @s phys_veh.radius run data get storage midwut:main cannon.item.components."minecraft:custom_data".phys_veh.caliber 10
scoreboard players operation #area main_score *= #area main_score
scoreboard players operation #area main_score /= #10000 const
scoreboard players operation #area main_score *= #pi_4 const
scoreboard players operation #area main_score /= #1000 const
#tellraw @a [{score:{name:"#area",objective:"main_score"}}]
scoreboard players operation @s phys_veh.drag_force_magnitude *= #area main_score
scoreboard players operation @s phys_veh.drag_force_magnitude /= #2000 const
#tellraw @a [{score:{name:"@s",objective:"phys_veh.drag_force_magnitude"}}]

#execute store result score @s phys_veh.pos.z run data get entity @s Pos[2] 100

schedule function physical_vehicles:zzz/shell/tick 1t replace