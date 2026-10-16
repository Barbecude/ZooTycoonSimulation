#$data modify storage midwut:main temp set value $(t)
#data modify storage midwut:main temp set from entity @s data.left_rotation
#$data modify storage midwut:main temp set value $(t)
#execute store result score #x main_score run data get storage midwut:main temp[0] 10000
#execute store result score #y main_score run data get storage midwut:main temp[1] 10000
#execute store result score #z main_score run data get storage midwut:main temp[2] 10000
#execute store result score #w main_score run data get storage midwut:main temp[3] 10000
#execute store result score @s phys_veh.add_quaternion.x run data get storage midwut:main temp[0] 10000
#execute store result score @s phys_veh.add_quaternion.y run data get storage midwut:main temp[1] 10000
#execute store result score @s phys_veh.add_quaternion.z run data get storage midwut:main temp[2] 10000
#execute store result score @s phys_veh.add_quaternion.w run data get storage midwut:main temp[3] 10000

scoreboard players operation #x main_score = @s phys_veh.quaternion.x
scoreboard players operation #y main_score = @s phys_veh.quaternion.y
scoreboard players operation #z main_score = @s phys_veh.quaternion.z

scoreboard players operation @s phys_veh.quaternion.x = @s phys_veh.quaternion.w
scoreboard players operation @s phys_veh.quaternion.y = @s phys_veh.quaternion.w
scoreboard players operation @s phys_veh.quaternion.z = @s phys_veh.quaternion.w
scoreboard players operation @s phys_veh.quaternion.x *= @s phys_veh.add_quaternion.x
scoreboard players operation @s phys_veh.quaternion.x /= #10000 const
scoreboard players operation @s phys_veh.quaternion.y *= @s phys_veh.add_quaternion.y
scoreboard players operation @s phys_veh.quaternion.y /= #10000 const
scoreboard players operation @s phys_veh.quaternion.z *= @s phys_veh.add_quaternion.z
scoreboard players operation @s phys_veh.quaternion.z /= #10000 const
scoreboard players operation @s phys_veh.quaternion.w *= @s phys_veh.add_quaternion.w
scoreboard players operation @s phys_veh.quaternion.w /= #10000 const

scoreboard players operation #temp main_score = #x main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.w
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.x += #temp main_score
scoreboard players operation #temp main_score = #x main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.z
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.y -= #temp main_score
scoreboard players operation #temp main_score = #x main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.y
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.z += #temp main_score
scoreboard players operation #x main_score *= @s phys_veh.add_quaternion.x
scoreboard players operation #x main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.w -= #x main_score

scoreboard players operation #temp main_score = #y main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.z
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.x += #temp main_score
scoreboard players operation #temp main_score = #y main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.w
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.y += #temp main_score
scoreboard players operation #temp main_score = #y main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.x
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.z -= #temp main_score
scoreboard players operation #y main_score *= @s phys_veh.add_quaternion.y
scoreboard players operation #y main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.w -= #y main_score

scoreboard players operation #temp main_score = #z main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.y
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.x -= #temp main_score
scoreboard players operation #temp main_score = #z main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.x
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.y += #temp main_score
scoreboard players operation #temp main_score = #z main_score
scoreboard players operation #temp main_score *= @s phys_veh.add_quaternion.w
scoreboard players operation #temp main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.z += #temp main_score
scoreboard players operation #z main_score *= @s phys_veh.add_quaternion.z
scoreboard players operation #z main_score /= #10000 const
scoreboard players operation @s phys_veh.quaternion.w -= #z main_score

scoreboard players set @s phys_veh.add_quaternion.x 0
scoreboard players set @s phys_veh.add_quaternion.y 0
scoreboard players set @s phys_veh.add_quaternion.z 0
scoreboard players set @s phys_veh.add_quaternion.w 0
