# Main
summon minecraft:text_display ~ ~1.0 ~ {data:{phys_veh:{rolling_in_air:14d,mass:7000,gravity:[0d,-11.03d,0d],bounding_box:[1.5d, 1.25d, 2.5d],mass_center:[0.0d,0.313d,0d],steering_angle:[30d,30d,1.9d,20d,15d],base_radius:0.688d,aligning_torque_divider:200,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.1",duration:4s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:other.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:other.ignition_starting",duration:2s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:other.engine.0",duration:2s,base_volume:5.0f,base_rpm:5000,multiplier:80},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_truck_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_truck_removed",volume:1f,pitch:1f}\
],\
gears:[-3.60d, 0.0d, 4.70d, 2.97d, 2.07d, 1.44d, 1.00d, 0.73d],\
engine:{friction:[60.0, 0.20, 0.002],idle_rpm:700,min_rpm:500,redline_rpm:3500,torque_curve_coeff:3,max_torque:1000,max_torque_rpm:1500,moment_of_inertia_reversed:0.8d,rpm_scaling_factor:100},\
transmission:{max_torque:3000,stiffness:250,final_gear:4.1d,gearbox_efficiency:0.93d},\
collisions:[\
[1.312d, -0.187d, 3.25d],\
[0.438d, -0.187d, 3.25d],\
[-0.438d, -0.187d, 3.25d],\
[-1.312d, -0.187d, 3.25d],\
[1.25d, 1.0d, 3.125d],\
[0.0d, 1.0d, 3.125d],\
[-1.188d, 1.0d, 3.125d],\
[1.375d, 2.125d, 0.938d],\
[1.375d, 1.313d, 1.625d],\
[0.0d, 1.313d, 1.625d],\
[-1.375d, 1.313d, 1.625d],\
[0.438d, 2.125d, 0.938d],\
[-0.438d, 2.125d, 0.938d],\
[-1.375d, 2.125d, 0.938d],\
[1.375d, 2.125d, -3.5d],\
[-1.375d, 2.125d, -3.5d],\
[0.438d, 2.125d, -3.5d],\
[-0.438d, 2.125d, -3.5d],\
[1.375d, 1.125d, -3.5d],\
[0.438d, 1.125d, -3.5d],\
[-0.438d, 1.125d, -3.5d],\
[-1.375d, 1.125d, -3.5d],\
[-1.5d, 0.125d, -3.375d],\
[-0.5d, 0.125d, -3.562d],\
[0.5d, 0.125d, -3.562d],\
[1.5d, 0.125d, -3.375d],\
[1.5d, -0.125d, -1.0d],\
[1.5d, -0.125d, 0.0d],\
[1.5d, -0.125d, 1.0d],\
[-1.5d, -0.125d, 1.0d],\
[-1.5d, -0.125d, 0.0d],\
[-1.5d, -0.125d, -1.0d],\
[1.375d, 2.125d, -2.25d],\
[1.375d, 2.125d, -1.188d],\
[1.375d, 2.125d, -0.125d],\
[-1.375d, 2.125d, -2.25d],\
[-1.375d, 2.125d, -1.188d],\
[-1.375d, 2.125d, -0.125d],\
[1.375d, -0.313d, 2.125d],\
[1.375d, -0.313d, -2.188d],\
[-1.375d, -0.313d, -2.188d],\
[-1.375d, -0.313d, 2.125d],\
],collision_box:[-1.5d, -0.437d, -3.5d, 1.5d, 2.125d, 3.375d],coeff_e:0.1d,friction_reduction_ratio:2.0d,\
armor_collisions:[{box:[-1.5d, -0.437d, -3.5d, 1.5d, 2.125d, 3.375d],thickness:40,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.have_locking_center_differential","phys_veh.debug._particles","phys_veh.debug._back_last_location"],teleport_duration:2,interpolation_duration:2}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,0.0d,2.25d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.armored_truck_4x4","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.688d,0.781d,0.188d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.688d,0.781d,0.188d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.688d,0.781d,-1.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.688d,0.781d,-1.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0d,1.063d,0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10029}},Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[4.0f,4.0f,4.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.696d, 1.191d, 0.884d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10030}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.steering_wheel"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:200,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:0,brakes_torque:7000,offset:[1.216d, 0.0d, 2.13d],longitudinal:{stiffness:1.8d,shape_factor:1.65d,curvature:0.2d,peak_factor:1.05d},lateral:{stiffness:1.4d,shape_factor:1.5d,curvature:-8d,peak_factor:0.95d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.6d,peak_factor:0.7d},inertia_radius:0.593d,radius:0.688d,tread_height:0.312d,suspension:{strength:9800,damping:30000,height:0.437d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10031}},Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:200,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:0,brakes_torque:7000,offset:[-1.216d, 0.0d, 2.13d],longitudinal:{stiffness:1.8d,shape_factor:1.65d,curvature:0.2d,peak_factor:1.05d},lateral:{stiffness:1.4d,shape_factor:1.5d,curvature:-8d,peak_factor:0.95d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.6d,peak_factor:0.7d},inertia_radius:0.593d,radius:0.688d,tread_height:0.312d,suspension:{strength:9800,damping:30000,height:0.437d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10032}},Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:200,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:1,brakes_torque:6000,hand_brake_torque:5000,offset:[-1.216d, 0.0d, -2.13d],longitudinal:{stiffness:1.8d,shape_factor:1.65d,curvature:0.2d,peak_factor:1.05d},lateral:{stiffness:1.4d,shape_factor:1.5d,curvature:-8d,peak_factor:0.95d},inertia_radius:0.593d,radius:0.688d,tread_height:0.312d,suspension:{strength:9800,damping:30000,height:0.437d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10032}},Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:200,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,wheel_axle_id:1,brakes_torque:6000,hand_brake_torque:5000,offset:[1.216d, 0.0d, -2.13d],longitudinal:{stiffness:1.8d,shape_factor:1.65d,curvature:0.2d,peak_factor:1.05d},lateral:{stiffness:1.4d,shape_factor:1.5d,curvature:-8d,peak_factor:0.95d},inertia_radius:0.593d,radius:0.688d,tread_height:0.312d,suspension:{strength:9800,damping:30000,height:0.437d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10031}},Tags:["phys_veh.armored_truck_4x4","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.add_angle.x 7853
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~5 ~

# Data
function physical_vehicles:zzz/summon_data/main
