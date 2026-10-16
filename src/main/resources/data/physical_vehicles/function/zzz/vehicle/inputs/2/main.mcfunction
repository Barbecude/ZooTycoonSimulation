function physical_vehicles:zzz/vehicle/inputs/2/w
execute unless score @s phys_veh.input2.w matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/2/w_norm
function physical_vehicles:zzz/vehicle/inputs/2/a
execute unless score @s phys_veh.input2.a matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/2/a_norm
function physical_vehicles:zzz/vehicle/inputs/2/s
execute unless score @s phys_veh.input2.s matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/2/s_norm
function physical_vehicles:zzz/vehicle/inputs/2/d
execute unless score @s phys_veh.input2.d matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/2/d_norm
function physical_vehicles:zzz/vehicle/inputs/2/sprint
execute unless score @s phys_veh.input2.sprint matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/2/sprint_norm
function physical_vehicles:zzz/vehicle/inputs/2/jump
execute unless score @s phys_veh.input2.jump matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/2/jump_norm
function physical_vehicles:zzz/vehicle/inputs/2/use
execute unless score @s phys_veh.input2.use matches 0..100 run function physical_vehicles:zzz/vehicle/inputs/2/use_norm

scoreboard players operation #phys_veh.input2.w main_score = @s phys_veh.input2.w
scoreboard players operation #phys_veh.input2.a main_score = @s phys_veh.input2.a
scoreboard players operation #phys_veh.input2.s main_score = @s phys_veh.input2.s
scoreboard players operation #phys_veh.input2.d main_score = @s phys_veh.input2.d
scoreboard players operation #phys_veh.input2.sprint main_score = @s phys_veh.input2.sprint
scoreboard players operation #phys_veh.input2.jump main_score = @s phys_veh.input2.jump
scoreboard players operation #phys_veh.input2.use main_score = @s phys_veh.input2.use
scoreboard players operation #phys_veh.input2.attack main_score = #input2.attack main_score