function physical_vehicles:zzz/vehicle/inputs/1/w
execute unless score @s phys_veh.input1.w matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/1/w_norm
function physical_vehicles:zzz/vehicle/inputs/1/a
execute unless score @s phys_veh.input1.a matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/1/a_norm
function physical_vehicles:zzz/vehicle/inputs/1/s
execute unless score @s phys_veh.input1.s matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/1/s_norm
function physical_vehicles:zzz/vehicle/inputs/1/d
execute unless score @s phys_veh.input1.d matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/1/d_norm
function physical_vehicles:zzz/vehicle/inputs/1/sprint
execute unless score @s phys_veh.input1.sprint matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/1/sprint_norm
function physical_vehicles:zzz/vehicle/inputs/1/jump
execute unless score @s phys_veh.input1.jump matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/1/jump_norm
function physical_vehicles:zzz/vehicle/inputs/1/use
execute unless score @s phys_veh.input1.use matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/1/use_norm

scoreboard players operation #phys_veh.input1.w main_score = @s phys_veh.input1.w
scoreboard players operation #phys_veh.input1.a main_score = @s phys_veh.input1.a
scoreboard players operation #phys_veh.input1.s main_score = @s phys_veh.input1.s
scoreboard players operation #phys_veh.input1.d main_score = @s phys_veh.input1.d
scoreboard players operation #phys_veh.input1.sprint main_score = @s phys_veh.input1.sprint
scoreboard players operation #phys_veh.input1.jump main_score = @s phys_veh.input1.jump
scoreboard players operation #phys_veh.input1.use main_score = @s phys_veh.input1.use
scoreboard players operation #phys_veh.input1.attack main_score = #input1.attack main_score