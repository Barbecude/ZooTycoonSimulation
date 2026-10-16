#> physical_vehicles:zzz/summon_data/zzzzzz_data/data/wheel
execute store result score @s phys_veh.radius run data get storage midwut:main Data2.data.phys_veh.radius 1000
execute store result score @s phys_veh.tread_height run data get storage midwut:main Data2.data.phys_veh.tread_height 1000
execute store result score @s phys_veh.wheel.smoke_factor run data get storage midwut:main Data2.data.phys_veh.smoke_factor 100
execute store result score @s phys_veh.wheel.heat_capacity run data get storage midwut:main Data2.data.phys_veh.heat_capacity 1
execute store result score @s phys_veh.wheel.pressure run data get storage midwut:main Data2.data.phys_veh.pressure 100
execute store result score @s phys_veh.wheel.pressure0 run data get storage midwut:main Data2.data.phys_veh.pressure0 100
execute store result score @s phys_veh.wheel.pressure_coeff run data get storage midwut:main Data2.data.phys_veh.pressure_coeff 100
execute store result score @s phys_veh.wheel.force_coeff run data get storage midwut:main Data2.data.phys_veh.force_coeff 100
execute store result score @s phys_veh.wheel.temperature run data get storage midwut:main Data2.data.phys_veh.temperature 1
execute store result score @s phys_veh.wheel.temperature0 run data get storage midwut:main Data2.data.phys_veh.temperature0 1
execute store result score @s phys_veh.wheel.longitudinal.stiffness run data get storage midwut:main Data2.data.phys_veh.longitudinal.stiffness 100
execute store result score @s phys_veh.wheel.longitudinal.shape_factor run data get storage midwut:main Data2.data.phys_veh.longitudinal.shape_factor 100
execute store result score @s phys_veh.wheel.longitudinal.curvature run data get storage midwut:main Data2.data.phys_veh.longitudinal.curvature 100
execute store result score @s phys_veh.wheel.longitudinal.peak_factor run data get storage midwut:main Data2.data.phys_veh.longitudinal.peak_factor 100
execute store result score @s phys_veh.wheel.lateral.stiffness run data get storage midwut:main Data2.data.phys_veh.lateral.stiffness 100
execute store result score @s phys_veh.wheel.lateral.shape_factor run data get storage midwut:main Data2.data.phys_veh.lateral.shape_factor 100
execute store result score @s phys_veh.wheel.lateral.curvature run data get storage midwut:main Data2.data.phys_veh.lateral.curvature 100
execute store result score @s phys_veh.wheel.lateral.peak_factor run data get storage midwut:main Data2.data.phys_veh.lateral.peak_factor 100
execute store result score @s[tag=phys_veh.wheel.steering] phys_veh.wheel.aligning_torque.stiffness run data get storage midwut:main Data2.data.phys_veh.aligning_torque.stiffness 100
execute store result score @s[tag=phys_veh.wheel.steering] phys_veh.wheel.aligning_torque.shape_factor run data get storage midwut:main Data2.data.phys_veh.aligning_torque.shape_factor 100
execute store result score @s[tag=phys_veh.wheel.steering] phys_veh.wheel.aligning_torque.curvature run data get storage midwut:main Data2.data.phys_veh.aligning_torque.curvature 100
execute store result score @s[tag=phys_veh.wheel.steering] phys_veh.wheel.aligning_torque.peak_factor run data get storage midwut:main Data2.data.phys_veh.aligning_torque.peak_factor 100
execute store result score @s phys_veh.strength run data get storage midwut:main Data2.data.phys_veh.suspension.strength 1
execute store result score @s phys_veh.damping run data get storage midwut:main Data2.data.phys_veh.suspension.damping 1
execute store result score @s phys_veh.wheel.suspension.height run data get storage midwut:main Data2.data.phys_veh.suspension.height 1000
execute store result score @s phys_veh.max_delta run data get storage midwut:main Data2.data.phys_veh.suspension.max_delta 1000
execute store result score @s phys_veh.max_delta_neg run data get storage midwut:main Data2.data.phys_veh.suspension.max_delta -1000
execute if data storage midwut:main Data2.data.phys_veh.friction_contribution_factor store result score @s phys_veh.friction_contribution_factor run data get storage midwut:main Data2.data.phys_veh.friction_contribution_factor 100
execute if data storage midwut:main Data2.data.phys_veh.wheel_axle_id store result score @s phys_veh.id.wheel_axle run data get storage midwut:main Data2.data.phys_veh.wheel_axle_id 1
execute unless score @s phys_veh.id.wheel_axle = @s phys_veh.id.wheel_axle run scoreboard players set @s phys_veh.id.wheel_axle -1
execute unless score @s phys_veh.friction_contribution_factor = @s phys_veh.friction_contribution_factor run scoreboard players set @s phys_veh.friction_contribution_factor 100
execute if score @s phys_veh.friction_contribution_factor matches 101.. run scoreboard players set @s phys_veh.friction_contribution_factor 100
execute if score @s phys_veh.friction_contribution_factor matches ..-1 run scoreboard players set @s phys_veh.friction_contribution_factor 0
execute if entity @s[tag=phys_veh.wheel.braking] store result score @s phys_veh.brakes_max_torque run data get storage midwut:main Data2.data.phys_veh.brakes_torque
execute if entity @s[tag=phys_veh.wheel.hand_brake] store result score @s phys_veh.hand_brake_torque run data get storage midwut:main Data2.data.phys_veh.hand_brake_torque 100
execute if score #tracked_control main_score matches 1 run tag @s add phys_veh.tracked_control
execute if score #have_on_board_clutch_lock main_score matches 1 run tag @s add phys_veh.have_on_board_clutch_lock
execute if entity @s[tag=phys_veh.wheel.braking] if score #tracked_control main_score matches 1 run scoreboard players operation @s phys_veh.track_brake.a = #track_brake.a main_score
execute if entity @s[tag=phys_veh.wheel.braking] if score #tracked_control main_score matches 1 run scoreboard players operation @s phys_veh.track_brake.d = #track_brake.d main_score
scoreboard players operation @s phys_veh.tread_height += @s phys_veh.wheel.suspension.height
#execute store result score @s phys_veh.drift_percent run data get storage midwut:main Data2.data.phys_veh.drift_percent 100
execute store result score @s phys_veh.moment_of_inertia.x run data get storage midwut:main Data2.data.phys_veh.inertia_radius 1000
#scoreboard players operation @s phys_veh.moment_of_inertia.x *= #88 const
#scoreboard players operation @s phys_veh.moment_of_inertia.x /= #100 const
scoreboard players operation @s phys_veh.moment_of_inertia.x *= @s phys_veh.moment_of_inertia.x
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.x = @s phys_veh.moment_of_inertia.x
scoreboard players operation @s phys_veh.moment_of_inertia.x *= @s phys_veh.mass
scoreboard players operation @s phys_veh.moment_of_inertia.x /= #20000 const
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.x /= #20000 const
