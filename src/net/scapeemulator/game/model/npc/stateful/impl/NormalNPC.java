/**
 * Copyright (c) 2012, Hadyn Richard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
 * THE SOFTWARE.
 */

package net.scapeemulator.game.model.npc.stateful.impl;

import static net.scapeemulator.game.model.npc.stateful.impl.NormalNPC.State.*;

import java.util.Random;

import net.scapeemulator.game.model.npc.stateful.StatefulNPC;
import net.scapeemulator.game.model.npc.stateful.handler.RandomWalkStateHandler;
import net.scapeemulator.game.model.npc.stateful.handler.WalkToSpawnStateHandler;
import net.scapeemulator.game.model.npc.stateful.impl.NormalNPC.State;

/**
 * Created by Hadyn Richard
 */
public final class NormalNPC extends StatefulNPC<State> {

	/**
	 * The random used to calculate if the NPC should randomly walk.
	 */
	private static final Random random = new Random();

	/**
	 * Each of the enumerable states.
	 */
	public enum State {
		NONE, WALK_RANDOMLY, WALK_TO_SPAWN
	}

	/**
	 * Constructs a new {@link NormalNPC};
	 * 
	 * @param type The type id.
	 */
	public NormalNPC(int type) {
		super(type);
		initialize();
	}

	/**
	 * Initializes the NPC by binding the appropriate state handlers.
	 */
	private void initialize() {
		bindHandler(WALK_RANDOMLY, new RandomWalkStateHandler());
		bindHandler(WALK_TO_SPAWN, new WalkToSpawnStateHandler());
	}

	@Override
	public State determineState() {
		if(getCombatHandler().getNoRetaliate() > 0 || frozen() || !alive()) {
			return NONE;
		}
		
		if(getWalkingBounds() != null) {
			if(!getWalkingBounds().withinArea(position.getX(), position.getY(), getDefinition().getLeashRange())) {
				return WALK_TO_SPAWN;
			}
			/* 10% chance to randomly walk around, ~every 3 seconds */
			if(random.nextInt(15) == 0 && getWalkingQueue().isEmpty() && !isTurnToTargetSet()) {
				return WALK_RANDOMLY;
			}
		} else {
			if(getSpawnPosition().getDistance(position) > getDefinition().getLeashRange()) {
				return WALK_TO_SPAWN;
			}
		}

		return NONE;
	}

}
