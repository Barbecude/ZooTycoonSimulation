tag @s add phys_veh.cannon

execute store result score @s phys_veh.max_delta run data get storage midwut:main Data2.data.phys_veh.cannon.recoil_length -1125
execute store result score @s phys_veh.strength run data get storage midwut:main Data2.data.phys_veh.cannon.recoil_strength
execute store result score @s phys_veh.damping run data get storage midwut:main Data2.data.phys_veh.cannon.recoil_damping 100
execute store result score #damping main_score run data get storage midwut:main Data2.data.phys_veh.cannon.recoil_damping 100
execute store result score @s phys_veh.radius run data get storage midwut:main Data2.data.phys_veh.cannon.caliber 5.625
execute store result score @s phys_veh.charge_time run data get storage midwut:main Data2.data.phys_veh.cannon.charge_time 100
execute store result score @s phys_veh.id.storage run data get storage midwut:main Data2.data.phys_veh.cannon.storage_id
scoreboard players operation @s phys_veh.charge_time *= #tick_rate main_score
scoreboard players operation @s phys_veh.charge_time /= #100 const
scoreboard players set @s phys_veh.max_delta_neg 100
scoreboard players operation @s phys_veh.max_delta_neg -= #damping main_score
execute if data storage midwut:main Data2.data.phys_veh.cannon.fire_sound_id run tag @s add phys_veh.cannon.have_fire_sound

execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"w"} run tag @s add phys_veh.cannon.fire_w
execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"a"} run tag @s add phys_veh.cannon.fire_a
execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"s"} run tag @s add phys_veh.cannon.fire_s
execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"d"} run tag @s add phys_veh.cannon.fire_d
execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"sprint"} run tag @s add phys_veh.cannon.fire_sprint
execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"jump"} run tag @s add phys_veh.cannon.fire_jump
execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"use"} run tag @s add phys_veh.cannon.fire_use
execute if data storage midwut:main Data2.data.phys_veh.cannon{fire_control:"attack"} run tag @s add phys_veh.cannon.fire_attack
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"w"} run tag @s add phys_veh.cannon.charge_w
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"a"} run tag @s add phys_veh.cannon.charge_a
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"s"} run tag @s add phys_veh.cannon.charge_s
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"d"} run tag @s add phys_veh.cannon.charge_d
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"sprint"} run tag @s add phys_veh.cannon.charge_sprint
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"jump"} run tag @s add phys_veh.cannon.charge_jump
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"use"} run tag @s add phys_veh.cannon.charge_use
execute if data storage midwut:main Data2.data.phys_veh.cannon{charge_control:"attack"} run tag @s add phys_veh.cannon.charge_attack

