data modify storage midwut:main Data set from entity @s
scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
tag @s add phys_veh.selected_part
function physical_vehicles:zzz/vehicle/other/tag_selected_part_add with storage midwut:main Data.data.phys_veh

execute if entity @s[tag=phys_veh.debug.particles] run function physical_vehicles:zzz/vehicle/collision/mass_center
function physical_vehicles:zzz/vehicle/math/quaternions/calcs
function physical_vehicles:zzz/vehicle/math/moment_of_inertia

execute store result score #main.pos.x main_score run data get storage midwut:main Data.Pos[0] 1000
execute store result score #main.pos.y main_score run data get storage midwut:main Data.Pos[1] 1000
execute store result score #main.pos.z main_score run data get storage midwut:main Data.Pos[2] 1000

execute rotated as @s as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=physical_vehicle,tag=!phys_veh.main,tag=phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/vehicle/at_parts/main

#execute if entity @s[tag=phys_veh.have_collision] run function physical_vehicles:zzz/kinetic/collision/pre

execute unless entity @s[scores={phys_veh.spin.x=0,phys_veh.spin.y=0,phys_veh.spin.z=0}] run function physical_vehicles:zzz/vehicle/math/spin/main
execute unless entity @s[scores={phys_veh.velocity.x=0,phys_veh.velocity.y=0,phys_veh.velocity.z=0}] run function physical_vehicles:zzz/vehicle/math/velocity/main

tag @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.selected_part,distance=..96] remove phys_veh.selected_part