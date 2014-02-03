/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.seasons.gui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.input.Keyboard;
import org.terasology.input.binds.inventory.InventoryButton;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.seasons.Season;
import org.terasology.utilities.OrdinalIndicator;
import org.terasology.world.WorldComponent;
import org.terasology.world.time.OnMidnightEvent;

/**
 * Created by Linus on 1/28/14.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class GuiSystem implements ComponentSystem {

    @In
    GUIManager guiManager;

    @Override
    public void initialise() {
        guiManager.registerWindow("seasons:calendar", CalendarWindow.class);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onToggleInventory(CalendarKey event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            guiManager.openWindow("seasons:calendar");
            event.consume();
        }
    }

    @Override
    public void shutdown() {

    }


}
