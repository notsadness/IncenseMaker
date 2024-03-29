
## Getting Started
The Incense Maker script supports all incense sticks types, creating these from their base logs through to adding the required herb. 

The script is currently developed to operate independently in three phases.
1. Craft Incense Sticks: Crafting of the logs
2. Ash Incense Sticks: Adding ash to the crafted incense sticks
3. Herb Incense Sticks: Adding the herb to the ashed incense sticks, completing the process

To start:

1.	Setup 3 bank presets:
	* Logs to be crafted into the base incense stick
	* 13 incense sticks and 26 ashes (2:1 ratio)
	* 27 incense sticks and 27 of the corresponding herb
2.	Select one of the three script options and it's corresponding preset 
3. Click start script

## Notes
The script contains fail safes against selecting incompatible combinations of either incense sticks, ashes or herbs. 

The script has 5 core states:
1. STOPPED
2. IDLE
3. RUNNING
4. PROCESSING
5. BANKING

**IDLE**: is used when the script returns from one of it's methods (ie, no logs in the bank).

**STOPPED**: is invoked via the "Stop Script" button, or when ***Logout on Completion*** is ticked in the ImGui.  When the logout option is selected, the script will first go into an IDLE state, followed by STOPPED when the actual logout occurs..

**RUNNING**: When the core script is executing.

**PROCESSING**: This state is effectively a "wait" function, which loops until Interface *1251* (the item production interface) is closed.  It then moves to the ***RUNNING*** state.

**BANKING**: Retrieves items from bank preset, and confirms items are present in the inventory.

## To Do List
1.	~~Refactor and remove the many redundant/duplicate methods~~
2.	~~Modify existing methods to use item name only instead of a combination of IDs and item names (I came across an issue with item stacks which only seemed to work when using an ID, not an item name. More testing is needed)~~
3.  ~~Move to switch/case for the required resources (logs/ashes/herbs), returning it's completed variant~~
4.	Add a task queue system
5.	~~Add script progress to the ImGui window~~