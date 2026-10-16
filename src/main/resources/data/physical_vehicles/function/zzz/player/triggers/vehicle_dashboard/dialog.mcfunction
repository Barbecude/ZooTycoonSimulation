$dialog show @s {\
  "type": "minecraft:confirmation",\
  "title": {\
    "translate": "phys_veh.vehicle_dashboard.title"\
  },\
  "inputs": $(vehicle_dashboard),\
  "can_close_with_escape": true,\
  "pause": false,\
  "yes": {\
    "label": {\
      "translate": "gui.done"\
    },\
    "action": {\
      "type": "minecraft:dynamic/run_command",\
      "template": "function physical_vehicles:zzz/vehicle/vehicle_dashboard/set {data:{$(value)}}"\
    }\
  },\
  "no": {\
    "label": {\
      "translate": "gui.cancel"\
    }\
  }\
}