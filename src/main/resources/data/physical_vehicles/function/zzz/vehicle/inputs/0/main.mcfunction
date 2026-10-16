function physical_vehicles:zzz/vehicle/inputs/0/w
execute unless score @s phys_veh.input0.w matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/0/w_norm
function physical_vehicles:zzz/vehicle/inputs/0/a
execute unless score @s phys_veh.input0.a matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/0/a_norm
function physical_vehicles:zzz/vehicle/inputs/0/s
execute unless score @s phys_veh.input0.s matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/0/s_norm
function physical_vehicles:zzz/vehicle/inputs/0/d
execute unless score @s phys_veh.input0.d matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/0/d_norm
function physical_vehicles:zzz/vehicle/inputs/0/sprint
execute unless score @s phys_veh.input0.sprint matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/0/sprint_norm
function physical_vehicles:zzz/vehicle/inputs/0/jump
execute unless score @s phys_veh.input0.jump matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/0/jump_norm
function physical_vehicles:zzz/vehicle/inputs/0/use
execute unless score @s phys_veh.input0.use matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/0/use_norm

scoreboard players operation #phys_veh.input0.w main_score = @s phys_veh.input0.w
scoreboard players operation #phys_veh.input0.a main_score = @s phys_veh.input0.a
scoreboard players operation #phys_veh.input0.s main_score = @s phys_veh.input0.s
scoreboard players operation #phys_veh.input0.d main_score = @s phys_veh.input0.d
scoreboard players operation #phys_veh.input0.sprint main_score = @s phys_veh.input0.sprint
scoreboard players operation #phys_veh.input0.jump main_score = @s phys_veh.input0.jump
scoreboard players operation #phys_veh.input0.use main_score = @s phys_veh.input0.use
scoreboard players operation #phys_veh.input0.attack main_score = #input0.attack main_score