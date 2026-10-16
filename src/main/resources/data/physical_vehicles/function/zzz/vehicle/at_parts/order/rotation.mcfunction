function physical_vehicles:zzz/vehicle/at_parts/order/rotation_parent
execute store result score @s phys_veh.quaternion.x run data get storage midwut:main left_rotation[0] 10000
execute store result score @s phys_veh.quaternion.y run data get storage midwut:main left_rotation[1] 10000
execute store result score @s phys_veh.quaternion.z run data get storage midwut:main left_rotation[2] 10000
execute store result score @s phys_veh.quaternion.w run data get storage midwut:main left_rotation[3] 10000
execute if entity @s[tag=phys_veh.add_angle.y] run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle_y
execute if entity @s[tag=phys_veh.add_angle.x] run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle_x
execute if entity @s[tag=phys_veh.add_angle.z] run function physical_vehicles:zzz/vehicle/at_parts/order/add_angle_z
data modify entity @s transformation.left_rotation set from storage midwut:main left_rotation
function physical_vehicles:zzz/vehicle/math/quaternions/to_matrix_no_fp
execute if entity @s[tag=phys_veh.gets_entity_rotation] run function physical_vehicles:zzz/vehicle/at_parts/order/entity_rotation
