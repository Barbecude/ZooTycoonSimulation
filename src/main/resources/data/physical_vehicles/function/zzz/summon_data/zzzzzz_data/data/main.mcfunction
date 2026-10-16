execute store result entity @s data.phys_veh.id int 1 run scoreboard players operation @s phys_veh.id.vehicle = #physical_vehicles.id.vehicle main_score

execute unless data storage midwut:main {data:"summoned"} run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/spawn_data
execute if data storage midwut:main {data:"summoned"} run tag @s add phys_veh.summoned_vehicle

data modify entity @s data.transformation set value {translation:[0f,0f,0f],scale:[1f,1f,1f],main_rotation:[0f,0f,0f,1f],steered_wheel_rotation:[0f,0f,0f,1f],steering_wheel_rotation:[0f,0f,0f,1f],wheel_x_rotation:[0f,0f,0f,1f]}
data modify entity @s data.Pos set value [0d,0d,0d]

data modify storage midwut:main Data set from entity @s

execute unless score @s phys_veh.wheel.angle = @s phys_veh.wheel.angle run scoreboard players set @s phys_veh.wheel.angle 0
execute unless score @s phys_veh.quaternion.x = @s phys_veh.quaternion.x run scoreboard players set @s phys_veh.quaternion.x 0
execute unless score @s phys_veh.quaternion.y = @s phys_veh.quaternion.y run scoreboard players set @s phys_veh.quaternion.y 0
execute unless score @s phys_veh.quaternion.z = @s phys_veh.quaternion.z run scoreboard players set @s phys_veh.quaternion.z 0
execute unless score @s phys_veh.quaternion.w = @s phys_veh.quaternion.w run scoreboard players set @s phys_veh.quaternion.w 10000
execute unless score @s phys_veh.last_quaternion.x = @s phys_veh.last_quaternion.x run scoreboard players set @s phys_veh.last_quaternion.x 0
execute unless score @s phys_veh.last_quaternion.y = @s phys_veh.last_quaternion.y run scoreboard players set @s phys_veh.last_quaternion.y 0
execute unless score @s phys_veh.last_quaternion.z = @s phys_veh.last_quaternion.z run scoreboard players set @s phys_veh.last_quaternion.z 0
execute unless score @s phys_veh.last_quaternion.w = @s phys_veh.last_quaternion.w run scoreboard players set @s phys_veh.last_quaternion.w 10000
execute unless score @s phys_veh.spin.x = @s phys_veh.spin.x run scoreboard players set @s phys_veh.spin.x 0
execute unless score @s phys_veh.spin.y = @s phys_veh.spin.y run scoreboard players set @s phys_veh.spin.y 0
execute unless score @s phys_veh.spin.z = @s phys_veh.spin.z run scoreboard players set @s phys_veh.spin.z 0
execute unless score @s phys_veh.velocity.x = @s phys_veh.velocity.x run scoreboard players set @s phys_veh.velocity.x 0
execute unless score @s phys_veh.velocity.y = @s phys_veh.velocity.y run scoreboard players set @s phys_veh.velocity.y 0
execute unless score @s phys_veh.velocity.z = @s phys_veh.velocity.z run scoreboard players set @s phys_veh.velocity.z 0
execute unless score @s phys_veh.add_angle.x = @s phys_veh.add_angle.x run scoreboard players set @s phys_veh.add_angle.x 0
execute unless score @s phys_veh.add_angle.y = @s phys_veh.add_angle.y run scoreboard players set @s phys_veh.add_angle.y 0
execute unless score @s phys_veh.add_angle.z = @s phys_veh.add_angle.z run scoreboard players set @s phys_veh.add_angle.z 0

execute store result storage midwut:main Data.data.transformation.main_rotation[0] float 0.0001 run scoreboard players get @s phys_veh.quaternion.x
execute store result storage midwut:main Data.data.transformation.main_rotation[1] float 0.0001 run scoreboard players get @s phys_veh.quaternion.y
execute store result storage midwut:main Data.data.transformation.main_rotation[2] float 0.0001 run scoreboard players get @s phys_veh.quaternion.z
execute store result storage midwut:main Data.data.transformation.main_rotation[3] float 0.0001 run scoreboard players get @s phys_veh.quaternion.w
data modify entity @s data.transformation.main_rotation set from storage midwut:main Data.data.transformation.main_rotation

execute store result score @s phys_veh.mass run data get storage midwut:main Data.data.phys_veh.mass 1
execute unless score @s phys_veh.rolling_in_air = @s phys_veh.rolling_in_air store result score @s phys_veh.rolling_in_air run data get storage midwut:main Data.data.phys_veh.rolling_in_air 174.5329252
execute unless score @s phys_veh.gravity.x = @s phys_veh.gravity.x store result score @s phys_veh.gravity.x run data get storage midwut:main Data.data.phys_veh.gravity[0] 100
execute unless score @s phys_veh.gravity.y = @s phys_veh.gravity.y store result score @s phys_veh.gravity.y run data get storage midwut:main Data.data.phys_veh.gravity[1] 100
execute unless score @s phys_veh.gravity.z = @s phys_veh.gravity.z store result score @s phys_veh.gravity.z run data get storage midwut:main Data.data.phys_veh.gravity[2] 100
execute unless score @s phys_veh.gravity.x = @s phys_veh.gravity.x unless data storage midwut:main Data.data.phys_veh.gravity[0] run scoreboard players set @s phys_veh.gravity.x 0
execute unless score @s phys_veh.gravity.y = @s phys_veh.gravity.y unless data storage midwut:main Data.data.phys_veh.gravity[1] run scoreboard players set @s phys_veh.gravity.y -981
execute unless score @s phys_veh.gravity.z = @s phys_veh.gravity.z unless data storage midwut:main Data.data.phys_veh.gravity[2] run scoreboard players set @s phys_veh.gravity.z 0
execute unless score @s phys_veh.bounding_box.x = @s phys_veh.bounding_box.x store result score @s phys_veh.bounding_box.x run data get storage midwut:main Data.data.phys_veh.bounding_box[0] 1000
execute unless score @s phys_veh.bounding_box.y = @s phys_veh.bounding_box.y store result score @s phys_veh.bounding_box.y run data get storage midwut:main Data.data.phys_veh.bounding_box[1] 1000
execute unless score @s phys_veh.bounding_box.z = @s phys_veh.bounding_box.z store result score @s phys_veh.bounding_box.z run data get storage midwut:main Data.data.phys_veh.bounding_box[2] 1000
execute unless score @s phys_veh.wheel.angle.left = @s phys_veh.wheel.angle.left store result score @s phys_veh.wheel.angle.left run data get storage midwut:main Data.data.phys_veh.steering_angle[0] -174.5329252
execute unless score @s phys_veh.wheel.angle.right = @s phys_veh.wheel.angle.right store result score @s phys_veh.wheel.angle.right run data get storage midwut:main Data.data.phys_veh.steering_angle[1] 174.5329252
execute unless score @s phys_veh.wheel.steering_angle = @s phys_veh.wheel.steering_angle store result score @s phys_veh.wheel.steering_angle run data get storage midwut:main Data.data.phys_veh.steering_angle[2] 174.5329252
execute unless score @s phys_veh.steering_wheel.multiplier = @s phys_veh.steering_wheel.multiplier store result score @s phys_veh.steering_wheel.multiplier run data get storage midwut:main Data.data.phys_veh.steering_angle[4] 100
execute store result score #steering_wheel main_score run data get storage midwut:main Data.data.phys_veh.steering_angle[3] 1000
execute store result storage midwut:main trigonometric.in int 0.5 run scoreboard players operation #steering_wheel main_score %= #360000 const
execute store result score @s phys_veh.steering_wheel.x_rotation_sin run function midwut:y_functions/math/trigonometric/sin with storage midwut:main trigonometric
execute store result score @s phys_veh.steering_wheel.x_rotation_cos run function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
execute store result score @s phys_veh.wheel.aligning_torque.divider run data get storage midwut:main Data.data.phys_veh.aligning_torque_divider 1
execute unless score @s phys_veh.coeff_e = @s phys_veh.coeff_e store result score @s phys_veh.coeff_e run data get storage midwut:main Data.data.phys_veh.coeff_e 1000
execute unless score @s phys_veh.friction_reduction_ratio = @s phys_veh.friction_reduction_ratio store result score @s phys_veh.friction_reduction_ratio run data get storage midwut:main Data.data.phys_veh.friction_reduction_ratio 10
execute store result score @s phys_veh.count_wheels if entity @e[type=#physical_vehicles:refers_to_vehicle/wheel,tag=phys_veh.wheel,tag=get_id,distance=..96]
execute store result score @s phys_veh.count_driving_wheels if entity @e[type=#physical_vehicles:refers_to_vehicle/wheel,tag=phys_veh.wheel.driving,tag=get_id,distance=..96]
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.steering_wheel,tag=get_id,distance=..96] run tag @s add phys_veh.have_steering_wheel
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/all,tag=get_id,distance=..96,nbt={data:{phys_veh:{input_id:0}}}] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/input_id/0
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/all,tag=get_id,distance=..96,nbt={data:{phys_veh:{input_id:1}}}] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/input_id/1
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/all,tag=get_id,distance=..96,nbt={data:{phys_veh:{input_id:2}}}] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/input_id/2
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/all,tag=get_id,distance=..96,nbt={data:{phys_veh:{input_id:3}}}] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/input_id/3
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/all,tag=get_id,distance=..96,nbt={data:{phys_veh:{input_id:4}}}] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/input_id/4

execute store result score @s phys_veh.base_radius run data get storage midwut:main Data.data.phys_veh.base_radius 1000
execute store result score @s phys_veh.engine_w_friction.a run data get storage midwut:main Data.data.phys_veh.engine.friction[0] 100
execute store result score @s phys_veh.engine_w_friction.b run data get storage midwut:main Data.data.phys_veh.engine.friction[1] 10000
execute store result score @s phys_veh.engine_w_friction.c run data get storage midwut:main Data.data.phys_veh.engine.friction[2] 10000
execute store result score @s phys_veh.engine_idle_w run data get storage midwut:main Data.data.phys_veh.engine.idle_rpm 0.10471975
execute store result score @s phys_veh.engine_min_w run data get storage midwut:main Data.data.phys_veh.engine.min_rpm 0.10471975
execute store result score @s phys_veh.engine_redline_rpm run data get storage midwut:main Data.data.phys_veh.engine.redline_rpm 1
execute store result score @s phys_veh.engine_torque.a run data get storage midwut:main Data.data.phys_veh.engine.torque_curve_coeff 1
execute store result score @s phys_veh.engine_max_torque run data get storage midwut:main Data.data.phys_veh.engine.max_torque 1
execute store result score @s phys_veh.engine_max_torque_w run data get storage midwut:main Data.data.phys_veh.engine.max_torque_rpm 0.10471975
execute store result score @s phys_veh.engine_moment_of_inertia run data get storage midwut:main Data.data.phys_veh.engine.moment_of_inertia_reversed 10
execute store result score @s phys_veh.rpm_scaling_factor run data get storage midwut:main Data.data.phys_veh.engine.rpm_scaling_factor 1

scoreboard players add @s phys_veh.engine_idle_w 1
scoreboard players add @s phys_veh.engine_min_w 1
scoreboard players add @s phys_veh.engine_max_torque_w 1
scoreboard players operation @s phys_veh.engine_redline_rpm /= @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.rpm_scaling_factor

execute store result score @s phys_veh.clutch_max_torque run data get storage midwut:main Data.data.phys_veh.transmission.max_torque 100
execute store result score @s phys_veh.clutch_max_torque_neg run data get storage midwut:main Data.data.phys_veh.transmission.max_torque -100
execute store result score @s phys_veh.clutch_stiffness run data get storage midwut:main Data.data.phys_veh.transmission.stiffness 1
execute store result score @s phys_veh.final_gear run data get storage midwut:main Data.data.phys_veh.transmission.final_gear 10
execute store result score @s phys_veh.gearbox_n run data get storage midwut:main Data.data.phys_veh.transmission.gearbox_efficiency 100

scoreboard players set #gear_counter main_score 0
scoreboard players set #temp2 main_score -1
data modify storage midwut:main gears set from storage midwut:main Data.data.phys_veh.gears
function physical_vehicles:zzz/summon_data/zzzzzz_data/data/gears_loop

scoreboard players operation @s phys_veh.gear_num = #phys_veh.spawn.initial_gear main_score
scoreboard players operation @s phys_veh.gear_num > @s phys_veh.gear_num_min
scoreboard players operation @s phys_veh.gear_num < @s phys_veh.gear_num_max
execute if entity @s[tag=phys_veh.doesnt_have_neutral_gear,scores={phys_veh.gear_num=0,phys_veh.gear_num_max=1..}] run scoreboard players set @s phys_veh.gear_num 1
execute if entity @s[tag=phys_veh.doesnt_have_neutral_gear,scores={phys_veh.gear_num=0,phys_veh.gear_num_max=..-1}] run scoreboard players set @s phys_veh.gear_num -1

scoreboard players operation #temp main_score = @s phys_veh.gear_num
execute store result storage midwut:main macros.gear_index int 1 run scoreboard players operation #temp main_score += @s phys_veh.gear_num_non_neg_index
function physical_vehicles:zzz/vehicle/engine/change_gear_get with storage midwut:main macros

execute if data storage midwut:main Data.data.phys_veh.transmission.transfers[0] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/transfers with storage midwut:main Data.data.phys_veh.transmission

execute if entity @s[tag=phys_veh.tracked_control] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/tracked_control
execute if data storage midwut:main Data.data.phys_veh.sounds[{id:"track"}] run tag @s add phys_veh.track.have_sound
execute if data storage midwut:main Data.data.phys_veh.sounds[{id:"horn"}] run tag @s add phys_veh.have_horn_sound

scoreboard players set @s phys_veh.control.brake 0
scoreboard players set @s phys_veh.control.throttle 0
scoreboard players set @s phys_veh.control.clutch 0
scoreboard players set @s phys_veh.transmission_w 0

execute if score #phys_veh.spawn.is_engine_started main_score matches 1 run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/engine_start

execute store result score #mass_center.x main_score run data get storage midwut:main Data.data.phys_veh.mass_center[0] 1000
execute store result score #mass_center.y main_score run data get storage midwut:main Data.data.phys_veh.mass_center[1] 1000
execute store result score #mass_center.z main_score run data get storage midwut:main Data.data.phys_veh.mass_center[2] 1000

execute store result score @s phys_veh.collision_box.nx run data get storage midwut:main Data.data.phys_veh.collision_box[0] 1000
execute store result score @s phys_veh.collision_box.ny run data get storage midwut:main Data.data.phys_veh.collision_box[1] 1000
execute store result score @s phys_veh.collision_box.nz run data get storage midwut:main Data.data.phys_veh.collision_box[2] 1000
execute store result score @s phys_veh.collision_box.px run data get storage midwut:main Data.data.phys_veh.collision_box[3] 1000
execute store result score @s phys_veh.collision_box.py run data get storage midwut:main Data.data.phys_veh.collision_box[4] 1000
execute store result score @s phys_veh.collision_box.pz run data get storage midwut:main Data.data.phys_veh.collision_box[5] 1000
scoreboard players operation @s phys_veh.collision_box.nx -= #mass_center.x main_score
scoreboard players operation @s phys_veh.collision_box.ny -= #mass_center.y main_score
scoreboard players operation @s phys_veh.collision_box.nz -= #mass_center.z main_score
scoreboard players operation @s phys_veh.collision_box.px -= #mass_center.x main_score
scoreboard players operation @s phys_veh.collision_box.py -= #mass_center.y main_score
scoreboard players operation @s phys_veh.collision_box.pz -= #mass_center.z main_score
execute if data storage midwut:main Data.data.phys_veh.armor_collisions[0] run tag @s add phys_veh.have_armor_collision

scoreboard players operation #x main_score = @s phys_veh.collision_box.nx
execute if score #x main_score matches ..-1 run scoreboard players operation #x main_score *= #-1 const
scoreboard players operation #x2 main_score = @s phys_veh.collision_box.px
execute if score #x2 main_score matches ..-1 run scoreboard players operation #x2 main_score *= #-1 const
scoreboard players operation #x main_score > #x2 main_score
scoreboard players operation #y main_score = @s phys_veh.collision_box.ny
execute if score #y main_score matches ..-1 run scoreboard players operation #y main_score *= #-1 const
scoreboard players operation #y2 main_score = @s phys_veh.collision_box.py
execute if score #y2 main_score matches ..-1 run scoreboard players operation #y2 main_score *= #-1 const
scoreboard players operation #y main_score > #y2 main_score
scoreboard players operation #z main_score = @s phys_veh.collision_box.nz
execute if score #z main_score matches ..-1 run scoreboard players operation #z main_score *= #-1 const
scoreboard players operation #z2 main_score = @s phys_veh.collision_box.pz
execute if score #z2 main_score matches ..-1 run scoreboard players operation #z2 main_score *= #-1 const
scoreboard players operation #z main_score > #z2 main_score

scoreboard players operation #IN:arctan2.X main_score = #z main_score
scoreboard players operation #IN:arctan2.Y main_score = #x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score
scoreboard players operation @s phys_veh.collision_box.radius = #IN:arctan2.X main_score
execute store result entity @s data.phys_veh.collision_box_radius float 0.001 run scoreboard players add @s phys_veh.collision_box.radius 1

scoreboard players operation #temp main_score = #mass_center.x main_score
execute if score #temp main_score matches ..-1 run scoreboard players operation #temp main_score *= #-1 const
scoreboard players operation @s phys_veh.bounding_box.x += #temp main_score
scoreboard players operation #temp main_score = #mass_center.y main_score
execute if score #temp main_score matches ..-1 run scoreboard players operation #temp main_score *= #-1 const
scoreboard players operation @s phys_veh.bounding_box.y += #temp main_score
scoreboard players operation #temp main_score = #mass_center.z main_score
execute if score #temp main_score matches ..-1 run scoreboard players operation #temp main_score *= #-1 const
scoreboard players operation @s phys_veh.bounding_box.z += #temp main_score

scoreboard players operation @s phys_veh.moment_of_inertia.x = @s phys_veh.mass
scoreboard players operation @s phys_veh.moment_of_inertia.y = @s phys_veh.mass
scoreboard players operation @s phys_veh.moment_of_inertia.z = @s phys_veh.mass

scoreboard players operation #x main_score = @s phys_veh.bounding_box.x
scoreboard players operation #x main_score *= #x main_score
scoreboard players operation #x main_score /= #12000 const
scoreboard players operation #y main_score = @s phys_veh.bounding_box.y
scoreboard players operation #y main_score *= #y main_score
scoreboard players operation #y main_score /= #12000 const
scoreboard players operation #z main_score = @s phys_veh.bounding_box.z
scoreboard players operation #z main_score *= #z main_score
scoreboard players operation #z main_score /= #12000 const

scoreboard players operation #temp main_score = #y main_score
scoreboard players operation #temp main_score += #z main_score
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.x = #temp main_score
scoreboard players operation @s phys_veh.moment_of_inertia.x *= #temp main_score

scoreboard players operation #y main_score += #x main_score
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.z = #y main_score
scoreboard players operation @s phys_veh.moment_of_inertia.z *= #y main_score

scoreboard players operation #z main_score += #x main_score
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.y = #z main_score
scoreboard players operation @s phys_veh.moment_of_inertia.y *= #z main_score

scoreboard players operation @s phys_veh.moment_of_inertia.x /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia.y /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia.z /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.x /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.y /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia_no_mass.z /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia_op.x = @s phys_veh.moment_of_inertia.x
scoreboard players operation @s phys_veh.moment_of_inertia_op.y = @s phys_veh.moment_of_inertia.y
scoreboard players operation @s phys_veh.moment_of_inertia_op.z = @s phys_veh.moment_of_inertia.z
scoreboard players operation @s phys_veh.moment_of_inertia_op.x /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia_op.y /= #10 const
scoreboard players operation @s phys_veh.moment_of_inertia_op.z /= #10 const


data modify storage midwut:main macros.x set from storage midwut:main Data.data.phys_veh.mass_center[0]
data modify storage midwut:main macros.y set from storage midwut:main Data.data.phys_veh.mass_center[1]
data modify storage midwut:main macros.z set from storage midwut:main Data.data.phys_veh.mass_center[2]
execute positioned as @s run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/main_tp_mass_center with storage midwut:main macros

execute store result score @s phys_veh.last_pos.x run data get storage midwut:main Data.Pos[0] 1000
execute store result score @s phys_veh.last_pos.y run data get storage midwut:main Data.Pos[1] 1000
execute store result score @s phys_veh.last_pos.z run data get storage midwut:main Data.Pos[2] 1000


data modify storage midwut:main colls set value []
data modify storage midwut:main coll set value [0,0,0]
data modify storage midwut:main temp set from storage midwut:main Data.data.phys_veh.collisions
function physical_vehicles:zzz/summon_data/zzzzzz_data/data/colls
data modify entity @s data.phys_veh.collisions set from storage midwut:main colls

execute if entity @s[tag=phys_veh.have_armor_collision] run function physical_vehicles:zzz/summon_data/zzzzzz_data/data/armor_collisions_pre

tag @s remove get_id