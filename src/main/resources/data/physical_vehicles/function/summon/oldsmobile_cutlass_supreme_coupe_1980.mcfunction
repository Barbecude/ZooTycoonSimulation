# Main
summon minecraft:text_display ~ ~1.25 ~ {data:{phys_veh:{rolling_in_air:23d,mass:1428,gravity:[0d,-11.03d,0d],bounding_box:[1.0d, 0.5d, 2.5d],mass_center:[0.0f,-0.687f,0.188f],steering_angle:[40d,40d,2.5d,22.5d,18.0d],base_radius:0.359d,aligning_torque_divider:10,\
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
gears:[-2.52, 0.0, 2.52, 1.52, 1.00],\
engine:{friction:[40.0d, 0.10d, 0.0007d],idle_rpm:650,min_rpm:450,redline_rpm:4800,torque_curve_coeff:2,max_torque:373,max_torque_rpm:2000,moment_of_inertia_reversed:1.2d,rpm_scaling_factor:100},\
transmission:{max_torque:600,stiffness:100,final_gear:3.1d,gearbox_efficiency:0.9d},\
collisions:[\
[0.875d, -0.938d, -1.547d],\
[-0.875d, -0.938d, -1.547d],\
[0.875d, -0.938d, 1.547d],\  
[-0.875d, -0.938d, 1.547d],\ 
[0.0d, -0.875d, -1.531d],\   
[0.0d, -0.812d, -2.25d],\    
[0.938d, -0.812d, -2.25d],\  
[-0.938d, -0.812d, -2.25d],\ 
[0.0d, -0.938d, -1.0d],\     
[0.938d, -0.938d, -1.0d],\   
[-0.938d, -0.938d, -1.0d],\  
[0.0d, -0.938d, 0.0d],\      
[0.938d, -0.938d, 0.0d],\    
[-0.938d, -0.938d, 0.0d],\   
[0.0d, -0.938d, 1.0d],\      
[0.938d, -0.938d, 1.0d],\    
[-0.938d, -0.938d, 1.0d],\   
[-0.938d, -0.875d, 2.0d],\   
[0.938d, -0.875d, 2.0d],\
[0.0d, -0.875d, 2.0d],\
[0.0d, -0.875d, 1.531d],\
[0.938d, -0.812d, 2.562d],\
[0.0d, -0.812d, 2.562d],\
[-0.938d, -0.812d, 2.562d],\
[0.938d, -0.375d, 2.422d],\
[0.0d, -0.375d, 2.422d],\
[-0.938d, -0.375d, 2.422d],\
[0.938d, -0.25d, 1.562d],\
[0.0d, -0.25d, 1.562d],\
[-0.938d, -0.25d, 1.562d],\
[0.938d, -0.25d, 1.0d],\
[0.0d, -0.25d, 1.0d],\
[-0.938d, -0.25d, 1.0d],\
[0.938d, -0.25d, 0.125d],\
[-0.938d, -0.25d, 0.125d],\
[0.938d, -0.25d, -0.75d],\
[-0.938d, -0.25d, -0.75d],\
[0.875d, 0.312d, 0.438d],\
[0.0d, 0.312d, 0.438d],\
[-0.875d, 0.312d, 0.438d],\
[-0.875d, 0.312d, -0.438d],\
[0.0d, 0.312d, -0.438d],\
[0.875d, 0.312d, -0.438d],\
[-0.875d, 0.312d, -1.312d],\
[0.0d, 0.312d, -1.312d],\
[0.875d, 0.312d, -1.312d],\
[0.938d, -0.25d, -1.547d],\
[0.0d, -0.25d, -1.547d],\
[-0.938d, -0.25d, -1.547d],\
[-0.938d, -0.25d, -2.422d],\
[0.0d, -0.25d, -2.422d],\
[0.938d, -0.25d, -2.422d],\
[-0.938d, -0.375d, -2.734d],\
[0.0d, -0.375d, -2.734d],\
[0.938d, -0.375d, -2.734d],\
[-0.938d, -0.812d, -2.797d],\
[0.0d, -0.812d, -2.797d],\
[0.938d, -0.812d, -2.797d],\
],collision_box:[-0.938d, -1.0d, -2.875d, 0.938d, 0.312d, 2.625d],coeff_e:0.08d,friction_reduction_ratio:10.0d,\
armor_collisions:[{box:[-0.938d, -1.0d, -2.875d, 0.938d, 0.312d, 2.625d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.debug._particles"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,-0.875d,1.5d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.45d,-0.937d,0.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.45d,-0.937d,0.0d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.45d,-0.937d,-1.125d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.45d,-0.937d,-1.125d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:6d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0d,0.0d,0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10013}},Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[2.0f,2.0f,2.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.469d, -0.427d, 0.555d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10014}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.steering_wheel"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.7d,heat_capacity:2000,temperature:25,temperature0:25,friction_contribution_factor:0.9d,brakes_torque:300,offset:[0.766d,-0.813d,1.531d],longitudinal:{stiffness:1.5d,shape_factor:1.6d,curvature:0.35d,peak_factor:0.95d},lateral:{stiffness:1.2d,shape_factor:1.55d,curvature:-0.55d,peak_factor:0.9d},aligning_torque:{stiffness:9d,shape_factor:2.1d,curvature:0d,peak_factor:0.15d},inertia_radius:0.328d,radius:0.359d,tread_height:0.125d,suspension:{strength:4500,damping:5500,height:0.156d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10015}},Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.7d,heat_capacity:2000,temperature:25,temperature0:25,friction_contribution_factor:0.9d,brakes_torque:300,offset:[-0.766d,-0.813d,1.531d],longitudinal:{stiffness:1.5d,shape_factor:1.6d,curvature:0.35d,peak_factor:0.95d},lateral:{stiffness:1.2d,shape_factor:1.55d,curvature:-0.55d,peak_factor:0.9d},aligning_torque:{stiffness:9d,shape_factor:2.1d,curvature:0d,peak_factor:0.15d},inertia_radius:0.328d,radius:0.359d,tread_height:0.125d,suspension:{strength:4500,damping:5500,height:0.156d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10015}},Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.7d,heat_capacity:2000,temperature:25,temperature0:25,friction_contribution_factor:0.9d,brakes_torque:200,hand_brake_torque:400,offset:[-0.766d,-0.813d,-1.531d],longitudinal:{stiffness:1.5d,shape_factor:1.6d,curvature:0.35d,peak_factor:0.95d},lateral:{stiffness:1.2d,shape_factor:1.55d,curvature:-0.55d,peak_factor:0.9d},inertia_radius:0.328d,radius:0.359d,tread_height:0.125d,suspension:{strength:4500,damping:5500,height:0.156d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10015}},Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.7d,heat_capacity:2000,temperature:25,temperature0:25,friction_contribution_factor:0.9d,brakes_torque:200,hand_brake_torque:400,offset:[0.766d,-0.813d,-1.531d],longitudinal:{stiffness:1.5d,shape_factor:1.6d,curvature:0.35d,peak_factor:0.95d},lateral:{stiffness:1.2d,shape_factor:1.55d,curvature:-0.55d,peak_factor:0.9d},inertia_radius:0.328d,radius:0.359d,tread_height:0.125d,suspension:{strength:4500,damping:5500,height:0.156d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10015}},Tags:["phys_veh.oldsmobile_cutlass_supreme_coupe_1980","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~3 ~

# Data
function physical_vehicles:zzz/summon_data/main
