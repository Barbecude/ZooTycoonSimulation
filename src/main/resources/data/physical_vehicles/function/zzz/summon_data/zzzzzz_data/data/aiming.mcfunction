tag @s add phys_veh.aiming

#execute store result score @s phys_veh.friction_reduction_ratio run data get storage midwut:main Data2.data.phys_veh.aiming.reducing_fov 1000
execute store result score @s phys_veh.aiming.offset_min run data get storage midwut:main Data2.data.phys_veh.aiming.offset_min 1
execute store result score @s phys_veh.aiming.offset_max run data get storage midwut:main Data2.data.phys_veh.aiming.offset_max 1
execute store result score @s phys_veh.aiming.angle_multiplier run data get storage midwut:main Data2.data.phys_veh.aiming.angle_multiplier 1000
execute store result score @s phys_veh.id.seat run data get storage midwut:main Data2.data.phys_veh.aiming.seat_id 1
execute store result score @s phys_veh.id.turret run data get storage midwut:main Data2.data.phys_veh.aiming.turret_id 1
execute store result score @s phys_veh.aiming_offset.x run data get storage midwut:main Data2.data.phys_veh.aiming.seat_offset[0] 1000
execute store result score @s phys_veh.aiming_offset.y run data get storage midwut:main Data2.data.phys_veh.aiming.seat_offset[1] 1000
execute store result score @s phys_veh.aiming_offset.z run data get storage midwut:main Data2.data.phys_veh.aiming.seat_offset[2] 1000

execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"w"} run tag @s add phys_veh.aiming_w
execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"a"} run tag @s add phys_veh.aiming_a
execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"s"} run tag @s add phys_veh.aiming_s
execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"d"} run tag @s add phys_veh.aiming_d
execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"sprint"} run tag @s add phys_veh.aiming_sprint
execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"jump"} run tag @s add phys_veh.aiming_jump
execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"use"} run tag @s add phys_veh.aiming_use
execute if data storage midwut:main Data2.data.phys_veh.aiming{control:"attack"} run tag @s add phys_veh.aiming_attack