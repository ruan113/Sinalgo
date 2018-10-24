package projects.leach_c.nodes.timers;

import projects.leach_c.nodes.messages.MsgEstacaoBaseFarol;
import sinalgo.nodes.timers.Timer;

public class TimerEstacaoBaseSinalizar extends Timer {
	@Override
	public void fire() {
		// TODO Auto-generated method stub

		node.broadcast(new MsgEstacaoBaseFarol());

	}
}
