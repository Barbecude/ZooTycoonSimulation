# Main
summon minecraft:text_display ~ ~1.25 ~ {data:{phys_veh:{rolling_in_air:23d,mass:2000,gravity:[0d,-11.03d,0d],bounding_box:[1.25d, 1.25d, 2.5d],mass_center:[0.0f,-0.125f,0.0f],steering_angle:[35d,35d,1.9d,22.5d,23.14d],base_radius:0.437d,aligning_torque_divider:10,\
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
gears:[-5.2d, 0.0d, 4.1d, 2.6d, 1.6d, 1.0d],\
engine:{friction:[20.0d, 0.08d, 0.00061d],idle_rpm:800,min_rpm:450,redline_rpm:4200,torque_curve_coeff:2,max_torque:167,max_torque_rpm:2500,moment_of_inertia_reversed:5,rpm_scaling_factor:100},\
transmission:{max_torque:220,stiffness:120,final_gear:4.6d,gearbox_efficiency:0.92d,transfers:[{value:1.0d,display:{"translate":"phys_veh.transfer.direct"}},{value:1.9d,display:{"translate":"phys_veh.transfer.low2"}}],current_transfer:0},\
collisions:[\
[1.125d, -0.375d, 2.812d],\
[0.0d, -0.375d, 2.812d],\   
[-1.125d, -0.375d, 2.812d],\
[0.0d, -0.375d, 1.812d],\   
[0.0d, -0.375d, 0.0d],\     
[1.125d, -0.375d, 1.0d],\   
[-1.125d, -0.375d, 1.0d],\  
[1.125d, -0.375d, 0.0d],\
[-1.125d, -0.375d, 0.0d],\
[1.125d, -0.375d, -1.0d],\
[1.125d, 0.625d, 1.0d],\
[1.125d, 0.625d, -1.0d],\
[-1.125d, 0.625d, 1.0d],\
[-1.125d, 0.625d, -1.0d],\
[-1.125d, -0.375d, -1.0d],\
[1.125d, -0.375d, -2.625d],\
[0.0d, -0.375d, -2.625d],\
[0.0d, -0.375d, -1.625d],\
[-1.125d, -0.375d, -2.625d],\
[-1.125d, 0.688d, 2.75d],\
[0.0d, 0.688d, 2.75d],\
[1.125d, 0.688d, 2.75d],\
[1.125d, 0.688d, -2.562d],\
[0.0d, 0.688d, -2.562d],\
[-1.125d, 0.688d, -2.562d],\
[1.125d, 1.688d, 2.375d],\
[0.0d, 1.688d, 2.375d],\
[-1.125d, 1.688d, 2.375d],\
[1.125d, 1.688d, 0.5d],\
[0.0d, 1.688d, 0.0d],\
[-1.125d, 1.688d, 0.5d],\
[1.125d, 1.688d, -0.5d],\
[-1.125d, 1.688d, -0.5d],\
[1.125d, 1.688d, -2.562d],\
[0.0d, 1.688d, -2.562d],\
[-1.125d, 1.688d, -2.562d],\
[1.0d, -0.625d, 1.562d],\
[-1.0d, -0.625d, 1.562d],\
[1.0d, -0.625d, -1.5d],\
[-1.0d, -0.625d, -1.5d],\
],collision_box:[-1.125d, -0.562d, -2.562d, 1.125d, 1.688d, 2.75d],coeff_e:0.07d,friction_reduction_ratio:8.0d,\
armor_collisions:[{box:[-1.125d, -0.562d, -2.562d, 1.125d, 1.688d, 2.75d],thickness:5,components:[{id:"engine",chance:0.3},{id:"tank",chance:0.2}]}]\
}},Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.main","phys_veh.car","phys_veh.have_collision","phys_veh.debug._particles"]}

# Engine hitbox
summon minecraft:text_display ~ ~ ~ {data:{phys_veh:{offset:[0.0d,-0.25d,2.0d]}},Passengers:[{id:"minecraft:rabbit",DeathLootTable:"",Silent:1b,Health:1024f,attributes:[{id:"minecraft:max_health",base:1024d}],NoAI:1b,Tags:["phys_veh.buhanka","physical_vehicle","ignore.passable_leaves","phys_veh.disable.rotation","phys_veh.disable.offset","get_id","phys_veh.hp"],NoGravity:1b,active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b}]}],Tags:["phys_veh.buhanka","physical_vehicle","phys_veh.disable.rotation","get_id","phys_veh.hp"]}

# Seats
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[0.484d,0.25d,1.5d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.buhanka","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.llama_seat_dr","phys_veh.disable.offset"]}],Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.seat_dr","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}
summon text_display ~ ~ ~ {data:{phys_veh:{offset:[-0.484d,0.25d,1.5d]}},Passengers:[{DeathLootTable:"",Tame:1b,Silent:1b,Invulnerable:1b,NoAI:1b,NoGravity:1b,attributes:[{id:"minecraft:max_health",base:1024d},{id:"minecraft:camera_distance",base:8d},{id:"minecraft:scale",base:0.75d}],Health:20f,id:"minecraft:llama",active_effects:[{id:"minecraft:invisibility",duration:-1,amplifier:127b,show_particles:0b},{id:"minecraft:resistance",duration:-1,amplifier:127b,show_particles:0b}],Tags:["phys_veh.buhanka","physical_vehicle","ignore.passable_leaves","get_id","phys_veh.seat","phys_veh.llama_seat","phys_veh.disable.offset"]}],Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.seat","phys_veh.seat_pos","phys_veh.disable.rotation"]}

# Model
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0d,0.0d,0d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10001}},Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.model"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[2.0f,2.0f,2.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{offset:[0.469d, 0.675d, 2.029d]}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10002}},teleport_duration:1,interpolation_duration:1,Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.steering_wheel"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}

# Wheels
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:1000,offset:[0.843d,-0.468d,1.531d],longitudinal:{stiffness:1.6d,shape_factor:1.7d,curvature:-0.6d,peak_factor:1.0d},lateral:{stiffness:1.3d,shape_factor:1.6d,curvature:-0.4d,peak_factor:0.94d},aligning_torque:{stiffness:8d,shape_factor:2.0d,curvature:0.6d,peak_factor:0.13d},inertia_radius:0.406d,radius:0.437d,tread_height:0.188d,suspension:{strength:5000,damping:8000,height:0.25d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10003}},Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:1000,offset:[-0.843d,-0.468d,1.531d],longitudinal:{stiffness:1.6d,shape_factor:1.7d,curvature:-0.6d,peak_factor:1.0d},lateral:{stiffness:1.3d,shape_factor:1.6d,curvature:-0.4d,peak_factor:0.94d},aligning_torque:{stiffness:8d,shape_factor:2.0d,curvature:0.6d,peak_factor:0.13d},inertia_radius:0.406d,radius:0.437d,tread_height:0.188d,suspension:{strength:5000,damping:8000,height:0.25d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10004}},Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.steering","phys_veh.wheel.braking"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:600,hand_brake_torque:900,offset:[-0.843d,-0.468d,-1.531d],longitudinal:{stiffness:1.6d,shape_factor:1.7d,curvature:-0.6d,peak_factor:1.0d},lateral:{stiffness:1.3d,shape_factor:1.6d,curvature:-0.4d,peak_factor:0.94d},inertia_radius:0.406d,radius:0.437d,tread_height:0.188d,suspension:{strength:5000,damping:8000,height:0.25d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10004}},Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}
summon item_display ~ ~ ~ {data:{phys_veh:{mass:20,pressure:2.2f,pressure0:2.2f,pressure_coeff:0.1f,smoke_factor:0.4d,heat_capacity:2000,temperature:25,temperature0:25,brakes_torque:600,hand_brake_torque:900,offset:[0.843d,-0.468d,-1.531d],longitudinal:{stiffness:1.6d,shape_factor:1.7d,curvature:-0.6d,peak_factor:1.0d},lateral:{stiffness:1.3d,shape_factor:1.6d,curvature:-0.4d,peak_factor:0.94d},inertia_radius:0.406d,radius:0.437d,tread_height:0.188d,suspension:{strength:5000,damping:8000,height:0.25d,max_delta:0.1d}}},item:{id:"minecraft:structure_void",components:{"minecraft:custom_model_data":10003}},Tags:["phys_veh.buhanka","physical_vehicle","get_id","phys_veh.wheel","phys_veh.wheel.driving","phys_veh.wheel.braking","phys_veh.wheel.hand_brake"],transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,0.0f,0.0f,1.0f],right_rotation:[0.0f,0.0f,0.0f,1.0f],scale:[1.0f,1.0f,1.0f]},teleport_duration:1,interpolation_duration:1}

# Custom Properties
#scoreboard players set @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] phys_veh.gravity.y 0
#tp @e[type=minecraft:text_display,tag=phys_veh.main,tag=get_id,limit=1] ~ ~3 ~

# Data
function physical_vehicles:zzz/summon_data/main
