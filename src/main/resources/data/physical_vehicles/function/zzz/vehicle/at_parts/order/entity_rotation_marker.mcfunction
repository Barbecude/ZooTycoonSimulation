function physical_vehicles:zzz/vehicle/math/quaternions/macro_facing with storage midwut:main macros
data modify storage midwut:main temp set from entity @s Rotation
execute store result score #y main_score run data get storage midwut:main temp[0] 1000
execute store result score #x main_score run data get storage midwut:main temp[1] 1000