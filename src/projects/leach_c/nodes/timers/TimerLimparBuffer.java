package projects.leach_c.nodes.timers;

import projects.leach_c.nodes.nodeImplementations.LeachNode;
import sinalgo.nodes.timers.Timer;

public class TimerLimparBuffer extends Timer{



	@Override
	public void fire() {
		// TODO Auto-generated method stub

		((LeachNode)node).limparBuffer();

	}

}
