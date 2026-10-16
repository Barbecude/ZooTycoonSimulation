scoreboard players set #ray main_score -3

scoreboard players operation #npx main_score = #px main_score
scoreboard players operation #npx main_score -= #x2 main_score
scoreboard players operation #nnx main_score = #x2 main_score
scoreboard players operation #nnx main_score -= #nx main_score
scoreboard players operation #npy main_score = #py main_score
scoreboard players operation #npy main_score -= #y2 main_score
scoreboard players operation #nny main_score = #y2 main_score
scoreboard players operation #nny main_score -= #ny main_score
scoreboard players operation #npz main_score = #pz main_score
scoreboard players operation #npz main_score -= #z2 main_score
scoreboard players operation #nnz main_score = #z2 main_score
scoreboard players operation #nnz main_score -= #nz main_score
scoreboard players operation #min main_score = #npx main_score
scoreboard players operation #min main_score < #nnx main_score
scoreboard players operation #min main_score < #npy main_score
scoreboard players operation #min main_score < #nny main_score
scoreboard players operation #min main_score < #npz main_score
scoreboard players operation #min main_score < #nnz main_score
function physical_vehicles:zzz/vehicle/collision/dynamic/normal_match/main
function physical_vehicles:zzz/shell/hit_vehicle/thickness

execute if score #velocity main_score matches 0.. run function physical_vehicles:zzz/shell/hit_vehicle/shell_ap
execute if score #velocity main_score matches -1 run function physical_vehicles:zzz/shell/hit_vehicle/shell_he


execute if score #penetration main_score < #thickness main_score run return run function physical_vehicles:zzz/shell/hit_vehicle/not_penetrate
scoreboard players set #ray main_score -4

execute unless score #velocity main_score matches -1 run function physical_vehicles:zzz/shell/hit_vehicle/impulse_scale
execute unless score #phys_veh.shell_impulse_disabled main_score matches 1 run function physical_vehicles:zzz/shell/hit_vehicle/veh_impulse

scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
execute as @e[type=minecraft:llama,distance=..64,tag=phys_veh.llama_seat,tag=phys_veh.have_seat_improvements.immunity,predicate=physical_vehicles:same_scores/vehicle_id] on passengers run function physical_vehicles:zzz/shell/hit_vehicle/immunity_remove

execute unless score #velocity main_score matches -1 if score #hit_velocity main_score matches 2.. run function physical_vehicles:zzz/shell/hit_vehicle/ray_pre

scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
execute if data storage midwut:main boxes[-1].modules[0] run function physical_vehicles:zzz/shell/hit_modules/loop
