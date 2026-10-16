function physical_vehicles:zzz/vehicle/inputs/3/w
execute unless score @s phys_veh.input3.w matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/3/w_norm
function physical_vehicles:zzz/vehicle/inputs/3/a
execute unless score @s phys_veh.input3.a matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/3/a_norm
function physical_vehicles:zzz/vehicle/inputs/3/s
execute unless score @s phys_veh.input3.s matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/3/s_norm
function physical_vehicles:zzz/vehicle/inputs/3/d
execute unless score @s phys_veh.input3.d matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/3/d_norm
function physical_vehicles:zzz/vehicle/inputs/3/sprint
execute unless score @s phys_veh.input3.sprint matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/3/sprint_norm
function physical_vehicles:zzz/vehicle/inputs/3/jump
execute unless score @s phys_veh.input3.jump matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/3/jump_norm
function physical_vehicles:zzz/vehicle/inputs/3/use
execute unless score @s phys_veh.input3.use matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/3/use_norm

scoreboard players operation #phys_veh.input3.w main_score = @s phys_veh.input3.w
scoreboard players operation #phys_veh.input3.a main_score = @s phys_veh.input3.a
scoreboard players operation #phys_veh.input3.s main_score = @s phys_veh.input3.s
scoreboard players operation #phys_veh.input3.d main_score = @s phys_veh.input3.d
scoreboard players operation #phys_veh.input3.sprint main_score = @s phys_veh.input3.sprint
scoreboard players operation #phys_veh.input3.jump main_score = @s phys_veh.input3.jump
scoreboard players operation #phys_veh.input3.use main_score = @s phys_veh.input3.use
scoreboard players operation #phys_veh.input3.attack main_score = #input3.attack main_score