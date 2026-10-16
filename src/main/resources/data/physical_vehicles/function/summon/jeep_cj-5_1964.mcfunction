# Main
summon minecraft:text_display ~ ~0.5 ~ {data:{phys_veh:{rolling_in_air:28d,mass:1150,gravity:[0d,-11.03d,0d],bounding_box:[1.0d, 0.75d, 2.0d],mass_center:[0.0f,0.125f,0.125f],steering_angle:[32d,32d,2.0d,22.5d,19.69d],base_radius:0.393d,aligning_torque_divider:10,\
sounds:[\
  {id:"horn",sound_id:"physical_vehicles:other.horn.1",duration:4s,volume:5f,pitch:1f},\
  {id:"switching_gear_start",sound_id:"physical_vehicles:other.switching_gear.start",volume:0.5f,pitch:1f},\
  {id:"switching_gear_finish",sound_id:"physical_vehicles:other.switching_gear.finish",volume:0.5f,pitch:1f},\
  {id:"ignition_start",sound_id:"physical_vehicles:other.ignition_start",duration:5s,volume:1.0f,pitch:1.0f},\
  {id:"ignition_starting",sound_id:"physical_vehicles:other.ignition_starting",duration:2s,volume:3.0f,pitch:1.0f},\
  {id:"engine",sound_id:"physical_vehicles:other.engine.0",duration:2s,base_volume:5.0f,base_rpm:5000,multiplier:80},\
  {id:"handbrake_tightened",sound_id:"physical_vehicles:other.handbrake_tightened",volume:1f,pitch:1f},\
  {id:"handbrake_removed",sound_id:"physical_vehicles:other.handbrake_removed",volume:1f,pitch:1f}\
],\
gears:[-3.8d, 0.0d, 2.8d, 1.6d, 1.0d],\
engine:{friction:[12.0d, 0.035d, 0.00051d],idle_rpm:800,min_rpm:450,redline_rpm:4800,torque_curve_coeff:2,max_torque:155,max_torque_rpm:2400,moment_of_inertia_reversed:8.3,rpm_scaling_factor:100},\
transmission:{max_torque:180,stiffness:60,final_gear:5.4d,gearbox_efficiency:0.92d},\
collisions:[\
[0.75d, -0.031d, 1.164d],\
[0.0d, -0.062d, 1.227d],\
[-0.75d, -0.031d, 1.164d],\
[0.75d, -0.031d, -1.148d],\
[0.0d, -0.031d, -1.148d],\
[-0.75d, -0.031d, -1.148d],\
[0.75d, 0.0d, 1.797d],\
[0.0d, 0.0d, 1.797d],\
[-0.75d, 0.0d, 1.797d],\
[0.5d, 0.719d, 1.602d],\
[-0.5d, 0.719d, 1.602d],\
[-0.75d, 0.719d, 0.477d],\
[0.0d, 0.719d, 0.477d],\
[0.75d, 0.719d, 0.477d],\
[0.0d, 1.407d, 0.227d],\
[0.75d, 1.407d, 0.227d],\
[-0.75d, 1.407d, 0.227d],\
[0.625d, 0.719d, 0.914d],\
[-0.625d, 0.719d, 0.914d],\
[0.0d, 0.625d, -1.836d],\
[-0.75d, 0.625d, -1.836d],\
[-0.75d, 0.625d, -0.836d],\
[0.75d, 0.625d, -1.836d],\
[0.75d, 0.625d, -0.836d],\
[-0.75d, -0.062d, 0.0d],\
[0.0d, -0.062d, 0.375d],\
[0.0d, -0.062d, -0.562d],\
[0.75d, -0.062d, 0.0d],\
[-0.75d, 0.0d, -1.961d],\
[0.0d, 0.0d, -1.961d],\
[0.75d, 0.0d, -1.961d],\
],collision_box:[-0.75d, -0.187d, -1.875d, 0.75d, 1.188d, 1.75d],coeff_e:0.08d,friction_reduction_ratio:13.0d,\
armor_collisions:[{box:[-0.75d, -0.187d, -1.875d, 0.75d, 1.188d, 1.75d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.debug._particles"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,-0.062d,1.0d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.375d,0.25d,-0.313d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:4d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.375d,0.25d,-0.313d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:4d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,0.563d,0.0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10010}},Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[2.0f,2.0f,2.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.386d, 0.817d, 0.101d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10011}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.steering_wheel"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:300,offset:[0.668d, 0.0d, 1.16d],longitudinal:{stiffness:1.7d,shape_factor:1.6d,curvature:0.3d,peak_factor:1.0d},lateral:{stiffness:1.4d,shape_factor:1.55d,curvature:-0.5d,peak_factor:0.95d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.5d,peak_factor:0.2d},inertia_radius:0.296d,radius:0.393d,tread_height:0.125d,suspension:{strength:3500,damping:5000,height:0.156d,max_delta:0.07d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10012}},Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:300,offset:[-0.668d, 0.0d, 1.16d],longitudinal:{stiffness:1.7d,shape_factor:1.6d,curvature:0.3d,peak_factor:1.0d},lateral:{stiffness:1.4d,shape_factor:1.55d,curvature:-0.5d,peak_factor:0.95d},aligning_torque:{stiffness:8d,shape_factor:2d,curvature:0.5d,peak_factor:0.2d},inertia_radius:0.296d,radius:0.393d,tread_height:0.125d,suspension:{strength:3500,damping:5000,height:0.156d,max_delta:0.07d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10012}},Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:200,hand_brake_torque:400,offset:[-0.668d, 0.0d, -1.16d],longitudinal:{stiffness:1.7d,shape_factor:1.6d,curvature:0.3d,peak_factor:1.0d},lateral:{stiffness:1.4d,shape_factor:1.55d,curvature:-0.5d,peak_factor:0.95d},inertia_radius:0.296d,radius:0.393d,tread_height:0.125d,suspension:{strength:3500,damping:5000,height:0.156d,max_delta:0.07d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10012}},Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:30,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.6d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:200,hand_brake_torque:400,offset:[0.668d, 0.0d, -1.16d],longitudinal:{stiffness:1.7d,shape_factor:1.6d,curvature:0.3d,peak_factor:1.0d},lateral:{stiffness:1.4d,shape_factor:1.55d,curvature:-0.5d,peak_factor:0.95d},inertia_radius:0.296d,radius:0.393d,tread_height:0.125d,suspension:{strength:3500,damping:5000,height:0.156d,max_delta:0.07d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10012}},Tags:["phys_veh.jeep_cj-5_1964","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~3 ~

# Data
function physical_vehicles:zzz/summon_data/main
