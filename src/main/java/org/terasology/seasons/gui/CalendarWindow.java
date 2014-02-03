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

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.input.BindButtonEvent;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.BindKeyListener;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.gui.widgets.UIButton;

/**
 * Created by Linus on 1/28/14.
 */
public class CalendarWindow extends UIWindow {

    private UILabel titleLabel;
    private UIButton nextYear;
    private UIButton previousYear;
    private UILabel currentYear;


    public CalendarWindow() {
        setId("seasons:calendar");
        //maximize();
        setBackgroundColor(new Color(0,0,0));
        setVisible(true);

        this.setHorizontalAlign(EHorizontalAlign.CENTER);
        this.setVerticalAlign(EVerticalAlign.CENTER);

        setCloseBinds(new String[]{"engine:frob"});
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE, Keyboard.KEY_P});

        titleLabel = new UILabel("Title");
        titleLabel.setVisible(true);
        titleLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        titleLabel.setVerticalAlign(EVerticalAlign.TOP);


        addDisplayElement(titleLabel);

        layout();
    }
}
