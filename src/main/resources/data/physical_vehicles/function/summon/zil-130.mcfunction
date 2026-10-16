# Main
summon minecraft:text_display ~ ~0.75 ~ {data:{phys_veh:{rolling_in_air:17d,mass:4415,gravity:[0d,-11.03d,0d],bounding_box:[1.25d, 1d, 3d],mass_center:[0.0d,0.5d,0.25d],steering_angle:[40d,40d,2.0d,45d,22.5d],base_radius:0.545d,aligning_torque_divider:100,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.2",duration:2s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:other.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:other.ignition_starting",duration:2s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:other.engine.0",duration:2s,base_volume:5.0f,base_rpm:5000,multiplier:80},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_truck_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_truck_removed",volume:1f,pitch:1f}\
],\
gears:[-7.1d, 0d, 7.4d, 4.1d, 2.3d, 1.5d, 1.0d],\
engine:{friction:[30d,0.1d,0.0007d],idle_rpm:800,min_rpm:400,redline_rpm:6000,torque_curve_coeff:2,max_torque:383,max_torque_rpm:2000,moment_of_inertia_reversed:1,rpm_scaling_factor:100},\
transmission:{max_torque:500,stiffness:200,final_gear:6.3d,gearbox_efficiency:0.95d},\
collisions:[\
[-0.562d, 1.124d, 3.023d],\
[0.562d, 1.124d, 3.023d],\
[1.125d, 0.843d, 3.023d],\
[1.239d, 0.843d, 1.688d],\
[0.914d, 1.828d, 1.547d],\
[-0.914d, 1.828d, 1.547d],\
[-0.914d, 1.828d, 0.281d],\
[0.914d, 1.828d, 0.281d],\
[0.914d, 0.07d, 0.281d],\
[-0.914d, 0.07d, 0.281d],\
[-1.222d, 0.843d, 1.688d],\
[-1.406d, 0.562d, -4.078d],\
[1.406d, 0.562d, -4.078d],\
[0.0d, 0.562d, -4.078d],\
[-1.406d, 1.406d, -4.078d],\
[1.406d, 1.406d, -4.078d],\
[0.0d, 1.406d, -4.078d],\
[1.406d, 1.398d, 0.141d],\
[-1.406d, 1.398d, 0.141d],\
[1.406d, 0.562d, 0.141d],\
[-1.406d, 0.562d, 0.141d],\
[1.406d, 0.562d, -0.797d],\
[-1.406d, 0.562d, -0.797d],\
[1.406d, 0.562d, -1.734d],\
[-1.406d, 0.562d, -1.734d],\
[1.406d, 0.562d, -2.672d],\
[-1.406d, 0.562d, -2.672d],\
[1.406d, 0.562d, -3.547d],\
[-1.406d, 0.562d, -3.547d],\
[0.703d, -0.001d, -3.656d],\
[-0.703d, -0.001d, -3.656d],\
[-0.703d, -0.001d, -2.719d],\
[0.703d, -0.001d, -2.719d],\
[-0.703d, -0.001d, -0.469d],\
[0.703d, -0.001d, -0.469d],\
[-1.125d, 0.843d, 3.023d],\
[-1.055d, -0.001d, 3.094d],\
[0.0d, -0.001d, 3.305d],\
[1.055d, -0.001d, 3.094d],\
[0.0d, -0.087d, 1.039d],\
[0.0d, -0.087d, 0.039d],\
[0.0d, -0.087d, -0.961d],\
[1.0d, -0.087d, -2.039d],\
[0.0d, -0.087d, -2.039d],\
[-1.0d, -0.087d, -2.039d],\
[1.0d, -0.087d, 2.039d],\
[0.0d, -0.087d, 2.039d],\
[-1.0d, -0.087d, 2.039d],\
],collision_box:[-1.312d, -0.212d, -4.062d, 1.312d, 1.788d, 3.188d],coeff_e:0.07d,friction_reduction_ratio:2.5d,\
armor_collisions:[{box:[-1.312d, -0.212d, -4.062d, 1.312d, 1.788d, 3.188d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.debug._particles","phys_veh.debug._back_last_location"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,0.25d,2.125d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.zil-130","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.zil-130","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.492d, 0.429d, 0.773d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.zil-130","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.492d, 0.429d, 0.773d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.zil-130","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,0.351d,0.0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10025}},Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[4.5f,4.5f,4.5f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.527d, 0.933d, 1.176d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10026}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.steering_wheel"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:45,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:1600,offset:[1.019d, 0.0d, 2.038d],longitudinal:{stiffness:1.3d,shape_factor:1.55d,curvature:0.6d,peak_factor:0.9d},lateral:{stiffness:1.0d,shape_factor:1.45d,curvature:0.5d,peak_factor:0.8d},aligning_torque:{stiffness:10d,shape_factor:2d,curvature:0.0d,peak_factor:0.4d},inertia_radius:0.5d,radius:0.545d,tread_height:0.188d,suspension:{strength:10000,damping:16000,height:0.281d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10027}},Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.125f,1.125f,1.125f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:45,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:1600,offset:[-1.019d, 0.0d, 2.038d],longitudinal:{stiffness:1.3d,shape_factor:1.55d,curvature:0.6d,peak_factor:0.9d},lateral:{stiffness:1.0d,shape_factor:1.45d,curvature:0.5d,peak_factor:0.8d},aligning_torque:{stiffness:10d,shape_factor:2d,curvature:0.0d,peak_factor:0.4d},inertia_radius:0.5d,radius:0.545d,tread_height:0.188d,suspension:{strength:10000,damping:16000,height:0.281d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10027}},Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.125f,1.125f,1.125f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:90,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:2000,hand_brake_torque:3000,offset:[-1.089d, 0.0d, -2.038d],longitudinal:{stiffness:1.7d,shape_factor:1.55d,curvature:0.55d,peak_factor:1.0d},lateral:{stiffness:1.4d,shape_factor:1.45d,curvature:0.45d,peak_factor:0.9d},inertia_radius:0.5d,radius:0.545d,tread_height:0.188d,suspension:{strength:10000,damping:16000,height:0.281d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10028}},Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.125f,1.125f,1.125f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:90,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.2d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:2000,hand_brake_torque:3000,offset:[1.089d, 0.0d, -2.038d],longitudinal:{stiffness:1.7d,shape_factor:1.55d,curvature:0.55d,peak_factor:1.0d},lateral:{stiffness:1.4d,shape_factor:1.45d,curvature:0.45d,peak_factor:0.9d},inertia_radius:0.5d,radius:0.545d,tread_height:0.188d,suspension:{strength:10000,damping:16000,height:0.281d,max_delta:0.2d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10028}},Tags:["phys_veh.zil-130","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.125f,1.125f,1.125f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0

# Data
function physical_vehicles:zzz/summon_data/main
