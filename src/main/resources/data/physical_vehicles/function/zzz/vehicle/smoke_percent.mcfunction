scoreboard players operation #hp.percent main_score = @s phys_veh.hp
scoreboard players operation #hp.percent main_score *= #1000 const
scoreboard players operation #hp.percent main_score /= @s phys_veh.hp_max
