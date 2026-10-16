execute unless score @s phys_veh.placement_directions = @s phys_veh.placement_directions run scoreboard players set @s phys_veh.placement_directions 4
execute unless score @s phys_veh.pedal_deltatime = @s phys_veh.pedal_deltatime run scoreboard players set @s phys_veh.pedal_deltatime 10
execute unless score @s phys_veh.gear_changing_cooldown = @s phys_veh.gear_changing_cooldown run scoreboard players set @s phys_veh.gear_changing_cooldown 40
advancement revoke @s only physical_vehicles:player_setup