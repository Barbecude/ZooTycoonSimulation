$execute store result score @s phys_veh.offset.x run data get storage midwut:main temp.equipment.head.components.minecraft:custom_data.phys_veh.offsets[{id:$(temp)}].offset[0] 1000
$execute store result score @s phys_veh.offset.y run data get storage midwut:main temp.equipment.head.components.minecraft:custom_data.phys_veh.offsets[{id:$(temp)}].offset[1] 1000
$execute store result score @s phys_veh.offset.z run data get storage midwut:main temp.equipment.head.components.minecraft:custom_data.phys_veh.offsets[{id:$(temp)}].offset[2] 1000

tag @s add phys_veh.position_changed

