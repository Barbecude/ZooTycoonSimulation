tag @s add phys_veh.collision_in_radius
data modify storage midwut:main temp set from entity @s Pos
execute store result score @s phys_veh.pos.x run data get storage midwut:main temp[0] 1000
execute store result score @s phys_veh.pos.y run data get storage midwut:main temp[1] 1000
execute store result score @s phys_veh.pos.z run data get storage midwut:main temp[2] 1000