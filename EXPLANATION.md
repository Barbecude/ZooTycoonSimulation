# IndoZoo Tycoon Codebase Analysis

This codebase implements a Minecraft mod called **"IndoZoo Tycoon"**, which adds a comprehensive zoo management simulation to the game.

## Core Features

### 1. Zoo Management System
*   **Economy**: Tracks your zoo's balance (money), ticket prices, and marketing level.
*   **Staff**: You can hire staff members to help manage the zoo:
    *   **Janitor**: Cleans up trash (TrashBlock).
    *   **Zookeeper**: Feeds animals.
    *   **Security**: Protects visitors from hostile mobs or bad visitors.
*   **Visitors**: AI entities that visit your zoo, generating ticket revenue.
*   **Rating**: A zoo rating system (0-100) that likely affects visitor spawn rates.

### 2. Zoo Computer (GUI)
*   The central hub for managing your zoo is the **Zoo Computer Block**.
*   It features a dashboard showing statistics (Balance, Staff, Visitors, Animals, Rating).
*   It provides a shop interface to purchase:
    *   **Animals**: Categorized into Land, Aquatic, Mythical, and Bugs.
    *   **Buildings**: Blocks, fences, paths, and decorations.
    *   **Food**: For animals.
    *   **Vehicles**: Minecarts and boats.
*   Allows renaming and releasing animals.

### 3. Mod Integrations
The mod is designed to work with several popular mods to provide content:
*   **Alex's Mobs**: Adds support for animals like Elephants, Tigers, Gorillas, Kangaroos, Komodo Dragons, and more.
*   **Naturalist**: Adds support for Giraffes, Zebras, Lions, Rhinos, etc.
*   **Macaw's Mods**: Automatically detects and sells building blocks (Fences, Bridges, Furniture, Roofs, etc.) from Macaw's mods.
*   **Pehkui**: Supports resizing entities (e.g., changing animal sizes) using reflection to avoid hard dependencies.

### 4. Custom Blocks & Items
*   **Utility**: Zoo Banner, Animal Tag, Capture Cage, Biome Changer.
*   **Functional**: Food Stall, Drink Stall, Animal Feeder, Restroom, Trash Can.
*   **Zone Marker**: To define zoo areas.

## Technical Implementation
*   **Data Storage**: Global zoo data is stored in `ZooData` (Server-side `SavedData`) and synced to `ClientZooData` for the UI.
*   **Commands**: A `zoocmd` command suite handles actions like `buy`, `hire`, `rename`, `setrating`, and `setbiome`.
*   **Networking**: Uses packets (`SyncBalancePacket`) to synchronize data between server and client.
