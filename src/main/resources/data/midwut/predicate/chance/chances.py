from os.path import abspath, basename
dir = abspath(__file__).replace(f"{basename(__file__)}", '')

for i in range(1, 100):
    i/=100
    with open(f"{dir}{i}.json","w+") as file:
        file.write(f'''{{
  "condition": "minecraft:random_chance",
  "chance": {i}
}}''')