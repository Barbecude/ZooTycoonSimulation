execute if score @s phys_veh.user_settings = @s phys_veh.user_settings run function physical_vehicles:zzz/player/triggers/user_settings/pre
scoreboard players enable @s phys_veh.user_settings
scoreboard players set @s phys_veh.user_settings -2147483648