scoreboard players operation #engine_friction_torque main_score = @s phys_veh.engine_idle_w
scoreboard players operation #engine_friction_torque main_score -= @s phys_veh.engine_w
scoreboard players operation #engine_friction_torque2 main_score = #engine_friction_torque main_score
scoreboard players operation #engine_friction_torque main_score *= @s phys_veh.engine_w_friction.b
scoreboard players operation #engine_friction_torque2 main_score *= #engine_friction_torque2 main_score
scoreboard players operation #engine_friction_torque2 main_score *= @s phys_veh.engine_w_friction.c
scoreboard players operation #engine_friction_torque main_score += #engine_friction_torque2 main_score
scoreboard players operation #engine_friction_torque main_score /= #100 const
scoreboard players operation #engine_friction_torque main_score += @s phys_veh.engine_w_friction.a


scoreboard players operation #engine_friction_torque main_score *= @s phys_veh.engine_moment_of_inertia
scoreboard players operation #engine_friction_torque main_score /= #tick_rate main_score
scoreboard players operation #engine_friction_torque main_score /= #500 const
scoreboard players operation @s phys_veh.engine_w += #engine_friction_torque main_score
