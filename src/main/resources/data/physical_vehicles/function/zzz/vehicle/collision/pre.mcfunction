# warn-off-file unreachable-condition
scoreboard players operation #main.velocity.x main_score = @s phys_veh.velocity.x
scoreboard players operation #main.velocity.y main_score = @s phys_veh.velocity.y
scoreboard players operation #main.velocity.z main_score = @s phys_veh.velocity.z

scoreboard players set #col_px_x main_score 0
scoreboard players set #col_px_y main_score 0
scoreboard players set #col_px_z main_score 0
scoreboard players set #col_px_v main_score 0
scoreboard players set #col_px_c main_score 0

scoreboard players set #col_py_x main_score 0
scoreboard players set #col_py_y main_score 0
scoreboard players set #col_py_z main_score 0
scoreboard players set #col_py_v main_score 0
scoreboard players set #col_py_c main_score 0

scoreboard players set #col_pz_x main_score 0
scoreboard players set #col_pz_y main_score 0
scoreboard players set #col_pz_z main_score 0
scoreboard players set #col_pz_v main_score 0
scoreboard players set #col_pz_c main_score 0

scoreboard players set #col_nx_x main_score 0
scoreboard players set #col_nx_y main_score 0
scoreboard players set #col_nx_z main_score 0
scoreboard players set #col_nx_v main_score 0
scoreboard players set #col_nx_c main_score 0

scoreboard players set #col_ny_x main_score 0
scoreboard players set #col_ny_y main_score 0
scoreboard players set #col_ny_z main_score 0
scoreboard players set #col_ny_v main_score 0
scoreboard players set #col_ny_c main_score 0

scoreboard players set #col_nz_x main_score 0
scoreboard players set #col_nz_y main_score 0
scoreboard players set #col_nz_z main_score 0
scoreboard players set #col_nz_v main_score 0
scoreboard players set #col_nz_c main_score 0

scoreboard players set #wx.x main_score 0
scoreboard players set #wx.y main_score 0
scoreboard players set #wx.z main_score 0
scoreboard players set #wy.x main_score 0
scoreboard players set #wy.y main_score 0
scoreboard players set #wy.z main_score 0
scoreboard players set #wz.x main_score 0
scoreboard players set #wz.y main_score 0
scoreboard players set #wz.z main_score 0

scoreboard players set #vx.y main_score 0
scoreboard players set #vx.z main_score 0
scoreboard players set #vy.x main_score 0
scoreboard players set #vy.z main_score 0
scoreboard players set #vz.x main_score 0
scoreboard players set #vz.y main_score 0

scoreboard players set #fr.x main_score 0
scoreboard players set #fr.y main_score 0
scoreboard players set #fr.z main_score 0

execute if entity @e[type=minecraft:text_display,tag=phys_veh.main,tag=!phys_veh.selected_part,limit=1,distance=..64] run function physical_vehicles:zzz/vehicle/collision/dynamic/main

data modify storage midwut:main collision set from storage midwut:main Data.data.phys_veh.collisions
function physical_vehicles:zzz/vehicle/collision/back_last_location/loop
function physical_vehicles:zzz/vehicle/collision/back_last_location/choice
function physical_vehicles:zzz/vehicle/math/spin/rot

data modify storage midwut:main collision set from storage midwut:main Data.data.phys_veh.collisions
function physical_vehicles:zzz/vehicle/collision/vertices/loop_main

execute unless function physical_vehicles:zzz/vehicle/collision/f_not_zero run function physical_vehicles:zzz/vehicle/collision/f_fric

execute if score #col_px_c main_score matches 1.. run function physical_vehicles:zzz/vehicle/collision/x/p
execute if score #col_nx_c main_score matches 1.. run function physical_vehicles:zzz/vehicle/collision/x/n
execute if score #col_py_c main_score matches 1.. run function physical_vehicles:zzz/vehicle/collision/y/p
execute if score #col_ny_c main_score matches 1.. run function physical_vehicles:zzz/vehicle/collision/y/n
execute if score #col_pz_c main_score matches 1.. run function physical_vehicles:zzz/vehicle/collision/z/p
execute if score #col_nz_c main_score matches 1.. run function physical_vehicles:zzz/vehicle/collision/z/n

execute if entity @s[tag=phys_veh.dynamic_collision_check] run function physical_vehicles:zzz/vehicle/collision/dynamic/remove_tags