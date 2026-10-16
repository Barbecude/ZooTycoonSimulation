$data modify storage midwut:main temp set value $(t)
execute store result score @s phys_veh.quaternion.x run data get storage midwut:main temp[0] 10000
execute store result score @s phys_veh.quaternion.y run data get storage midwut:main temp[1] 10000
execute store result score @s phys_veh.quaternion.z run data get storage midwut:main temp[2] 10000
execute store result score @s phys_veh.quaternion.w run data get storage midwut:main temp[3] 10000

