function physical_vehicles:zzz/vehicle/inputs/4/w
execute unless score @s phys_veh.input4.w matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/4/w_norm
function physical_vehicles:zzz/vehicle/inputs/4/a
execute unless score @s phys_veh.input4.a matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/4/a_norm
function physical_vehicles:zzz/vehicle/inputs/4/s
execute unless score @s phys_veh.input4.s matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/4/s_norm
function physical_vehicles:zzz/vehicle/inputs/4/d
execute unless score @s phys_veh.input4.d matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/4/d_norm
function physical_vehicles:zzz/vehicle/inputs/4/sprint
execute unless score @s phys_veh.input4.sprint matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/4/sprint_norm
function physical_vehicles:zzz/vehicle/inputs/4/jump
execute unless score @s phys_veh.input4.jump matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/4/jump_norm
function physical_vehicles:zzz/vehicle/inputs/4/use
execute unless score @s phys_veh.input4.use matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/4/use_norm

scoreboard players operation #phys_veh.input4.w main_score = @s phys_veh.input4.w
scoreboard players operation #phys_veh.input4.a main_score = @s phys_veh.input4.a
scoreboard players operation #phys_veh.input4.s main_score = @s phys_veh.input4.s
scoreboard players operation #phys_veh.input4.d main_score = @s phys_veh.input4.d
scoreboard players operation #phys_veh.input4.sprint main_score = @s phys_veh.input4.sprint
scoreboard players operation #phys_veh.input4.jump main_score = @s phys_veh.input4.jump
scoreboard players operation #phys_veh.input4.use main_score = @s phys_veh.input4.use
scoreboard players operation #phys_veh.input4.attack main_score = #input4.attack main_score